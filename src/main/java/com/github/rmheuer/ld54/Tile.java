package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;

import java.io.IOException;

public enum Tile {
    // Index is the frame number in Piskel
    EMPTY(1),
    SOLID(2);

    public static final int TILE_SIZE_PX = 16;

    private final int idx;
    private Texture2DRegion texture;

    public static void init(Renderer renderer) throws IOException {
        Bitmap bitmap = Bitmap.decode(ResourceUtil.readAsStream("tiles.png"));
        int tilesX = bitmap.getWidth() / TILE_SIZE_PX;
        int tilesY = bitmap.getHeight() / TILE_SIZE_PX;

        Texture2D texture = renderer.createTexture2D();
        texture.setData(bitmap);

        float sizeX = 1.0f / tilesX;
        float sizeY = 1.0f / tilesY;
        for (Tile tile : values()) {
            int tileIdx = tile.idx;
            if (tileIdx < 0)
                continue;

            int col = tileIdx % tilesX;
            int row = tileIdx / tilesX;

            float x = col / (float) tilesX;
            float y = row / (float) tilesY;
            tile.texture = texture.getSubRegion(x, y, x + sizeX, y + sizeY).getFlippedX();
//            tile.texture = texture.getSubRegion(0, 0, 1, 1);
        }
    }

    Tile(int idx) {
        this.idx = idx - 1;
    }

    public Texture2DRegion getTexture() {
        return texture;
    }
}
