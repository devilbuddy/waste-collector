package com.dg.ssrl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class MainMenu {

    private static class InstructionComponent {
        final String text;
        final Components.Sprite sprite;
        Vector2 position = new Vector2();

        float startX;
        boolean in = true;

        public InstructionComponent(String text, Components.Sprite sprite) {
            this.text = text;
            this.sprite = sprite;
        }

        public void update(float delta) {
            sprite.update(delta);
        }

        public void resize(int width) {
            startX = width*1.5f;
            position.x = startX;
        }

        public void resetToStartPosition() {
            position.x = startX;
        }
    }

    private static final int NUM_STARS = 50;
    private static final float EASE_TIME = 2.0f;

    private int hudWidth;
    private int hudHeight;

    private float easeTime = 0;
    private Vector2 instructionTarget = new Vector2();
    private int instructionComponentIndex = 0;
    private InstructionComponent[] instructionComponents;

    private final Assets assets;

    private StarField starField;

    public MainMenu(Assets assets) {
        this.assets = assets;
        starField = new StarField(assets, NUM_STARS, 1, 50f);

        instructionComponents = new InstructionComponent[] {
                new InstructionComponent("[GARBAGE-MAN]", new Components.Sprite(assets.getMonsterTextureRegion(MonsterType.Player))),
                new InstructionComponent("[GARBAGE]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Waste))),
                new InstructionComponent("[AMMO]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Ammo))),
                new InstructionComponent("[5X-AMMO]", new Components.Sprite(assets.getItemTextureRegion(ItemType.AmmoCrate))),
                new InstructionComponent("[ROCKET]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Rocket))),
                new InstructionComponent("[HEALTH]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Heart))),
                new InstructionComponent("[ADRENALINE]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Adrenaline))),
                new InstructionComponent("[WARP]", new Components.Sprite(assets.teleporterFrames, 0.2f, 0, Assets.COLOR_SKY_BLUE)),
                new InstructionComponent("[KEYCARD]", new Components.Sprite(assets.getItemTextureRegion(ItemType.Key))),
                new InstructionComponent("[ESCAPE]", new Components.Sprite(assets.exitFrames, 0.1f, 0, Assets.COLOR_SKY_BLUE))
        };
    }

    public void resize(int width, int height) {
        this.hudWidth = width;
        this.hudHeight = height;
        starField.resize(width, height);
        for (int i = 0; i < instructionComponents.length; i++) {
            instructionComponents[i].resize(hudWidth);
        }
    }

    public void update(float delta) {
        starField.update(delta);

        InstructionComponent instructionComponent = instructionComponents[instructionComponentIndex];
        instructionComponent.update(delta);
        easeTime += delta;
        float alpha = easeTime/EASE_TIME;
        Interpolation interpolation = Interpolation.pow2In;
        if (instructionComponent.in) {
            instructionTarget.x = hudWidth / 2;
            instructionComponent.position.interpolate(instructionTarget, alpha, interpolation);
            if (easeTime > EASE_TIME) {
                instructionComponent.in = false;
                easeTime = 0;
            }
        } else {
            instructionTarget.x = -hudWidth;
            instructionComponent.position.interpolate(instructionTarget, alpha, interpolation);
            if (easeTime > EASE_TIME) {
                //done
                instructionComponent.in = true;
                instructionComponent.resetToStartPosition();
                easeTime = 0;
                instructionComponentIndex = (instructionComponentIndex + 1) % instructionComponents.length;
            }
        }
    }

    public void render(SpriteBatch spriteBatch, ScoreData highScore) {
        starField.render(spriteBatch);

        spriteBatch.setColor(Color.WHITE);
        float logoW = assets.logo.getRegionWidth() * 2;
        float logoH = assets.logo.getRegionHeight() * 2;
        float logoX = hudWidth/2 - logoW /2;
        float logoY = (hudHeight/3 * 2) - logoH / 2;
        spriteBatch.draw(assets.logo, logoX, logoY, logoW, logoH);

        float y = hudHeight / 2 + assets.tapToStartText.glyphLayout.height;

        if (highScore != null) {
            assets.font.setColor(Color.YELLOW);
            renderHudItemCentered(spriteBatch, assets.getGlyphLayoutCacheItem("HI-SCORE " + highScore.score), y);
        }

        assets.font.setColor(Color.ORANGE);
        y -= 2 *assets.font.getLineHeight();
        renderHudItemCentered(spriteBatch, assets.tapToStartText, y);

        y -= 2.5f * assets.font.getLineHeight();
        renderInstructionComponent(instructionComponents[instructionComponentIndex], spriteBatch, y);
    }

    private float renderHudItemCentered(SpriteBatch spriteBatch, Assets.GlyphLayoutCacheItem glyphLayoutCacheItem, float y) {
        assets.font.draw(spriteBatch, glyphLayoutCacheItem.text, hudWidth / 2 - glyphLayoutCacheItem.glyphLayout.width / 2, y);
        return glyphLayoutCacheItem.glyphLayout.height;
    }

    private void renderInstructionComponent(InstructionComponent instructionComponent, SpriteBatch spriteBatch, float topY) {
        float x = instructionComponent.position.x;
        Assets.GlyphLayoutCacheItem text = assets.getGlyphLayoutCacheItem(instructionComponent.text);
        assets.font.setColor(Color.ORANGE);
        assets.font.draw(spriteBatch, text.text, x - text.glyphLayout.width/2, topY);

        topY -= assets.font.getLineHeight();

        Components.Sprite sprite = instructionComponent.sprite;
        TextureRegion textureRegion = sprite.getTextureRegion();
        int w = textureRegion.getRegionWidth();
        int h = textureRegion.getRegionHeight();

        int displayWidth = w * 2;
        int displayHeight = h * 2;

        spriteBatch.setColor(sprite.color);
        spriteBatch.draw(sprite.getTextureRegion(), x - displayWidth/2, topY - displayHeight - 4, displayWidth, displayHeight);
    }
}
