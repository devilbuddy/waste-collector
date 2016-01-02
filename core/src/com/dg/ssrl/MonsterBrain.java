package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

import static com.dg.ssrl.Components.Brain;
import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;

public class MonsterBrain implements Brain {

    private static final String tag = "MonsterBrain";

    private final MonsterType monsterType;
    private final int entityId;

    public MonsterBrain(MonsterType monsterType, int entityId) {
        this.monsterType = monsterType;
        this.entityId = entityId;
    }

    @Override
    public boolean act(final World world) {
        if (monsterType.immobile) {
            return true;
        } else {

            final Entity entity = world.getEntity(entityId);
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

                Gdx.app.log(tag, "targetDirection:" + targetDirection);
                BrainCore.MoveResult moveResult = BrainCore.move(world, entity, targetDirection);
                //return moveResult.acted;
                return true;
            } else {
                return false;
            }

        }
    }
}
