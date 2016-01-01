package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;

import static com.dg.ssrl.Components.Actor;
import static com.dg.ssrl.Components.Effect;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Sprite;
import static com.dg.ssrl.Components.Stats;
import static com.dg.ssrl.Components.Update;
import static com.dg.ssrl.Components.Updater;
import static com.dg.ssrl.Components.Solid;

public class EntityFactory {

    private final Assets assets;
    private final AtomicInteger entityIdCounter;

    public EntityFactory(Assets assets) {
        this.assets = assets;
        entityIdCounter = new AtomicInteger();
    }

    private Entity createEntity() {
        return new Entity(entityIdCounter.incrementAndGet());
    }

    public Entity makePlayer() {
        Entity entity = createEntity();
        entity.addComponent(new Position());
        entity.addComponent(new Solid(true));
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

    public Entity makeMonster(int x, int y, MonsterType monsterType) {
        Position position = new Position(x, y);

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);

        Entity entity = createEntity();
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Solid(true));
        entity.addComponent(new Sprite(assets.getMonsterTextureRegion(monsterType)));
        entity.addComponent(new Actor(new MonsterBrain(entity.id), monsterType.speed));
        entity.addComponent(new Stats(monsterType.hitPoints));

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

    public Entity makeItem(int x, int y) {
        Entity entity = createEntity();
        entity.addComponent(new Position(x, y));
        entity.addComponent(new Sprite(assets.tiles[9][0]));

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        entity.addComponent(moveAnimation);

        /*
        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));
        */
        return entity;
    }

}
