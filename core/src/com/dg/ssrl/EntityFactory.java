package com.dg.ssrl;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import static com.dg.ssrl.Components.*;

public class EntityFactory {

    private AtomicInteger entityIdCounter = new AtomicInteger();

    private final Assets assets;

    public EntityFactory(Assets assets) {
        this.assets = assets;
    }

    public Entity makePlayer() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new Position());
        entity.addComponent(new Sprite(assets.tiles[4][2]));
        entity.addComponent(new MoveAnimation(50f));
        return entity;
    }

    public Entity makeBullet() {
        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(new MoveAnimation(150f));
        entity.addComponent(new Sprite(assets.tiles[4][3]));
        return entity;
    }

    private class MonsterBrain implements Brain {

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
                Direction direction = Direction.CARDINAL_DIRECTIONS[random.nextInt(Direction.CARDINAL_DIRECTIONS.length)];
                final Position current = entity.getComponent(Position.class);
                final Position targetPosition = current.clone().translate(direction);
                targetPosition.x = targetPosition.x % world.getWidth();
                targetPosition.y = targetPosition.y % world.getHeight();
                while (targetPosition.x < 0) { targetPosition.x += world.getWidth(); }
                while (targetPosition.y < 0) { targetPosition.y += world.getHeight(); }

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

    public Entity makeMonster(int x, int y) {

        Components.Position position = new Position();
        position.set(x, y);

        Components.MoveAnimation moveAnimation = new MoveAnimation(50f);
        moveAnimation.setPosition(x * Assets.TILE_SIZE, y * Assets.TILE_SIZE).setDirection(Direction.EAST);

        Entity entity = new Entity(entityIdCounter.incrementAndGet());
        entity.addComponent(position);
        entity.addComponent(moveAnimation);
        entity.addComponent(new Sprite(assets.tiles[5][1]));
        entity.addComponent(new Actor(new MonsterBrain(entity.id)));
        return entity;
    }

}
