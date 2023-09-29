package com.github.rmheuer.engine.render.texture;

import com.github.rmheuer.engine.utils.SafeCloseable;

public interface Texture extends SafeCloseable {
    enum Filter {
        NEAREST,
        LINEAR
    }

    void setMinFilter(Filter minFilter);
    void setMagFilter(Filter magFilter);
    default void setFilters(Filter filter) {
        setMinFilter(filter);
        setMagFilter(filter);
    }
}
