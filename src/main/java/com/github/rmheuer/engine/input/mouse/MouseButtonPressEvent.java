package com.github.rmheuer.engine.input.mouse;

import org.joml.Vector2d;

public final class MouseButtonPressEvent extends MouseButtonEvent {
    public MouseButtonPressEvent(Mouse mouse, Vector2d cursorPos, MouseButton button) {
        super(mouse, cursorPos, button);
    }

    @Override
    public String toString() {
        return "MouseButtonPressEvent{" +
                "cursorPos=" + getCursorPos() + "," +
                "button=" + getButton() +
                '}';
    }
}
