package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.input.keyboard.Key;
import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import org.joml.Vector2i;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

public enum Tile {
    // Index is the frame number in Piskel
    EMPTY(false, 0), // Empty tile (shows space behind)
    BACKGROUND(false, 2),
    SOLID(true, 1),
    GOAL(false, 3, 4, 5),
    SPIKE(true, 6),
    CUBE_SENSOR(false, 7) {
        @Override
        public void tick(Level level, int x, int y) {
//            boolean activated = LudumDare54.INSTANCE.getWindow().getKeyboard().isKeyPressed(Key.U);
            boolean activated = false;
            for (Entity e : level.getTouchingEntities(x, y)) {
                if (e instanceof Box) {
                    activated = true;
                    break;
                }
            }

            Set<Vector2i> queue = new HashSet<>();
            queue.add(new Vector2i(x, y));
            boolean anyEffect = false;
            while (!queue.isEmpty()) {
                Iterator<Vector2i> iter = queue.iterator();
                Vector2i pos = iter.next();
                iter.remove();

                if (pos.x < 0 || pos.x >= Level.SIZE || pos.y < 0 || pos.y >= Level.SIZE)
                    continue;

                Tile tile = level.getTile(pos.x, pos.y);
                boolean propagate = true;
                if (activated) {
                    if (tile == Tile.CUBE_WIRE_OFF) {
                        level.setTile(pos.x, pos.y, Tile.CUBE_WIRE_ON);
                    } else if (tile == Tile.CUBE_DOOR_CLOSED) {
                        level.setTile(pos.x, pos.y, Tile.CUBE_DOOR_OPEN);
                    } else if (tile != Tile.CUBE_SENSOR) {
                        propagate = false;
                    }
                } else {
                    if (tile == Tile.CUBE_WIRE_ON) {
                        level.setTile(pos.x, pos.y, Tile.CUBE_WIRE_OFF);
                    } else if (tile == Tile.CUBE_DOOR_OPEN) {
                        level.setTile(pos.x, pos.y, Tile.CUBE_DOOR_CLOSED);
                    } else if (tile != Tile.CUBE_SENSOR) {
                        propagate = false;
                    }
                }

                if (propagate) {
                    if (tile != Tile.CUBE_SENSOR)
                        anyEffect = true;
                    queue.add(new Vector2i(pos).add(1, 0));
                    queue.add(new Vector2i(pos).add(0, 1));
                    queue.add(new Vector2i(pos).add(-1, 0));
                    queue.add(new Vector2i(pos).add(0, -1));
                }
            }

            if (anyEffect)
                LudumDare54.INSTANCE.playSensorSound(activated);
        }
    },
    CUBE_WIRE_OFF(true, 8),
    CUBE_WIRE_ON(true, 9, 10, 11, 10),
    CUBE_DOOR_CLOSED(true, 12),
    CUBE_DOOR_OPEN(false, 13);

    public static void tick(float dt) {
        for (Tile tile : values())
            tile.anim.tick(dt);
    }

    public static final int TILE_SIZE_PX = 16;

    private final int[] idx;
    private Animation anim;
//    private Texture2DRegion texture;
    private final boolean solid;

    public static void init(Renderer renderer) throws IOException {
        Bitmap bitmap = Bitmap.decode(ResourceUtil.readAsStream("tiles2-3.png"));
        int tilesX = bitmap.getWidth() / TILE_SIZE_PX;
        int tilesY = bitmap.getHeight() / TILE_SIZE_PX;

        Texture2D texture = renderer.createTexture2D();
        texture.setData(bitmap);

        float sizeX = 1.0f / tilesX;// - 0.0001f;
        float sizeY = 1.0f / tilesY;// - 0.0001f;
        for (Tile tile : values()) {
            Texture2DRegion[] frames = new Texture2DRegion[tile.idx.length];
            for (int i = 0; i < frames.length; i++) {
                int tileIdx = tile.idx[i] - 1;
                if (tileIdx < 0)
                    continue;

                int col = tileIdx % tilesX;
                int row = tileIdx / tilesX;

                float x = col / (float) tilesX;
                float y = row / (float) tilesY;

                frames[i] = texture.getSubRegion(x, y, x + sizeX, y + sizeY).getFlippedX();
            }
            tile.anim = new Animation(null, frames, 10);
        }
    }

    Tile(boolean solid, int... idx) {
        this.idx = idx;
        this.solid = solid;
    }

    public Texture2DRegion getTexture() {
        return anim.getCurrentFrameTexture();
    }

    public boolean isSolid() {
        return solid;
    }

    public void tick(Level level, int x, int y) {}
}
