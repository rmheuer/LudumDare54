package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.render.texture.Texture;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.render2d.DrawList2D;

import java.util.Arrays;

public final class TileMap {
    public static final int WIDTH = 4 * 6;
    public static final int HEIGHT = 3 * 6;

    private final Tile[] tiles;

    public TileMap() {
        tiles = new Tile[WIDTH * HEIGHT];
        Arrays.fill(tiles, Tile.EMPTY);
    }

    public Tile getTile(int x, int y) {
        return tiles[x + y * WIDTH];
    }

    public void setTile(int x, int y, Tile tile) {
        tiles[x + y * WIDTH] = tile;
    }

    public void render(DrawList2D draw) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Texture2DRegion tex = getTile(x, y).getTexture();
                if (tex != null)
                    draw.drawImage(x, y, 1, 1, getTile(x, y).getTexture(), 0, 0, 1, 1);
            }
        }
    }

    public boolean collides(float x, float y, float w, float h) {
        int minX = (int) Math.floor(x);
        int maxX = (int) Math.ceil(x + w);
        int minY = (int) Math.floor(y);
        int maxY = (int) Math.ceil(y + h);
        for (int j = minY; j < maxY; j++) {
            for (int i = minX; i < maxX; i++) {
                if (getTile(i, j).isSolid())
                    return true;
            }
        }
        return false;
    }
}
