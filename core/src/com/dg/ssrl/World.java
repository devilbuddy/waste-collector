package com.dg.ssrl;

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
}
