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

                if (moveAnimation.direction == moveDirection) {
                    Position position = player.getComponent(Position.class);

                    final Position targetPosition = position.clone();
                    targetPosition.translate(moveDirection);
                    targetPosition.x = targetPosition.x % world.getWidth();
                    targetPosition.y = targetPosition.y % world.getHeight();
                    while (targetPosition.x < 0) {
						targetPosition.x += world.getWidth();
					}
                    while (targetPosition.y < 0) {
						targetPosition.y += world.getHeight();
					}

                    Gdx.app.log(tag, "targetPosition:" + targetPosition);

                    if (world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
                        moveAnimation.startMove(position, Assets.TILE_SIZE, moveDirection, new Runnable() {
                            @Override
                            public void run() {
                                moveAnimation.setPosition(targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE);
                            }
                        });
                        world.move(player, targetPosition.x, targetPosition.y);
                        acted = true;
                    }

                } else {
                    moveAnimation.startTurn(moveDirection, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    acted = true;
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
                    if (world.getCell(bulletEnd.x, bulletEnd.y).isWalkable()) {
                        bulletEnd.translate(moveAnimation.direction);
                        distanceTiles++;
                    } else {
                        hitSomething = true;
                    }
                }

                final Entity bullet = entityFactory.makeBullet();
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
