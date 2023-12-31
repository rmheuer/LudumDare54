package com.github.rmheuer.engine.audio.play;

import org.joml.Vector3fc;

public final class DummySound extends PlayingSound {
    public DummySound() {
        super(-1);
    }

    @Override
    public void setPositionAbsolute(Vector3fc pos) {}

    @Override
    public void setPositionRelative(Vector3fc pos) {}

    @Override
    public void setGain(float gain) {}

    @Override
    public void setPitch(float pitch) {}

    @Override
    public void setLooping(boolean looping) {}

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void stop() {}
}
