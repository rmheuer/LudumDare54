package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.input.keyboard.Key;
import com.github.rmheuer.engine.input.keyboard.Keyboard;
import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.math.MathUtil;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.render2d.DrawList2D;
import org.joml.Vector2f;

import java.io.IOException;

public final class Player {
    private final Level level;
//    private final Texture2DRegion texture;
    private final Animation anim;
    private final Vector2f position;

    public Player(Level level, Renderer r) throws IOException {
        this.level = level;
        anim = Animation.fromSpriteSheetVertical(r, ResourceUtil.readAsStream("dude.png"), 12);
//        texture = r.createTexture2D(ResourceUtil.readAsStream("whatever-this-is.png")).getFlippedX();
        position = new Vector2f(4, 4);
    }

    final Vector2f vel = new Vector2f(0, 0);
    boolean onGround = false;
    boolean flipped = false;

    private boolean collides() {
        float down = 0.1f;
        float up = 0.1f;
        float left = 0.1f;
        float right = 0.1f;
        switch (level.getGravity()) {
            case DOWN: down = 0; break;
            case UP: up = 0; break;
            case LEFT: left = 0; break;
            case RIGHT: right = 0; break;
        }

        return level.collides(position.x + left, position.y + down, 1 - right - left, 1 - up - down);
    }

    public void control(float delta, Keyboard kb) {
        anim.tick(delta);

        GravityDir dir = level.getGravity();
        if (onGround && kb.isKeyPressed(Key.UP)) {
            switch (dir) {
                case UP: vel.y = -20; break;
                case DOWN: vel.y = 20; break;
                case LEFT: vel.x = 20; break;
                case RIGHT: vel.x = -20; break;
            }
        }

        boolean isYAxis = false;
        switch (dir) {
            case UP: vel.y += 1; isYAxis = true; break;
            case DOWN: vel.y -= 1; isYAxis = true; break;
            case LEFT: vel.x -= 1; break;
            case RIGHT: vel.x += 1; break;
        }

        if (collides()) {
            // Fix being inside a block
            if (isYAxis) {
                position.y = Math.round(position.y);
            } else {
                position.x = Math.round(position.x);
            }
        }

        float speed = 1.5f;
        if (kb.isKeyPressed(Key.LEFT)) {
            flipped = true;
            if (isYAxis)
                vel.x -= speed;
            else
                vel.y -= speed;
        }
        if (kb.isKeyPressed(Key.RIGHT)) {
            flipped = false;
            if (isYAxis)
                vel.x += speed;
            else
                vel.y += speed;
        }

        Vector2f move = new Vector2f(vel).mul(delta);
        position.x += move.x;
        if (collides()) {
            position.x -= move.x;
            if (!isYAxis) {
                if (dir == GravityDir.LEFT && vel.x < 0) {
//                    position.x = (float) Math.floor(position.x) + 0.01f;
                    onGround = true;
                } else if (dir == GravityDir.RIGHT && vel.x > 0) {
//                    position.x = (float) Math.ceil(position.x) - 0.01f;
                    onGround = true;
                }

                vel.x = 0;
            }
        } else if (!isYAxis) {
            onGround = false;
        }

        position.y += move.y;
        if (collides()) {
            position.y -= move.y;

            if (isYAxis) {
                if (dir == GravityDir.DOWN && vel.y < 0) {
//                    position.y = (float) Math.floor(position.y) + 0.01f;
                    onGround = true;
                } else if (dir == GravityDir.UP && vel.y > 0) {
//                    position.x = (float) Math.floor(position.y) - 0.01f;
                    onGround = true;
                }

                vel.y = 0;
            }
        } else if (isYAxis) {
            onGround = false;
        }

        if (isYAxis)
            vel.x *= 0.8;
        else
            vel.y *= 0.8;

        float rot = 0;
        switch (level.getGravity()) {
            case DOWN: rot = 0; break;
            case RIGHT: rot = (float) Math.PI / 2; break;
            case UP: rot = (float) Math.PI; break;
            case LEFT: rot = (float) (3 * Math.PI / 2); break;
        }
        angle = MathUtil.lerp(angle, rot, delta * 12);
    }

    float angle = 0;

    public GravityDir getFacing() {
        switch (level.getGravity()) {
            case DOWN: case UP:
                return flipped ? GravityDir.LEFT : GravityDir.RIGHT;
            case LEFT: case RIGHT:
                return flipped ? GravityDir.DOWN : GravityDir.UP;
        }
        throw new IllegalStateException();
    }

    public void render(DrawList2D draw) {
        GravityDir dir = level.getGravity();
        Texture2DRegion tex = anim.getCurrentFrameTexture().getFlippedX();
        tex = (flipped ^ (dir == GravityDir.UP || dir == GravityDir.LEFT)) ? tex.getFlippedY() : tex;

        draw.pushTransform();
        draw.getPoseStack().stack.translate(position.x + 0.5f, position.y + 0.5f, 0);
        draw.getPoseStack().stack.rotateZ(angle);
        draw.getPoseStack().stack.translate(-0.5f, -0.5f, 0);
        draw.drawImage(0, 0, 1, 1, tex, 0, 0, 1, 1);
        draw.popTransform();
    }
}
