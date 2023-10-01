package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render2d.DrawList2D;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public final class SpaceBackground {
    private final int PARTICLE_COUNT = 200;

    private static final class Particle {
        Vector2f position;
        float lifeTime;
        float time;
        float speed;

        public Particle(Vector2f position, float lifeTime, float speed) {
            this.position = position;
            this.lifeTime = lifeTime;
            this.time = 0;
            this.speed = speed;
        }
    }

    private final Level level;
    private final Particle[] particles;

    public SpaceBackground(Level level) {
        this.level = level;
        particles = new Particle[PARTICLE_COUNT];
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = new Particle(new Vector2f(
                    Level.SIZE * (float) Math.random(),
                    Level.SIZE * (float) Math.random()
            ), 1 + 4 * (float) Math.random(),
                    5 + 3 * (float) Math.random());
        }
    }

    public void tick(float delta) {
        for (Particle particle : particles) {
            GravityDir dir = level.getGravity();
            Vector2f gravity = null;
            switch (dir) {
                case DOWN: gravity = new Vector2f(0, -1); break;
                case UP: gravity = new Vector2f(0, 1); break;
                case LEFT: gravity = new Vector2f(-1, 0); break;
                case RIGHT: gravity = new Vector2f(1, 0); break;
            }

            particle.position.fma(particle.speed * delta, gravity);

            particle.time += delta;
            if (particle.time >= particle.lifeTime ||
                particle.position.x < 0.1f || particle.position.x >= Level.SIZE - 0.1f ||
                particle.position.y < 0.1f || particle.position.y >= Level.SIZE - 0.1f) {
                particle.position = new Vector2f(
                        Level.SIZE * (float) Math.random(),
                        Level.SIZE * (float) Math.random()
                );
                particle.lifeTime = 1 + 4 * (float) Math.random();
                particle.time = 0;
                particle.speed = 5 + 3 * (float) Math.random();
            }
        }
    }

    public void render(DrawList2D draw) {
        for (Particle particle : particles) {
            float bright = 2 * (0.5f - Math.abs(particle.time / particle.lifeTime - 0.5f));
            draw.fillQuad(particle.position, 1/16f, 1/16f, new ColorRGBA(bright, bright, bright));
        }
    }
}
