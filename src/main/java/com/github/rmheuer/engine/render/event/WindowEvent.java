package com.github.rmheuer.engine.render.event;

import com.github.rmheuer.engine.event.Event;
import com.github.rmheuer.engine.render.Window;

public abstract class WindowEvent extends Event {
    private final Window window;

    public WindowEvent(Window window) {
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }
}
