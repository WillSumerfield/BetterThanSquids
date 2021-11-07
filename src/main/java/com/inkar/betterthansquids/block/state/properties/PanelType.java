package com.inkar.betterthansquids.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum PanelType implements StringRepresentable {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west"),
    DOUBLE("double");

    private final String name;

    private PanelType (String p_61775_) {
        this.name = p_61775_;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}