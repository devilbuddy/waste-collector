package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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
				if (Gdx.graphics.supportsDisplayModeChange()) {
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

	private TimeStep timeStep = new TimeStep();

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private DebugInputSwitcher debugInputSwitcher;
	private MapMovementInputHandler mapMovementInputHandler;

    private Assets assets = new Assets();
	private SpriteBatch spriteBatch;
	private MapRenderer mapRenderer;

    private Stage stage;

	private EntityFactory entityFactory;

	Entity player;
	private World world;

	public Game(Point[] debugScreenSizes) {
		debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
		mapMovementInputHandler = new MapMovementInputHandler();

        inputMultiplexer.addProcessor(debugInputSwitcher);
		inputMultiplexer.addProcessor(mapMovementInputHandler);
        inputMultiplexer.addProcessor(new GestureDetector(20, 0.1f, 1.1f, 0.15f, mapMovementInputHandler));
		mapRenderer = new MapRenderer(assets);

		entityFactory = new EntityFactory();
		player = entityFactory.makePlayer();

	}

	@Override
	public void create () {
        spriteBatch = new SpriteBatch();
        assets.create();
        Gdx.input.setInputProcessor(inputMultiplexer);

        stage = new Stage(new StretchViewport(160, 240));
        stage.setDebugAll(true);

		initWorld();
    }

	private void initWorld() {
		int width = 16;
		int height = 16;
		Generator.LevelData levelData = Generator.generate(System.currentTimeMillis(), width, height);

		Point start = levelData.start;
		player.getComponent(Entity.Position.class).set(start.x, start.y);
		player.getComponent(Entity.MoveAnimation.class).animations[0].position.set(start.x * Assets.TILE_SIZE, start.y * Assets.TILE_SIZE);
		player.getComponent(Entity.MoveAnimation.class).direction = Direction.EAST;

		world = new World(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				world.getCell(x, y).type = levelData.tiles[y][x];
			}
		}
		world.addEntity(player);
	}

    @Override
    public void resize(int width, int height) {
		Gdx.app.log(tag, "resize " + width + " " + height);
        mapRenderer.resize(width, height);

        stage.getViewport().update(width, height);
		stage.getViewport().apply(true);

		Gdx.app.log(tag, "viewport " + stage.getViewport().getWorldWidth() + " " + stage.getViewport().getWorldHeight());

    }

	@Override
	public void render () {
		timeStep.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();

		Viewport viewport = stage.getViewport();
		viewport.apply(true);
		spriteBatch.begin();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.draw(assets.tiles[0][7], 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

		assets.font.draw(spriteBatch, "Foo", 50, viewport.getWorldHeight() - assets.font.getLineHeight());

		spriteBatch.end();

		spriteBatch.begin();
		mapRenderer.render(world, spriteBatch, player.id);
		spriteBatch.end();

	}

	private void step(float delta) {
		Entity.MoveAnimation moveAnimation = player.getComponent(Entity.MoveAnimation.class);

		if(moveAnimation.isBusy()) {
			moveAnimation.update(delta);
		} else {
			Direction moveDirection = mapMovementInputHandler.getMovementDirection();

			if(moveDirection != Direction.NONE) {
				Gdx.app.log(tag, "moveDirection=" + moveDirection);

				if (moveAnimation.direction == moveDirection) {
					Entity.Position position = player.getComponent(Entity.Position.class);
					final Entity.Position targetPosition = position.clone();

					targetPosition.translate(moveDirection);

					Gdx.app.log(tag, "targetPosition:" + targetPosition);

					if(world.contains(targetPosition.x, targetPosition.y) && world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
						moveAnimation.initMove(position, targetPosition, new Runnable() {
							@Override
							public void run() {
								world.move(player, targetPosition.x, targetPosition.y);
							}
						});
					} else {
						// wrap around
						Entity.Position start1 = position.clone();
						Entity.Position end1 = position.clone();
						end1.translate(moveDirection);

						Entity.Position end2 = end1.clone();
						end2.x = end2.x % world.getWidth();
						end2.y = end2.y % world.getHeight();
						while (end2.x < 0) { end2.x += world.getWidth(); }
						while (end2.y < 0) { end2.y += world.getHeight(); }

						if (world.getCell(end2.x, end2.y).isWalkable()) {
							Entity.Position start2 = end2.clone();
							start2.translate(moveDirection.opposite());

							final Entity.Position finalTarget = end2.clone();
							moveAnimation.initMove(start1, end1, start2, end2, new Runnable() {
								@Override
								public void run() {
									world.move(player, finalTarget.x, finalTarget.y);
								}
							});
						}
					}
				} else {
					moveAnimation.initTurn(moveDirection, new Runnable() {
						@Override
						public void run() {

						}
					});
				}
			}
			MapMovementInputHandler.Action action;
			while ((action = mapMovementInputHandler.popAction()) != null) {
				if (action == MapMovementInputHandler.Action.FIRE) {
					Gdx.app.log(tag, "FIRE");
				} else if (action == MapMovementInputHandler.Action.BOMB) {
					Gdx.app.log(tag, "BOMB");
				}
			}
		}
	}
}
