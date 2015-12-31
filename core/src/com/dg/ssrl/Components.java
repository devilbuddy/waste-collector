package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

import static com.dg.ssrl.Entity.Component;

public class Components {

    public interface Updater {
        void update(float delta, World world);
    }

    public static class Update implements Component {
        private final Updater updater;
        public Update(Updater updater) {
            this.updater = updater;
        }
        public void update(float delta, World world) {
            updater.update(delta, world);
        }
    }

    public static class Stats implements Component {
        private int maxHealth;
        private int health;
        public String healthString;

        public Stats(int maxHealth) {
            this.maxHealth = maxHealth;
            this.health = maxHealth;
            updateHealthString();
        }
        private void updateHealthString() {
            this.healthString = "" + health + "/" + maxHealth;
        }

        public void damage(int amount) {
            health -= amount;
            if (health < 0) {
                health = 0;
            }
        }

        public boolean isAlive() {
            return health > 0;
        }
    }

    public static class Position implements Component {
        public int x;
        public int y;

        public Position() {}

        public Position(int x, int y) {
            set(x, y);
        }

        public Position set(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
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

        public void update(float delta, World world) {
            Rectangle worldBounds = world.bounds;

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

    public static class Actor implements Component {
        public Brain brain;
        public Actor(Brain brain) {
            this.brain = brain;
        }
        public boolean act(World world) {
            return brain.act(world);
        }
    }

    public static class Effect implements Component {

        private static final String tag = "Effect";

        public static class Particle {
            Vector2 velocity = new Vector2();
            public Vector2 position = new Vector2();
            public Color color = new Color(Color.ORANGE);

            public Particle(float x, float y) {
                position.set(x, y);
            }
        }

        private float duration = 0.2f;
        private float lifeTime = 0;

        public int numParticles = 10;
        public Particle[] particles;

        private Vector2 tmp = new Vector2();

        Random r = new Random(System.currentTimeMillis());
        Vector2 gravity = new Vector2(0, -5);

        public Effect(float x, float y, int numParticles) {
            this.numParticles = numParticles;
            particles = new Particle[numParticles];

            for (int i = 0; i < numParticles; i++) {
                particles[i] = new Particle(x, y);
                particles[i].velocity.set((5 + r.nextInt(10)) * (r.nextBoolean() ? -1 : 1), ((5 + r.nextInt(10)) * (r.nextBoolean() ? -1 : 1)));
            }
        }

        public void update(float delta) {
            lifeTime += delta;

            for (int i = 0; i < numParticles; i++) {
                Particle p = particles[i];
                tmp.set(p.velocity).scl(delta);
                particles[i].position.add(tmp);

                tmp.set(gravity).scl(delta);
                //particles[i].position.add(tmp);
            }
        }

        public boolean isDone() {
            return lifeTime >= duration;
        }
    }
}
