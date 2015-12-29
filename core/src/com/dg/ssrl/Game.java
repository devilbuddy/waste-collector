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

		Actor actor = new Actor(new PlayerBrain(mapMovementInputHandler, scheduler, entityFactory));
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
