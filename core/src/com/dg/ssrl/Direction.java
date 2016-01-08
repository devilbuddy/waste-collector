package com.dg.ssrl;

public enum Direction {

    NONE(0, 0),
    NORTH(0, 1),
    SOUTH(0, -1),
    WEST(-1, 0),
    EAST(1, 0);

    public static final Direction[] CARDINAL_DIRECTIONS = {NORTH, SOUTH, EAST, WEST};

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    Direction opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
        }
        return NONE;
    }
    Direction turn() {
        switch (this) {
            case NORTH:
                return EAST;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            case EAST:
                return SOUTH;
        }
        return NONE;
    }
}
