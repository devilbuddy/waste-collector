package com.dg.ssrl;

import java.util.concurrent.atomic.AtomicInteger;

import static com.dg.ssrl.Components.*;

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

    public Entity makePlayer(int x, int y, PlayerInputAdapter playerInputAdapter, Scheduler scheduler) {
        final Entity entity = createEntity();
        entity.addComponent(new Position(x, y));
        entity.addComponent(new Solid(true));
        entity.addComponent(new Sprite(assets.tiles[4][2]));

        final Actor actor = new Actor(new PlayerBrain(playerInputAdapter, scheduler, assets.sounds), MonsterType.Player.speed);
        entity.addComponent(actor);

        entity.addComponent(new Stats(MonsterType.Player, new OnDied() {
            @Override
            public void onDied() {
                entity.alive = false;
                actor.alive = false;
            }
        }));
        ItemContainer itemContainer = new ItemContainer();
        itemContainer.add(ItemType.Ammo, 10);
        itemContainer.add(ItemType.Rocket, 1);
        entity.addComponent(itemContainer);


        final MoveAnimation moveAnimation = new MoveAnimation(50f).setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        entity.addComponent(moveAnimation);

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));

        return entity;
    }

    public Entity makeBullet(ItemType itemType) {
        Entity entity = createEntity();
        entity.addComponent(new Sprite(assets.getBulletTextureRegion(itemType)));

        final MoveAnimation moveAnimation = new MoveAnimation(itemType.speed);
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

        final Entity entity = createEntity();
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Solid(true));
        entity.addComponent(new Sprite(assets.getMonsterTextureRegion(monsterType)));

        Brain brain = null;
        switch (monsterType) {
            case Egg:
                brain = new MonsterBrain.EggBrain(entity.id);
                break;
            case Cannon:
                brain = new MonsterBrain.CannonBrain(entity.id, assets.sounds);
                break;
            default:
                brain = new MonsterBrain(entity.id, assets.sounds);
        }
        entity.addComponent(new Actor(brain, monsterType.speed));

        entity.addComponent(new Stats(monsterType, new OnDied() {
            @Override
            public void onDied() {
                entity.alive = false;
                entity.getComponent(Actor.class).alive = false;
            }
        }));
        if (monsterType.canCarryItems) {
            entity.addComponent(new ItemContainer());
        }
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

    public Entity makeItem(int x, int y, ItemType itemType) {
        final Entity entity = createEntity();
        entity.addComponent(new Position(x, y));
        entity.addComponent(new Sprite(assets.getItemTextureRegion(itemType)));

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        entity.addComponent(moveAnimation);

        ItemContainer itemContainer;
        if (itemType == ItemType.Heart) {
            itemContainer = new ItemContainer(new OnEmptied() {
                @Override
                public void run(Entity emptiedBy) {
                    Stats stats = emptiedBy.getComponent(Stats.class);
                    stats.heal(1);
                    entity.alive = false;
                }
            });
        } else {
            itemContainer = new ItemContainer(new OnEmptied() {
                @Override
                public void run(Entity emptiedBy) {
                    entity.alive = false;
                }
            });
        }
        if (itemType == ItemType.AmmoCrate) {
            itemContainer.add(ItemType.Ammo, 5);
        } else {
            itemContainer.add(itemType, 1);
        }
        entity.addComponent(itemContainer);

        return entity;
    }

    public Entity makeExit(int x, int y) {

        final Entity entity = createEntity();
        entity.addComponent(new Position(x, y));

        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        entity.addComponent(moveAnimation);

        final Sprite sprite = new Sprite(assets.exitFrames, 0.2f);
        entity.addComponent(sprite);

        entity.addComponent(new Trigger(new TriggerAction() {
            @Override
            public void run(final World world, Entity triggeredBy) {
                Entity player = world.getPlayer();
                if (player != null) {
                    if (player.id == triggeredBy.id) {

                        ItemContainer itemContainer = player.getComponent(ItemContainer.class);
                        int keyCount = itemContainer.getAmount(ItemType.Key);
                        if (keyCount > 0) {
                            itemContainer.remove(ItemType.Key, 1);
                            world.setCompleted();
                            assets.sounds.play(Assets.Sounds.SoundId.EXIT);
                        }
                    }
                }
            }
        }));

        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                sprite.update(delta);
            }
        }));

        return entity;
    }

}
