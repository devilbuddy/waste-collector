package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
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

    public static class MoveState implements Component {
        private static final String tag = "MoveState";

        public Direction direction = Direction.NONE;

        private enum State {
            MOVE,
            TURN,
            DONE
        }

        public static class Movement {
            Vector2 target = new Vector2();
            Vector2 position = new Vector2();
        }

        public Movement[] movements = new Movement[2];

        Vector2 tmp = new Vector2();

        float duration = 0.4f;
        float stateTime = 0f;

        State state = State.DONE;
        private Runnable onDone;

        int numMovements = 1;

        public MoveState() {
            movements[0] = new Movement();
            movements[1] = new Movement();
        }

        public void initMove(float startX, float startY, float targetX, float targetY, Runnable onDone) {
            Gdx.app.log(tag, "initMove " + startX + " " + startY + " " + targetX + " " + targetY);
            numMovements = 1;

            movements[0].position.set(startX, startY);
            movements[0].target.set(targetX, targetY);

            stateTime = 0f;

            state = State.MOVE;
            this.onDone = onDone;
        }

        public void initMove(float startX, float startY, float targetX, float targetY, float start2X, float start2Y, float target2X, float target2Y, Runnable onDone) {
            Gdx.app.log(tag, "initMove " + startX + " " + startY + " " + targetX + " " + targetY);
            numMovements = 2;

            movements[0].position.set(startX, startY);
            movements[0].target.set(targetX, targetY);


            movements[1].position.set(start2X, start2Y);
            movements[1].target.set(target2X, target2Y);


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
                for (int i = 0; i < numMovements; i++) {
                    movements[i].position.lerp(movements[i].target, alpha);
                    if (tmp.set(movements[i].position).dst(movements[i].target) < 0.1f) {
                        done |= true;
                    }
                }
                if (done) {
                    onDone();
                    if(numMovements == 2) {
                        movements[0].position.set(movements[1].target);
                    }
                    numMovements = 1;
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
