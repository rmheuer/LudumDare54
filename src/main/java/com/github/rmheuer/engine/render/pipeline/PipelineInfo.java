package com.github.rmheuer.engine.render.pipeline;

import com.github.rmheuer.engine.render.shader.ShaderProgram;

public final class PipelineInfo {
    private final ShaderProgram shader;
    private boolean blend;

    public PipelineInfo(ShaderProgram shader) {
        this.shader = shader;
        blend = true;
    }

    public PipelineInfo setBlend(boolean blend) {
        this.blend = blend;
        return this;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public boolean isBlend() {
        return blend;
    }
}
