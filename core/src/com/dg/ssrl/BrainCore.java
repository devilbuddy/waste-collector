package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;

public class BrainCore {

    private static final String tag = "BrainCore";

    public static class MoveResult {
        public boolean acted = false;
        public boolean moved = false;
        public Position endPosition = new Position();
    }

    private static MoveResult moveResult = new MoveResult();

    public static MoveResult move(final World world, final Entity entity, final Direction moveDirection) {
        final MoveAnimation moveAnimation = entity.getComponent(MoveAnimation.class);
        final Position currentPosition = entity.getComponent(Position.class);

        moveResult.acted = false;
        moveResult.moved = false;
        moveResult.endPosition.set(currentPosition);

        if (moveAnimation.direction == moveDirection) {

            final Position targetPosition = currentPosition.clone();
            world.translateWraparound(targetPosition, moveDirection);

            Gdx.app.log(tag, "targetPosition:" + targetPosition);

            if (world.getCell(targetPosition.x, targetPosition.y).isWalkable()) {
                moveAnimation.startMove(currentPosition, Assets.TILE_SIZE, moveDirection, new Runnable() {
                    @Override
                    public void run() {
                        moveAnimation.setPosition(targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE);
                    }
                });
                world.move(entity, targetPosition.x, targetPosition.y);

                moveResult.acted = true;
                moveResult.moved = true;
                moveResult.endPosition.set(targetPosition);
            }

        } else {
            moveAnimation.startTurn(moveDirection, new Runnable() {
                @Override
                public void run() {

                }
            });
            moveResult.acted = true;
        }

        return moveResult;
    }

}
