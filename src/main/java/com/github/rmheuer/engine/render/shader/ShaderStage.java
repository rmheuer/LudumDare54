package com.github.rmheuer.engine.render.shader;

import com.github.rmheuer.engine.utils.SafeCloseable;

public interface ShaderStage extends SafeCloseable {
    enum Type {
        VERTEX,
        FRAGMENT
    }

    Type getType();
}
