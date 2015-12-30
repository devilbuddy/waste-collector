package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static com.dg.ssrl.Components.*;

public class MapRenderer {
    private static final String tag = "MapRenderer";

    private final Assets assets;

    private int virtualWidth;
    private int virtualHeight;

    public Rectangle bounds = new Rectangle();

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

        spriteBatch.setColor(Color.WHITE);
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

                Sprite sprite = entity.getComponent(Sprite.class);

                if (sprite != null) {
                    MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);

                    if (moveAnimation != null) {
                        Rectangle moveBounds = new Rectangle(moveAnimation.bounds);
                        Vector2 position = new Vector2();
                        moveBounds.getPosition(position);
                        renderWithFacing(position, moveAnimation.direction, sprite, spriteBatch);
                        if (!world.bounds.contains(moveBounds)) {

                            float offsetX = moveAnimation.direction.dx * world.bounds.width;
                            float offsetY = moveAnimation.direction.dy * world.bounds.height;

                            position.set(moveBounds.x - offsetX, moveBounds.y - offsetY);
                            renderWithFacing(position, moveAnimation.direction, sprite, spriteBatch);
                        }
                    }
                    Effect effect = entity.getComponent(Effect.class);
                    if (effect != null) {
                        renderEffect(effect, sprite, spriteBatch);
                    }
                }

            }
        }
    }

    private void renderEffect(Effect effect, Sprite sprite, SpriteBatch spriteBatch) {
        for (int i = 0; i < effect.numParticles; i++) {
            Effect.Particle p = effect.particles[i];
            spriteBatch.setColor(p.color);
            spriteBatch.draw(sprite.region, bounds.x + p.position.x, bounds.y + p.position.y);
        }
    }

    private void renderWithFacing(Vector2 position, Direction direction, Sprite sprite, SpriteBatch spriteBatch) {
        switch (direction) {
            case NORTH:
                spriteBatch.draw(sprite.region, bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 270);
                break;
            case SOUTH:
                spriteBatch.draw(sprite.region, bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 90);
                break;
            case EAST:
                spriteBatch.draw(sprite.region.getTexture(),
                        bounds.x + position.x,
                        bounds.y + position.y,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        sprite.region.getRegionX(),
                        sprite.region.getRegionY(),
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        true,
                        false);
                break;
            case WEST:
                spriteBatch.draw(sprite.region.getTexture(),
                        bounds.x + position.x,
                        bounds.y + position.y,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        sprite.region.getRegionX(),
                        sprite.region.getRegionY(),
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        false,
                        false);
                break;
        }
    }

}
