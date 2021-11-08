package com.inkar.betterthansquids.block.state.properties;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BetterThanSquidsBlockStateProperties
{
    public static final EnumProperty<PanelType> PANEL_TYPE = EnumProperty.create("type", PanelType.class);

    public static final IntegerProperty BOTTLES = IntegerProperty.create("bottles", 1, 4);
}
