package com.dg.ssrl;

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
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            levelData.tiles[y][x] = World.Cell.Type.Wall;
        }

        levelData.start.x = 1;
        levelData.start.y = 1;

        return levelData;
    }
}
