package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by magnus on 2015-10-04.
 */
public class MapRenderer {
    private static final String tag = "MapRenderer";

    private final Assets assets;

    private int virtualWidth;
    private int virtualHeight;

    private Rectangle bounds = new Rectangle();

    public MapRenderer(Assets assets) {
        this.assets = assets;
    }

    public void resize(int virtualWidth, int virtualHeight) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;

    }

    public void render(World world, SpriteBatch spriteBatch) {

        int width = world.getWidth();
        int height = world.getHeight();

        bounds.width = width * Assets.TILE_SIZE;
        bounds.height = width * Assets.TILE_SIZE;
        bounds.x = virtualWidth/2 - bounds.width/2;
        bounds.y = virtualHeight/2 - bounds.height/2;


        World.Cell[][] cells = world.getCells();


        float yy = bounds.y;
        for (int y = 0; y < height; y++) {
            float xx = bounds.x;
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


        for (Entity entity : world.entities) {
            if (entity.alive) {
                Entity.MoveAnimation moveAnimation = entity.getComponent(Entity.MoveAnimation.class);
                if (moveAnimation != null) {
                    int animationCount = moveAnimation.animationCount;
                    for (int i = 0; i < animationCount; i++) {
                        Vector2 position = moveAnimation.animations[i].position;
                        switch (moveAnimation.direction) {
                            case NORTH:
                                spriteBatch.draw(assets.tiles[4][0], bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 270);
                                break;
                            case SOUTH:
                                spriteBatch.draw(assets.tiles[4][0], bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 90);
                                break;
                            case EAST:
                                spriteBatch.draw(assets.tilesTexture,
                                        bounds.x + position.x,
                                        bounds.y + position.y,
                                        Assets.TILE_SIZE,
                                        Assets.TILE_SIZE,
                                        0,
                                        4 * Assets.TILE_SIZE,
                                        Assets.TILE_SIZE,
                                        Assets.TILE_SIZE,
                                        true,
                                        false);
                                break;
                            case WEST:
                                spriteBatch.draw(assets.tilesTexture,
                                        bounds.x + position.x,
                                        bounds.y + position.y,
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
                Entity.MoveAnimation2 moveAnimation2 = entity.getComponent(Entity.MoveAnimation2.class);
                if (moveAnimation2 != null) {

                    Rectangle moveBounds = new Rectangle(moveAnimation2.bounds);

                    spriteBatch.draw(assets.tiles[4][3], bounds.x + moveBounds.x, bounds.y + moveBounds.y, 4, 4, 8, 8, 1, 1, 270);

                    if (!world.bounds.contains(moveBounds)) {

                        float offsetX = moveAnimation2.direction.dx * world.bounds.width;
                        float offsetY = moveAnimation2.direction.dy * world.bounds.height;

                        spriteBatch.draw(assets.tiles[4][3], moveBounds.x - offsetX,  bounds.y + moveBounds.y - offsetY, 4, 4, 8, 8, 1, 1, 270);
                    }

                }
            }




        }


    }

}
