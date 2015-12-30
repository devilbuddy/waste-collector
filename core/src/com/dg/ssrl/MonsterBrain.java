package com.dg.ssrl;

import java.util.Random;

import static com.dg.ssrl.Components.*;

public class MonsterBrain implements Brain {

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

            Entity player = world.getPlayer();



            Direction direction = Direction.CARDINAL_DIRECTIONS[random.nextInt(Direction.CARDINAL_DIRECTIONS.length)];
            final Position current = entity.getComponent(Position.class);
            final Position targetPosition = current.clone().translate(direction);
            targetPosition.x = targetPosition.x % world.getWidth();
            targetPosition.y = targetPosition.y % world.getHeight();
            while (targetPosition.x < 0) {
                targetPosition.x += world.getWidth();
            }
            while (targetPosition.y < 0) {
                targetPosition.y += world.getHeight();
            }

            if (world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
                moveAnimation.startMove(current, Assets.TILE_SIZE, direction, new Runnable() {
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
