package com.dg.ssrl;

public enum ItemType {

    Key("KEY", 0, 0, false, Assets.Sounds.SoundId.NONE),
    Ammo("AMMO", 1, 150f, false, Assets.Sounds.SoundId.LASER),
    AmmoCrate("AMMO_CRATE", 0, 0, false, Assets.Sounds.SoundId.NONE),
    Rocket("ROCKETS", 3, 80f, false, Assets.Sounds.SoundId.ROCKET),
    Waste("WASTE", 0, 0, false, Assets.Sounds.SoundId.NONE),
    Heart("HEART", 0, 0, false, Assets.Sounds.SoundId.NONE),
    Adrenaline("ADRENALINE", 0, 0, true, Assets.Sounds.SoundId.NONE)
    ;

    public static ItemType[] PICK_UPS = {
            Ammo
    };

    public static ItemType[] RARE_PICK_UPS = {
            AmmoCrate,
            Heart,
            Rocket,
            Adrenaline
    };

    public final String name;
    public final int damage;
    public final float speed;
    public final boolean consumedOnPickUp;
    public final Assets.Sounds.SoundId soundId;

    ItemType(String name, int damage, float speed, boolean consumedOnPickUp, Assets.Sounds.SoundId soundId) {
        this.name = name;
        this.damage = damage;
        this.speed = speed;
        this.consumedOnPickUp = consumedOnPickUp;
        this.soundId = soundId;
    }
}
