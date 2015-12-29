package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

class PlayerBrain implements Components.Brain {

	private static final String tag = "PlayerBrain";

    private final MapMovementInputHandler mapMovementInputHandler;
    private final Scheduler scheduler;
    private final EntityFactory entityFactory;

	public PlayerBrain(MapMovementInputHandler inputHandler, Scheduler scheduler, EntityFactory entityFactory) {
        this.mapMovementInputHandler = inputHandler;
        this.scheduler = scheduler;
        this.entityFactory = entityFactory;
    }

    @Override
    public boolean act(final World world) {
        boolean acted = false;

        final Entity player = world.getEntity(world.playerEntityId);
        final Components.MoveAnimation playerMoveAnimation = player.getComponent(Components.MoveAnimation.class);

        if (!playerMoveAnimation.isBusy()) {

            Direction moveDirection = mapMovementInputHandler.getMovementDirection();

            if (moveDirection != Direction.NONE) {
                Gdx.app.log(tag, "moveDirection=" + moveDirection);

                if (playerMoveAnimation.direction == moveDirection) {
                    Components.Position position = player.getComponent(Components.Position.class);

                    final Components.Position targetPosition = position.clone();
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
                        playerMoveAnimation.startMove(position, Assets.TILE_SIZE, moveDirection, new Runnable() {
                            @Override
                            public void run() {
                                playerMoveAnimation.setPosition(targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE);
                            }
                        });
                        world.move(player, targetPosition.x, targetPosition.y);
                        acted = true;
                    }

                } else {
                    playerMoveAnimation.startTurn(moveDirection, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    acted = true;
                }
            }
            MapMovementInputHandler.Action action;
            while ((action = mapMovementInputHandler.popAction()) != null) {
                if (action == MapMovementInputHandler.Action.FIRE) {
                    Gdx.app.log(tag, "FIRE");

                    Components.Position bulletStartPosition = player.getComponent(Components.Position.class).clone().translate(playerMoveAnimation.direction);

                    Components.Position endPosition = bulletStartPosition.clone();
                    boolean hitSomething = false;
                    int distanceTiles = 0;
                    while (!hitSomething) {
                        endPosition.x = endPosition.x % world.getWidth();
                        endPosition.y = endPosition.y % world.getHeight();
                        while (endPosition.x < 0) {
							endPosition.x += world.getWidth();
						}
                        while (endPosition.y < 0) {
							endPosition.y += world.getHeight();
						}
                        if (world.getCell(endPosition.x, endPosition.y).isWalkable()) {
                            endPosition.translate(playerMoveAnimation.direction);
                            distanceTiles++;
                        } else {
                            hitSomething = true;
                        }
                    }

                    final Entity bullet = entityFactory.makeBullet();
                    scheduler.lock();
                    bullet.getComponent(Components.MoveAnimation.class).startMove(bulletStartPosition, distanceTiles * Assets.TILE_SIZE, playerMoveAnimation.direction, new Runnable() {
                        @Override
                        public void run() {
                            bullet.alive = false;
                            scheduler.unlock();
                        }
                    });

                    world.addEntity(bullet);
                    acted = true;

                } else if (action == MapMovementInputHandler.Action.BOMB) {
                    Gdx.app.log(tag, "BOMB");
                }
            }
        }
        return acted;
    }
}
