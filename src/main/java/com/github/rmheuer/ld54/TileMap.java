package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.render2d.DrawList2D;

import java.util.Arrays;

public final class TileMap {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 12;

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
                draw.drawImage(x, y, 1, 1, getTile(x, y).getTexture(), 0, 0, 1, 1);
            }
        }
    }
}
