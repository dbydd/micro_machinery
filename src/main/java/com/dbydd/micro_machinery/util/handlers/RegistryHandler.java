package com.dbydd.micro_machinery.util.handlers;

import javax.annotation.Nonnull;

import com.dbydd.micro_machinery.fluid.MoltenMaterial;
import com.dbydd.micro_machinery.init.ModBlocks;
import com.dbydd.micro_machinery.init.ModFluids;
//import com.dbydd.micro_machinery.init.ModBlocks;
import com.dbydd.micro_machinery.init.ModItems;
import com.dbydd.micro_machinery.util.IHasModel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class RegistryHandler {

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0])); //添加items到ITEMS数组
    }
        
        @SubscribeEvent
        public static void onBlockRegister(RegistryEvent.Register<Block> event) {
                event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0])); //添加blocks到BLOCKS数组
                TileRegisteryHandler.registerTileEntities();
                
        }
        
        
        
        @SubscribeEvent
        public static void onModelRegister(ModelRegistryEvent event) {

                for(Item item : ModItems.ITEMS) { //for循环 注册items
                        if(item instanceof IHasModel) {
                                ((IHasModel)item).registerModels();
                        }
                }
                

               for(Block block : ModBlocks.BLOCKS) { //for循环 注册blocks
                        if(block instanceof IHasModel) {
                                ((IHasModel)block).registerModels();
                        }
                }
               
        }
        
        public static void preInitRegisteries(FMLPreInitializationEvent event)
        {
        	ModFluids.registerFluids(ModFluids.MOLTEN_COPPER);
        	ModFluids.registerFluids(ModFluids.MOLTEN_TIN);
        	ModFluids.registerFluids(ModFluids.MOLTEN_BRONZE);
        	ModFluids.registerFluids(ModFluids.MOLTEN_STEEL);
        	ModFluids.registerFluids(ModFluids.MOLTEN_SS);
        	ModFluids.registerFluids(ModFluids.MOLTEN_TUNGSTEN);
        	ModFluids.registerFluids(ModFluids.MOLTEN_NICKEL);
        	ModFluids.registerFluids(ModFluids.MOLTEN_INVAR);
        	ModFluids.registerFluids(ModFluids.MOLTEN_HSS);
        	ModFluids.registerFluids(ModFluids.MOLTEN_SILVER);
        	ModFluids.registerFluids(ModFluids.MOLTEN_GOLD);
        	ModFluids.registerFluids(ModFluids.MOLTEN_MANGANESE);
        	ModFluids.registerFluids(ModFluids.MOLTEN_CHROMIUM);
        	ModFluids.registerFluids(ModFluids.MOLTEN_VANADIUM);
        	ModFluids.registerFluids(ModFluids.MOLTEN_COBALT);
        	ModFluids.registerFluids(ModFluids.MOLTEN_TITANIUM);
        	ModFluids.registerFluids(ModFluids.MOLTEN_ALUMINUM);
        	ModFluids.registerFluids(ModFluids.MOLTEN_NCALLOY);
        	ModFluids.registerFluids(ModFluids.MOLTEN_IRON);
        	ModFluids.registerFluids(ModFluids.MOLTEN_FERROCHROME);
        	ModFluids.registerFluids(ModFluids.GOLDEN_APPLE_JUICE);
        	ModFluids.registerFluids(ModFluids.APPLE_JUICE);
        	ModFluids.registerFluids(ModFluids.ETHENE);
        	
        	
        }
}
