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

    public static class Sounds {

        public enum SoundId {
            LASER,
            HIT,
            PICKUP,
            EXIT,
            ROCKET,
            SPAWN,
            TELEPORT,
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
            sounds.put(SoundId.TELEPORT, Gdx.audio.newSound(Gdx.files.internal("teleport.wav")));
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

    private Texture gameTexture;
    private TextureRegion[][] tiles;
    public BitmapFont font;


    public TextureRegion floor;

    public TextureRegion whitePixel;

    public TextureRegion[] exitFrames;
    public TextureRegion[] teleporterFrames;

    public TextureRegion progressBarLeft;
    public TextureRegion progressBarCenter;
    public TextureRegion progressBarRight;


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


    //Arnes 16 color palette - http://androidarts.com/palette/16pal.htm
    public static final Color COLOR_BLACK = new Color(0x000000ff);
    public static final Color COLOR_GREY = new Color(0x9D9D9Dff);
    public static final Color COLOR_WHITE = new Color(0xFFFFFFff);
    public static final Color COLOR_RED = new Color(0xBE2633ff);
    public static final Color COLOR_PINK = new Color(0xE06F8Bff);
    public static final Color COLOR_DARK_BROWN = new Color(0x493C2Bff);
    public static final Color COLOR_BROWN = new Color(0xA46422ff);
    public static final Color COLOR_ORANGE = new Color(0xEB8931ff);
    public static final Color COLOR_YELLOW = new Color(0xF7E26Bff);
    public static final Color COLOR_DARK_GREEN = new Color(0x2F484Eff);
    public static final Color COLOR_GREEN = new Color(0x44891Aff);
    public static final Color COLOR_LIGHT_GREEN = new Color(0xA3CE27ff);
    public static final Color COLOR_NIGHT_BLUE = new Color(0x1B2632ff);
    public static final Color COLOR_DARK_BLUE = new Color(0x005784ff);
    public static final Color COLOR_SKY_BLUE = new Color(0x31A2F2ff);
    public static final Color COLOR_LIGHT_BLUE = new Color(0xB2DCEFff);

    private Map<String, GlyphLayoutCacheItem> glyphLayoutCacheItemMap = new HashMap<String, GlyphLayoutCacheItem>();

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
        gameTexture = new Texture(Gdx.files.internal("sprites.png"));

        tiles = TextureRegion.split(gameTexture, TILE_SIZE, TILE_SIZE);


        floor = tiles[2][0];
        whitePixel = new TextureRegion(gameTexture, 11,19,1,1);

        font = new BitmapFont(Gdx.files.internal("kongtext.fnt"));

        monsterSprites.put(MonsterType.Player, tiles[4][0]);
        monsterSprites.put(MonsterType.Crawler, tiles[5][1]);
        monsterSprites.put(MonsterType.Stealer, tiles[5][3]);
        monsterSprites.put(MonsterType.Brute, tiles[6][2]);
        monsterSprites.put(MonsterType.Egg, tiles[6][6]);
        monsterSprites.put(MonsterType.Cannon, tiles[6][4]);
        monsterSprites.put(MonsterType.Grower, tiles[5][5]);
        monsterSprites.put(MonsterType.Robot, tiles[6][0]);

        itemSprites.put(ItemType.Key, tiles[3][3]);
        itemSprites.put(ItemType.Ammo, tiles[3][4]);
        itemSprites.put(ItemType.AmmoCrate, tiles[3][5]);
        itemSprites.put(ItemType.Waste, tiles[3][6]);
        itemSprites.put(ItemType.Rocket, tiles[3][1]);
        itemSprites.put(ItemType.Heart, tiles[3][2]);
        itemSprites.put(ItemType.Adrenaline, tiles[3][7]);

        bulletSprites.put(ItemType.Ammo, tiles[4][2]);
        bulletSprites.put(ItemType.Rocket, tiles[4][3]);


        exitFrames = new TextureRegion[] {tiles[8][1], tiles[8][2], tiles[8][3], tiles[8][2], tiles[8][1], tiles[8][0]};
        teleporterFrames = new TextureRegion[] {tiles[9][0], tiles[9][1], tiles[9][2], tiles[9][3]};

        progressBarLeft = new TextureRegion(gameTexture, 0, 104, 1, 3);
        progressBarCenter = new TextureRegion(gameTexture, 1, 104, 1, 3);
        progressBarRight = new TextureRegion(gameTexture, 2, 104, 1, 3);


        logo = new TextureRegion(gameTexture, 0, 88, 47, 15);

        sounds.create();

        autoTileSet = new TextureRegion[] {
                tiles[0][0],
                tiles[1][1],
                tiles[0][2],
                tiles[1][3],
                tiles[0][4],
                tiles[1][5],
                tiles[0][6],
                tiles[1][7],
                tiles[0][8],
                tiles[1][9],
                tiles[0][10],
                tiles[1][11],
                tiles[0][12],
                tiles[1][13],
                tiles[0][14],
                tiles[1][15],
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
