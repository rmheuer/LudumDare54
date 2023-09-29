package com.github.rmheuer.engine.render.camera;

import org.joml.Matrix4f;

public interface Projection {
    Matrix4f getMatrix(float viewportW, float viewportH);
}
