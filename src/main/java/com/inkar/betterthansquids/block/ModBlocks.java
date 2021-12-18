package com.inkar.betterthansquids.block;

import com.inkar.betterthansquids.BetterThanSquids;
import com.inkar.betterthansquids.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlocks
{
    // A list of items to be registered to the game
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BetterThanSquids.MOD_ID);

    //region Block Registration

    public static final RegistryObject<Block> OAK_PANEL = registerBlock("oak_panel",
            () -> new PanelBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)),
            (new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static final RegistryObject<Block> BOTTLE_BLOCK = registerBlock("bottle_block",
            () -> new BottleBlock(BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE).noOcclusion().strength(0.1F).sound(SoundType.GLASS).isViewBlocking((a,b,c) -> false)),
            (new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final RegistryObject<Block> MOSSLE_BLOCK = registerBlock("mossle_block",
            () -> new MossleBlock(BlockBehaviour.Properties.of(Material.MOSS, MaterialColor.COLOR_GREEN).strength(0.2F, 0.2F).sound(SoundType.MOSS)),
            (new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    //endregion

    private static <T extends Block> void registerBlockItem (String name, RegistryObject<T> block, Item.Properties itemProperties)
    {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), itemProperties));
    }

    private static <T extends Block>RegistryObject<T> registerBlock (String name, Supplier<T>block, Item.Properties itemProperties)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, itemProperties);
        return toReturn;
    }

    // A method used to add this register to the eventBus
    public static void register (IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
