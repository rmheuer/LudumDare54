package com.github.rmheuer.engine.render.event;

import com.github.rmheuer.engine.render.Window;

public final class WindowCloseEvent extends WindowEvent {
    public WindowCloseEvent(Window window) {
        super(window);
    }

    @Override
    public String toString() {
        return "WindowCloseEvent{}";
    }
}
