package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {


    Snake(Actor.Speed.SLOW, 2, false),
    Rat(Actor.Speed.FAST, 1, false),
    Egg(Actor.Speed.MEDIUM, 3, true);

    public final Actor.Speed speed;
    public final int hitPoints;
    public final boolean immobile;

    MonsterType(Actor.Speed speed, int hitPoints, boolean immobile) {
        this.speed = speed;
        this.hitPoints = hitPoints;
        this.immobile = immobile;
    }

}
