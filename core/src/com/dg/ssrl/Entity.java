package com.dg.ssrl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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

    public static class Sprite implements Component {
        public TextureRegion region;

        public Sprite(TextureRegion textureRegion) {
            this.region = textureRegion;
        }
    }

    public static class MoveAnimation implements Component {
        private enum State {
            MOVE,
            TURN,
            DONE
        }

        public Rectangle bounds = new Rectangle();
        public Direction direction = Direction.NONE;
        private Runnable callback;

        private float speed = 50f;
        private float currentDistance;
        private float distance;
        private State state = State.DONE;
        private float stateTime = 0;

        public MoveAnimation() {
            bounds.width = Assets.TILE_SIZE;
            bounds.height = Assets.TILE_SIZE;
        }

        public void setPosition(float x, float y) {
            bounds.x = x;
            bounds.y = y;
        }

        public void init(Position start, float distance, Direction direction, Runnable callback) {
            bounds.x = start.x * Assets.TILE_SIZE;
            bounds.y = start.y * Assets.TILE_SIZE;
            this.distance = distance;
            currentDistance = 0;

            this.direction = direction;
            this.callback = callback;
            this.state = State.MOVE;
            this.stateTime = 0;
        }

        public void initTurn(Direction direction, Runnable runnable) {
            this.direction = direction;
            this.state = State.TURN;
            this.callback = runnable;
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

    private HashMap<Class<? extends Component>, Component> components = new HashMap<Class<? extends Component>, Component>();

    public final int id;
    public boolean alive = true;
    @Override
    public String toString() {
        return "Entity{" +
                "components=" + components +
                ", id=" + id +
                '}';
    }

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
