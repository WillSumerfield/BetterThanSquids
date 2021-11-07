package com.inkar.betterthansquids.item;

import com.inkar.betterthansquids.BetterThanSquids;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems
{

    // A list of items to be registered to the game
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterThanSquids.MOD_ID);

    //region Item Registration

    public static final RegistryObject<Item> GOLDEN_POTATO = ITEMS.register("golden_potato",
            () -> new Item((new Item.Properties()).food(new FoodProperties.Builder().nutrition(5).saturationMod(2.4F).effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 0), 1.0F).alwaysEat().build()).tab(CreativeModeTab.TAB_FOOD)));

    //endregion

    // A method used to add this register to the eventBus
    public static void register (IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }

}
