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

public final class Player extends Entity {
    private final Animation anim;

    public Player(Level level, Renderer r) throws IOException {
        super(level);
        anim = Animation.fromSpriteSheetVertical(r, ResourceUtil.readAsStream("dude.png"), 12);
//        texture = r.createTexture2D(ResourceUtil.readAsStream("whatever-this-is.png")).getFlippedX();
        position.set(4, 4);
    }

    boolean jumpedPrevFrame = false;

    public void control(float delta, Keyboard kb) {
        anim.tick(delta);

        GravityDir dir = level.getGravity();
        if (onGround && (kb.isKeyPressed(Key.UP) || kb.isKeyPressed(Key.W))) {
            if (!jumpedPrevFrame)
                LudumDare54.INSTANCE.playJumpSound();
            jumpedPrevFrame = true;
            switch (dir) {
                case UP: vel.y = -20; break;
                case DOWN: vel.y = 20; break;
                case LEFT: vel.x = 20; break;
                case RIGHT: vel.x = -20; break;
            }
        } else {
            jumpedPrevFrame = false;
        }

        boolean isYAxis = dir == GravityDir.UP || dir == GravityDir.DOWN;

        float speed = 1.5f;
        if (kb.isKeyPressed(Key.LEFT) || kb.isKeyPressed(Key.A)) {
            flipped = true;
            if (isYAxis)
                vel.x -= speed;
            else
                vel.y -= speed;
        }
        if (kb.isKeyPressed(Key.RIGHT) || kb.isKeyPressed(Key.D)) {
            flipped = false;
            if (isYAxis)
                vel.x += speed;
            else
                vel.y += speed;
        }
    }

    public void resetVelocity() {
        vel.set(0);
    }

    @Override
    public void tick(float dt) {
        super.tick(dt);

        float rot = 0;
        switch (level.getGravity()) {
            case DOWN: rot = 0; break;
            case RIGHT: rot = (float) Math.PI / 2; break;
            case UP: rot = (float) Math.PI; break;
            case LEFT: rot = (float) (3 * Math.PI / 2); break;
        }
        angle = MathUtil.lerp(angle, rot, dt * 12);
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
