package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dg.ssrl.Components.Actor;
import static com.dg.ssrl.Components.Actor.Brain;
import static com.dg.ssrl.Components.Effect;
import static com.dg.ssrl.Components.ItemContainer;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Stats.OnDied;
import static com.dg.ssrl.Components.ItemContainer.OnEmptied;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Solid;
import static com.dg.ssrl.Components.Sprite;
import static com.dg.ssrl.Components.Stats;
import static com.dg.ssrl.Components.Trigger;
import static com.dg.ssrl.Components.Update;
import static com.dg.ssrl.Components.Update.Updater;

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

    public Entity makePlayer(int x, int y, PlayerInputAdapter playerInputAdapter) {
        final Entity entity = createEntity();
        final Actor actor = new Actor(new PlayerBrain(playerInputAdapter, assets.sounds), MonsterType.Player.speed);
        final MoveAnimation moveAnimation = new MoveAnimation(50f).setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        ItemContainer itemContainer = new ItemContainer();
        itemContainer.add(ItemType.Ammo, 10);
        itemContainer.add(ItemType.Rocket, 1);

        entity.addComponent(new Position(x, y));
        entity.addComponent(new Solid(true));
        entity.addComponent(new Sprite(assets.getMonsterTextureRegion(MonsterType.Player), 1));
        entity.addComponent(actor);
        entity.addComponent(new Stats(MonsterType.Player, new OnDied() {
            @Override
            public void onDied() {
                entity.alive = false;
                actor.alive = false;
            }
        }));
        entity.addComponent(itemContainer);
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
        final MoveAnimation moveAnimation = new MoveAnimation(itemType.speed);

        entity.addComponent(new Sprite(assets.getBulletTextureRegion(itemType), 1));
        entity.addComponent(moveAnimation);
        entity.addComponent(new Update(new Updater() {
            @Override
            public void update(float delta, World world) {
                moveAnimation.update(delta, world);
            }
        }));

        return entity;
    }

    private Brain makeBrain(MonsterType monsterType, int entityId) {
        switch (monsterType) {
            case Egg:
                return new MonsterBrain.EggBrain(entityId);
            case Cannon:
                return new MonsterBrain.CannonBrain(entityId, assets.sounds);
            case Grower:
                return new MonsterBrain.GrowerBrain(entityId, assets.sounds);
            default:
                return new MonsterBrain(entityId, monsterType, assets.sounds);
        }
    }

    public Entity makeMonster(int x, int y, MonsterType monsterType) {
        final Entity entity = createEntity();
        Position position = new Position(x, y);
        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);

        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Solid(true));
        entity.addComponent(new Sprite(assets.getMonsterTextureRegion(monsterType), 1));
        entity.addComponent(new Actor(makeBrain(monsterType, entity.id), monsterType.speed));
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

    private static final float EXPLOSION_DURATION = 0.3f;

    public Entity makeDamageEffect(float x, float y) {
        return makeExplosion(x, y, Assets.damageColor);
    }

    public Entity makeExplosion(float x, float y, Color color) {
        final Entity entity = createEntity();
        final Effect effect = new Effect(x, y, 20, EXPLOSION_DURATION, color);

        entity.addComponent(effect);
        entity.addComponent(new Sprite(assets.whitePixel, 2));
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
        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);

        entity.addComponent(new Position(x, y));
        entity.addComponent(new Sprite(assets.getItemTextureRegion(itemType), 0));
        entity.addComponent(moveAnimation);

        ItemContainer itemContainer;
        if (itemType == ItemType.Heart) {
            itemContainer = new ItemContainer(new OnEmptied() {
                @Override
                public void run(Entity emptiedBy, World world) {
                    Stats stats = emptiedBy.getComponent(Stats.class);
                    stats.heal(1);
                    entity.alive = false;
                }
            });
        } else if (itemType == ItemType.Adrenaline) {
            itemContainer = new ItemContainer(new ItemContainer.OnEmptied() {
                @Override
                public void run(Entity emptiedBy, World world) {
                    EntityFactory entityFactory = world.getEntityFactory();

                    Actor actor = emptiedBy.getComponent(Actor.class);
                    if (actor != null) {
                        Entity adrenaline = entityFactory.createAdrenaline(emptiedBy.id);
                        world.addEntity(adrenaline);
                    }
                    entity.alive = false;
                }
            });
        } else {
            itemContainer = new ItemContainer(new OnEmptied() {
                @Override
                public void run(Entity emptiedBy, World world) {
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

    private Entity createAdrenaline(final int affectingEntityId) {
        final Entity entity = createEntity();

        final int duration = 10;

        Actor actor = new Actor(new Brain() {
            int ticks = 0;
            @Override
            public boolean act(World world) {
                Entity affecting = world.getEntity(affectingEntityId);
                if (affecting != null) {
                    Actor affectedActor = affecting.getComponent(Actor.class);
                    affectedActor.setSpeed(Actor.Speed.FAST);
                    ticks++;
                    if (ticks > duration) {
                        entity.alive = false;
                        affectedActor.resetSpeed();
                    } else {
                        Position position = affecting.getComponent(Position.class);
                        EntityFactory entityFactory = world.getEntityFactory();
                        world.addEntity(entityFactory.makeExplosion(position.x * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, position.y * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, Assets.LIGHT_YELLOW));
                    }
                } else {
                    entity.alive = false;
                }
                return true;
            }
        }, Actor.Speed.MEDIUM);


        entity.addComponent(actor);

        return entity;
    }

    public Entity makeTeleporter(int x, int y) {
        final Entity entity = createEntity();
        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        final Sprite sprite = new Sprite(assets.teleporterFrames, 0.2f, 0, Assets.SEA_BLUE);
        entity.addComponent(new Position(x, y));
        entity.addComponent(moveAnimation);
        entity.addComponent(sprite);
        entity.addComponent(new Trigger(new Trigger.TriggerAction() {

            Random random = new Random(System.currentTimeMillis());

            @Override
            public void run(final World world, Entity triggeredBy) {

                Position target = world.getRandomFreePosition(random);
                if (target != null) {
                    EntityFactory entityFactory = world.getEntityFactory();
                    Position oldPosition = triggeredBy.getComponent(Position.class);
                    world.addEntity(entityFactory.makeExplosion(oldPosition.x * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, oldPosition.y * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, Color.MAGENTA));

                    world.addEntity(entityFactory.makeExplosion(target.x * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, target.y * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, Color.MAGENTA));

                    world.move(triggeredBy, target.x, target.y);
                    MoveAnimation triggeredByMoveAnimation = triggeredBy.getComponent(MoveAnimation.class);
                    triggeredByMoveAnimation.setPosition(target.x * Assets.TILE_SIZE, target.y * Assets.TILE_SIZE);

                    assets.sounds.play(Assets.Sounds.SoundId.TELEPORT);
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

    public Entity makeExit(int x, int y) {
        final Entity entity = createEntity();
        final MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);
        final Sprite sprite = new Sprite(assets.exitFrames, 0.1f, 0, Assets.SKY_BLUE);

        entity.addComponent(new Position(x, y));
        entity.addComponent(moveAnimation);
        entity.addComponent(sprite);
        entity.addComponent(new Trigger(new Trigger.TriggerAction() {
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
