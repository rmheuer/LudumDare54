package com.github.rmheuer.engine.render.opengl;

import com.github.rmheuer.engine.render.texture.Texture;

public interface OpenGLTexture extends Texture {
    void bind(int slot);
}
