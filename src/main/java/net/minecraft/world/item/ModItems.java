package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ModAbstractMinecart;
import net.minecraft.world.food.Foods;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Fluids;

@net.minecraftforge.registries.ObjectHolder("minecraft")
public class ModItems {
    public static final Item LAMP_MINECART = registerItem("lamp_minecart", new MinecartItem(ModAbstractMinecart.Type.LAMP, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));


    private static <T> Optional<T> ifPart2(T pSecondaryPart) {
        return Optional.empty();
    }

    private static Item registerBlock(Block pBlock) {
        return registerBlock(new BlockItem(pBlock, new Item.Properties()));
    }

    private static Item registerBlock(Block pBlock, CreativeModeTab pItemGroup) {
        return registerBlock(new BlockItem(pBlock, (new Item.Properties()).tab(pItemGroup)));
    }

    private static Item registerBlock(Block pBlock, Optional<CreativeModeTab> pOptionalCategory) {
        return pOptionalCategory.map((p_151102_) -> {
            return registerBlock(pBlock, p_151102_);
        }).orElseGet(() -> {
            return registerBlock(pBlock);
        });
    }

    private static Item registerBlock(Block pBlock, CreativeModeTab pCategory, Block... pVariants) {
        BlockItem blockitem = new BlockItem(pBlock, (new Item.Properties()).tab(pCategory)) {
            @Override
            public void registerBlocks(java.util.Map<Block, Item> map, Item self) {
                super.registerBlocks(map, self);
                for (Block b : pVariants) {
                    map.put(b, self);
                }
            }

            @Override
            public void removeFromBlockToItemMap(java.util.Map<Block, Item> map, Item self) {
                super.removeFromBlockToItemMap(map, self);
                for (Block b : pVariants) {
                    map.remove(b);
                }
            }
        };

        return registerBlock(blockitem);
    }

    private static Item registerBlock(BlockItem pItem) {
        return registerBlock(pItem.getBlock(), pItem);
    }

    protected static Item registerBlock(Block pBlock, Item pItem) {
        return registerItem(Registry.BLOCK.getKey(pBlock), pItem);
    }

    private static Item registerItem(String pKey, Item pItem) {
        return registerItem(new ResourceLocation(pKey), pItem);
    }

    private static Item registerItem(ResourceLocation pKey, Item pItem) {
        if (pItem instanceof BlockItem) {
            ((BlockItem)pItem).registerBlocks(Item.BY_BLOCK, pItem);
        }

        return Registry.register(Registry.ITEM, pKey, pItem);
    }
}
