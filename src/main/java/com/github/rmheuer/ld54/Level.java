package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.math.PoseStack;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.render2d.DrawList2D;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.Arrays;

public final class Level {
    public static final int SIZE = 4 * 6;

    private final Tile[] tiles;
    private GravityDir gravity;

    private Bitmap nextLayout;
    private float appearedness;

    public Level() throws IOException {
        Bitmap layout = Bitmap.decode(ResourceUtil.readAsStream("map.png"));

        tiles = new Tile[SIZE * SIZE];
        gravity = GravityDir.DOWN;

        appearedness = -1;

        applyLayout(layout);
    }

    private void applyLayout(Bitmap layout) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                ColorRGBA pixel = layout.getPixel(x, SIZE - y - 1);
                if (pixel.getAlpha() < 0.1) {
                    setTile(x, y, Tile.EMPTY);
                } else if (pixel.getRed() > 0.5) {
                    setTile(x, y, Tile.BACKGROUND);
                } else {
                    setTile(x, y, Tile.SOLID);
                }
            }
        }
    }

    public void load(Bitmap nextLevel) {
        nextLayout = nextLevel;
        appearedness = -1;
    }

    public boolean isTransitioning() {
        return appearedness != 1;
    }

    public GravityDir getGravity() {
        return gravity;
    }

    public void setGravity(GravityDir dir) {
        gravity = dir;
    }

    public Tile getTile(int x, int y) {
        return tiles[x + y * SIZE];
    }

    public void setTile(int x, int y, Tile tile) {
        tiles[x + y * SIZE] = tile;
    }

    public void tick(float dt) {
        float rate = 3 * dt;
        if (nextLayout != null) {
            appearedness += rate;
            if (appearedness > 0) {
                appearedness = 0;
                applyLayout(nextLayout);
                nextLayout = null;
            }
        } else if (appearedness < 1) {
            appearedness += rate;
            if (appearedness > 1)
                appearedness = 1;
        }
    }

    public void render(DrawList2D draw) {
        PoseStack pose = draw.getPoseStack();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pose.stack.pushMatrix();
                pose.stack.translate(x + 0.5f, y + 0.5f, 0);
                pose.stack.rotateZ((float) ((1 - appearedness) * Math.PI / 2));
                pose.stack.scale(Math.abs(appearedness));
                Texture2DRegion tex = getTile(x, y).getTexture();
                if (tex != null)
                    draw.drawImage(-0.5f, -0.5f, 1, 1, getTile(x, y).getTexture(), 0, 0, 1, 1);
                pose.stack.popMatrix();
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
