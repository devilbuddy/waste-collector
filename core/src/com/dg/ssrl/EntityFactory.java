package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;
import static com.dg.ssrl.Components.*;

public class EntityFactory {

    private AtomicInteger entityIdCounter = new AtomicInteger();

    private final Assets assets;

    private Entity createEntity() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        return entity;
    }

    public EntityFactory(Assets assets) {
        this.assets = assets;
    }

    public Entity makePlayer() {
        Entity entity = createEntity();
        entity.addComponent(new Position());
        entity.addComponent(new Sprite(assets.tiles[4][2]));
        entity.addComponent(new Stats(3));

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        entity.addComponent(moveAnimation);

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));

        return entity;
    }

    public Entity makeBullet() {
        Entity entity = createEntity();
        entity.addComponent(new Sprite(assets.tiles[4][3]));

        final MoveAnimation moveAnimation = new MoveAnimation(150f);
        entity.addComponent(moveAnimation);

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));

        return entity;
    }

    public Entity makeMonster(int x, int y) {
        Position position = new Position(x, y);

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);

        Entity entity = createEntity();
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Sprite(assets.tiles[5][1]));
        entity.addComponent(new Actor(new MonsterBrain(entity.id)));
        entity.addComponent(new Stats(2));

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));

        return entity;
    }

    public Entity makeExplosion(float x, float y) {
        final Entity entity = createEntity();
        entity.addComponent(new Sprite(assets.whitePixel));
        final Effect effect = new Effect(x, y, 10);

        entity.addComponent(effect);

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                effect.update(delta);
                if (effect.isDone()) {
                    entity.alive = false;
                }
            }
        }));

        return entity;
    }

}
