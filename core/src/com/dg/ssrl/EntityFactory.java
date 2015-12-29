package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by magnus on 2015-09-13.
 */
public class EntityFactory {

    private AtomicInteger entityIdCounter = new AtomicInteger();

    private final Assets assets;

    public EntityFactory(Assets assets) {
        this.assets = assets;
    }

    public Entity makePlayer() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new Entity.Position());
        entity.addComponent(new Entity.Sprite(assets.tiles[4][2]));
        entity.addComponent(new Entity.MoveAnimation(50f));
        return entity;
    }

    public Entity makeBullet() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new Entity.MoveAnimation(150f));
        entity.addComponent(new Entity.Sprite(assets.tiles[4][3]));
        return entity;
    }

    public Entity makeMonster(int x, int y) {

        Entity.Position position = new Entity.Position();
        position.set(x, y);

        Entity.MoveAnimation moveAnimation = new Entity.MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE);
        moveAnimation.direction = Direction.EAST;

        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Entity.Sprite(assets.tiles[5][1]));


        return entity;
    }

}
