package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.input.keyboard.Key;
import com.github.rmheuer.engine.input.keyboard.Keyboard;
import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.render2d.DrawList2D;
import org.joml.Vector2f;

import java.io.IOException;

public final class Player {
    private final Texture2DRegion texture;
    private final Vector2f position;

    public Player(Renderer r) throws IOException {
        texture = r.createTexture2D(ResourceUtil.readAsStream("idk.png")).getFlippedX();
        position = new Vector2f(4, 4);
    }

    float moveY = 0;
    public void control(float delta, Keyboard kb) {
        float moveX = 0;
        if (kb.isKeyPressed(Key.RIGHT)) moveX += 10;
        if (kb.isKeyPressed(Key.LEFT)) moveX -= 10;

        if (position.y <= 2) {
            moveY = 0;
            if (kb.isKeyPressed(Key.UP))
                moveY = 20;
        } else if (moveY > -10) {
            moveY -= 1.5f; // gravity
        }

        position.x += moveX * delta;
        position.y += moveY * delta;

//        accel.set(0, -3);
//        double moveAcc = delta * 300;
//        if (kb.isKeyPressed(Key.LEFT)) accel.x -= moveAcc;
//        if (kb.isKeyPressed(Key.RIGHT)) accel.x += moveAcc;
//        if (kb.isKeyPressed(Key.DOWN)) accel.y -= moveAcc;
//        if (kb.isKeyPressed(Key.UP)) accel.y += moveAcc;
//
//        position.fma(0.5f * delta, velocity);
//        velocity.mul(1 - (0.1f * delta));
//        velocity.fma(delta, accel);
//        position.fma(0.5f * delta, velocity);
    }

    public void render(DrawList2D draw) {
        draw.drawImage(position, 1, 1, texture, 0, 0, 1, 1);
    }
}
