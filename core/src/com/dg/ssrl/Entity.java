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
    }

    public static class MoveState implements Component {
        private static final String tag = "MoveState";

        private enum State {
            BUSY,
            DONE
        }

        Vector2 target = new Vector2();
        Vector2 position = new Vector2();
        Vector2 tmp = new Vector2();

        float duration = 0.4f;
        float stateTime = 0f;

        State state = State.DONE;
        private Runnable onDone;

        public void init(float startX, float startY, float targetX, float targetY, Runnable onDone) {
            Gdx.app.log(tag, "init " + startX + " " + startY + " " + targetX + " " + targetY);
            position.set(startX, startY);
            target.set(targetX, targetY);

            stateTime = 0f;

            state = State.BUSY;
            this.onDone = onDone;
        }

        public void update(float delta) {
            stateTime += delta;

            float alpha = MathUtils.clamp(stateTime/duration, 0.0f, 1.0f);
            position.lerp(target, alpha);
            if (tmp.set(position).dst(target) < 0.1f) {
                state = State.DONE;
                onDone.run();
                onDone = null;
            }
        }
        public boolean isBusy() {
            return state == State.BUSY;
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
