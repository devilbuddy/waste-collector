package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

import static com.dg.ssrl.Components.Brain;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;

public class MonsterBrain implements Brain {

    private static final String tag = "MonsterBrain";

    private final int entityId;
    private final Assets.Sounds sounds;

    public MonsterBrain(int entityId, Assets.Sounds sounds) {
        this.entityId = entityId;
        this.sounds = sounds;
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
            int lowestValue = world.dijkstraMap[current.y][current.x];
            for (Direction direction : Direction.CARDINAL_DIRECTIONS) {
                Position p = current.clone();
                p = world.translateWraparound(p, direction);

                int value = world.dijkstraMap[p.y][p.x];
                if (value < lowestValue) {
                    lowestValue = value;
                    targetDirection = direction;
                }
            }

            //Gdx.app.log(tag, "targetDirection:" + targetDirection);
            BrainCore.MoveResult moveResult = BrainCore.move(world, entity, targetDirection, sounds);
            //return moveResult.acted;
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
            Position position = entity.getComponent(Position.class).clone();

            Entity explosion = entityFactory.makeExplosion(position.x * Assets.TILE_SIZE + Assets.TILE_SIZE/2, position.y * Assets.TILE_SIZE + Assets.TILE_SIZE/2);
            world.addEntity(explosion);

            if (ticksToActivate == 0) {
                entity.alive = false;
                Entity monster = entityFactory.makeMonster(position.x, position.y, MonsterType.Snake);
                world.addEntity(monster);
            }
            return true;
        }
    }
}
