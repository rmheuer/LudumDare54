package com.github.rmheuer.ld54;

import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.render.texture.Texture2DRegion;
import com.github.rmheuer.engine.utils.SafeCloseable;

import java.io.IOException;
import java.io.InputStream;

public final class Animation implements SafeCloseable {
    public static Animation fromSpriteSheetVertical(Renderer renderer, InputStream stream, float frameRate) throws IOException {
        Bitmap bmp = Bitmap.decode(stream);
        Texture2D texture = renderer.createTexture2D();
        texture.setData(bmp);

        // Assume square
        int sz = bmp.getWidth();

        int count = bmp.getHeight() / sz;
        Texture2DRegion[] frames = new Texture2DRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = texture.getSubRegion(0, i / (float) count, 1, (i + 1) / (float) count);
        }

        return new Animation(texture, frames, frameRate);
    }

    private final Texture2D spriteSheet;
    private final Texture2DRegion[] frames;
    private final float frameInterval;
    private int currentFrame;

    private float frameProgress;

    public Animation(Texture2D spriteSheet, Texture2DRegion[] frames, float frameRate) {
        this.spriteSheet = spriteSheet;
        this.frames = frames;
        this.frameInterval = 1 / frameRate;
        currentFrame = 0;
        frameProgress = 0;
    }

    public void tick(double dt) {
        frameProgress += dt;
        while (frameProgress > frameInterval) {
            frameProgress -= frameInterval;
            currentFrame++;
            if (currentFrame >= frames.length)
                currentFrame = 0;
        }
    }

    public Texture2DRegion getCurrentFrameTexture() {
        return frames[currentFrame];
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void close() {
        spriteSheet.close();
    }
}
