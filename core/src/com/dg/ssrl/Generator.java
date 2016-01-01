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
        public List<Entity> monsters = new ArrayList<Entity>();
    }

    private static class TemplateData {
        String[] template;

        public TemplateData(String[] template) {
            this.template = template;
        }
    }

    private static TemplateData[] templates = {
        new TemplateData(new String[] {
                "####..####",
                "#........#",
                "#........#",
                "#........#",
                "..........",
                "..........",
                "#........#",
                "#........#",
                "#........#",
                "####..####"
        }),
        new TemplateData(new String[] {
                "##########",
                "..........",
                "#........#",
                "####..####",
                "#........#",
                "#........#",
                "#........#",
                "#........#",
                "..........",
                "##########"
        }),
    };

    public static LevelData generate(long seed, int width, int height, EntityFactory entityFactory) {
        Random random = new Random(seed);

        TemplateData templateData = templates[random.nextInt(templates.length)];

        LevelData levelData = new LevelData();

        levelData.width = width;
        levelData.height = height;
        levelData.tiles = new World.Cell.Type[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c  = templateData.template[height - y - 1].charAt(x);
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
        Collections.shuffle(floors, random);

        levelData.start = floors.remove(0);

        for (int i = 0; i < 4; i++) {
            Position monsterPosition = floors.remove(0);

            MonsterType[] monsterTypes = MonsterType.values();
            MonsterType monsterType = monsterTypes[random.nextInt(monsterTypes.length)];

            Entity monster = entityFactory.makeMonster(monsterPosition.x, monsterPosition.y, monsterType);

            levelData.monsters.add(monster);
        }

        return levelData;
    }
}
