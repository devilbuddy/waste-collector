package com.dg.ssrl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by magnus on 2015-10-04.
 */
public class MapRenderer {

    Viewport viewport = new FitViewport(80, 80);

    private final Assets assets;

    public MapRenderer(Assets assets) {
        this.assets = assets;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
        viewport.getCamera().position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() /2, 0);
    }

    public void render(SpriteBatch spriteBatch) {

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);


        int size = 10;

        int yy = 0;

        for (int y = 0; y < size; y++) {

            int xx = 0;
            for (int x = 0; x < size; x++) {
                spriteBatch.draw(assets.tiles[0][x%2], xx, yy);
                xx += 8;
            }
            yy += 8;
        }
    }

}
