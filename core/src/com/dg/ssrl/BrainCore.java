package com.dg.ssrl;

import com.badlogic.gdx.Gdx;

import static com.dg.ssrl.Components.MoveAnimation;
import static com.dg.ssrl.Components.Position;
import static com.dg.ssrl.Components.ItemContainer;

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

            if (world.isWalkable(targetPosition)) {
                moveAnimation.startMove(currentPosition, Assets.TILE_SIZE, moveDirection, new Runnable() {
                    @Override
                    public void run() {
                        moveAnimation.setPosition(targetPosition.x * Assets.TILE_SIZE, targetPosition.y * Assets.TILE_SIZE);
                    }
                });

                ItemContainer itemContainer = entity.getComponent(ItemContainer.class);
                if (itemContainer != null) {
                    World.Cell cell = world.getCell(targetPosition);
                    for (int i = 0; i < cell.getEntityCount(); i++) {
                        int entityId = cell.getEntityId(i);
                        Entity e = world.getEntity(entityId);
                        ItemContainer pickupItem = e.getComponent(ItemContainer.class);
                        if (pickupItem != null) {
                            pickupItem.emptyInto(itemContainer);
                            Gdx.app.log(tag, itemContainer.toString());
                        }
                    }
                }

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
