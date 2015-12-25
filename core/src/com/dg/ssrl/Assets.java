package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by magnus on 2015-10-04.
 */
public class Assets {

    public static final int TILE_SIZE = 8;

    private Texture tilesTexture;
    public TextureRegion[][] tiles;
    public BitmapFont font;

    public void create() {
        tilesTexture = new Texture(Gdx.files.internal("tiles.png"));

        tiles = TextureRegion.split(tilesTexture, TILE_SIZE, TILE_SIZE);

        font = new BitmapFont(Gdx.files.internal("kongtext.fnt"));
    }


}
