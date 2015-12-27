package com.dg.ssrl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntArray;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by magnus on 2015-12-21.
 */
public class World {


    public static class Cell {
        public enum Type {
            Wall(false),
            Floor(true);


            private boolean walkable;

            Type(boolean walkable) {
                this.walkable = walkable;
            }
        }
        public Type type;
        private IntArray entityIds = new IntArray();

        public Cell() {
            type = Type.Floor;
        }

        boolean isWalkable() {
            return type.walkable && entityIds.size == 0;
        }

    }

    private final int width;
    private final int height;

    private final Cell[][] cells;
    public ArrayList<Entity> entities = new ArrayList<Entity>();
    public Rectangle bounds = new Rectangle();

    public World(int width, int height) {
        this.width = width;
        this.height = height;

        cells = new Cell[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell();
            }
        }

        bounds.set(0, 0, width * Assets.TILE_SIZE, height * Assets.TILE_SIZE);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public Cell getCell(int x, int y) {
        return cells[y][x];
    }

    public void addEntity(Entity entity) {
        entities.add(entity);

        Entity.Position position = entity.getComponent(Entity.Position.class);
        if(position != null) {
            getCell(position.x, position.y).entityIds.add(entity.id);
        }
    }

    public Entity getEntity(int id) {
        for (int i = 0; i < entities.size(); i++) {
            if(entities.get(i).id == id) {
                return entities.get(i);
            }
        }
        return null;
    }

    public void move(Entity entity, int toX, int toY) {
        Entity.Position position = entity.getComponent(Entity.Position.class);
        getCell(position.x, position.y).entityIds.removeValue(entity.id);
        position.set(toX, toY);
        getCell(position.x, position.y).entityIds.add(entity.id);
    }

    public boolean contains(int x, int y) {
        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }
}
