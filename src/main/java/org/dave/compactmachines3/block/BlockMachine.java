package org.dave.compactmachines3.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.tile.TileEntityTunnel;
import org.dave.compactmachines3.world.ChunkLoadingMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.TeleportationTools;

import java.util.ArrayList;
import java.util.List;

public class BlockMachine extends BlockBase implements IMetaBlockName, ITileEntityProvider {

    public static final PropertyEnum<EnumMachineSize> SIZE = PropertyEnum.create("size", EnumMachineSize.class);

    public BlockMachine(Material material) {
        super(material);

        this.setHardness(8.0F);
        this.setResistance(20.0F);

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);

        setDefaultState(blockState.getBaseState().withProperty(SIZE, EnumMachineSize.TINY));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos what) {
        super.neighborChanged(state, world, pos, blockIn, what);

        if(world.isRemote) {
            return;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            return;
        }

        // Make sure we don't stack overflow when we get in a notifyBlockChange loop.
        // Just ensure only a single notification happens per tick.
        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
        if(te.lastNeighborUpdateTick == world.getTotalWorldTime()) {
            return;
        }

        te.lastNeighborUpdateTick = world.getTotalWorldTime();
        for(EnumFacing side : EnumFacing.values()) {
            BlockPos neighborPos = te.getTunnelForSide(side);
            if(neighborPos == null) {
                continue;
            }

            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            if(!(machineWorld.getTileEntity(neighborPos) instanceof TileEntityTunnel)) {
                continue;
            }

            world.notifyNeighborsOfStateChange(neighborPos, Blockss.tunnel, false);
        }
    }

    /*
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for(EnumMachineSize size : EnumMachineSize.values()) {
            subItems.add(new ItemStack(this, 1, size.getMeta()));
        }
    }
    */

    @Override
    public int damageDropped(IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SIZE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SIZE).getMeta();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(SIZE, EnumMachineSize.getFromMeta(meta));
    }

    @Override
    public String getSpecialName(ItemStack stack) {
        return this.getStateFromMeta(stack.getItemDamage()).getValue(SIZE).getName();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMachine();
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if(world.isRemote) {
            super.breakBlock(world, pos, state);
            return;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            return;
        }

        TileEntityMachine te = (TileEntityMachine) world.getTileEntity(pos);
        WorldSavedDataMachines.INSTANCE.removeMachinePosition(te.coords);

        // TODO: Think about adding harvesting CMs functionality back in
        BlockMachine.spawnItemWithNBT(world, pos, state.getValue(BlockMachine.SIZE), te);

        ChunkLoadingMachines.unforceChunk(te.coords);
        world.removeTileEntity(pos);

        super.breakBlock(world, pos, state);
    }

    public static void spawnItemWithNBT(World world, BlockPos pos, EnumMachineSize size, TileEntityMachine te) {
        if(world.isRemote) {
           return;
        }

        ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("coords", te.coords);
        if(te.hasOwner()) {
            compound.setUniqueId("owner", te.getOwner());
        }
        stack.setTagCompound(compound);

        if(te.getCustomName().length() > 0) {
            stack.setStackDisplayName(te.getCustomName());
        }

        EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY() + 0.5, pos.getZ(), stack);
        entityItem.lifespan = 1200;
        entityItem.setPickupDelay(10);

        float motionMultiplier = 0.02F;
        entityItem.motionX = (float) world.rand.nextGaussian() * motionMultiplier;
        entityItem.motionY = (float) world.rand.nextGaussian() * motionMultiplier + 0.1F;
        entityItem.motionZ = (float) world.rand.nextGaussian() * motionMultiplier;

        world.spawnEntity(entityItem);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if(!(world.getTileEntity(pos) instanceof TileEntityMachine)) {
            // Not a tile entity machine. Should not happen.
            return;
        }

        TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(pos);
        if(tileEntityMachine.coords != -1) {
            // The machine already has data for some reason
            return;
        }

        if(stack.hasTagCompound()) {
            if(stack.getTagCompound().hasKey("coords")) {
                int coords = stack.getTagCompound().getInteger("coords");
                if (coords != -1) {
                    tileEntityMachine.coords = coords;
                }
            }

            if(stack.hasDisplayName()) {
                tileEntityMachine.setCustomName(stack.getDisplayName());
            }

            if(stack.getTagCompound().hasKey("owner")) {
                tileEntityMachine.setOwner(stack.getTagCompound().getUniqueId("owner"));
            }
        }

        if(!tileEntityMachine.hasOwner() && placer instanceof EntityPlayer) {
            tileEntityMachine.setOwner((EntityPlayer)placer);
        }

        tileEntityMachine.markDirty();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        if(!(world.getTileEntity(pos)instanceof TileEntityMachine)) {
            return false;
        }

        TileEntityMachine machine = (TileEntityMachine)world.getTileEntity(pos);

        ItemStack playerStack = player.getHeldItemMainhand();
        if(!playerStack.isEmpty()) {
            Item playerItem = playerStack.getItem();

            // TODO: Convert the ability to teleport into a machine into an itemstack capability
            if(playerItem instanceof ItemPersonalShrinkingDevice) {
                TeleportationTools.teleportPlayerToMachine((EntityPlayerMP) player, machine);

                WorldSavedDataMachines.INSTANCE.addMachinePosition(machine.coords, pos, world.provider.getDimension());
            }
        }

        return true;
    }
}
