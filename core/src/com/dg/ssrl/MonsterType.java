package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {

    Snake(Actor.Speed.SLOW, 2),
    Rat(Actor.Speed.FAST, 1),
    Egg(Actor.Speed.MEDIUM, 3),
    ;

    public final Actor.Speed speed;
    public final int hitPoints;

    MonsterType(Actor.Speed speed, int hitPoints) {
        this.speed = speed;
        this.hitPoints = hitPoints;
    }

}
