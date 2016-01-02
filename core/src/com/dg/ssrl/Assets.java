package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class Assets {

    public static final int TILE_SIZE = 8;

    public Texture tilesTexture;
    public TextureRegion[][] tiles;
    public BitmapFont font;

    public TextureRegion wallSolid;

    public TextureRegion wall;
    public TextureRegion floor;

    public TextureRegion whitePixel;

    public TextureRegion key;
    public TextureRegion egg;

    private Map<MonsterType, TextureRegion> monsterSprites = new HashMap<MonsterType, TextureRegion>();

    public void create() {
        tilesTexture = new Texture(Gdx.files.internal("tiles.png"));

        tiles = TextureRegion.split(tilesTexture, TILE_SIZE, TILE_SIZE);

        wallSolid = tiles[2][0];
        wall = tiles[2][1];
        floor = tiles[0][8];

        whitePixel = new TextureRegion(tilesTexture, 18,18,1,1);

        font = new BitmapFont(Gdx.files.internal("kongtext.fnt"));

        monsterSprites.put(MonsterType.Snake, tiles[5][1]);
        monsterSprites.put(MonsterType.Rat, tiles[7][1]);
        monsterSprites.put(MonsterType.Egg, tiles[11][0]);

        key = tiles[9][0];
    }

    public TextureRegion getMonsterTextureRegion(MonsterType monsterType) {
        return monsterSprites.get(monsterType);
    }

}
