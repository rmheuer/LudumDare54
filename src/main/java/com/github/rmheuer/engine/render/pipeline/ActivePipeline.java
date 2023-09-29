package com.github.rmheuer.engine.render.pipeline;

import com.github.rmheuer.engine.render.mesh.Mesh;
import com.github.rmheuer.engine.render.shader.ShaderUniform;
import com.github.rmheuer.engine.render.texture.Texture;
import com.github.rmheuer.engine.utils.SafeCloseable;

public interface ActivePipeline extends SafeCloseable {
    void bindTexture(int slot, Texture texture);

    ShaderUniform getUniform(String name);

    void draw(Mesh mesh);
}
