package com.dg.ssrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static com.dg.ssrl.Components.Position;

public class Generator {

    public static class LevelData {
        public int width;
        public int height;
        public World.Cell.Type[][] tiles;
        public Position start = new Position(0,0);
        public List<Position> monsters = new ArrayList<Position>();
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

        ArrayList<Position> floors = new ArrayList<Position>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (levelData.tiles[y][x] == World.Cell.Type.Floor) {
                    floors.add(new Position(x, y));
                }
            }
        }
        Collections.shuffle(floors);

        levelData.start = floors.remove(0);

        for (int i = 0; i < 4; i++) {
            Position monster = floors.remove(0);
            levelData.monsters.add(monster);
        }

        return levelData;
    }
}
