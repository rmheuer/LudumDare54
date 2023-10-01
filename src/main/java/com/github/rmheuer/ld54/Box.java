package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render2d.DrawList2D;

import java.io.IOException;

public final class Box extends Entity {
    private static Texture2D texture;

    public static void load(Renderer r) throws IOException {
        texture = r.createTexture2D(ResourceUtil.readAsStream("box.png"));
    }

    public Box(Level level, float x, float y) {
        super(level);
        position.set(x, y);
    }

    @Override
    public void render(DrawList2D draw) {
        draw.drawImage(position.x, position.y, 1, 1, texture.getFlippedX(), 0, 0, 1, 1);
    }
}
