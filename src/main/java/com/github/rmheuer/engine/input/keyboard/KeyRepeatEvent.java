package com.github.rmheuer.engine.input.keyboard;

public final class KeyRepeatEvent extends KeyEvent {
    public KeyRepeatEvent(Keyboard keyboard, Key key) {
        super(keyboard, key);
    }

    @Override
    public String toString() {
        return "KeyRepeatEvent{key=" + getKey() + "}";
    }
}
