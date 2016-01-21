package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class StarField {

    private static class Star {
        Vector2 position = new Vector2();
        Vector2 velocity = new Vector2();
        Color color = new Color();

        public void update(float delta) {
            float dx = velocity.x * delta;
            float dy = velocity.y * delta;
            position.add(dx, dy);
        }
    }


    private final Assets assets;
    private final int numStars;
    private final Random random;
    private final int starSize;
    private final float baseVelocity;
    private Star[] stars;
    private int width;
    private int height;


    public StarField(Assets assets, int numStars, int starSize, float baseVelocity) {
        this.assets = assets;
        this.numStars = numStars;
        this.starSize = starSize;
        this.baseVelocity = baseVelocity;
        random = new Random();
        stars = new Star[numStars];
        for (int i = 0; i < numStars; i++) {
            stars[i] = new Star();
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < numStars; i++) {
            Star star = stars[i];
            star.position.x = random.nextInt(width);
            star.position.y = random.nextInt(height);

            float velocityPercent = random.nextFloat();
            star.velocity.y = baseVelocity * velocityPercent;

            star.color.set(Color.WHITE);
            star.color.mul(velocityPercent);
        }
    }

    public void update(float delta) {
        for (int i = 0; i < numStars; i++) {
            Star star = stars[i];
            star.update(delta);
            if (star.position.y > height) {
                star.position.y = -1;
                star.position.x = random.nextInt(width);
            }
        }
    }

    public void render(SpriteBatch spriteBatch) {
        for (int i = 0; i < numStars; i++) {
            Star star = stars[i];
            spriteBatch.setColor(star.color);
            spriteBatch.draw(assets.whitePixel, star.position.x, star.position.y, starSize, starSize);
        }
    }


}
