package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {

    Player(Actor.Speed.MEDIUM, 3, true),

    Crawler(Actor.Speed.SLOW, 2, false),
    Stealer(Actor.Speed.FAST, 1, true),
    Brute(Actor.Speed.MEDIUM, 3, false),
    Egg(Actor.Speed.EXTRA_SLOW, 1, true),
    Cannon(Actor.Speed.MEDIUM, 1, false),
    Grower(Actor.Speed.EXTRA_SLOW, 1, false)
    ;

    public static MonsterType[] ENEMIES = {
            Crawler, Stealer, Egg, Brute, Cannon, Grower
    };

    public final Actor.Speed speed;
    public final int hitPoints;
    public final boolean canCarryItems;

    MonsterType(Actor.Speed speed, int hitPoints, boolean canCarryItems) {
        this.speed = speed;
        this.hitPoints = hitPoints;
        this.canCarryItems = canCarryItems;
    }

}
