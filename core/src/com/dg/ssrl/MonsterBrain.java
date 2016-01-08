package com.dg.ssrl;

import java.util.Random;

import static com.dg.ssrl.Components.Brain;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.Stats;
import static com.dg.ssrl.Components.ItemContainer;

public class MonsterBrain implements Brain {

    private static final String tag = "MonsterBrain";

    private final int entityId;
    private final Assets.Sounds sounds;
    private final Random random = new Random(System.currentTimeMillis());

    public MonsterBrain(int entityId, Assets.Sounds sounds) {
        this.entityId = entityId;
        this.sounds = sounds;
    }

    private Direction findAttackDirection(World world, final Position current) {
        Direction targetDirection = Direction.NONE;
        Position targetPosition = current.copy();

        int lowestValue = world.dijkstraMap[current.y][current.x];
        for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
            targetPosition.set(current);
            targetPosition = world.translateWraparound(targetPosition, direction);

            int value = world.dijkstraMap[targetPosition.y][targetPosition.x];
            if (value < lowestValue) {
                lowestValue = value;
                targetDirection = direction;
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

            int value = world.dijkstraMap[targetPosition.y][targetPosition.x];
            if (value != Integer.MAX_VALUE && value > highestValue) {
                highestValue = value;
                targetDirection = direction;
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

            Direction targetDirection = Direction.NONE;

            ItemContainer itemContainer = entity.getComponent(ItemContainer.class);
            if (itemContainer != null && itemContainer.getAmount(ItemType.Key) > 0) {
                targetDirection = findFleeDirection(world, current);
            } else {
                targetDirection = findAttackDirection(world, current);
            }

            boolean doMove = true;
            if (targetDirection == moveAnimation.direction) {

                Position target = current.copy();
                world.translateWraparound(target, targetDirection);

                World.Cell targetCell = world.getCell(target);
                for (int i = 0; i < targetCell.getEntityCount(); i++) {
                    Entity targetEntity = world.getEntity(targetCell.getEntityId(i));
                    Stats targetStats = targetEntity.getComponent(Stats.class);
                    if (targetStats != null && targetStats.monsterType != MonsterType.Player) {
                        //targetDirection = Direction.CARDINAL_DIRECTIONS[random.nextInt(Direction.CARDINAL_DIRECTIONS.length)];
                        doMove = false;
                        //break;
                    }
                }
            }

            if (doMove) {
                //Gdx.app.log(tag, "targetDirection:" + targetDirection);
                BrainCore.MoveResult moveResult = BrainCore.move(world, entity, targetDirection, sounds);
                //return moveResult.acted;
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

            Entity explosion = entityFactory.makeExplosion(position.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2, position.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2);
            world.addEntity(explosion);

            if (ticksToActivate == 0) {
                entity.alive = false;
                Entity monster = entityFactory.makeMonster(position.x, position.y, MonsterType.Crawler);
                world.addEntity(monster);
            }
            return true;
        }
    }

    public static class CannonBrain implements Brain {
        private final int entityId;
        private final Assets.Sounds sounds;
        boolean rotate = true;
        public CannonBrain(int entityId, Assets.Sounds sounds) {
            this.entityId = entityId;
            this.sounds = sounds;
        }

        @Override
        public boolean act(World world) {
            Entity entity = world.getEntity(entityId);
            MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);
            if (rotate) {
                Direction newDirection = moveAnimation.direction.turn();
                BrainCore.move(world, entity, newDirection, sounds);
                rotate = false;
            } else {
                BrainCore.fire(world, entity, moveAnimation.direction, ItemType.Rocket, sounds);
                rotate = true;
            }
            return true;
        }
    }
}
