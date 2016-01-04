package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;

import static com.dg.ssrl.Components.*;

public class Game extends ApplicationAdapter {

	private class DebugInputSwitcher extends InputAdapter {
		private int index = 0;
		private final Position[] debugScreenSizes;
		public  DebugInputSwitcher(Position[] debugScreenSizes) {
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
					Position size = debugScreenSizes[index];
					Gdx.graphics.setDisplayMode(size.x, size.y, false);
				}
			} else if (keycode == Input.Keys.G) {
				initWorld(true);
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

	private int hudWidth = width * 2;
	private int hudHeight;

	private OrthographicCamera mapCamera = new OrthographicCamera();
	private OrthographicCamera hudCamera = new OrthographicCamera();

	private TimeStep timeStep = new TimeStep();

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private PlayerInputAdapter playerInputAdapter;

    private Assets assets = new Assets();
	private SpriteBatch spriteBatch;
	private MapRenderer mapRenderer;
	private Scheduler scheduler;

	private EntityFactory entityFactory;
	private World world;

    private enum State {
        PLAY,
        GAME_OVER,
        FADE_OUT_LEVEL,
        FADE_IN_LEVEL
    }

    private State state = State.PLAY;

	public Game(Position[] debugScreenSizes) {
		DebugInputSwitcher debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
		playerInputAdapter = new PlayerInputAdapter();

        inputMultiplexer.addProcessor(debugInputSwitcher);
		inputMultiplexer.addProcessor(playerInputAdapter);
        inputMultiplexer.addProcessor(new GestureDetector(20, 0.1f, 1.1f, 0.15f, playerInputAdapter));
		mapRenderer = new MapRenderer(assets);

		scheduler = new Scheduler();
	}

	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputMultiplexer);

		spriteBatch = new SpriteBatch();
        assets.create();
		entityFactory = new EntityFactory(assets);

		initWorld(true);
    }

	private void initWorld(boolean resetPlayer) {
        playerInputAdapter.clear();
		scheduler.clear();

		int width = 10;
		int height = 10;
		Generator.LevelData levelData = Generator.generate(System.currentTimeMillis(), width, height, entityFactory);

        Entity oldPlayer = null;
        if (world != null) {
            oldPlayer = world.getPlayer();
        }

		world = new World(width, height, entityFactory, scheduler);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				world.getCell(x, y).type = levelData.tiles[y][x];
			}
		}

        if (resetPlayer) {
            world.addPlayer(entityFactory.makePlayer(levelData.start.x, levelData.start.y, playerInputAdapter, scheduler));
        } else {
            oldPlayer.getComponent(Position.class).set(levelData.start);
            oldPlayer.getComponent(MoveAnimation.class).reset().setPosition(levelData.start.x * Assets.TILE_SIZE, levelData.start.y * Assets.TILE_SIZE);
            world.addPlayer(oldPlayer);
        }
		world.addEntity(entityFactory.makeExit(levelData.exit.x, levelData.exit.y));

		for (int i = 0; i < levelData.entities.size(); i++) {
			Entity entity = levelData.entities.get(i);
			world.addEntity(entity);
		}
	}

    @Override
    public void resize(int w, int h) {
		Gdx.app.log(tag, "resize " + w + " " + h);

		float ratio = (float)h/(float)w;
		height = (int)(width * ratio);

		mapCamera.setToOrtho(false, width, height);
		mapCamera.update();

        mapRenderer.resize(width, height);

		hudHeight = height * 2;
		hudCamera.setToOrtho(false, hudWidth, hudHeight);
        hudCamera.update();
    }

	@Override
	public void render () {
		timeStep.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();
        spriteBatch.setProjectionMatrix(mapCamera.combined);

		mapRenderer.render(world, spriteBatch);

		spriteBatch.setColor(Color.BLACK);
		spriteBatch.draw(assets.whitePixel, 0, 0, width, mapRenderer.bounds.y);

		float mapTopY = mapRenderer.bounds.y + mapRenderer.bounds.height;
		spriteBatch.draw(assets.whitePixel, 0, mapTopY, width, height - mapTopY);

        if (state == State.FADE_OUT_LEVEL) {
            spriteBatch.setColor(Color.BLACK);
            float percentage = stateTime / SWITCH_LEVEL_ANIMATION_TIME;
            float hiddenHeight = percentage * mapRenderer.bounds.height;
            float hiddenY = mapRenderer.bounds.y + mapRenderer.bounds.height - hiddenHeight;
            spriteBatch.draw(assets.whitePixel, mapRenderer.bounds.x, hiddenY, mapRenderer.bounds.width, hiddenHeight);
        } else if (state == State.FADE_IN_LEVEL) {
            spriteBatch.setColor(Color.BLACK);
            float percentage = 1f - stateTime / SWITCH_LEVEL_ANIMATION_TIME;
            float hiddenHeight = percentage * mapRenderer.bounds.height;
            float hiddenY = mapRenderer.bounds.y + mapRenderer.bounds.height - hiddenHeight;
            spriteBatch.draw(assets.whitePixel, mapRenderer.bounds.x, hiddenY, mapRenderer.bounds.width, hiddenHeight);
        } else if (state == State.GAME_OVER) {
            String gameOver = "Game Over";
            GlyphLayout glyphLayout = new GlyphLayout(assets.font, gameOver);
            float x = mapRenderer.bounds.x + mapRenderer.bounds.width/2 - glyphLayout.width/2;
            float y = mapRenderer.bounds.y + mapRenderer.bounds.height/2 + glyphLayout.height;
            assets.font.draw(spriteBatch, gameOver, x, y);
        }

		renderHud();

		spriteBatch.end();
	}

	private void renderHud() {
        Entity player = world.getPlayer();
        if (player != null) {
            spriteBatch.setProjectionMatrix(hudCamera.combined);
            spriteBatch.setColor(Color.WHITE);
            Stats stats = world.getPlayer().getComponent(Stats.class);
            assets.font.draw(spriteBatch, stats.healthString, 4, hudHeight);
        }

	}

    private static final float SWITCH_LEVEL_ANIMATION_TIME = 0.75f;
    private float stateTime = 0;

    private void setState(State state) {
        this.state = state;
        stateTime = 0;
    }

	private void step(float delta) {

        stateTime += delta;

        switch (state) {
            case PLAY: {
                if (world.isCompleted()) {
                    setState(State.FADE_OUT_LEVEL);
                } else if(world.isGameOver()) {
                    setState(State.GAME_OVER);
                } else {
                    world.update(delta);
                }
                break;
            }
            case FADE_OUT_LEVEL: {
                if (stateTime > SWITCH_LEVEL_ANIMATION_TIME) {
                    initWorld(false);
                    setState(State.FADE_IN_LEVEL);
                }
                break;
            }
            case FADE_IN_LEVEL: {
                if (stateTime > SWITCH_LEVEL_ANIMATION_TIME) {
                    setState(State.PLAY);
                }
                break;
            }
            case GAME_OVER: {
                world.update(delta);
                if (playerInputAdapter.popAction() == PlayerInputAdapter.Action.FIRE) {
                    initWorld(true);
                    setState(State.PLAY);
                }
                break;
            }
        }


	}

}
