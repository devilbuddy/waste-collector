package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.ItemContainer;
import static com.dg.ssrl.Components.Stats;
import static com.dg.ssrl.Components.Trigger;

public class BrainCore {

    private static final String tag = "BrainCore";

    public static class MoveResult {
        public boolean acted = false;
        public boolean moved = false;
        public boolean turned = false;
        public Position endPosition = new Position();
    }

    private static MoveResult moveResult = new MoveResult();

    public static MoveResult move(final World world, final Entity entity, final Direction moveDirection, final MonsterType monsterType, final Assets.Sounds sounds) {
        final EntityFactory entityFactory = world.getEntityFactory();
        final Scheduler scheduler = world.getScheduler();
        final MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);
        final Position currentPosition = entity.getComponent(Position.class);

        moveResult.acted = false;
        moveResult.moved = false;
        moveResult.turned = false;
        moveResult.endPosition.set(currentPosition);

        final Position targetPosition = currentPosition.copy();
        world.translateWraparound(targetPosition, moveDirection);

        final World.Cell targetCell = world.getCell(targetPosition);

        if (moveAnimation.direction == moveDirection) {
            // Gdx.app.log(tag, "targetPosition:" + targetPosition);
            if (world.isWalkable(targetPosition)) {

                final boolean targetContainsTrigger = world.containsEntityWithComponent(targetPosition, Trigger.class);
                if (targetContainsTrigger) {
                    scheduler.lock();
                }

                moveAnimation.startMove(currentPosition, Assets.TILE_SIZE, moveDirection, new Runnable() {
                    @Override
                    public void run() {
                        moveAnimation.setPosition(targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE);

                        // triggers
                        for (int i = 0; i < targetCell.getEntityCount(); i++) {
                            int entityId = targetCell.getEntityId(i);
                            Trigger trigger = world.getEntity(entityId).getComponent(Trigger.class);
                            if (trigger != null) {
                                trigger.triggerAction.run(world, entity);
                            }
                        }
                        if (targetContainsTrigger) {
                            scheduler.unlock();
                        }
                    }
                });

                ItemContainer itemContainer = entity.getComponent(ItemContainer.class);
                if (itemContainer != null) {
                    for (int i = 0; i < targetCell.getEntityCount(); i++) {
                        int entityId = targetCell.getEntityId(i);
                        Entity e = world.getEntity(entityId);
                        ItemContainer pickupItem = e.getComponent(ItemContainer.class);
                        if (pickupItem != null) {
                            pickupItem.emptyInto(itemContainer);
                            pickupItem.onEmptied(entity, world);
                            Gdx.app.log(tag, itemContainer.toString());
                            sounds.play(Assets.Sounds.SoundId.PICKUP);
                        }
                    }
                }

                world.move(entity, targetPosition.x, targetPosition.y);

                moveResult.acted = true;
                moveResult.moved = true;
                moveResult.endPosition.set(targetPosition);
            } else {
                // bump
                for (int i = 0; i < targetCell.getEntityCount(); i++) {
                    int entityId = targetCell.getEntityId(i);
                    final Entity targetEntity = world.getEntity(entityId);
                    final Stats targetStats = targetEntity.getComponent(Stats.class);
                    if (targetStats != null) {
                        scheduler.lock();
                        moveAnimation.startBump(currentPosition, moveDirection, new Runnable() {
                            @Override
                            public void run() {
                                Entity explosion = entityFactory.makeDamageEffect(targetPosition.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2, targetPosition.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2);
                                world.addEntity(explosion);

                                sounds.play(Assets.Sounds.SoundId.HIT);

                                targetStats.damage(monsterType.bumpDamage);
                                targetEntity.alive = targetStats.isAlive();
                                scheduler.unlock();
                            }
                        });

                        moveResult.acted = true;
                        moveResult.moved = false;
                    }
                }
            }

        } else {
            moveAnimation.startTurn(moveDirection);
            moveResult.acted = false;
            moveResult.turned = true;
        }

        return moveResult;
    }

    public static void fire(final World world, final Entity entity, final Direction direction, final ItemType itemType, final Assets.Sounds sounds) {
        Gdx.app.log(tag, "fire " + entity + " " + direction);
        final EntityFactory entityFactory = world.getEntityFactory();
        final Scheduler scheduler = world.getScheduler();

        Position bulletStart = entity.getComponent(Position.class).copy().translate(direction);

        final Position bulletEnd = bulletStart.copy();
        boolean hitSomething = false;
        int distanceTiles = 0;
        while (!hitSomething) {
            world.wraparound(bulletEnd);

            if (world.isWalkable(bulletEnd)) {
                bulletEnd.translate(direction);
                distanceTiles++;
            } else {
                hitSomething = true;
            }
        }

        sounds.play(itemType.soundId);
        final Entity bullet = entityFactory.makeBullet(itemType);
        scheduler.lock();
        bullet.getComponent(MoveAnimation.class).startMove(bulletStart, distanceTiles * Assets.TILE_SIZE, direction, new Runnable() {
            @Override
            public void run() {
                bullet.alive = false;
                scheduler.unlock();

                float explosionCenterX = bulletEnd.x * Assets.TILE_SIZE + Assets.TILE_SIZE / 2;
                float explosionCenterY = bulletEnd.y * Assets.TILE_SIZE + Assets.TILE_SIZE / 2;

                if (itemType == ItemType.Ammo) {
                    Entity explosion = entityFactory.makeExplosion(explosionCenterX, explosionCenterY, Assets.bulletExplosionColor);
                    world.addEntity(explosion);
                } else if (itemType == ItemType.Rocket) {
                    for (int y = -1; y < 2; y++) {
                        for (int x = -1; x < 2; x++) {
                            Color color = Assets.bulletExplosionColor;
                            if(x == 0 && y == 0) {
                                color = Assets.rocketExplosionColor;
                            }
                            Entity explosion = entityFactory.makeExplosion(explosionCenterX + x * Assets.TILE_SIZE, explosionCenterY + y * Assets.TILE_SIZE, color);
                            world.addEntity(explosion);
                        }
                    }
                }

                sounds.play(Assets.Sounds.SoundId.HIT);

                Gdx.app.log(tag, "hit in cell " + bulletEnd);
                if (itemType == ItemType.Ammo) {
                    projectileDamage(world, bulletEnd, itemType.damage);
                } else if (itemType == ItemType.Rocket) {
                    Position p = bulletEnd.copy();

                    for (int y = -1; y < 2; y++) {
                        for (int x = -1; x < 2; x++) {
                            p.set(bulletEnd).translate(x, y);
                            if (world.contains(p)) {
                                int damage = 1;
                                if(x == 0 && y == 0) {
                                    damage = itemType.damage;
                                }
                                projectileDamage(world, p, damage);
                            }
                        }
                    }
                }

            }
        });

        world.addEntity(bullet);
    }

    private static void projectileDamage(World world, Position position, int damage) {
        World.Cell cell = world.getCell(position.x, position.y);
        if (cell.type == World.Cell.Type.Wall && damage == ItemType.Rocket.damage) {
            world.destroyWall(position);
        } else {
            int entityCount = cell.getEntityCount();
            for (int i = 0; i < entityCount; i++) {
                int entityId = cell.getEntityId(i);
                Entity hitEntity = world.getEntity(entityId);
                Components.Stats hitEntityStats = hitEntity.getComponent(Components.Stats.class);
                if (hitEntityStats != null) {
                    Gdx.app.log(tag, "hit stats " + hitEntityStats);
                    hitEntityStats.damage(damage);
                }
            }
        }

    }

}
