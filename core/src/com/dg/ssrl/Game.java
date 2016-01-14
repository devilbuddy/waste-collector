package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

import static com.dg.ssrl.Components.ItemContainer;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Sprite;
import static com.dg.ssrl.Components.Stats;

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

	private ScoreData highScore;

    private enum State {
		MENU,
        PLAY,
        GAME_OVER,
        FADE_OUT_LEVEL,
        FADE_IN_LEVEL,
		WIN
    }

    private State state = State.MENU;

	public static final float LONG_PRESS_DURATION = 1.1f;
	private static final int WORLD_WIDTH = 10;
	private static final int WORLD_HEIGHT = 10;


	private GestureDetector gestureDetector;
	public Game(Position[] debugScreenSizes) {
		DebugInputSwitcher debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
		playerInputAdapter = new PlayerInputAdapter();
		gestureDetector = new GestureDetector(20, 0.1f, LONG_PRESS_DURATION, 0.15f, playerInputAdapter);

		inputMultiplexer.addProcessor(debugInputSwitcher);
		inputMultiplexer.addProcessor(playerInputAdapter);
        inputMultiplexer.addProcessor(gestureDetector);
		mapRenderer = new MapRenderer(assets);

		scheduler = new Scheduler();
	}

	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputMultiplexer);
		spriteBatch = new SpriteBatch();
        assets.create();
		entityFactory = new EntityFactory(assets);

		highScore = loadScore();
		createStarField();
		createInstructionComponents();
    }

	private void initWorld(boolean reset) {
        playerInputAdapter.clear();
		scheduler.clear();

		int sector = 0;
		Entity oldPlayer = null;
		if (world != null) {
			oldPlayer = world.getPlayer();
			if (!reset) {
				sector = world.getSector();
			}
		}
		sector += 1;


		Generator.LevelData levelData = Generator.generate(System.currentTimeMillis(), WORLD_WIDTH, WORLD_HEIGHT, sector, entityFactory);

		world = new World(WORLD_WIDTH, WORLD_HEIGHT, entityFactory, scheduler, sector);
		for (int y = 0; y < WORLD_HEIGHT; y++) {
			for (int x = 0; x < WORLD_WIDTH; x++) {
				world.getCell(x, y).type = levelData.tiles[y][x];
			}
		}

        if (reset) {
            world.addPlayer(entityFactory.makePlayer(levelData.start.x, levelData.start.y, playerInputAdapter));
        } else {
            oldPlayer.getComponent(Position.class).set(levelData.start);
            oldPlayer.getComponent(MoveAnimation.class).reset().setPosition(levelData.start.x * Assets.TILE_SIZE, levelData.start.y * Assets.TILE_SIZE);
            world.addPlayer(oldPlayer);
        }

		int wasteStart = world.getPlayer().getComponent(ItemContainer.class).getAmount(ItemType.Waste);
		world.wasteTarget = levelData.wasteCount + wasteStart;

		world.addExit(entityFactory.makeExit(levelData.exit.x, levelData.exit.y));

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

		initStarField();
    }

	@Override
	public void render () {

		timeStep.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();
        spriteBatch.setProjectionMatrix(mapCamera.combined);

		float topGutterHeight = (4 * assets.font.getCapHeight()) / 2;
		
		if (state != State.MENU) {
			mapRenderer.render(world, spriteBatch, topGutterHeight);

			spriteBatch.setColor(Color.BLACK);
			spriteBatch.draw(assets.whitePixel, 0, 0, width, mapRenderer.bounds.y);

			float mapTopY = mapRenderer.bounds.y + mapRenderer.bounds.height;
			spriteBatch.draw(assets.whitePixel, 0, mapTopY, width, height - mapTopY);

			if (state == State.FADE_OUT_LEVEL || state == State.FADE_IN_LEVEL) {
				spriteBatch.setColor(Color.BLACK);
				float percentage;
				if (state == State.FADE_IN_LEVEL) {
					percentage = 1f - stateTime / SWITCH_LEVEL_ANIMATION_TIME;
				} else {
					percentage = stateTime / SWITCH_LEVEL_ANIMATION_TIME;
				}
				float hiddenHeight = percentage * mapRenderer.bounds.height;
				float hiddenY = mapRenderer.bounds.y + mapRenderer.bounds.height - hiddenHeight;
				spriteBatch.draw(assets.whitePixel, mapRenderer.bounds.x, hiddenY, mapRenderer.bounds.width, hiddenHeight);
			} else if (state == State.GAME_OVER) {
				Assets.GlyphLayoutCacheItem gameOver = assets.gameOverText;
				float x = mapRenderer.bounds.x + mapRenderer.bounds.width / 2 - gameOver.glyphLayout.width / 2;
				float y = mapRenderer.bounds.y + (mapRenderer.bounds.height / 3)*2 + gameOver.glyphLayout.height;
				assets.font.setColor(Color.ORANGE);
				assets.font.draw(spriteBatch, gameOver.text, x, y);
			} else if (state == State.WIN) {
				spriteBatch.setColor(Color.BLACK);
				float hiddenHeight = mapRenderer.bounds.height;
				float hiddenY = mapRenderer.bounds.y + mapRenderer.bounds.height - hiddenHeight;
				spriteBatch.draw(assets.whitePixel, mapRenderer.bounds.x, hiddenY, mapRenderer.bounds.width, hiddenHeight);

				Assets.GlyphLayoutCacheItem congratulations = assets.congratulationsText;
				float x = mapRenderer.bounds.x + mapRenderer.bounds.width / 2 - congratulations.glyphLayout.width / 2;
				float y = mapRenderer.bounds.y + (mapRenderer.bounds.height / 3)*2 + congratulations.glyphLayout.height;

				Color tmp = new Color(Color.RED);
				float add = stateTime - (int) stateTime;
				tmp.add(add, add, add, 0);
				assets.font.setColor(tmp);

				assets.font.draw(spriteBatch, congratulations.text, x, y);
			}
		}

		renderHud();

		spriteBatch.end();
	}

	private float renderHudItemCentered(Assets.GlyphLayoutCacheItem glyphLayoutCacheItem, float y) {
		assets.font.draw(spriteBatch, glyphLayoutCacheItem.text, hudWidth / 2 - glyphLayoutCacheItem.glyphLayout.width / 2, y);
		return glyphLayoutCacheItem.glyphLayout.height;
	}

	private void renderHud() {
		spriteBatch.setProjectionMatrix(hudCamera.combined);
		if (state == State.MENU) {
			renderMainMenuHud();
		} else if (state == State.WIN) {
			renderWinHud();
		} else if (state == State.GAME_OVER) {
			renderGameOverHud();
		} else {
			renderGameHud();
		}
	}

	private void renderMainMenuHud() {

		for (int i = 0; i < NUM_STARS; i++) {
			Star star = stars[i];
			spriteBatch.setColor(star.color);
			spriteBatch.draw(assets.whitePixel, star.position.x, star.position.y);
		}

		spriteBatch.setColor(Color.WHITE);
		float logoW = assets.logo.getRegionWidth() * 2;
		float logoH = assets.logo.getRegionHeight() * 2;
		float logoX = hudWidth/2 - logoW /2;
		float logoY = (hudHeight/3 * 2) - logoH / 2;
		spriteBatch.draw(assets.logo, logoX, logoY, logoW, logoH);

		float y = hudHeight / 2 + assets.tapToStartText.glyphLayout.height;

		if (highScore != null) {
			assets.font.setColor(Color.YELLOW);
			renderHudItemCentered(assets.getGlyphLayoutCacheItem("HI-SCORE " + highScore.score), y);
		}

		assets.font.setColor(Color.ORANGE);
		y -= 2 *assets.font.getLineHeight();
		renderHudItemCentered(assets.tapToStartText, y);

		y -= 2.5f * assets.font.getLineHeight();
		renderInstructionComponent(instructionComponents[instructionComponentIndex], spriteBatch, y);
	}

	private void renderWinHud() {
		ScoreData scoreData = world.getScoreData();
		assets.font.setColor(Color.YELLOW);
		float y = (mapRenderer.bounds.y + mapRenderer.bounds.height/2) * 2 + assets.font.getLineHeight();

		y -= renderHudItemCentered(assets.wasteCollectedText, y);
		y -= renderHudItemCentered(assets.getGlyphLayoutCacheItem(scoreData.getWasteCollectedString()), y);
		y -= renderHudItemCentered(assets.scoreText, y);
		renderHudItemCentered(assets.getGlyphLayoutCacheItem(scoreData.getScoreString()), y);
	}

	private void renderGameOverHud() {
		ScoreData scoreData = world.getScoreData();
		assets.font.setColor(Color.YELLOW);
		float y = (mapRenderer.bounds.y + mapRenderer.bounds.height/2) * 2 + assets.font.getLineHeight();

		y -= renderHudItemCentered(assets.sectorReachedText, y);
		y -= renderHudItemCentered(assets.getGlyphLayoutCacheItem(scoreData.getSectorString()), y);
		y -= renderHudItemCentered(assets.wasteCollectedText, y);
		y -= renderHudItemCentered(assets.getGlyphLayoutCacheItem(scoreData.getWasteCollectedString()), y);
		y -= renderHudItemCentered(assets.scoreText, y);
		renderHudItemCentered(assets.getGlyphLayoutCacheItem(scoreData.getScoreString()), y);
	}

	private void renderGameHud() {
		Entity player = world.getPlayer();
		if (player != null) {

			float firstColumnX = 4;
			float firstRowY = hudHeight;
			float secondColumnX = hudWidth / 2;
			float secondRowY = hudHeight - assets.font.getCapHeight();
			float thirdRowY = hudHeight - 2 * assets.font.getCapHeight();
			float fourthRowY = hudHeight - 3 * assets.font.getCapHeight();

			Stats stats = player.getComponent(Stats.class);
			ItemContainer itemContainer = player.getComponent(ItemContainer.class);
			String sectorString = "SECTOR " + world.getSector();

			assets.font.setColor(Color.ORANGE);
			assets.font.draw(spriteBatch, stats.healthString, firstColumnX, firstRowY);
			assets.font.draw(spriteBatch, itemContainer.getAmountString(ItemType.Ammo), firstColumnX, secondRowY);
			assets.font.draw(spriteBatch, itemContainer.getAmountString(ItemType.Rocket), firstColumnX, thirdRowY);

			assets.font.draw(spriteBatch, sectorString, secondColumnX, firstRowY);
			assets.font.draw(spriteBatch, itemContainer.getAmountString(ItemType.Waste), secondColumnX, secondRowY);

			if (gestureDetector.isLongPressed()) {
				float percentage = playerInputAdapter.getLongPressPercentage();
				if (percentage > 0.1f && itemContainer.getAmount(ItemType.Rocket) > 0) {
					float longPressBarWidth = hudWidth * percentage;
					float longPressBarY = fourthRowY - 5; //(mapRenderer.bounds.y * 2) - 3;
					spriteBatch.setColor(Color.RED);
					spriteBatch.draw(assets.whitePixel, 0, longPressBarY, longPressBarWidth, 2);
				}
			}
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
			case MENU: {
				updateStarField(delta);
				updateInstructionComponents(delta);
				if (playerInputAdapter.popAction() == PlayerInputAdapter.Action.FIRE_PRIMARY) {
					initWorld(true);
					setState(State.PLAY);
				}
				break;
			}
            case PLAY: {
                if (world.isCompleted()) {
                    setState(State.FADE_OUT_LEVEL);
                } else if(world.isGameOver()) {
                    setState(State.GAME_OVER);
					saveScore(world.getScoreData());
					highScore = loadScore();
                } else {

					ItemContainer itemContainer = world.getPlayer().getComponent(ItemContainer.class);
					boolean hasKey = itemContainer.getAmount(ItemType.Key) > 0;
					world.getExit().getComponent(Sprite.class).enableAnimation(hasKey);

					if (world.canSpawnRobot) {
						if (itemContainer.getAmount(ItemType.Waste) == world.wasteTarget) {
							Position p = world.getFreePositionFurthestFromPlayer();
							if (p != null) {
								Entity robot = entityFactory.makeMonster(p.x, p.y, MonsterType.Robot);
								world.addEntity(robot);
								world.addEntity(entityFactory.makeExplosion(p.x * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, p.y * Assets.TILE_SIZE + Assets.TILE_SIZE / 2, Color.MAGENTA));
								world.canSpawnRobot = false;
								assets.sounds.play(Assets.Sounds.SoundId.TELEPORT);

							}
						}
					}

                    world.update(delta);
                }
                break;
            }
            case FADE_OUT_LEVEL: {
                if (stateTime > SWITCH_LEVEL_ANIMATION_TIME) {
					if (world.getSector() >= World.MAX_SECTOR) {
						//Win!
						world.generateStats();
						setState(State.WIN);
					} else {
						initWorld(false);
						setState(State.FADE_IN_LEVEL);
					}
                }
                break;
            }
            case FADE_IN_LEVEL: {
                if (stateTime > SWITCH_LEVEL_ANIMATION_TIME) {
                    setState(State.PLAY);
                }
                break;
            }
            case GAME_OVER:
				world.update(delta);
				if (playerInputAdapter.popAction() == PlayerInputAdapter.Action.FIRE_PRIMARY) {
					setState(State.MENU);
				}
				break;
			case WIN: {
                if (playerInputAdapter.popAction() == PlayerInputAdapter.Action.FIRE_PRIMARY) {
                    setState(State.MENU);
                }
                break;
            }
        }


	}

	private static class InstructionComponent {
		final String text;
		final Sprite sprite;
		Vector2 position = new Vector2();

		float startX;
		boolean in = true;

		public InstructionComponent(String text, Sprite sprite, int x) {
			this.text = text;
			this.sprite = sprite;
			startX = x;
			position.x = x;
		}

		public void update(float delta) {
			sprite.update(delta);
		}

	}

	public void updateInstructionComponents(float delta) {
		InstructionComponent instructionComponent = instructionComponents[instructionComponentIndex];
		instructionComponent.update(delta);

		easeTime += delta;
		float alpha = easeTime/EASE_TIME;
		if (instructionComponent.in) {
			instructionTarget.x = width*2 / 2;
			instructionComponent.position.interpolate(instructionTarget, alpha, Interpolation.pow2In);

			if (easeTime > EASE_TIME) {
				instructionComponent.in = false;
				easeTime = 0;
			}
		} else {
			instructionTarget.x = -width;
			instructionComponent.position.interpolate(instructionTarget, alpha, Interpolation.pow2In);

			if (easeTime > EASE_TIME) {
				//done
				instructionComponent.in = true;
				instructionComponent.position.x = instructionComponent.startX;
				easeTime = 0;
				instructionComponentIndex = (instructionComponentIndex + 1) % instructionComponents.length;
			}
		}

	}

	private float easeTime = 0;
	private static final float EASE_TIME = 2.5f;

	private Vector2 instructionTarget = new Vector2();
	private int instructionComponentIndex = 0;
	private InstructionComponent[] instructionComponents;

	private void createInstructionComponents() {
		int x = width * 2 + 80;
		instructionComponents = new InstructionComponent[] {
				new InstructionComponent("[GARBAGE-MAN]", new Sprite(assets.getMonsterTextureRegion(MonsterType.Player)), x),
				new InstructionComponent("[COLLECT]", new Sprite(assets.getItemTextureRegion(ItemType.Waste)), x),
				new InstructionComponent("[SURVIVE]", new Sprite(assets.getMonsterTextureRegion(MonsterType.Crawler)), x),
				new InstructionComponent("[REVIVE]", new Sprite(assets.getItemTextureRegion(ItemType.Heart)), x),
				new InstructionComponent("[WARP]", new Sprite(assets.teleporterFrames, 0.2f, 0, Assets.SEA_BLUE), x),
				new InstructionComponent("[KEYCARD]", new Sprite(assets.getItemTextureRegion(ItemType.Key)), x),
				new InstructionComponent("[ESCAPE]", new Sprite(assets.exitFrames, 0.1f, 0, Assets.SKY_BLUE), x)
		};
	}

	private void renderInstructionComponent(InstructionComponent instructionComponent, SpriteBatch spriteBatch, float topY) {

		float x = instructionComponent.position.x;
		Assets.GlyphLayoutCacheItem text = assets.getGlyphLayoutCacheItem(instructionComponent.text);
		assets.font.setColor(Color.ORANGE);
		assets.font.draw(spriteBatch, text.text, x - text.glyphLayout.width/2, topY);

		topY -= assets.font.getLineHeight();

		Sprite sprite = instructionComponent.sprite;
		TextureRegion textureRegion = sprite.getTextureRegion();
		int w = textureRegion.getRegionWidth();
		int h = textureRegion.getRegionHeight();

		int displayWidth = w * 2;
		int displayHeight = h * 2;

		spriteBatch.setColor(sprite.color);
		spriteBatch.draw(sprite.getTextureRegion(), x - displayWidth/2, topY - displayHeight - 4, displayWidth, displayHeight);
	}


	private static class Star {
		Vector2 position = new Vector2();
		Vector2 velocity = new Vector2();
		Color color = new Color();

		public void update(float delta) {
			float dx = velocity.x * delta;
			float dy = velocity.y * delta;
			position.add(dx, dy);
		}
	}

	private static final int NUM_STARS = 50;

	private Star[] stars;
	private Random random = new Random(System.currentTimeMillis());

	private void createStarField() {
		stars = new Star[NUM_STARS];
		for (int i = 0; i < NUM_STARS; i++) {
			stars[i] = new Star();
		}
	}

	private void initStarField() {
		for (int i = 0; i < NUM_STARS; i++) {
			Star star = stars[i];
			star.position.x = random.nextInt(hudWidth);
			star.position.y = random.nextInt(hudHeight);

			float velocityPercent = random.nextFloat();
			star.velocity.y = 50f * velocityPercent;

			star.color.set(Color.WHITE);
			star.color.mul(velocityPercent);
		}
	}

	private void updateStarField(float delta) {
		for (int i = 0; i < NUM_STARS; i++) {
			Star star = stars[i];
			star.update(delta);
			if (star.position.y > hudHeight) {
				star.position.y = -1;
				star.position.x = random.nextInt(hudWidth);
			}
		}
	}

	private static final String SCORE_PREFERENCES = "highscore";
	private static final String SCORE_VERSION_KEY = "version";

	private static final String SCORE_SECTOR_KEY = "sector";
	private static final String SCORE_WASTE_KEY = "waste";

	private static final int SCORE_VERSION = 1;

	private void saveScore(ScoreData scoreData) {
		Preferences preferences = Gdx.app.getPreferences(SCORE_PREFERENCES);

		boolean save = false;
		ScoreData current = loadScore();
		if (current == null || current.score < scoreData.score) {
			save = true;
		}

		if (save) {
			preferences.putInteger(SCORE_VERSION_KEY, SCORE_VERSION);
			preferences.putInteger(SCORE_SECTOR_KEY, scoreData.sector);
			preferences.putInteger(SCORE_WASTE_KEY, scoreData.wasteCollected);
			preferences.flush();
		}

	}

	private ScoreData loadScore() {
		Preferences preferences = Gdx.app.getPreferences(SCORE_PREFERENCES);
		if (preferences.contains(SCORE_SECTOR_KEY) && preferences.contains(SCORE_WASTE_KEY)) {
			return new ScoreData(preferences.getInteger(SCORE_SECTOR_KEY), preferences.getInteger(SCORE_WASTE_KEY));
		}
		return null;
	}

}
