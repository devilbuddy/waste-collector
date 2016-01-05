package com.dg.ssrl;

public enum ItemType {
    Key("KEY"),
    Ammo("AMMO"),
    AmmoCrate("AMMO_CRATE"),
    Waste("WASTE");


    public static ItemType[] PICK_UPS = {
            Ammo,
            AmmoCrate,
            Waste
    };

    public final String name;
    ItemType(String name) {
        this.name = name;
    }
}
