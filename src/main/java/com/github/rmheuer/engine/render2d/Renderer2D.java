package com.github.rmheuer.engine.render2d;

import com.github.rmheuer.engine.io.ResourceUtil;
import com.github.rmheuer.engine.render.ColorRGBA;
import com.github.rmheuer.engine.render.Renderer;
import com.github.rmheuer.engine.render.mesh.Mesh;
import com.github.rmheuer.engine.render.pipeline.ActivePipeline;
import com.github.rmheuer.engine.render.pipeline.PipelineInfo;
import com.github.rmheuer.engine.render.shader.ShaderProgram;
import com.github.rmheuer.engine.render.texture.Bitmap;
import com.github.rmheuer.engine.render.texture.Texture2D;
import com.github.rmheuer.engine.utils.SafeCloseable;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;

public final class Renderer2D implements SafeCloseable {
    public static final int MAX_TEXTURE_SLOTS = 16;

    private static final String VERTEX_SHADER_PATH = "engine/shaders/render2d/vertex.glsl";
    private static final String FRAGMENT_SHADER_PATH = "engine/shaders/render2d/fragment.glsl";

    private final Renderer renderer;
    private final Mesh mesh;
    private final ShaderProgram shader;
    private final Texture2D whiteTex;

    public Renderer2D(Renderer renderer) {
        this.renderer = renderer;
        mesh = renderer.createMesh();
        try {
            shader = renderer.createShaderProgram(
                    ResourceUtil.readAsStream(VERTEX_SHADER_PATH),
                    ResourceUtil.readAsStream(FRAGMENT_SHADER_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load built-in shaders", e);
        }

        Bitmap whiteData = new Bitmap(1, 1, ColorRGBA.white());
        whiteTex = renderer.createTexture2D();
        whiteTex.setData(whiteData);
//        whiteTex = new Image(1, 1, whiteData);

        try (ActivePipeline pipe = renderer.bindPipeline(new PipelineInfo(shader))) {
            for (int i = 0; i < MAX_TEXTURE_SLOTS; i++) {
                pipe.getUniform("u_Textures[" + i + "]").setInt(i);
            }
        }
    }

    private void drawBatch(ActivePipeline pipeline, VertexBatch batch) {
        Texture2D[] textures = batch.getTextures();
        for (int i = 0; i < MAX_TEXTURE_SLOTS; i++) {
            Texture2D tex = textures[i];
            if (tex != null) {
                pipeline.bindTexture(i, tex);
            }
        }
        mesh.setData(batch.getData(), Mesh.DataUsage.DYNAMIC);
        pipeline.draw(mesh);

//        // Natives must be pre-obtained to prevent accidental state changes
//        ShaderProgram.Native nShader = shader.getNative(nom);
//        Mesh.Native nMesh = mesh.getNative(nom);
//        Image.Native[] nTextures = new Image.Native[RenderConstants.MAX_TEXTURE_SLOTS];
//
//        for (int i = 0; i < nTextures.length; i++) {
//            Image tex = batch.getTextures()[i];
//            if (tex != null) {
//                nTextures[i] = tex.getNative(nom);
//            }
//        }
//
//        nShader.bind();
//        for (int i = 0; i < nTextures.length; i++) {
//            Image.Native nTex = nTextures[i];
//            if (nTex != null) {
//                nTex.bindToSlot(i);
//            }
//        }
//
//        nShader.updateUniformValues();
//        nMesh.render();
//        nShader.unbind();
    }

//    public void draw(DrawList2D list) {
//        draw(list, new Transform());
//    }

    public void draw(DrawList2D list, Matrix4f transform, Matrix4f projection, Matrix4f view) {
//        RenderBackend.get().setCullMode(WindingOrder.CLOCKWISE, CullMode.NONE);

        try (ActivePipeline pipe = renderer.bindPipeline(new PipelineInfo(shader))) {
            pipe.getUniform("u_Transform").setMat4(transform);
            pipe.getUniform("u_Projection").setMat4(projection);
            pipe.getUniform("u_View").setMat4(view);

            List<VertexBatch> batches = VertexBatcher2D.batch(list.getVertices(), list.getIndices(), whiteTex);
            for (VertexBatch batch : batches) {
                drawBatch(pipe, batch);
            }
        }

//        shader.getUniform("u_Transform").setMatrix4f(transform.getGlobalMatrix());
    }

    @Override
    public void close() {
        mesh.close();
        shader.close();
        whiteTex.close();
    }
}
