package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by magnus on 2015-09-13.
 */
public class EntityFactory {

    private AtomicInteger entityIdCounter = new AtomicInteger();

    public Entity makePlayer() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new Entity.Position());
        entity.addComponent(new Entity.MoveState());
        return entity;
    }

}
