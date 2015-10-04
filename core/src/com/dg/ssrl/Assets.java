package com.dg.ssrl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by magnus on 2015-10-04.
 */
public class Assets {

    private Texture tilesTexture;
    public TextureRegion[][] tiles;
    public void create() {
        tilesTexture = new Texture(Gdx.files.internal("tiles.png"));

        tiles = TextureRegion.split(tilesTexture, 8, 8);
    }


}
