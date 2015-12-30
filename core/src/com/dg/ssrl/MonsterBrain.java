package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

import java.util.Random;

import static com.dg.ssrl.Components.*;

public class MonsterBrain implements Brain {

    private static final String tag = "MonsterBrain";

    private final int entityId;
    private Random random = new Random();

    public MonsterBrain(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean act(final World world) {
        final Entity entity = world.getEntity(entityId);
        MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);

        if (!moveAnimation.isBusy()) {

            final Position current = entity.getComponent(Position.class);

            Direction targetDirection = Direction.NONE;
            int lowestValue = world.dijkstraMap[current.y][current.x];
            for(Direction direction : Direction.CARDINAL_DIRECTIONS) {
                Position p = current.clone().translate(direction);
                p.x = p.x % world.getWidth();
                p.y = p.y % world.getHeight();
                while (p.x < 0) {
                    p.x += world.getWidth();
                }
                while (p.y < 0) {
                    p.y += world.getHeight();
                }
                int value = world.dijkstraMap[p.y][p.x];
                if (value < lowestValue) {
                    lowestValue = value;
                    targetDirection = direction;
                }
            }

            Gdx.app.log(tag, "targetDirection:" + targetDirection);
            final Position targetPosition = current.clone().translate(targetDirection);
            targetPosition.x = targetPosition.x % world.getWidth();
            targetPosition.y = targetPosition.y % world.getHeight();
            while (targetPosition.x < 0) {
                targetPosition.x += world.getWidth();
            }
            while (targetPosition.y < 0) {
                targetPosition.y += world.getHeight();
            }

            if (world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
                moveAnimation.startMove(current, Assets.TILE_SIZE, targetDirection, new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                world.move(entity, targetPosition.x, targetPosition.y);
            }
            return true;
        } else {
            return false;
        }

    }
}
