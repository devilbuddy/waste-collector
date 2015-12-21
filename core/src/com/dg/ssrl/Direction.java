package com.dg.ssrl;

/**
 * Created by magnus.ornebring on 31/05/15.
 */
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
}
