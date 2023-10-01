package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.audio.PlayOptions;
import com.github.rmheuer.engine.audio.data.AudioSample;
import com.github.rmheuer.engine.audio.play.PlayingSound;
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
import com.github.rmheuer.engine.render.camera.ConstantOrthoProjection;
import com.github.rmheuer.engine.render.camera.ScaledOrthoProjection;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render2d.DrawList2D;
import com.github.rmheuer.engine.render2d.Rectangle;
import com.github.rmheuer.engine.render2d.Renderer2D;
import com.github.rmheuer.engine.render2d.font.Font;
import com.github.rmheuer.engine.render2d.font.TrueTypeFont;
import com.github.rmheuer.engine.runtime.BaseGame;
import com.github.rmheuer.engine.runtime.EngineRuntime;
import com.github.rmheuer.engine.runtime.FixedRateExecutor;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.IOException;

public final class LudumDare54 extends BaseGame implements Listener {
    public static LudumDare54 INSTANCE;

    private final Renderer2D render2d;
    private final Camera camera;
    private final FixedRateExecutor update;

    private final Level level;
    private final Player player;
    private final SpaceBackground background;

    private final AudioSample gravityChangeSound;
    private final AudioSample levelSwitchSound;
    private final AudioSample outOfFuelSound;
    private final AudioSample sensorActivatedSound;
    private final AudioSample sensorDeactivatedSound;
    private final AudioSample playerDieSound;
    private final AudioSample jumpSound;
    private PlayingSound music;

    private final Font pixelFont;
    private int spaceLeft = 10;

    public LudumDare54() throws IOException {
        super(new WindowSettings(
                Level.SIZE * Tile.TILE_SIZE_PX * 2,
                Level.SIZE * Tile.TILE_SIZE_PX * 2,
                "Fancy Boots")
                .setFullScreen(true));
        INSTANCE = this;
        render2d = new Renderer2D(getRenderer());
        update = new FixedRateExecutor(1 / 60.0f, this::fixedTick);

        setBackgroundColor(ColorRGBA.black());

        camera = new Camera(new ScaledOrthoProjection(ScaledOrthoProjection.ScaleMode.FIT, Level.SIZE, Level.SIZE, -1, 1));
        camera.getTransform().position.set(Level.SIZE / 2f, Level.SIZE / 2f, 0);

        Tile.init(getRenderer());
        Box.load(getRenderer());
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
        level.addEntity(player);
        level.setPlayer(player);

        getEventBus().registerListener(this);

        gravityChangeSound = getAudioSystem().createSample(ResourceUtil.readAsStream("better_gravity.ogg"));
        levelSwitchSound = getAudioSystem().createSample(ResourceUtil.readAsStream("level switch.ogg"));
        outOfFuelSound = getAudioSystem().createSample(ResourceUtil.readAsStream("out_of_fuel.ogg"));
        sensorActivatedSound = getAudioSystem().createSample(ResourceUtil.readAsStream("what.ogg"));
        sensorDeactivatedSound = getAudioSystem().createSample(ResourceUtil.readAsStream("woo.ogg"));
        playerDieSound = getAudioSystem().createSample(ResourceUtil.readAsStream("die.ogg"));
        jumpSound = getAudioSystem().createSample(ResourceUtil.readAsStream("jump.ogg"));

        pixelFont = new TrueTypeFont(getRenderer(), ResourceUtil.readAsStream("Pixelated-0.ttf"), 16);

        boxTexture = getRenderer().createTexture2D(ResourceUtil.readAsStream("box.png"));

        background = new SpaceBackground(level);

        level.load(Bitmap.decode(ResourceUtil.readAsStream("levels/level00.png")));
    }
    final Texture2D boxTexture;

    private void fixedTick(float dt) {
        if (!level.isTransitioning() && level.getCurrentLevel() != 0)
            player.control(dt, getWindow().getKeyboard());
    }

    @Override
    protected void tick(float dt) {
        Tile.tick(dt);
        background.tick(dt);
        update.update(dt);
        level.tick(dt);

        if (music == null || !music.isPlaying()) {
            try {
                music = getAudioSystem().play(PlayOptions.play2D(
                        getAudioSystem()
                                .createStream(ResourceUtil.readAsStream("song.ogg"))));
                System.out.println("Music restarted");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private final GravityDir[] dirSequence = {GravityDir.DOWN, GravityDir.RIGHT, GravityDir.UP, GravityDir.LEFT};
//    int dirIdx = 0;

    public void switchToLevel(int level, boolean playSound) {
        String levelFile = String.format("levels/level%02d.png", level);
        try {
            this.level.load(Bitmap.decode(ResourceUtil.readAsStream(levelFile)));
            this.level.setCurrentLevel(level);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (playSound)
            getAudioSystem().play(PlayOptions
                        .play2D(levelSwitchSound));

        spaceLeft = 10;
    }

    public void playerDied() {
        switchToLevel(level.getCurrentLevel(), false);
        getAudioSystem().play(PlayOptions.play2D(playerDieSound));
    }

    public void playSensorSound(boolean activated) {
        getAudioSystem().play(PlayOptions.play2D(activated ? sensorActivatedSound : sensorDeactivatedSound));
    }

    public void playJumpSound() {
        getAudioSystem().play(PlayOptions.play2D(jumpSound).setPitch(0.6f + 0.8f * (float) Math.random()));
    }

    boolean whichMap = true;
    @EventHandler
    public void onKeyPress(KeyPressEvent event) {
        if (event.getKey() == Key.SPACE) {
            if (level.getCurrentLevel() == 0) {
                switchToLevel(1, true);
                return;
            }

            if (spaceLeft > 0) {
                getAudioSystem().play(PlayOptions
                        .play2D(gravityChangeSound)
                        .setPitch(0.6f + 0.8f * (float) Math.random()));
                spaceLeft--;
                level.setGravity(player.getFacing());
            } else {
                getAudioSystem().play(PlayOptions.play2D(outOfFuelSound));
            }
//            dirIdx++;
//            dirIdx %= dirSequence.length;
//            level.setGravity(dirSequence[dirIdx]);
        } else if (event.getKey() == Key.R && level.getCurrentLevel() != 0) {
            switchToLevel(level.getCurrentLevel(), true);
        } else if (event.getKey() == Key.ESCAPE) {
            stop();
        }
    }

    @Override
    protected void render(Renderer renderer) {
        DrawList2D draw = new DrawList2D();
        PoseStack poseStack = draw.getPoseStack();

        background.render(draw);
        level.render(draw);
        if (level.getCurrentLevel() != 0)
            player.render(draw);
//        for (int depth = 9; depth >= 0; depth--) {
//            poseStack.push();
//            poseStack.stack.scale(1 - depth * 0.008f);
//            poseStack.stack.translate(-TileMap.WIDTH / 2f, -TileMap.HEIGHT / 2f, 0);
//            tileMap.render(draw);
//            poseStack.pop();
//        }

        Vector2i size = getWindow().getFramebufferSize();
        Vector2i virtualSize = getWindow().getSize();

        render2d.draw(draw, new Matrix4f(), camera.getProjectionMatrix(size.x, size.y), camera.getViewMatrix());

        DrawList2D screenDraw = new DrawList2D();
        screenDraw.getPoseStack().stack.scale(1, -1, 1);

        float textOffset = 5;
        String left = "SPACE LEFT: " + spaceLeft;
        if (spaceLeft == 0) {
            left += " (PRESS R TO RESTART)";
        }

        if (level.getCurrentLevel() == 0) {
            textOffset = virtualSize.y / 4f;
            left = "PRESS SPACE TO START";
        }

        float width = pixelFont.textWidth(left);
        screenDraw.fillQuad(Rectangle.fromCenterSizes(0, virtualSize.y / 2f - textOffset - 8, width + 4, 20), new ColorRGBA(0, 0, 0, 0.6f));
        screenDraw.drawText(left, 2, virtualSize.y / 2f - textOffset + 2, 0.5f, 1f, pixelFont, ColorRGBA.black());
        screenDraw.drawText(left, 0, virtualSize.y / 2f - textOffset, 0.5f, 1f, pixelFont, ColorRGBA.white());

        ConstantOrthoProjection screenProj = new ConstantOrthoProjection(1, -1, 1);
        render2d.draw(screenDraw, new Matrix4f(), screenProj.getMatrix(virtualSize.x, virtualSize.y), new Matrix4f());
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
