package com.dg.ssrl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

/**
 * Created by magnus on 2015-09-13.
 */
public class Entity {

    interface Component {}


    public static class Position implements Component {
        public int x;
        public int y;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void translate(Direction direction) {
            x += direction.dx;
            y += direction.dy;
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

    public static class MoveAnimation implements Component {
        private static final String tag = "MoveAnimation";

        private enum State {
            MOVE,
            TURN,
            DONE
        }

        public static class Animation {
            Vector2 target = new Vector2();
            Vector2 position = new Vector2();
        }

        private static final int ANIMATIONS_SIZE = 2;

        public Direction direction = Direction.NONE;
        public Animation[] animations;
        int animationCount = 1;
        private Runnable onDone;

        private Vector2 tmp = new Vector2();

        private float duration = 0.4f;
        private float stateTime = 0f;
        private State state = State.DONE;

        public MoveAnimation() {
            animations = new Animation[ANIMATIONS_SIZE];
            for (int i = 0; i < ANIMATIONS_SIZE; i++) {
                animations[i] = new Animation();
            }
        }

        public void initMove(Position start, Position end, Runnable onDone) {
            animationCount = 1;
            animations[0].position.set(start.x * Assets.TILE_SIZE, start.y * Assets.TILE_SIZE);
            animations[0].target.set(end.x * Assets.TILE_SIZE, end.y * Assets.TILE_SIZE);
            stateTime = 0f;
            state = State.MOVE;
            this.onDone = onDone;
        }

        public void initMove(Position start, Position end, Position start2, Position end2, Runnable onDone) {
            animationCount = 2;
            animations[0].position.set(start.x * Assets.TILE_SIZE, start.y * Assets.TILE_SIZE);
            animations[0].target.set(end.x * Assets.TILE_SIZE, end.y * Assets.TILE_SIZE);
            animations[1].position.set(start2.x * Assets.TILE_SIZE, start2.y * Assets.TILE_SIZE);
            animations[1].target.set(end2.x * Assets.TILE_SIZE, end2.y * Assets.TILE_SIZE);
            stateTime = 0f;
            state = State.MOVE;
            this.onDone = onDone;
        }

        public void initTurn(Direction direction, Runnable onDone) {
            this.direction = direction;
            stateTime = 0f;
            this.onDone = onDone;
            state = State.TURN;
        }

        public void update(float delta) {
            stateTime += delta;

            if (state == State.MOVE) {
                float alpha = MathUtils.clamp(stateTime/duration, 0.0f, 1.0f);
                boolean done = false;
                for (int i = 0; i < animationCount; i++) {
                    animations[i].position.lerp(animations[i].target, alpha);
                    if (tmp.set(animations[i].position).dst(animations[i].target) < 0.1f) {
                        done |= true;
                    }
                }
                if (done) {
                    onDone();
                    if(animationCount == 2) {
                        animations[0].position.set(animations[1].target);
                    }
                    animationCount = 1;
                }

            } else if (state == State.TURN) {
                if (stateTime > duration) {
                    onDone();
                }
            }

        }

        private void onDone() {
            state = State.DONE;
            onDone.run();
            onDone = null;
        }

        public boolean isBusy() {
            return state != State.DONE;
        }
    }

    private HashMap<Class<? extends Component>, Component> components = new HashMap<Class<? extends Component>, Component>();

    public final int id;

    public Entity(int id) {
        this.id = id;
    }

    public <T> T getComponent(Class<T> clazz) {
        T component = (T)components.get(clazz);
        return component;
    }

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
    }
}
