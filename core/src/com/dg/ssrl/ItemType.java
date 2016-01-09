package com.dg.ssrl;

public enum ItemType {
    Key("KEY", 0, 0, Assets.Sounds.SoundId.NONE),
    Ammo("AMMO", 1, 150f, Assets.Sounds.SoundId.LASER),
    AmmoCrate("AMMO_CRATE", 0, 0, Assets.Sounds.SoundId.NONE),
    Rocket("ROCKETS", 3, 80f, Assets.Sounds.SoundId.ROCKET),
    Waste("WASTE", 0, 0, Assets.Sounds.SoundId.NONE),
    Heart("HEART", 0, 0, Assets.Sounds.SoundId.NONE);


    public static ItemType[] PICK_UPS = {
            Ammo,
            AmmoCrate,
            Rocket,
            Heart
    };

    public final String name;
    public final int damage;
    public final float speed;
    public final Assets.Sounds.SoundId soundId;

    ItemType(String name, int damage, float speed, Assets.Sounds.SoundId soundId) {
        this.name = name;
        this.damage = damage;
        this.speed = speed;
        this.soundId = soundId;
    }
}
