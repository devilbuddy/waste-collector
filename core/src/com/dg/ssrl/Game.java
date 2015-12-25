package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Game extends ApplicationAdapter {

	private static class DebugInputSwitcher extends InputAdapter {
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
	private World world = new World(16, 16);

	public Game(Point[] debugScreenSizes) {
		debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
		mapMovementInputHandler = new MapMovementInputHandler();
        inputMultiplexer.addProcessor(debugInputSwitcher);
		inputMultiplexer.addProcessor(mapMovementInputHandler);
        mapRenderer = new MapRenderer(assets);

		entityFactory = new EntityFactory();
		player = entityFactory.makePlayer();

		world.addEntity(player);
	}

	@Override
	public void create () {
        spriteBatch = new SpriteBatch();
        assets.create();
        Gdx.input.setInputProcessor(inputMultiplexer);

        stage = new Stage(new StretchViewport(160, 240));
        stage.setDebugAll(true);
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
		spriteBatch.begin();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.draw(assets.tiles[0][7], 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
		spriteBatch.end();

		spriteBatch.begin();
		mapRenderer.render(world, spriteBatch, player.id);
		spriteBatch.end();

	}

	private void step(float delta) {
		Entity.MoveState moveState = player.getComponent(Entity.MoveState.class);

		if(moveState.isBusy()) {
			moveState.update(delta);
		} else {
			Direction moveDirection = mapMovementInputHandler.getMovementDirection();
			if(moveDirection != Direction.NONE) {
				Gdx.app.log(tag, "moveDirection=" + moveDirection);
				Entity.Position position = player.getComponent(Entity.Position.class);
				final Entity.Position targetPosition = position.clone();
				targetPosition.translate(moveDirection);
				moveState.init(position.x * Assets.TILE_SIZE, position.y * Assets.TILE_SIZE,
						targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE, new Runnable() {
							@Override
							public void run() {
								player.getComponent(Entity.Position.class).set(targetPosition.x, targetPosition.y);
							}
						});
			}
		}
	}
}
