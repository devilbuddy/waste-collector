package com.dg.ssrl;

import java.util.ArrayList;

/**
 * Created by magnus on 2015-12-21.
 */
public class World {


    public static class Cell {
        public enum Type {
            Wall,
            Floor
        }
        public Type type;

    }

    private final int width;
    private final int height;

    private final Cell[][] cells;
    private ArrayList<Entity> entities = new ArrayList<Entity>();

    public World(int width, int height) {
        this.width = width;
        this.height = height;

        cells = new Cell[height][width];
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

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public Entity getEntity(int id) {
        for (int i = 0; i < entities.size(); i++) {
            if(entities.get(i).id == id) {
                return entities.get(i);
            }
        }
        return null;
    }

}
