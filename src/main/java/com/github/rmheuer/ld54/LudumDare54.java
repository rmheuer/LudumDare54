package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.audio.PlayOptions;
import com.github.rmheuer.engine.audio.data.AudioSample;
import com.github.rmheuer.engine.event.EventHandler;
import com.github.rmheuer.engine.event.Listener;
import com.github.rmheuer.engine.input.keyboard.Key;
import com.github.rmheuer.engine.input.keyboard.KeyPressEvent;
import com.github.rmheuer.engine.input.keyboard.Keyboard;
import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.math.MathUtil;
import com.github.rmheuer.engine.math.PoseStack;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.WindowSettings;
import com.github.rmheuer.engine.render.camera.Camera;
import com.github.rmheuer.engine.render.camera.ScaledOrthoProjection;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render2d.DrawList2D;
import com.github.rmheuer.engine.render2d.Renderer2D;
import com.github.rmheuer.engine.runtime.BaseGame;
import com.github.rmheuer.engine.runtime.EngineRuntime;
import com.github.rmheuer.engine.runtime.FixedRateExecutor;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.IOException;

public final class LudumDare54 extends BaseGame implements Listener {
    private final Renderer2D render2d;
    private final Camera camera;
    private final FixedRateExecutor update;

    private final Level level;
    private final Player player;

    private final AudioSample gravityChangeSound;

    public LudumDare54() throws IOException {
        super(new WindowSettings(
                Level.SIZE * Tile.TILE_SIZE_PX * 2,
                Level.SIZE * Tile.TILE_SIZE_PX * 2,
                "Fancy Boots"));
        render2d = new Renderer2D(getRenderer());
        update = new FixedRateExecutor(1 / 60.0f, this::fixedTick);

        setBackgroundColor(ColorRGBA.black());

        camera = new Camera(new ScaledOrthoProjection(ScaledOrthoProjection.ScaleMode.FIT, Level.SIZE, Level.SIZE, -1, 1));
        camera.getTransform().position.set(Level.SIZE / 2f, Level.SIZE / 2f, 0);

        Tile.init(getRenderer());
        level = new Level();

//        for (int x = 0; x < Level.SIZE; x++) {
//            level.setTile(x, 0, Tile.SOLID);
//            level.setTile(x, Level.SIZE - 1, Tile.SOLID);
//        }
//        for (int y = 0; y < Level.SIZE; y++) {
//            level.setTile(0, y, Tile.SOLID);
//            level.setTile(Level.SIZE - 1, y, Tile.SOLID);
//        }

//        for (int i = 0; i < 80; i++) {
//            int x = (int) (Math.random() * Level.SIZE);
//            int y = (int) (Math.random() * Level.SIZE);
//            level.setTile(x, y, Tile.SOLID);
//        }

        player = new Player(level, getRenderer());

        getEventBus().registerListener(this);

        gravityChangeSound = getAudioSystem().createSample(ResourceUtil.readAsStream("better_gravity.ogg"));
    }

    private void fixedTick(float dt) {
        if (!level.isTransitioning())
            player.control(dt, getWindow().getKeyboard());
    }

    @Override
    protected void tick(float dt) {
        level.tick(dt);
        update.update(dt);
    }

//    private final GravityDir[] dirSequence = {GravityDir.DOWN, GravityDir.RIGHT, GravityDir.UP, GravityDir.LEFT};
//    int dirIdx = 0;

    boolean whichMap = true;
    @EventHandler
    public void onKeyPress(KeyPressEvent event) {
        if (event.getKey() == Key.SPACE) {
            level.setGravity(player.getFacing());
            getAudioSystem().play(PlayOptions
                    .play2D(gravityChangeSound)
                    .setPitch(0.6f + 0.8f * (float) Math.random()));
//            dirIdx++;
//            dirIdx %= dirSequence.length;
//            level.setGravity(dirSequence[dirIdx]);
        } else if (event.getKey() == Key.Q) {
            try {
                level.load(Bitmap.decode(ResourceUtil.readAsStream(whichMap ? "map2.png" : "map.png")));
                whichMap = !whichMap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void render(Renderer renderer) {
        DrawList2D draw = new DrawList2D();
        PoseStack poseStack = draw.getPoseStack();

        level.render(draw);
        player.render(draw);
//        for (int depth = 9; depth >= 0; depth--) {
//            poseStack.push();
//            poseStack.stack.scale(1 - depth * 0.008f);
//            poseStack.stack.translate(-TileMap.WIDTH / 2f, -TileMap.HEIGHT / 2f, 0);
//            tileMap.render(draw);
//            poseStack.pop();
//        }

        Vector2i size = getWindow().getFramebufferSize();

        render2d.draw(draw, new Matrix4f(), camera.getProjectionMatrix(size.x, size.y), camera.getViewMatrix());
    }

    @Override
    protected void cleanUp() {
        render2d.close();
    }

    public static void main(String[] args) {
        if (EngineRuntime.restartForMacOS(args))
            return;

        try {
            new LudumDare54().run();
        } catch (IOException e) {
            System.err.println("Failed to load assets");
            e.printStackTrace();
        }
    }
}
