package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;
import static com.dg.ssrl.Components.*;
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
        entity.addComponent(new Position());
        entity.addComponent(new Sprite(assets.tiles[4][2]));
        entity.addComponent(new MoveAnimation(50f));
        return entity;
    }

    public Entity makeBullet() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new MoveAnimation(150f));
        entity.addComponent(new Sprite(assets.tiles[4][3]));
        return entity;
    }

    public Entity makeMonster(int x, int y) {

        Components.Position position = new Position();
        position.set(x, y);

        Components.MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE);
        moveAnimation.direction = Direction.EAST;

        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Sprite(assets.tiles[5][1]));

        return entity;
    }

}
