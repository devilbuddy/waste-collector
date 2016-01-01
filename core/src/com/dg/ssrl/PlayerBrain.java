package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import static com.dg.ssrl.Components.*;

class PlayerBrain implements Brain {

	private static final String tag = "PlayerBrain";

    private final PlayerInputAdapter playerInputAdapter;
    private final Scheduler scheduler;
    private final EntityFactory entityFactory;

	public PlayerBrain(PlayerInputAdapter playerInputAdapter, Scheduler scheduler, EntityFactory entityFactory) {
        this.playerInputAdapter = playerInputAdapter;
        this.scheduler = scheduler;
        this.entityFactory = entityFactory;
    }

    @Override
    public boolean act(final World world) {
        boolean acted = false;

        final Entity player = world.getEntity(world.playerEntityId);
        final MoveAnimation moveAnimation = player.getComponent(MoveAnimation.class);

        if (!moveAnimation.isBusy()) {

            Direction moveDirection = playerInputAdapter.getMovementDirection();

            if (moveDirection != Direction.NONE) {
                Gdx.app.log(tag, "moveDirection=" + moveDirection);

                BrainCore.MoveResult moveResult = BrainCore.move(world, player, moveDirection);
                acted = moveResult.acted;
                if (moveResult.moved) {
                    world.updateDijkstraMap(moveResult.endPosition.x, moveResult.endPosition.y);
                }
            }

            if (!acted) {
                acted = processActions(world);
            }
        }
        return acted;
    }

    private boolean processActions(final World world) {
        boolean acted = false;

        final Entity player = world.getEntity(world.playerEntityId);
        final MoveAnimation moveAnimation = player.getComponent(MoveAnimation.class);

        PlayerInputAdapter.Action action;
        while ((action = playerInputAdapter.popAction()) != null) {
            if (action == PlayerInputAdapter.Action.FIRE) {
                Gdx.app.log(tag, "FIRE");

                Position bulletStart = player.getComponent(Position.class).clone().translate(moveAnimation.direction);

                final Position bulletEnd = bulletStart.clone();
                boolean hitSomething = false;
                int distanceTiles = 0;
                while (!hitSomething) {
                    bulletEnd.x = bulletEnd.x % world.getWidth();
                    bulletEnd.y = bulletEnd.y % world.getHeight();
                    while (bulletEnd.x < 0) {
                        bulletEnd.x += world.getWidth();
                    }
                    while (bulletEnd.y < 0) {
                        bulletEnd.y += world.getHeight();
                    }
                    World.Cell cell = world.getCell(bulletEnd.x, bulletEnd.y);
                    if (cell.isWalkable()) {
                        bulletEnd.translate(moveAnimation.direction);
                        distanceTiles++;
                    } else {
                        hitSomething = true;



                    }
                }

                final Entity bullet = entityFactory.makeBullet();
                final boolean hit = hitSomething;
                scheduler.lock();
                bullet.getComponent(MoveAnimation.class).startMove(bulletStart, distanceTiles * Assets.TILE_SIZE, moveAnimation.direction, new Runnable() {
                    @Override
                    public void run() {
                        bullet.alive = false;
                        scheduler.unlock();

                        float explosionX = bulletEnd.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2;
                        float explosionY = bulletEnd.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2;
                        switch (moveAnimation.direction) {
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

                        if (hit) {
                            World.Cell cell = world.getCell(bulletEnd.x, bulletEnd.y);
                            int entityCount = cell.getEntityCount();
                            for (int i = 0; i < entityCount; i++) {
                                int entityId = cell.getEntityId(i);
                                Entity entity = world.getEntity(entityId);
                                Stats stats = entity.getComponent(Stats.class);
                                if (stats != null) {
                                    stats.damage(1);
                                    entity.alive = stats.isAlive();
                                }
                            }
                        }
                    }
                });

                world.addEntity(bullet);
                acted = true;

            } else if (action == PlayerInputAdapter.Action.BOMB) {
                Gdx.app.log(tag, "BOMB");
            }
        }
        return acted;
    }

}
