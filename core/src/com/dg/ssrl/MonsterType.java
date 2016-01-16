package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {

    Player(Actor.Speed.MEDIUM, 3, true, 1),

    Crawler(Actor.Speed.SLOW, 2, false, 1),
    Stealer(Actor.Speed.FAST, 1, true, 1),
    Brute(Actor.Speed.MEDIUM, 3, false, 2),
    Egg(Actor.Speed.EXTRA_SLOW, 1, true, 0),
    Cannon(Actor.Speed.MEDIUM, 2, false, 0),
    Grower(Actor.Speed.EXTRA_SLOW, 1, false, 0),
    Robot(Actor.Speed.FAST, 3, false, 1)
    ;

    public static MonsterType[] ENEMIES = {
            Crawler, Stealer, Brute,
    };

    public static MonsterType[] STATIONARY_ENEMIES = {
            Egg, Cannon, Grower
    };


    public final Actor.Speed speed;
    public final int hitPoints;
    public final boolean canCarryItems;
    public final int bumpDamage;

    MonsterType(Actor.Speed speed, int hitPoints, boolean canCarryItems, int bumpDamage) {
        this.speed = speed;
        this.hitPoints = hitPoints;
        this.canCarryItems = canCarryItems;
        this.bumpDamage = bumpDamage;
    }

}
