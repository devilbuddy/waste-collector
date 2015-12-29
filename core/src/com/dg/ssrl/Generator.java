package com.dg.ssrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by magnus on 2015-12-25.
 */
public class Generator {

    public static class LevelData {
        public int width;
        public int height;
        public World.Cell.Type[][] tiles;
        public Point start = new Point(0,0);
        public List<Point> monsters = new ArrayList<Point>();
    }

    private static final String[] template = new String[] {
            "####..####",
            "#........#",
            "#........#",
            "#........#",
            "..........",
            "..........",
            "#........#",
            "#........#",
            "#........#",
            "####..####",
    };

    public static LevelData generate(long seed, int width, int height) {
        Random random = new Random(seed);

        LevelData levelData = new LevelData();

        levelData.width = width;
        levelData.height = height;
        levelData.tiles = new World.Cell.Type[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c  = template[y].charAt(x);

                levelData.tiles[y][x] = c == '#' ? World.Cell.Type.Wall : World.Cell.Type.Floor ;
            }
        }

        for (int i = 0; i < 10; i++) {
            int x = 2 + random.nextInt(width - 4);
            int y = 2 + random.nextInt(height - 4);
            levelData.tiles[y][x] = World.Cell.Type.Wall;
        }

        ArrayList<Point> floors = new ArrayList<Point>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (levelData.tiles[y][x] == World.Cell.Type.Floor) {
                    floors.add(new Point(x, y));
                }
            }
        }
        Collections.shuffle(floors);

        levelData.start = floors.remove(0);

        for (int i = 0; i < 3; i++) {
            Point monster = floors.remove(0);
            levelData.monsters.add(monster);
        }

        return levelData;
    }
}
