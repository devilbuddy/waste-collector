package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;

import static com.dg.ssrl.Components.*;

public class Game extends ApplicationAdapter {

	private class DebugInputSwitcher extends InputAdapter {
		private int index = 0;
		private final Point[] debugScreenSizes;
		public  DebugInputSwitcher(Point[] debugScreenSizes) {
			this.debugScreenSizes = debugScreenSizes;
		}
		@Override
		public boolean keyDown (int keycode) {
			if (keycode == Input.Keys.R) {
				if (Gdx.graphics.supportsDisplayModeChange() && debugScreenSizes != null) {
					index++;
					if (index >= debugScreenSizes.length) {
						index = 0;
					}
					Point size = debugScreenSizes[index];
					Gdx.graphics.setDisplayMode(size.x, size.y, false);
				}
			} else if (keycode == Input.Keys.G) {
				initWorld();
			}
			return false;
		}
	}

	private class TimeStep {
		private static final int MAX_UPDATE_ITERATIONS = 5;
		private static final float FIXED_TIME_STEP = 1f / 60f;
		private float accumulator = 0;

		public void update() {
			// step
			accumulator += Gdx.graphics.getRawDeltaTime();
			int iterations = 0;
			while (accumulator > FIXED_TIME_STEP && iterations < MAX_UPDATE_ITERATIONS) {
				step(FIXED_TIME_STEP);
				accumulator -= FIXED_TIME_STEP;
				iterations++;
			}
		}

	}

	private static final String tag = "Game";


	private int width = 80;
	private int height;

	private OrthographicCamera camera = new OrthographicCamera();

	private TimeStep timeStep = new TimeStep();

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private MapMovementInputHandler mapMovementInputHandler;

    private Assets assets = new Assets();
	private SpriteBatch spriteBatch;
	private MapRenderer mapRenderer;
	private Scheduler scheduler;

	private EntityFactory entityFactory;

	private World world;

	private class PlayerBrain implements Brain {

		@Override
		public boolean act(final World world) {
			boolean acted = false;

			final Entity player = world.getEntity(world.playerEntityId);
			final MoveAnimation playerMoveAnimation = player.getComponent(MoveAnimation.class);

			if(!playerMoveAnimation.isBusy()) {

				Direction moveDirection = mapMovementInputHandler.getMovementDirection();

				if(moveDirection != Direction.NONE) {
					Gdx.app.log(tag, "moveDirection=" + moveDirection);

					if (playerMoveAnimation.direction == moveDirection) {
						Position position = player.getComponent(Position.class);

						final Position targetPosition = position.clone();
						targetPosition.translate(moveDirection);
						targetPosition.x = targetPosition.x % world.getWidth();
						targetPosition.y = targetPosition.y % world.getHeight();
						while (targetPosition.x < 0) { targetPosition.x += world.getWidth(); }
						while (targetPosition.y < 0) { targetPosition.y += world.getHeight(); }

						Gdx.app.log(tag, "targetPosition:" + targetPosition);

						if(world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
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

						Position bulletStartPosition = player.getComponent(Position.class).clone().translate(playerMoveAnimation.direction);

						Position endPosition = bulletStartPosition.clone();
						boolean hitSomething = false;
						int distanceTiles = 0;
						while (!hitSomething) {
							endPosition.x = endPosition.x % world.getWidth();
							endPosition.y = endPosition.y % world.getHeight();
							while (endPosition.x < 0) { endPosition.x += world.getWidth(); }
							while (endPosition.y < 0) { endPosition.y += world.getHeight(); }
							if (world.getCell(endPosition.x, endPosition.y).isWalkable()) {
								endPosition.translate(playerMoveAnimation.direction);
								distanceTiles++;
							} else {
								hitSomething = true;
							}
						}

						final Entity bullet = entityFactory.makeBullet();
						scheduler.lock();
						bullet.getComponent(MoveAnimation.class).startMove(bulletStartPosition, distanceTiles * Assets.TILE_SIZE, playerMoveAnimation.direction, new Runnable() {
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


	public Game(Point[] debugScreenSizes) {
		DebugInputSwitcher debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
		mapMovementInputHandler = new MapMovementInputHandler();

        inputMultiplexer.addProcessor(debugInputSwitcher);
		inputMultiplexer.addProcessor(mapMovementInputHandler);
        inputMultiplexer.addProcessor(new GestureDetector(20, 0.1f, 1.1f, 0.15f, mapMovementInputHandler));
		mapRenderer = new MapRenderer(assets);

		scheduler = new Scheduler();
	}

	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputMultiplexer);

		spriteBatch = new SpriteBatch();
        assets.create();
		entityFactory = new EntityFactory(assets);

		initWorld();
    }



	private void initWorld() {

		scheduler.clear();

		int width = 10;
		int height = 10;
		Generator.LevelData levelData = Generator.generate(System.currentTimeMillis(), width, height);

		world = new World(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				world.getCell(x, y).type = levelData.tiles[y][x];
			}
		}

		Entity player = entityFactory.makePlayer();

		Point start = levelData.start;
		player.getComponent(Position.class).set(start.x, start.y);
		player.getComponent(MoveAnimation.class).setPosition(start.x * Assets.TILE_SIZE, start.y * Assets.TILE_SIZE).setDirection(Direction.EAST);

		Actor actor = new Actor(new PlayerBrain());
		player.addComponent(actor);

		world.addPlayer(player);

		scheduler.addActor(actor);

		for (int i = 0; i < levelData.monsters.size(); i++) {
			Point monsterPoint = levelData.monsters.get(i);
			Entity monster = entityFactory.makeMonster(monsterPoint.x, monsterPoint.y);
			world.addEntity(monster);

			scheduler.addActor(monster.getComponent(Actor.class));
		}
	}

    @Override
    public void resize(int w, int h) {
		Gdx.app.log(tag, "resize " + w + " " + h);

		float ratio = (float)h/(float)w;
		height = (int)(width * ratio);

		camera.setToOrtho(false, width, height);
		camera.update();

        mapRenderer.resize(width, height);
    }

	@Override
	public void render () {
		timeStep.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);

		mapRenderer.render(world, spriteBatch);

		spriteBatch.setColor(Color.BLACK);
		spriteBatch.draw(assets.whitePixel, 0, 0, width, mapRenderer.bounds.y);
		spriteBatch.draw(assets.whitePixel, 0, mapRenderer.bounds.y + mapRenderer.bounds.height, width, height - (mapRenderer.bounds.y + mapRenderer.bounds.height) );


		spriteBatch.setColor(Color.WHITE);
		assets.font.draw(spriteBatch, "100", 4, height);

		spriteBatch.end();

	}

	private void step(float delta) {
		for (int i = world.entities.size() - 1; i >= 0; i--) {
			Entity entity = world.entities.get(i);
			if (entity.alive) {
				MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);
				if (moveAnimation != null) {
					moveAnimation.update(delta, world.bounds);
				}
			} else {
				world.entities.remove(i);
			}
		}

		scheduler.update(world);
	}

}
