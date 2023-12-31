package com.github.rmheuer.engine.render;

import com.github.rmheuer.engine.io.IOUtil;
import com.github.rmheuer.engine.render.mesh.Mesh;
import com.github.rmheuer.engine.render.pipeline.ActivePipeline;
import com.github.rmheuer.engine.render.pipeline.PipelineInfo;
import com.github.rmheuer.engine.render.shader.ShaderProgram;
import com.github.rmheuer.engine.render.shader.ShaderStage;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a renderer used to render into a window.
 */
public interface Renderer {
    /**
     * Sets the region of the viewport to render into.
     *
     * @param x x position of the lower-left corner
     * @param y y position of the lower-left corner
     * @param width width of the viewport rect
     * @param height height of the viewport rect
     */
    void setViewportRect(int x, int y, int width, int height);

    /**
     * Sets the color used to fill the window when clearing the color buffer.
     *
     * @param color new clear color
     */
    void setClearColor(ColorRGBA color);

    /**
     * Clears the contents of the specified buffers.
     *
     * @param buffers buffers to clear
     */
    void clear(BufferType... buffers);

    /**
     * Binds a pipeline for use.
     *
     * @param pipeline pipeline configuration
     * @return access to draw commands
     * @throws IllegalStateException if another pipeline is currently bound
     */
    ActivePipeline bindPipeline(PipelineInfo pipeline);

    ShaderStage createShaderStage(ShaderStage.Type type, String glsl);
    ShaderProgram createShaderProgram(ShaderStage... stages);
    default ShaderStage createShaderStage(ShaderStage.Type type, InputStream in) throws IOException {
        return createShaderStage(type, IOUtil.readToString(in));
    }
    default ShaderProgram createShaderProgram(String vertexSrc, String fragmentSrc) {
        try (ShaderStage vertexStage = createShaderStage(ShaderStage.Type.VERTEX, vertexSrc);
             ShaderStage fragmentStage = createShaderStage(ShaderStage.Type.FRAGMENT, fragmentSrc)) {
            return createShaderProgram(vertexStage, fragmentStage);
        }
    }
    default ShaderProgram createShaderProgram(InputStream vertexSrc, InputStream fragmentSrc) throws IOException {
        try (ShaderStage vertexStage = createShaderStage(ShaderStage.Type.VERTEX, vertexSrc);
             ShaderStage fragmentStage = createShaderStage(ShaderStage.Type.FRAGMENT, fragmentSrc)) {
            return createShaderProgram(vertexStage, fragmentStage);
        }
    }

    Mesh createMesh();

    Texture2D createTexture2D();
    default Texture2D createTexture2D(InputStream in) throws IOException {
        Bitmap bitmap = Bitmap.decode(in);
        Texture2D tex = createTexture2D();
        tex.setData(bitmap);
        return tex;
    }
}
