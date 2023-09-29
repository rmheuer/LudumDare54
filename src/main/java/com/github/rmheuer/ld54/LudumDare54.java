package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.WindowSettings;
import com.github.rmheuer.engine.render.camera.Camera;
import com.github.rmheuer.engine.render.camera.ScaledOrthoProjection;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render2d.DrawList2D;
import com.github.rmheuer.engine.render2d.Rectangle;
import com.github.rmheuer.engine.render2d.Renderer2D;
import com.github.rmheuer.engine.runtime.BaseGame;
import com.github.rmheuer.engine.runtime.EngineRuntime;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.io.IOException;

public final class LudumDare54 extends BaseGame {
    private final Renderer2D render2d;
    private final Camera camera;

    private final TileMap tileMap;

    public LudumDare54() throws IOException {
        super(new WindowSettings(
                TileMap.WIDTH * Tile.TILE_SIZE_PX * 3,
                TileMap.HEIGHT * Tile.TILE_SIZE_PX * 3,
                "Ludum Dare 54"));
        render2d = new Renderer2D(getRenderer());

        setBackgroundColor(ColorRGBA.black());

        camera = new Camera(new ScaledOrthoProjection(ScaledOrthoProjection.ScaleMode.FIT, TileMap.WIDTH, TileMap.HEIGHT, -1, 1));
        camera.getTransform().position.set(TileMap.WIDTH / 2f, TileMap.HEIGHT / 2f, 0);

        Tile.init(getRenderer());
        tileMap = new TileMap();

        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * TileMap.WIDTH);
            int y = (int) (Math.random() * TileMap.HEIGHT);
            tileMap.setTile(x, y, Tile.SOLID);
        }
    }

    @Override
    protected void tick(float dt) {

    }

    @Override
    protected void render(Renderer renderer) {
        DrawList2D draw = new DrawList2D();

        tileMap.render(draw);

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
