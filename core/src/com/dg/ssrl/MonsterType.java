package com.dg.ssrl;

import static com.dg.ssrl.Components.Actor;

public enum MonsterType {

    Player(Actor.Speed.MEDIUM, 3),
    Snake(Actor.Speed.SLOW, 2),
    Rat(Actor.Speed.FAST, 1),
    Egg(Actor.Speed.MEDIUM, 3),
    ;

    public static MonsterType[] ENEMIES = {
            Snake, Rat, Egg
    };

    public final Actor.Speed speed;
    public final int hitPoints;

    MonsterType(Actor.Speed speed, int hitPoints) {
        this.speed = speed;
        this.hitPoints = hitPoints;
    }

}
