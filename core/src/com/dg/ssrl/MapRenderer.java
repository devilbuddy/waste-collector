package com.dg.ssrl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by magnus on 2015-10-04.
 */
public class MapRenderer {

    int tileSize = 8;
    int mapSize = 16;
    Viewport viewport = new FitViewport(tileSize * mapSize, tileSize * mapSize);

    private final Assets assets;

    public MapRenderer(Assets assets) {
        this.assets = assets;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render(SpriteBatch spriteBatch) {

        viewport.apply(true);
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);


        int size = 16;

        int yy = 0;

        for (int y = 0; y < size; y++) {

            int xx = 0;
            for (int x = 0; x < size; x++) {
                spriteBatch.draw(assets.tiles[y%3][x%2], xx, yy);
                xx += 8;
            }
            yy += 8;
        }
    }

}
