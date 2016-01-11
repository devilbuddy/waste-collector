package com.dg.ssrl;

import java.util.Random;

import static com.dg.ssrl.Components.Brain;
import static com.dg.ssrl.Components.ItemContainer;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Stats;

public class MonsterBrain implements Brain {

    private static final String tag = "MonsterBrain";

    private final int entityId;
    private final MonsterType monsterType;
    private final Assets.Sounds sounds;

    public MonsterBrain(int entityId, MonsterType monsterType, Assets.Sounds sounds) {
        this.entityId = entityId;
        this.monsterType = monsterType;
        this.sounds = sounds;
    }

    private Direction findAttackDirection(World world, final Position current) {
        Direction targetDirection = Direction.NONE;
        Position targetPosition = current.copy();

        int lowestValue = world.dijkstraMap[current.y][current.x];
        for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
            targetPosition.set(current);
            targetPosition = world.translateWraparound(targetPosition, direction);

            Entity player = world.getPlayer();
            boolean targetIsPlayer = false;
            if (player != null) {
                targetIsPlayer = targetPosition.equals(player.getComponent(Position.class));
            }

            if (targetIsPlayer || world.isWalkable(targetPosition)) {
                int value = world.dijkstraMap[targetPosition.y][targetPosition.x];
                if (value < lowestValue) {
                    lowestValue = value;
                    targetDirection = direction;
                }
            }
        }
        return targetDirection;
    }

    private Direction findFleeDirection(World world, final Position current) {
        Direction targetDirection = Direction.NONE;
        Position targetPosition = current.copy();

        int highestValue = 0;
        for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
            targetPosition.set(current);
            targetPosition = world.translateWraparound(targetPosition, direction);

            if (world.isWalkable(targetPosition)) {
                int value = world.dijkstraMap[targetPosition.y][targetPosition.x];
                if (value != Integer.MAX_VALUE && value > highestValue) {
                    highestValue = value;
                    targetDirection = direction;
                }
            }
        }
        return targetDirection;
    }

    @Override
    public boolean act(final World world) {
        final Entity entity = world.getEntity(entityId);
        if (!entity.alive) {
            return true;
        }
        final MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);

        if (!moveAnimation.isBusy()) {
            final Position current = entity.getComponent(Position.class);
            ItemContainer itemContainer = entity.getComponent(ItemContainer.class);

            Direction targetDirection;
            if (itemContainer != null && itemContainer.getAmount(ItemType.Key) > 0) {
                targetDirection = findFleeDirection(world, current);
            } else {
                targetDirection = findAttackDirection(world, current);
            }

            boolean doMove = targetDirection != Direction.NONE;
            if (targetDirection == moveAnimation.direction) {

                Position target = current.copy();
                world.translateWraparound(target, targetDirection);

                World.Cell targetCell = world.getCell(target);
                for (int i = 0; i < targetCell.getEntityCount(); i++) {
                    Entity targetEntity = world.getEntity(targetCell.getEntityId(i));
                    Stats targetStats = targetEntity.getComponent(Stats.class);
                    if (targetStats != null && targetStats.monsterType != MonsterType.Player) {
                        doMove = false;
                    }
                }
            }

            if (doMove) {
                BrainCore.move(world, entity, targetDirection, monsterType, sounds);
            }
            return true;
        } else {
            return false;
        }
    }

    public static class EggBrain implements Brain {
        private final int entityId;
        int ticksToActivate = 3;

        public EggBrain(int entityId) {
            this.entityId = entityId;
        }

        @Override
        public boolean act(World world) {
            EntityFactory entityFactory = world.getEntityFactory();
            Entity entity = world.getEntity(entityId);

            ticksToActivate--;
            Position position = entity.getComponent(Position.class).copy();

            Entity explosion = entityFactory.makeExplosion(position.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2, position.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2, 0.1f);
            world.addEntity(explosion);

            if (ticksToActivate == 0) {
                entity.alive = false;
                Entity monster = entityFactory.makeMonster(position.x, position.y, MonsterType.Crawler);
                world.addEntity(monster);
            }
            return true;
        }
    }

    public static class GrowerBrain implements Brain {
        private final int entityId;
        private final Assets.Sounds sounds;

        private Random random = new Random(System.currentTimeMillis());

        private static final int MAX_GROW = 2;

        private int growCount = 0;
        private float tryGrow = 0.75f;


        public GrowerBrain(int entityId, Assets.Sounds sounds) {
            this.entityId = entityId;
            this.sounds = sounds;
        }

        @Override
        public boolean act(World world) {
            boolean grow = random.nextFloat() < tryGrow;
            if (grow) {
                Entity entity = world.getEntity(entityId);
                Position position = entity.getComponent(Position.class).copy();
                Direction growDirection = Direction.CARDINAL_DIRECTIONS[random.nextInt(Direction.CARDINAL_DIRECTIONS.length)];
                world.translateWraparound(position, growDirection);
                if (world.isWalkable(position)) {
                    EntityFactory entityFactory = world.getEntityFactory();
                    Entity spawnedGrower = entityFactory.makeMonster(position.x, position.y, MonsterType.Grower);
                    world.addEntity(spawnedGrower);

                    sounds.play(Assets.Sounds.SoundId.SPAWN);
                    growCount++;
                    if (growCount < MAX_GROW) {
                        tryGrow = 0.05f;
                    } else {
                        tryGrow = 0;
                    }
                }

            }
            return true;
        }
    }

    public static class CannonBrain implements Brain {
        private final int entityId;
        private final Assets.Sounds sounds;

        private static final int ROTATE_DELAY = 2;
        private int rotateDelay;
        public CannonBrain(int entityId, Assets.Sounds sounds) {
            this.entityId = entityId;
            this.sounds = sounds;
        }

        @Override
        public boolean act(World world) {
            Entity entity = world.getEntity(entityId);
            MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);

            if (world.getPlayer() != null) {
                boolean rotate = true;

                Position position = entity.getComponent(Position.class);
                final Position bulletEnd = position.copy().translate(moveAnimation.direction);
                boolean hitSomething = false;
                while (!hitSomething) {
                    world.wraparound(bulletEnd);

                    if (world.isWalkable(bulletEnd)) {
                        bulletEnd.translate(moveAnimation.direction);
                    } else {
                        hitSomething = true;
                    }
                }

                World.Cell cell = world.getCell(bulletEnd);
                for (int i = 0; i < cell.getEntityCount(); i++) {
                    int entityId = cell.getEntityId(i);
                    if (world.getEntity(entityId).id == world.getPlayer().id) {
                        rotate = false;
                        break;
                    }
                }

                if (rotate) {
                    rotateDelay--;
                    if (rotateDelay <= 0) {
                        Direction newDirection = moveAnimation.direction.turn();
                        BrainCore.move(world, entity, newDirection, MonsterType.Cannon, sounds);
                        rotateDelay = ROTATE_DELAY;
                    }
                } else {
                    BrainCore.fire(world, entity, moveAnimation.direction, ItemType.Ammo, sounds);
                }
            }

            return true;
        }
    }
}
