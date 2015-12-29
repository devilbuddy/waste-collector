package com.dg.ssrl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by magnus on 2015-12-29.
 */
public class Components {

    public static class Position implements Entity.Component {
        public int x;
        public int y;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position translate(Direction direction) {
            x += direction.dx;
            y += direction.dy;
            return this;
        }

        public Position clone() {
            Position p = new Position();
            p.set(x, y);
            return p;
        }

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static class Sprite implements Entity.Component {
        public TextureRegion region;

        public Sprite(TextureRegion textureRegion) {
            this.region = textureRegion;
        }
    }

    public static class MoveAnimation implements Entity.Component {
        private enum State {
            MOVE,
            TURN,
            DONE
        }

        public Rectangle bounds = new Rectangle();
        public Direction direction = Direction.NONE;
        private Runnable callback;

        private float speed;
        private float currentDistance;
        private float distance;
        private State state = State.DONE;
        private float stateTime = 0;

        public MoveAnimation(float speed) {
            this.speed = speed;
            bounds.width = Assets.TILE_SIZE;
            bounds.height = Assets.TILE_SIZE;
        }

        public MoveAnimation setPosition(float x, float y) {
            bounds.x = x;
            bounds.y = y;
            return this;
        }

        public MoveAnimation setDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        public void startMove(Position start, float distance, Direction direction, Runnable callback) {
            bounds.x = start.x * Assets.TILE_SIZE;
            bounds.y = start.y * Assets.TILE_SIZE;
            this.distance = distance;
            currentDistance = 0;

            this.direction = direction;
            this.callback = callback;
            this.state = State.MOVE;
            this.stateTime = 0;
        }

        public void startTurn(Direction direction, Runnable callback) {
            this.direction = direction;
            this.state = State.TURN;
            this.callback = callback;
            this.stateTime = 0;
        }

        public void update(float delta, Rectangle worldBounds) {
            if (state == State.MOVE) {
                float dx = direction.dx * speed * delta;
                float dy = direction.dy * speed * delta;

                currentDistance += dx;
                currentDistance += dy;

                bounds.x += dx;
                bounds.y += dy;

                if (!bounds.overlaps(worldBounds)) {
                    bounds.x -= direction.dx * worldBounds.width;
                    bounds.y -= direction.dy * worldBounds.height;
                }

                if (Math.abs(currentDistance) > distance) {
                    onDone();
                }
            } else if (state == State.TURN){
                stateTime += delta;
                if(stateTime > 0.4f) {
                    onDone();
                }
            }
        }

        public boolean isBusy() {
            return state != State.DONE;
        }

        private void onDone() {
            callback.run();
            callback = null;
            state = State.DONE;
        }

    }

    public interface Brain {
        boolean act(World world);
    }

    public static class Actor implements Entity.Component {
        public Brain brain;
        public Actor(Brain brain) {
            this.brain = brain;
        }
        public boolean act(World world) {
            return brain.act(world);
        }
    }
}
