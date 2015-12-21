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

    public void render(World world, SpriteBatch spriteBatch) {

        viewport.apply(true);
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);


        World.Cell[][] cells = world.getCells();

        int width = world.getWidth();
        int height = world.getHeight();

        int yy = 0;
        for (int y = 0; y < height; y++) {
            int xx = 0;
            for (int x = 0; x < width; x++) {
                World.Cell cell = cells[y][x];

                spriteBatch.draw(assets.tiles[y%3][x%2], xx, yy);
                xx += Assets.TILE_SIZE;
            }
            yy += Assets.TILE_SIZE;
        }
    }

}
