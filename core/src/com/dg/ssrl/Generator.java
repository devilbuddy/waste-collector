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
        public Position exit = new Position(0,0);
        public List<Entity> entities = new ArrayList<Entity>();
    }

    private static class TemplateData {
        String[] template;

        public TemplateData(String[] template) {
            this.template = template;
        }
        public static boolean isFloor(char c) {
            return c == '.' || isOptionalWall(c);
        }
        public static boolean isOptionalWall(char c) {
            return c == 'o';
        }
    }

    private static TemplateData[] templates = {
        new TemplateData(new String[] {
                "####..####",
                "#........#",
                "#.oooooo.#",
                "#.oooooo.#",
                "..oooooo..",
                "..oooooo..",
                "#.oooooo.#",
                "#.oooooo.#",
                "#........#",
                "####..####"
        }),
        new TemplateData(new String[] {
                "##########",
                "..oooooo..",
                "#........#",
                "####..####",
                "#........#",
                "#.oooooo.#",
                "#.o....o.#",
                "#.oooooo.#",
                "..........",
                "##########"
        }),
        new TemplateData(new String[] {
                "#.######.#",
                "#.oooooo.#",
                "#........#",
                "#...##...#",
                "##..##..##",
                "....##....",
                "#.oooooo.#",
                "#.oooooo.#",
                "#........#",
                "#.######.#"
        }),
        new TemplateData(new String[] {
                "###.##.###",
                "#........#",
                "#..####..#",
                "#........#",
                "..oooooo..",
                "#.oooooo.#",
                "#........#",
                "#..####..#",
                "#........#",
                "###.##.###"
        })
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
                if (TemplateData.isFloor(c)) {
                    levelData.tiles[y][x] = World.Cell.Type.Floor;
                } else {
                    levelData.tiles[y][x] = World.Cell.Type.Wall;
                }
            }
        }

        // optional extra walls
        ArrayList<Position> optionalWalls = new ArrayList<Position>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c  = templateData.template[height - y - 1].charAt(x);
                if (TemplateData.isOptionalWall(c)) {
                    optionalWalls.add(new Position(x, y));
                }
            }
        }
        Collections.shuffle(optionalWalls, random);
        int numExtraFloors = optionalWalls.size()/3;
        for (int i = 0; i < numExtraFloors; i++) {
            Position p = optionalWalls.remove(0);
            levelData.tiles[p.y][p.x] = World.Cell.Type.Wall;
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
        levelData.exit = floors.remove(0);

        // monsters
        for (int i = 0; i < 4; i++) {
            Position monsterPosition = floors.remove(0);
            MonsterType monsterType = MonsterType.ENEMIES[random.nextInt(MonsterType.ENEMIES.length)];
            Entity monster = entityFactory.makeMonster(monsterPosition.x, monsterPosition.y, monsterType);
            levelData.entities.add(monster);
        }

        // items
        for (int i = 0; i < 4; i++) {
            Position itemPosition = floors.remove(0);
            Entity item = entityFactory.makeItem(itemPosition.x, itemPosition.y, ItemType.Key);
            levelData.entities.add(item);
        }

        return levelData;
    }
}
