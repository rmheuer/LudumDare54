package com.github.rmheuer.engine.input.mouse;

import org.joml.Vector2d;

public interface Mouse {
    Vector2d getCursorPos();
    boolean isButtonPressed(MouseButton button);
}
