package org.dave.compactmachines3.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.block.BlockTunnel;
import org.dave.compactmachines3.block.BlockWall;
import org.dave.compactmachines3.block.BlockWallBreakable;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.item.ItemBlockMachine;
import org.dave.compactmachines3.item.ItemBlockWall;
import org.dave.compactmachines3.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines3.item.ItemTunnelTool;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.tile.TileEntityTunnel;

@Mod.EventBusSubscriber
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockTunnel(Material.IRON).setUnlocalizedName("tunnel").setRegistryName(CompactMachines3.MODID, "tunnel"));
        GameRegistry.registerTileEntity(TileEntityTunnel.class, "TileEntityTunnel");

        event.getRegistry().register(new BlockWall(Material.IRON).setUnlocalizedName("wall").setRegistryName(CompactMachines3.MODID, "wall"));
        event.getRegistry().register(new BlockWallBreakable(Material.IRON).setUnlocalizedName("wallbreakable").setRegistryName(CompactMachines3.MODID, "wallbreakable"));

        event.getRegistry().register(new BlockMachine(Material.IRON).setUnlocalizedName("machine").setRegistryName(CompactMachines3.MODID, "machine"));
        GameRegistry.registerTileEntity(TileEntityMachine.class, "TileEntityMachine");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(Blockss.tunnel).setRegistryName(Blockss.tunnel.getRegistryName()));
        event.getRegistry().register(new ItemBlockWall(Blockss.wall).setRegistryName(Blockss.wall.getRegistryName()));
        event.getRegistry().register(new ItemBlock(Blockss.wallBreakable).setRegistryName(Blockss.wallBreakable.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(Blockss.machine).setRegistryName(Blockss.machine.getRegistryName()));

        event.getRegistry().register(new ItemPersonalShrinkingDevice().setUnlocalizedName("psd").setRegistryName(CompactMachines3.MODID, "psd"));
        event.getRegistry().register(new ItemTunnelTool().setUnlocalizedName("tunneltool").setRegistryName(CompactMachines3.MODID, "tunneltool"));
    }

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
