package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static final int TILE_SIZE = 8;

    public Texture tilesTexture;
    public TextureRegion[][] tiles;
    public BitmapFont font;

    public TextureRegion wall;
    public TextureRegion floor;

    public TextureRegion whitePixel;

    public void create() {
        tilesTexture = new Texture(Gdx.files.internal("tiles.png"));

        tiles = TextureRegion.split(tilesTexture, TILE_SIZE, TILE_SIZE);

        wall = tiles[2][0];
        floor = tiles[0][8];

        whitePixel = new TextureRegion(tilesTexture, 18,18,1,1);

        font = new BitmapFont(Gdx.files.internal("kongtext.fnt"));
    }


}
