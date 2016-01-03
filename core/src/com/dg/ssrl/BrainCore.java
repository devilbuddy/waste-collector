package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

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
        public Position endPosition = new Position();
    }

    private static MoveResult moveResult = new MoveResult();

    public static MoveResult move(final World world, final Entity entity, final Direction moveDirection, final Assets.Sounds sounds) {
        final EntityFactory entityFactory = world.getEntityFactory();
        final Scheduler scheduler = world.getScheduler();
        final MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);
        final Position currentPosition = entity.getComponent(Position.class);

        moveResult.acted = false;
        moveResult.moved = false;
        moveResult.endPosition.set(currentPosition);

        if (moveAnimation.direction == moveDirection) {

            final Position targetPosition = currentPosition.clone();
            world.translateWraparound(targetPosition, moveDirection);

            Gdx.app.log(tag, "targetPosition:" + targetPosition);

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
                        World.Cell cell = world.getCell(targetPosition);
                        for (int i = 0; i < cell.getEntityCount(); i++) {
                            int entityId = cell.getEntityId(i);
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
                    World.Cell cell = world.getCell(targetPosition);
                    for (int i = 0; i < cell.getEntityCount(); i++) {
                        int entityId = cell.getEntityId(i);
                        Entity e = world.getEntity(entityId);
                        ItemContainer pickupItem = e.getComponent(ItemContainer.class);
                        if (pickupItem != null) {
                            pickupItem.emptyInto(itemContainer);
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
                World.Cell cell = world.getCell(targetPosition);
                for (int i = 0; i < cell.getEntityCount(); i++) {
                    int entityId = cell.getEntityId(i);
                    Entity e = world.getEntity(entityId);
                    Stats stats = e.getComponent(Stats.class);
                    if (stats != null) {
                        Entity explosion = entityFactory.makeExplosion(targetPosition.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2, targetPosition.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2);
                        world.addEntity(explosion);

                        stats.damage(1);
                        e.alive = stats.isAlive();
                    }
                }
            }

        } else {
            moveAnimation.startTurn(moveDirection, new Runnable() {
                @Override
                public void run() {

                }
            });
            moveResult.acted = true;
        }

        return moveResult;
    }

    public static void fire(final World world, final Entity entity, final Direction direction, final Assets.Sounds sounds) {
        final EntityFactory entityFactory = world.getEntityFactory();
        final Scheduler scheduler = world.getScheduler();

        Position bulletStart = entity.getComponent(Position.class).clone().translate(direction);

        final Position bulletEnd = bulletStart.clone();
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

        sounds.play(Assets.Sounds.SoundId.LASER);
        final Entity bullet = entityFactory.makeBullet();
        final boolean hit = hitSomething;
        scheduler.lock();
        bullet.getComponent(MoveAnimation.class).startMove(bulletStart, distanceTiles * Assets.TILE_SIZE, direction, new Runnable() {
            @Override
            public void run() {
                bullet.alive = false;
                scheduler.unlock();

                float explosionX = bulletEnd.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2;
                float explosionY = bulletEnd.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2;
                switch (direction) {
                    case NORTH:
                        explosionY -= Assets.TILE_SIZE/2;
                        break;
                    case SOUTH:
                        explosionY += Assets.TILE_SIZE/2;
                        break;
                    case EAST:
                        explosionX -= Assets.TILE_SIZE/2;
                        break;
                    case WEST:
                        explosionX += Assets.TILE_SIZE/2;
                        break;
                }
                Entity explosion = entityFactory.makeExplosion(explosionX, explosionY);
                world.addEntity(explosion);

                sounds.play(Assets.Sounds.SoundId.HIT);
                if (hit) {
                    World.Cell cell = world.getCell(bulletEnd.x, bulletEnd.y);
                    int entityCount = cell.getEntityCount();
                    for (int i = 0; i < entityCount; i++) {
                        int entityId = cell.getEntityId(i);
                        Entity entity = world.getEntity(entityId);
                        Components.Stats stats = entity.getComponent(Components.Stats.class);
                        if (stats != null) {
                            stats.damage(1);
                            entity.alive = stats.isAlive();
                        }
                    }
                }
            }
        });

        world.addEntity(bullet);
    }

}
