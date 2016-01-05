package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {

    Player(Actor.Speed.MEDIUM, 3, true),
    Snake(Actor.Speed.SLOW, 2, false),
    Rat(Actor.Speed.FAST, 1, true),
    Alien(Actor.Speed.MEDIUM, 3, false),
    Egg(Actor.Speed.SLOW, 3, true),
    ;

    public static MonsterType[] ENEMIES = {
            Snake, Rat, Egg, Alien
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
