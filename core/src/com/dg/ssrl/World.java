package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntArray;

import java.util.ArrayList;

import static com.dg.ssrl.Components.Actor;
import static com.dg.ssrl.Components.ItemContainer;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Solid;
import static com.dg.ssrl.Components.Update;

public class World {
    private static final String tag = "World";


    public static class Cell {
        public enum Type {
            Wall(false),
            Floor(true);
            private boolean walkable;

            Type(boolean walkable) {
                this.walkable = walkable;
            }
        }
        public Type type;
        private IntArray entityIds = new IntArray();

        public Cell() {
            type = Type.Floor;
        }

        boolean isWalkable() {
            return type.walkable;
        }

        public int getEntityCount() {
            return entityIds.size;
        }

        public int getEntityId(int index) {
            return entityIds.get(index);
        }

        public void removeEntity(int entityId) {
            entityIds.removeValue(entityId);
        }
    }

    private final int width;
    private final int height;
    private final EntityFactory entityFactory;
    private final Scheduler scheduler;

    private final Cell[][] cells;
    public ArrayList<Entity> entities = new ArrayList<Entity>();
    public Rectangle bounds = new Rectangle();
    public int playerEntityId;
    public int exitEntityId;

    public int[][] dijkstraMap;

    private boolean completed;

    public World(int width, int height, EntityFactory entityFactory, Scheduler scheduler) {
        this.width = width;
        this.height = height;
        this.entityFactory = entityFactory;
        this.scheduler = scheduler;

        cells = new Cell[height][width];
        dijkstraMap = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell();
                dijkstraMap[y][x] = Integer.MAX_VALUE;
            }
        }

        bounds.set(0, 0, width * Assets.TILE_SIZE, height * Assets.TILE_SIZE);
        Gdx.app.log(tag, "bounds: " + bounds);
    }

    public void setCompleted() {
        completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isGameOver() {
        return playerEntityId == -1;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void update(float delta) {
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity entity = entities.get(i);
            if (entity.alive) {
                Update update = entity.getComponent(Update.class);
                if (update != null) {
                    update.update(delta, this);
                }
            } else {
                Actor actor = entity.getComponent(Actor.class);
                if (actor != null) {
                    scheduler.removeActor(actor);
                }
                Position position = entity.getComponent(Position.class);
                if (position != null) {
                    getCell(position.x, position.y).removeEntity(entity.id);
                }
                // dropped items
                ItemContainer itemContainer = entity.getComponent(ItemContainer.class);
                if (itemContainer != null && position != null) {
                    for (ItemType itemType : itemContainer.content.keySet()) {
                        Entity item = entityFactory.makeItem(position.x, position.y, itemType);
                        addEntity(item);
                    }
                }

                if (entity.id == playerEntityId) {
                    playerEntityId = -1;
                }

                entities.remove(i);

            }
        }

        scheduler.update(this);
    }

    public Position translateWraparound(Position p, Direction direction) {
        p.translate(direction);
        return wraparound(p);
    }

    public Position wraparound(Position p) {
        p.x = p.x % width;
        p.y = p.y % height;
        while (p.x < 0) {
            p.x += width;
        }
        while (p.y < 0) {
            p.y += height;
        }
        return p;
    }

    public void updateDijkstraMap(int goalX, int goalY) {
        // http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps

        // To get a Dijkstra map, you start with an integer array representing your map,
        // with some set of goal cells set to zero and all the rest set to a very high number.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(x == goalX && y == goalY) {
                    dijkstraMap[y][x] = 0;
                } else {
                    dijkstraMap[y][x] = Integer.MAX_VALUE;
                }
            }
        }

        // Iterate through the map's "floor" cells -- skip the impassable wall cells.
        // If any floor tile has a value that is at least 2 greater than its lowest-value floor neighbor,
        // set it to be exactly 1 greater than its lowest value neighbor. Repeat until no changes are made.
        boolean changed = true;
        int iterations = 0;
        while(changed) {
            iterations++;
            changed = false;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (getCell(x, y).type == Cell.Type.Floor) {
                        int value = dijkstraMap[y][x];
                        int lowestNeighborValue = Integer.MAX_VALUE;
                        for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
                            Components.Position p = new Components.Position();
                            p.set(x, y);
                            p = translateWraparound(p, direction);

                            if (getCell(p.x, p.y).type == Cell.Type.Floor) {
                                int neighborValue = dijkstraMap[p.y][p.x];
                                if (neighborValue < lowestNeighborValue) {
                                    lowestNeighborValue = neighborValue;
                                }
                            }
                        }
                        if (value - lowestNeighborValue >= 2) {
                            dijkstraMap[y][x] = lowestNeighborValue + 1;
                            changed = true;
                        }
                    }
                }
            }
        }

        Gdx.app.log(tag, "iterations:" + iterations);
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

    public Cell getCell(int x, int y) {
        return cells[y][x];
    }


    public Cell getCell(Position p) {
        return getCell(p.x, p.y);
    }

    public void addPlayer(Entity entity) {
        playerEntityId = entity.id;
        addEntity(entity);

        Components.Position position = entity.getComponent(Components.Position.class);
        updateDijkstraMap(position.x, position.y);
    }

    public void addExit(Entity exit) {
        exitEntityId = exit.id;
        addEntity(exit);
    }

    public void addEntity(Entity entity) {
        entities.add(entity);

        Components.Position position = entity.getComponent(Components.Position.class);
        if(position != null) {
            getCell(position.x, position.y).entityIds.add(entity.id);
        }
        if(entity.getComponent(Actor.class) != null) {
            scheduler.addActor(entity.getComponent(Actor.class));
        }
    }

    public Entity getPlayer() {
        return getEntity(playerEntityId);
    }

    public Entity getExit() {
        return getEntity(exitEntityId);
    }

    public Entity getEntity(int id) {
        for (int i = 0; i < entities.size(); i++) {
            if(entities.get(i).id == id) {
                return entities.get(i);
            }
        }
        return null;
    }

    public void move(Entity entity, int toX, int toY) {
        Components.Position position = entity.getComponent(Components.Position.class);
        getCell(position.x, position.y).entityIds.removeValue(entity.id);
        position.set(toX, toY);
        getCell(position.x, position.y).entityIds.add(entity.id);
    }

    public boolean contains(int x, int y) {
        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }

    public boolean isWalkable(Position position) {
        if (contains(position.x, position.y)) {
            Cell cell = getCell(position.x, position.y);
            if (cell.type.walkable) {
                boolean containsSolidEntities = false;

                for (int i = 0; i < cell.entityIds.size; i++) {
                    int entityId = cell.entityIds.get(i);
                    Entity entity = getEntity(entityId);
                    Solid solid = entity.getComponent(Solid.class);
                    if (solid != null && solid.isSolid()) {
                        containsSolidEntities = true;
                        break;
                    }
                }
                return !containsSolidEntities;
            }
        }
        return false;
    }

    public boolean containsEntityWithComponent(Position position, Class clazz) {
        Cell cell = getCell(position.x, position.y);
        for (int i = 0; i < cell.entityIds.size; i++) {
            int entityId = cell.entityIds.get(i);
            Entity entity = getEntity(entityId);
            if (entity.getComponent(clazz) != null) {
                return true;
            }
        }
        return false;
    }
}
