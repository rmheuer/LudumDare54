package com.github.rmheuer.engine.render.mesh;

import com.github.rmheuer.engine.utils.SafeCloseable;

public interface Mesh extends SafeCloseable {
    enum DataUsage {
        STATIC,
        DYNAMIC,
        STREAM
    }

    void setData(MeshData data, DataUsage usage);
    boolean hasData();
}
