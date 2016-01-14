package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.dg.ssrl.Entity.Component;

public class Components {

    public interface TriggerAction {
        void run(World world, Entity triggeredBy);
    }

    public static class Trigger implements Component {
        public final TriggerAction triggerAction;
        public Trigger(TriggerAction triggerAction) {
            this.triggerAction = triggerAction;
        }
    }

    public interface OnEmptied {
        void run(Entity emptiedBy);
    }
    public static class ItemContainer implements Component {

        public Map<ItemType, Integer> content = new HashMap<ItemType, Integer>();

        private final OnEmptied onEmptied;

        public ItemContainer() {
            this(new OnEmptied() {
                @Override
                public void run(Entity emptiedBy) {

                }
            });
        }

        public ItemContainer(OnEmptied onEmptied) {
            this.onEmptied = onEmptied;
        }

        public void add(ItemType itemType, int amount) {
            int newAmount = amount;
            if (content.containsKey(itemType)) {
                newAmount += content.get(itemType);
            }
            content.put(itemType, newAmount);
        }

        public int getAmount(ItemType itemType) {
            if (content.containsKey(itemType)) {
                return content.get(itemType);
            }
            return 0;
        }

        public String getAmountString(ItemType itemType) {
            return itemType.name + " " + getAmount(itemType);
        }

        public void remove(ItemType itemType, int amount) {
            if (content.containsKey(itemType)) {
                int current = content.get(itemType);
                current -= amount;
                if (current < 0) {
                    current = 0;
                }
                content.put(itemType, current);
            }
        }

        public void clear() {
            content.clear();
        }

        public void emptyInto(ItemContainer other, Entity emptiedBy) {
            for (ItemType key : content.keySet()) {
                int amountToAdd = content.get(key);
                other.add(key, amountToAdd);
            }
            clear();
            onEmptied.run(emptiedBy);
        }

        @Override
        public String toString() {
            return "ItemContainer{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Solid implements Component {
        private boolean solid;

        public Solid(boolean solid) {
            this.solid = solid;
        }

        public boolean isSolid() {
            return solid;
        }
    }

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

    public interface OnDied {
        void onDied();
    }

    public static class Stats implements Component {
        public final MonsterType monsterType;
        private int maxHealth;
        private final OnDied onDied;
        private int health;
        public String healthString;

        public Stats(MonsterType monsterType, OnDied onDied) {
            this.monsterType = monsterType;
            this.onDied = onDied;

            maxHealth = monsterType.hitPoints;
            health = maxHealth;
            updateHealthString();
        }
        private void updateHealthString() {
            healthString = "HEALTH " + health;
        }

        public void heal(int amount) {
            health += amount;
            if (health > maxHealth) {
                health = maxHealth;
            }
            updateHealthString();
        }

        public void damage(int amount) {
            health -= amount;
            if (health <= 0) {
                health = 0;
                onDied.onDied();
            }
            updateHealthString();
        }

        public boolean isAlive() {
            return health > 0;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "health=" + health +
                    ", maxHealth=" + maxHealth +
                    '}';
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

        public Position set(Position other) {
            return set(other.x, other.y);
        }

        public Position translate(Direction direction) {
            x += direction.dx;
            y += direction.dy;
            return this;
        }

        public Position translate(int dx, int dy) {
            x += dx;
            y += dy;
            return this;
        }

        public Position copy() {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Position position = (Position) o;

            if (x != position.x) return false;
            return y == position.y;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }


    }

    public static class Sprite implements Component {
        private final TextureRegion[] textureRegions;
        private final float frameDuration;
        public final int renderPass;
        public final Color color = new Color(Color.WHITE);

        private float stateTime;
        private boolean animationEnabled = true;

        public Sprite(TextureRegion textureRegion) {
            this(new TextureRegion[]{ textureRegion}, 1, 0);
        }

        public Sprite(TextureRegion textureRegion, int renderPass) {
            this(new TextureRegion[]{ textureRegion}, 1, renderPass);
        }

        public Sprite(TextureRegion[] textureRegions, float frameDuration, int renderPass) {
            this.textureRegions = textureRegions;
            this.frameDuration = frameDuration;
            this.renderPass = renderPass;
        }

        public void enableAnimation(boolean animationEnabled) {
            this.animationEnabled = animationEnabled;
            if (!animationEnabled) {
                stateTime = 0;
            }
        }

        public void update(float delta) {
            if (animationEnabled) {
                stateTime += delta;
            }
        }

        public TextureRegion getTextureRegion() {
            int frameNumber = (int)(stateTime / frameDuration);
            frameNumber = frameNumber % textureRegions.length;
            return textureRegions[frameNumber];
        }
    }

    public static class MoveAnimation implements Component {
        private enum State {
            MOVE,
            TURN,
            BUMP,
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

        Vector2 start = new Vector2();
        Vector2 tmp = new Vector2();
        Vector2 target = new Vector2();

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

        public MoveAnimation reset() {
            state = State.DONE;
            callback = null;
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

        public void startTurn(Direction direction) {
            startTurn(direction, null);
        }

        public void startBump(Position start, Direction direction, Runnable callback) {
            bounds.x = start.x * Assets.TILE_SIZE;
            bounds.y = start.y * Assets.TILE_SIZE;
            this.direction = direction;
            this.state = State.BUMP;
            this.callback = callback;
            this.stateTime = 0;

            bounds.getPosition(this.start);
            tmp.set(this.start);
            bounds.getPosition(target);
            target.add(direction.dx * 3, direction.dy * 3);

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
                if(stateTime > 0.3f) {
                    onDone();
                }
            } else if (state == State.BUMP) {

                stateTime += delta;
                float alpha = stateTime / 0.3f;

                tmp.interpolate(target, alpha, Interpolation.pow3);
                bounds.setPosition(tmp);

                if (alpha > 1f) {
                    bounds.setPosition(start);
                    onDone();
                }
            }
        }

        public boolean isBusy() {
            return state != State.DONE;
        }

        private void onDone() {
            if (callback != null) {
                callback.run();
                callback = null;
            }
            state = State.DONE;
        }
    }

    public interface Brain {
        boolean act(World world);
    }

    public static class Actor implements Component {

        public enum Speed {
            EXTRA_SLOW(7),
            SLOW(4),
            MEDIUM(3),
            FAST(2);

            final int ticksToAct;
            Speed(int ticksToAct) {
                this.ticksToAct = ticksToAct;
            }
        }

        public Brain brain;
        private final Speed speed;
        private int ticks;
        public boolean alive = true;
        public Actor(Brain brain, Speed speed) {
            this.brain = brain;
            this.speed = speed;
        }

        public boolean tick() {
            ticks++;
            if (ticks >= speed.ticksToAct) {
                return true;
            } else {
                return false;
            }
        }

        public void reset() {
            ticks = 0;
        }

        public boolean act(World world) {
            return brain.act(world);
        }
    }

    public static class Effect implements Component {

        private static final String tag = "Effect";

        public static class Particle {
            Vector2 velocity = new Vector2();
            Vector2 acceleration = new Vector2();
            public Vector2 position = new Vector2();
            public final Color color;

            public Particle(float x, float y, Color color) {
                position.set(x, y);
                this.color = color;
            }
        }

        private float duration;
        private float lifeTime = 0;

        public int numParticles = 20;
        public Particle[] particles;
        Random r = new Random(System.currentTimeMillis());

        public Effect(float x, float y, int numParticles, float duration, Color color) {
            this.duration = duration;
            this.numParticles = numParticles;

            particles = new Particle[numParticles];

            for (int i = 0; i < numParticles; i++) {
                Particle p = new Particle(x, y, color);
                p.acceleration.set((r.nextBoolean() ? -0.5f : 0.5f) * r.nextFloat(), (r.nextBoolean() ? -0.5f : 0.5f) * r.nextFloat());
                p.velocity.set((r.nextBoolean() ? -0.25f : 0.25f) * r.nextFloat(),  (r.nextBoolean() ? -0.25f : 0.25f) * r.nextFloat());
                particles[i] = p;
            }
        }

        public void update(float delta) {
            lifeTime += delta;

            for (int i = 0; i < numParticles; i++) {
                Particle p = particles[i];

                float dx = p.acceleration.x;
                float dy = p.acceleration.y;

                p.velocity.add(dx * delta, dy * delta);

                p.position.add(p.velocity);

            }
        }

        public boolean isDone() {
            return lifeTime >= duration;
        }
    }
}
