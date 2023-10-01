package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.math.PoseStack;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.render2d.DrawList2D;
import com.github.rmheuer.engine.render2d.Rectangle;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Level {
    public static final int SIZE = 4 * 6;

    private final Tile[] tiles;
    private GravityDir gravity;
    private Player player;

    private Bitmap nextLayout;
    private float appearedness;

    private final List<Entity> entities;

    private int currentLevel;

    public Level() {
        tiles = new Tile[SIZE * SIZE];
        gravity = GravityDir.DOWN;

        Arrays.fill(tiles, Tile.EMPTY);

        appearedness = -1;
        entities = new ArrayList<>();

        currentLevel = 0;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private boolean aboutEqual(float a, float b) {
        return Math.abs(a - b) < 0.1;
    }

    private boolean aboutEqual(ColorRGBA color, float r, float g, float b) {
        return aboutEqual(color.getRed(), r) &&
                aboutEqual(color.getGreen(), g) &&
                aboutEqual(color.getBlue(), b);
    }

    private void applyLayout(Bitmap layout) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                ColorRGBA pixel = layout.getPixel(x, SIZE - y - 1);
                Tile tile = null;
                if (pixel.getAlpha() < 0.1) {
                    tile = Tile.EMPTY;
                } else if (aboutEqual(pixel, 0.5f, 0.5f, 0.5f)) {
                    tile = Tile.SPIKE;
                } else if (aboutEqual(pixel, 1.0f, 225/255f, 0.0f)) {
                    tile = Tile.BACKGROUND;
                } else if (aboutEqual(pixel, 0.0f, 1.0f, 0.074f)) {
                    tile = Tile.SOLID;
                } else if (aboutEqual(pixel, 0.0f, 1.0f, 1.0f)) {
                    tile = Tile.BACKGROUND;
                    addEntity(new Box(this, x, y));
                } else if (aboutEqual(pixel, 0.0f, 98 / 255f, 1.0f)) {
                    tile = Tile.BACKGROUND;
                    player.position.set(x, y);
                } else if (aboutEqual(pixel, 1.0f, 0.0f, 0.0f)) {
                    tile = Tile.GOAL;
                } else if (aboutEqual(pixel, 0.5f, 0.0f, 1.0f)) {
                    tile = Tile.CUBE_WIRE_OFF;
                } else if (aboutEqual(pixel, 0.5f, 0.0f, 0.5f)) {
                    tile = Tile.CUBE_DOOR_CLOSED;
                } else if (aboutEqual(pixel, 1.0f, 0.0f, 1.0f)) {
                    tile = Tile.CUBE_SENSOR;
                } else {
                    tile = Tile.EMPTY;
//                    throw new RuntimeException("Invalid tile color mapping: " + pixel);
                }

                setTile(x, y, tile);
            }
        }
    }

    public void load(Bitmap nextLevel) {
        nextLayout = nextLevel;
        appearedness = -1;
        entities.clear();
        entities.add(player);
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

        if (appearedness == 1) {
            for (Entity e : entities) {
                e.tick(dt);
            }

            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    getTile(x, y).tick(this, x, y);
                }
            }

            Rectangle bb = player.getCollisionBox();
            int minX = (int) Math.floor(bb.getMin().x);
            int maxX = (int) Math.ceil(bb.getMax().x);
            int minY = (int) Math.floor(bb.getMin().y);
            int maxY = (int) Math.ceil(bb.getMax().y);
            outer: for (int j = minY; j < maxY; j++) {
                for (int i = minX; i < maxX; i++) {
                    if (i < 0 || i >= SIZE || j < 0 || j >= SIZE) {
                        // out of bounds, player loses
                        LudumDare54.INSTANCE.playerDied();
                        break outer;
                    }

                    if (getTile(i, j) == Tile.SPIKE) {
                        // touch spike oh no
                        LudumDare54.INSTANCE.playerDied();
                        break outer;
                    } else if (getTile(i, j) == Tile.GOAL) {
                        LudumDare54.INSTANCE.switchToLevel(++currentLevel, true);
                    }
                }
            }
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

        for (Entity e : entities) {
            e.render(draw);
        }
    }

    public List<Entity> getTouchingEntities(int tileX, int tileY) {
        List<Entity> out = new ArrayList<>();
        for (Entity e : entities) {
            Rectangle bb = e.getBoundingBox();
            if (tileX >= (int) Math.floor(bb.getMin().x) && tileY >= (int) Math.floor(bb.getMin().y) &&
                tileX <= (int) Math.ceil(bb.getMax().x) && tileY <= (int) Math.ceil(bb.getMax().y)) {
                out.add(e);
            }
        }
        return out;
    }

    public boolean collides(float x, float y, float w, float h, Entity ignore) {
        boolean player = ignore instanceof Player;
        int minX = (int) Math.floor(x);
        int maxX = (int) Math.ceil(x + w);
        int minY = (int) Math.floor(y);
        int maxY = (int) Math.ceil(y + h);
        for (int j = minY; j < maxY; j++) {
            for (int i = minX; i < maxX; i++) {
                if (i < 0 || i >= SIZE || j < 0 || j >= SIZE)
                    return !player;
                if (getTile(i, j).isSolid())
                    return !player || getTile(i, j) != Tile.SPIKE;
            }
        }
        for (Entity e : entities) {
            if (e == ignore)
                continue;
            Rectangle bb = e.getBoundingBox();
            if (x <= bb.getMax().x && x + w >= bb.getMin().x
                    && y <= bb.getMax().y && y + h >= bb.getMin().y
                ) {
                return true;
            }
        }
        return false;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }
}
