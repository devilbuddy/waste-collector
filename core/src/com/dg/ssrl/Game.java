package com.dg.ssrl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

			}
			return false;
		}
	}
	private static final String tag = "Game";

	private DebugInputSwitcher debugInputSwitcher;
	private InputMultiplexer inputMultiplexer = new InputMultiplexer();

    private Assets assets = new Assets();

    private SpriteBatch spriteBatch;
	private MapRenderer mapRenderer;

	public Game(Point[] debugScreenSizes) {
		debugInputSwitcher = new DebugInputSwitcher(debugScreenSizes);
        inputMultiplexer.addProcessor(debugInputSwitcher);

        mapRenderer = new MapRenderer(assets);
	}

	@Override
	public void create () {
        spriteBatch = new SpriteBatch();
        assets.create();
        Gdx.input.setInputProcessor(inputMultiplexer);
	}

    @Override
    public void resize(int width, int height) {
        Gdx.app.log(tag, "resize " + width + " " + height);
        mapRenderer.resize(width, height);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

        mapRenderer.render(spriteBatch);

		spriteBatch.end();
	}


}
