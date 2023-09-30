package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;

import java.io.IOException;

public enum Tile {
    // Index is the frame number in Piskel
    EMPTY(0, false), // Empty tile (shows space behind)
    BACKGROUND(1, false),
    SOLID(2, true);

    public static final int TILE_SIZE_PX = 16;

    private final int idx;
    private Texture2DRegion texture;
    private final boolean solid;

    public static void init(Renderer renderer) throws IOException {
        Bitmap bitmap = Bitmap.decode(ResourceUtil.readAsStream("tiles.png"));
        int tilesX = bitmap.getWidth() / TILE_SIZE_PX;
        int tilesY = bitmap.getHeight() / TILE_SIZE_PX;

        Texture2D texture = renderer.createTexture2D();
        texture.setData(bitmap);

        float sizeX = 1.0f / tilesX - 0.0001f;
        float sizeY = 1.0f / tilesY - 0.0001f;
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

    Tile(int idx, boolean solid) {
        this.idx = idx - 1;
        this.solid = solid;
    }

    public Texture2DRegion getTexture() {
        return texture;
    }

    public boolean isSolid() {
        return solid;
    }
}
