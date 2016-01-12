package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class Assets {

    public TextureRegion playerTextureRegion;

    public static class Sounds {

        public enum SoundId {
            LASER,
            HIT,
            PICKUP,
            EXIT,
            ROCKET,
            SPAWN,
            NONE
        }
        private Map<SoundId, Sound> sounds = new HashMap<SoundId, Sound>();

        public void create() {
            sounds.put(SoundId.LASER, Gdx.audio.newSound(Gdx.files.internal("laser-shoot.wav")));
            sounds.put(SoundId.HIT, Gdx.audio.newSound(Gdx.files.internal("hit-hurt.wav")));
            sounds.put(SoundId.PICKUP, Gdx.audio.newSound(Gdx.files.internal("pickup-coin.wav")));
            sounds.put(SoundId.EXIT, Gdx.audio.newSound(Gdx.files.internal("powerup.wav")));
            sounds.put(SoundId.ROCKET, Gdx.audio.newSound(Gdx.files.internal("rocket.wav")));
            sounds.put(SoundId.SPAWN, Gdx.audio.newSound(Gdx.files.internal("spawn.wav")));
        }

        public void play(SoundId soundId) {
            Sound sound = sounds.get(soundId);
            if (sound != null) {
                sound.play();
            }
        }
    }

    public static class GlyphLayoutCacheItem {
        public final String text;
        public final GlyphLayout glyphLayout;

        public GlyphLayoutCacheItem(String text, BitmapFont font) {
            this.text = text;
            glyphLayout = new GlyphLayout(font, text);
        }
    }

    public static final int TILE_SIZE = 8;

    public Texture tilesTexture;
    public TextureRegion[][] tiles;
    public BitmapFont font;

    public TextureRegion wallSolid;

    public TextureRegion wall;
    public TextureRegion floor;

    public TextureRegion whitePixel;

    public TextureRegion[] exitFrames;
    public TextureRegion[] teleporterFrames;

    public TextureRegion logo;

    private Map<MonsterType, TextureRegion> monsterSprites = new HashMap<MonsterType, TextureRegion>();
    private Map<ItemType, TextureRegion> itemSprites = new HashMap<ItemType, TextureRegion>();
    private Map<ItemType, TextureRegion> bulletSprites = new HashMap<ItemType, TextureRegion>();

    public Sounds sounds = new Sounds();

    public TextureRegion[] autoTileSet;

    public GlyphLayoutCacheItem gameOverText;
    public GlyphLayoutCacheItem wasteCollectedText;
    public GlyphLayoutCacheItem sectorReachedText;
    public GlyphLayoutCacheItem tapToStartText;
    public GlyphLayoutCacheItem scoreText;
    public GlyphLayoutCacheItem congratulationsText;

    public Color floorColor = new Color(0x1f1f1fff);

    public Map<String, GlyphLayoutCacheItem> glyphLayoutCacheItemMap = new HashMap<String, GlyphLayoutCacheItem>();

    public static Color bulletExplosionColor = Color.ORANGE;
    public static Color rocketExplosionColor = Color.RED;
    public static Color damageColor = Color.FIREBRICK;

    public GlyphLayoutCacheItem getGlyphLayoutCacheItem(String text) {
        GlyphLayoutCacheItem item = glyphLayoutCacheItemMap.get(text);
        if (item == null) {
            item = new GlyphLayoutCacheItem(text, font);
            glyphLayoutCacheItemMap.put(text, item);
        }
        return item;
    }

    public void create() {
        tilesTexture = new Texture(Gdx.files.internal("tiles.png"));

        tiles = TextureRegion.split(tilesTexture, TILE_SIZE, TILE_SIZE);

        wallSolid = tiles[2][0];
        wall = tiles[2][1];
        floor = tiles[1][15];

        whitePixel = new TextureRegion(tilesTexture, 18,18,1,1);

        font = new BitmapFont(Gdx.files.internal("kongtext.fnt"));

        monsterSprites.put(MonsterType.Player, tiles[4][2]);
        monsterSprites.put(MonsterType.Crawler, tiles[5][2]);
        monsterSprites.put(MonsterType.Stealer, tiles[5][4]);
        monsterSprites.put(MonsterType.Brute, tiles[5][3]);
        monsterSprites.put(MonsterType.Egg, tiles[11][0]);
        monsterSprites.put(MonsterType.Cannon, tiles[5][5]);
        monsterSprites.put(MonsterType.Grower, tiles[5][6]);
        monsterSprites.put(MonsterType.Robot, tiles[5][0]);

        itemSprites.put(ItemType.Key, tiles[9][0]);
        itemSprites.put(ItemType.Ammo, tiles[10][0]);
        itemSprites.put(ItemType.AmmoCrate, tiles[10][1]);
        itemSprites.put(ItemType.Waste, tiles[8][3]);
        itemSprites.put(ItemType.Rocket, tiles[10][3]);
        itemSprites.put(ItemType.Heart, tiles[7][4]);

        bulletSprites.put(ItemType.Ammo, tiles[4][3]);
        bulletSprites.put(ItemType.Rocket, tiles[4][4]);


        exitFrames = new TextureRegion[] {tiles[12][1], tiles[12][2], tiles[12][3], tiles[12][2], tiles[12][1], tiles[12][0]};
        teleporterFrames = new TextureRegion[] {tiles[13][0], tiles[13][1], tiles[13][2], tiles[13][3], tiles[13][4]};

        logo = new TextureRegion(tilesTexture, 80, 112, 48, 16);

        sounds.create();

        autoTileSet = new TextureRegion[] {
                tiles[0][16],
                tiles[1][17],
                tiles[0][18],
                tiles[1][19],
                tiles[0][20],
                tiles[1][21],
                tiles[0][22],
                tiles[1][23],
                tiles[0][24],
                tiles[1][25],
                tiles[0][26],
                tiles[1][27],
                tiles[0][28],
                tiles[1][29],
                tiles[0][30],
                tiles[1][31],
        };


        gameOverText = new GlyphLayoutCacheItem("GAME OVER", font);
        wasteCollectedText = new GlyphLayoutCacheItem("WASTE COLLECTED", font);
        sectorReachedText = new GlyphLayoutCacheItem("SECTOR REACHED", font);
        tapToStartText = new GlyphLayoutCacheItem("TAP TO START", font);
        scoreText = new GlyphLayoutCacheItem("SCORE", font);
        congratulationsText = new GlyphLayoutCacheItem("CLEARED", font);
    }

    public TextureRegion getMonsterTextureRegion(MonsterType monsterType) {
        return monsterSprites.get(monsterType);
    }

    public TextureRegion getItemTextureRegion(ItemType itemType) {
        return itemSprites.get(itemType);
    }

    public TextureRegion getBulletTextureRegion(ItemType itemType) {
        return bulletSprites.get(itemType);
    }
}
