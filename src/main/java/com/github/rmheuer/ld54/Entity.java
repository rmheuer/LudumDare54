package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.render2d.DrawList2D;
import com.github.rmheuer.engine.render2d.Rectangle;
import org.joml.Vector2f;

// Things that are affected by gravity in the leve
public abstract class Entity {
    protected final Level level;
    protected final Vector2f position;

    public Entity(Level level) {
        this.level = level;
        position = new Vector2f();
    }

    final Vector2f vel = new Vector2f(0, 0);
    boolean onGround = false;
    boolean flipped = false;

    // For now assuming everything is 1 tile big
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

        return level.collides(position.x + left, position.y + down, 1 - right - left, 1 - up - down, this);
    }

    public Rectangle getBoundingBox() {
        return Rectangle.fromXYSizes(position.x, position.y, 1, 1);
    }

    public Rectangle getCollisionBox() {
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

        return Rectangle.fromXYSizes(position.x + left, position.y + down, 1 - right - left, 1 - up - down);
    }

    public void tick(float dt) {
        GravityDir dir = level.getGravity();

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

        Vector2f move = new Vector2f(vel).mul(dt);
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
    }

    public abstract void render(DrawList2D draw);
}
