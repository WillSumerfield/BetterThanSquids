package com.inkar.betterthansquids.item;

import com.inkar.betterthansquids.BetterThanSquids;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ModAbstractMinecart;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.Blocks;
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

    public static final RegistryObject<Item> LAMP_MINECART = ITEMS.register("lamp_minecart",
        () -> new MinecartItem(ModAbstractMinecart.Type.LAMP, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));



    //endregion

    // A method used to add this register to the eventBus
    public static void register (IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }

}
