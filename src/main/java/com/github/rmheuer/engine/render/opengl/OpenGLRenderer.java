package com.github.rmheuer.engine.render.opengl;

import com.github.rmheuer.engine.render.BufferType;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.mesh.Mesh;
import com.github.rmheuer.engine.render.pipeline.ActivePipeline;
import com.github.rmheuer.engine.render.pipeline.PipelineInfo;
import com.github.rmheuer.engine.render.shader.ShaderProgram;
import com.github.rmheuer.engine.render.shader.ShaderStage;
import com.github.rmheuer.engine.render.shader.ShaderUniform;
import com.github.rmheuer.engine.render.texture.Texture;
import com.github.rmheuer.engine.render.texture.Texture2D;

import static org.lwjgl.opengl.GL33C.*;

public final class OpenGLRenderer implements Renderer {
    private boolean pipelineActive = false;

    @Override
    public void setViewportRect(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
    }

    @Override
    public void setClearColor(ColorRGBA color) {
        glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    @Override
    public void clear(BufferType... buffers) {
        if (buffers.length == 0)
            return;

        int bits = 0;
        for (BufferType type : buffers) {
            switch (type) {
                case COLOR: bits |= GL_COLOR_BUFFER_BIT; break;
                case DEPTH: bits |= GL_DEPTH_BUFFER_BIT; break;
                case STENCIL: bits |= GL_STENCIL_BUFFER_BIT; break;
            }
        }

        glClear(bits);
    }

    private void setEnabled(int feature, boolean enabled) {
        if (enabled)
            glEnable(feature);
        else
            glDisable(feature);
    }

    @Override
    public ActivePipeline bindPipeline(PipelineInfo pipeline) {
        if (pipelineActive)
            throw new IllegalStateException("Another pipeline is already active");
        pipelineActive = true;

        OpenGLShaderProgram shader = (OpenGLShaderProgram) pipeline.getShader();
        shader.bind();

        setEnabled(GL_BLEND, pipeline.isBlend());
        if (pipeline.isBlend())
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return new ActivePipelineImpl(shader);
    }

    @Override
    public ShaderStage createShaderStage(ShaderStage.Type type, String glsl) {
        return new OpenGLShaderStage(type, glsl);
    }

    @Override
    public ShaderProgram createShaderProgram(ShaderStage... stages) {
        return new OpenGLShaderProgram(stages);
    }

    @Override
    public Mesh createMesh() {
        return new OpenGLMesh();
    }

    @Override
    public Texture2D createTexture2D() {
        return new OpenGLTexture2D();
    }

    private final class ActivePipelineImpl implements ActivePipeline {
        private final OpenGLShaderProgram shader;

        public ActivePipelineImpl(OpenGLShaderProgram shader) {
            this.shader = shader;
        }

        @Override
        public void bindTexture(int slot, Texture texture) {
            ((OpenGLTexture) texture).bind(slot);
        }

        @Override
        public ShaderUniform getUniform(String name) {
            return shader.getUniform(name);
        }

        @Override
        public void draw(Mesh mesh) {
            ((OpenGLMesh) mesh).render();
        }

        @Override
        public void close() {
            pipelineActive = false;
        }
    }
}
