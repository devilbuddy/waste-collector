package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by magnus on 2015-10-04.
 */
public class MapRenderer {


    private static final String tag = "MapRenderer";

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

    public void render(World world, SpriteBatch spriteBatch, int playerEntityId) {

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
                TextureRegion region = null;
                switch (cell.type) {
                    case Floor:
                        region = assets.floor;
                        break;
                    case Wall:
                        region = assets.wall;
                        break;
                }
                spriteBatch.draw(region, xx, yy);
                xx += Assets.TILE_SIZE;
            }
            yy += Assets.TILE_SIZE;
        }

        Entity entity = world.getEntity(playerEntityId);
        if (entity != null) {
            Entity.MoveState moveState = entity.getComponent(Entity.MoveState.class);

            switch (moveState.direction) {
                case NORTH:
                    spriteBatch.draw(assets.tiles[4][0], moveState.position.x, moveState.position.y, 4, 4, 8, 8, 1, 1, 270);
                    break;
                case SOUTH:
                    spriteBatch.draw(assets.tiles[4][0], moveState.position.x, moveState.position.y, 4, 4, 8, 8, 1, 1, 90);
                    break;
                case EAST:
                    spriteBatch.draw(assets.tilesTexture,
                            moveState.position.x,
                            moveState.position.y,
                            Assets.TILE_SIZE,
                            Assets.TILE_SIZE,
                            0,
                            4 * Assets.TILE_SIZE,
                            Assets.TILE_SIZE,
                            Assets.TILE_SIZE,
                            true,
                            false);

                    break;
                case WEST:spriteBatch.draw(assets.tilesTexture,
                        moveState.position.x,
                        moveState.position.y,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        0,
                        4 * Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        false,
                        false);

                    break;
            }

        }

    }

}
