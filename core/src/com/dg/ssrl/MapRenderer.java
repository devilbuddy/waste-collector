package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;

import static com.dg.ssrl.Components.*;

public class MapRenderer {
    private static final String tag = "MapRenderer";

    private final Assets assets;

    private int virtualWidth;
    private int virtualHeight;

    public Rectangle bounds = new Rectangle();

    // Autotiling
    private int currentAutoTileWorldSequenceId = -1;
    private TextureRegion[][] mapTiles;
    private boolean[][] floors;

    public MapRenderer(Assets assets) {
        this.assets = assets;
    }

    public void resize(int virtualWidth, int virtualHeight) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
    }

    private void autoTile(World world) {
        if (currentAutoTileWorldSequenceId == world.sequenceId) {
            return;
        }
        currentAutoTileWorldSequenceId = world.sequenceId;
        int width = world.getWidth();
        int height = world.getHeight();

        mapTiles = new TextureRegion[height][width];
        floors = new boolean[height][width];

        World.Cell[][] cells = world.getCells();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                World.Cell cell = cells[y][x];
                if (cell.type == World.Cell.Type.Floor) {
                    mapTiles[y][x] = assets.floor;
                    floors[y][x] = true;
                } else {
                    floors[y][x] = false;
                    int indexValue = 0;
                    Position p = new Position(x,y);
                    p.translate(Direction.NORTH);
                    if (world.contains(p) && world.getCell(p).type == World.Cell.Type.Wall) {
                        indexValue += 1;
                    }
                    p.set(x, y);
                    p.translate(Direction.EAST);
                    if (world.contains(p) && world.getCell(p).type == World.Cell.Type.Wall) {
                        indexValue += 2;
                    }
                    p.set(x, y);
                    p.translate(Direction.SOUTH);
                    if (world.contains(p) && world.getCell(p).type == World.Cell.Type.Wall) {
                        indexValue += 4;
                    }
                    p.set(x, y);
                    p.translate(Direction.WEST);
                    if (world.contains(p) && world.getCell(p).type == World.Cell.Type.Wall) {
                        indexValue += 8;
                    }
                    mapTiles[y][x] = assets.autoTileSet[indexValue];
                }
            }
        }

    }

    public void render(World world, SpriteBatch spriteBatch, float topGutterHeight) {

        autoTile(world);

        int width = world.getWidth();
        int height = world.getHeight();

        bounds.width = width * Assets.TILE_SIZE;
        bounds.height = height * Assets.TILE_SIZE;

        bounds.x = virtualWidth/2 - bounds.width/2;
        bounds.y = virtualHeight - bounds.height - topGutterHeight;

        float yy = bounds.y;
        for (int y = 0; y < height; y++) {
            float xx = bounds.x;
            for (int x = 0; x < width; x++) {

                if(floors[y][x]) {
                    spriteBatch.setColor(assets.floorColor);
                } else {
                    spriteBatch.setColor(Color.PURPLE);
                }
                TextureRegion region = mapTiles[y][x];
                spriteBatch.draw(region, xx, yy);

                /*
                assets.font.setColor(1, 0, 0, 0.5f);
                if (world.dijkstraMap[y][x] != Integer.MAX_VALUE) {
                    assets.font.draw(spriteBatch, "" + world.dijkstraMap[y][x], xx, yy + 8);
                }
                assets.font.setColor(Color.WHITE);
                */

                xx += Assets.TILE_SIZE;
            }
            yy += Assets.TILE_SIZE;
        }

        spriteBatch.setColor(Color.WHITE);

        int renderPass = 0;
        boolean needAnotherRenderPass = true;
        while (needAnotherRenderPass) {
            needAnotherRenderPass = false;
            for (IntMap.Entry<Entity> entry : world.entities.entries()) {
                Entity entity = entry.value;
                if (entity.alive) {
                    Sprite sprite = entity.getComponent(Sprite.class);

                    if (sprite != null) {
                        if (sprite.renderPass == renderPass) {
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
                        } else if (sprite.renderPass > renderPass) {
                            needAnotherRenderPass = true;
                        }
                    }

                }
            }
            renderPass++;
        }
    }

    private void renderEffect(Effect effect, Sprite sprite, SpriteBatch spriteBatch) {
        for (int i = 0; i < effect.numParticles; i++) {
            Effect.Particle p = effect.particles[i];
            spriteBatch.setColor(p.color);
            spriteBatch.draw(sprite.getTextureRegion(), bounds.x + p.position.x, bounds.y + p.position.y);
        }
    }

    private void renderWithFacing(Vector2 position, Direction direction, Sprite sprite, SpriteBatch spriteBatch) {
        switch (direction) {
            case NORTH:
                spriteBatch.draw(sprite.getTextureRegion(), bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 270);
                break;
            case SOUTH:
                spriteBatch.draw(sprite.getTextureRegion(), bounds.x + position.x, bounds.y + position.y, 4, 4, 8, 8, 1, 1, 90);
                break;
            case EAST:
                spriteBatch.draw(sprite.getTextureRegion().getTexture(),
                        bounds.x + position.x,
                        bounds.y + position.y,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        sprite.getTextureRegion().getRegionX(),
                        sprite.getTextureRegion().getRegionY(),
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        true,
                        false);
                break;
            case WEST:
                spriteBatch.draw(sprite.getTextureRegion().getTexture(),
                        bounds.x + position.x,
                        bounds.y + position.y,
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        sprite.getTextureRegion().getRegionX(),
                        sprite.getTextureRegion().getRegionY(),
                        Assets.TILE_SIZE,
                        Assets.TILE_SIZE,
                        false,
                        false);
                break;
        }
    }

}
