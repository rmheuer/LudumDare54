package com.github.rmheuer.engine.input.keyboard;

public final class KeyReleaseEvent extends KeyEvent {
    public KeyReleaseEvent(Keyboard keyboard, Key key) {
        super(keyboard, key);
    }

    @Override
    public String toString() {
        return "KeyReleaseEvent{key=" + getKey() + "}";
    }
}
