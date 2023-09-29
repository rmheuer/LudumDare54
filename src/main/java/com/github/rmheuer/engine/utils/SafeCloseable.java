package com.github.rmheuer.engine.utils;

public interface SafeCloseable extends AutoCloseable {
    @Override
    void close();
}
