package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;

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
        public int wasteCount;
        public Color wallColor;
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
                "#.######.#",
                "..........",
                "#..oooo..#",
                "#.oooooo.#",
                "#.oooooo.#",
                "#.oooooo.#",
                "#.oooooo.#",
                "#..oooo..#",
                "..........",
                "#.######.#"
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


    private static final Color[] WALL_COLORS = {
            Assets.COLOR_YELLOW,
            Assets.COLOR_ORANGE,
            Assets.COLOR_LIGHT_GREEN
    };

    public static LevelData generate(long seed, int width, int height, int depth, EntityFactory entityFactory) {
        Random random = new Random(seed);

        TemplateData templateData = templates[random.nextInt(templates.length)];

        LevelData levelData = new LevelData();

        levelData.wallColor = WALL_COLORS[random.nextInt(WALL_COLORS.length)];

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
        // remove immediate neighbors from list of floors
        for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
            floors.remove(levelData.start.copy().translate(direction));
        }

        levelData.exit = floors.remove(0);

        Position keyPosition = floors.remove(0);
        levelData.entities.add(entityFactory.makeItem(keyPosition.x, keyPosition.y, ItemType.Key));

        if (random.nextBoolean()) {
            Position teleporterPosition = floors.remove(0);
            levelData.entities.add(entityFactory.makeTeleporter(teleporterPosition.x, teleporterPosition.y));
        }

        // monsters
        int monsterCount = 2 + (int)Math.sqrt(depth) + (depth/6);

        float stationaryChance = 0.25f;
        for (int i = 0; i < monsterCount; i++) {
            if (floors.size() > 0) {
                Position monsterPosition = floors.remove(0);
                MonsterType monsterType = MonsterType.ENEMIES[random.nextInt(MonsterType.ENEMIES.length)];
                float spawnStationary = random.nextFloat();
                if (spawnStationary < stationaryChance) {
                    monsterType = MonsterType.STATIONARY_ENEMIES[random.nextInt(MonsterType.STATIONARY_ENEMIES.length)];
                }
                Entity monster = entityFactory.makeMonster(monsterPosition.x, monsterPosition.y, monsterType);
                levelData.entities.add(monster);
            }
        }

        // items
        int wasteCount = 2 + random.nextInt(2);
        for (int i = 0; i < wasteCount; i++) {
            if (floors.size() > 0) {
                Position itemPosition = floors.remove(0);
                Entity item = entityFactory.makeItem(itemPosition.x, itemPosition.y, ItemType.Waste);
                levelData.entities.add(item);
                levelData.wasteCount++;
            }
        }

        int pickupCount = 2 + random.nextInt(2);
        for (int i = 0; i < pickupCount; i++) {
            if (floors.size() > 0) {
                Position itemPosition = floors.remove(0);
                ItemType itemType = ItemType.PICK_UPS[random.nextInt(ItemType.PICK_UPS.length)];
                Entity item = entityFactory.makeItem(itemPosition.x, itemPosition.y, itemType);
                levelData.entities.add(item);
            }
        }

        int rarePickupCount = random.nextInt(2) + (depth / 3);
        for (int i = 0; i < rarePickupCount; i++) {
            if (floors.size() > 0) {
                Position itemPosition = floors.remove(0);
                ItemType itemType = ItemType.RARE_PICK_UPS[random.nextInt(ItemType.RARE_PICK_UPS.length)];
                Entity item = entityFactory.makeItem(itemPosition.x, itemPosition.y, itemType);
                levelData.entities.add(item);
            }
        }

        return levelData;
    }
}
