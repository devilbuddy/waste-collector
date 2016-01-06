package com.dg.ssrl;

public enum ItemType {
    Key("KEY", 0),
    Ammo("AMMO", 1),
    AmmoCrate("AMMO_CRATE", 0),
    Rocket("ROCKET", 3),
    Waste("WASTE", 0);


    public static ItemType[] PICK_UPS = {
            Ammo,
            AmmoCrate,
            Waste,
            Rocket
    };

    public final String name;
    public final int damage;

    ItemType(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }
}
