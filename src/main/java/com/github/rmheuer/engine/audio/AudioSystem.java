package com.github.rmheuer.engine.audio;

import com.github.rmheuer.engine.audio.data.AudioData;
import com.github.rmheuer.engine.audio.data.AudioSample;
import com.github.rmheuer.engine.audio.data.AudioStream;
import com.github.rmheuer.engine.audio.play.DummySound;
import com.github.rmheuer.engine.audio.play.PlayingSample;
import com.github.rmheuer.engine.audio.play.PlayingSound;
import com.github.rmheuer.engine.audio.play.PlayingStream;
import com.github.rmheuer.engine.math.PoseStack;
import com.github.rmheuer.engine.math.Transform;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public final class AudioSystem implements AutoCloseable {
    private static final int MAX_SOURCES = 255;

    private final long device;
    private final long context;
    private final int[] sourcePool;
    private final AtomicInteger poolIdx;
    private final AudioThread thread;

    public AudioSystem() {
        device = alcOpenDevice((ByteBuffer) null);
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        context = alcCreateContext(device, (IntBuffer) null);
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        sourcePool = new int[MAX_SOURCES];
        alGenSources(sourcePool);
        poolIdx = new AtomicInteger(MAX_SOURCES - 1);

        thread = new AudioThread(this);
        thread.start();
    }

    public AudioSample createSample(InputStream in) throws IOException {
        return new AudioSample(in);
    }

    public AudioStream createStream(InputStream in) throws IOException {
        return new AudioStream(in);
    }

    private int getSource() {
        int idx = poolIdx.getAndUpdate((i) -> i < 0 ? i : i - 1);
        if (idx >= 0)
            return sourcePool[idx];
        else
            return -1; // Out of sources
    }

    void returnSource(int id) {
        if (id != -1)
            sourcePool[poolIdx.incrementAndGet()] = id;
    }

    public PlayingSound play(PlayOptions options) {
        int source = getSource();
        if (source == -1)
            return new DummySound();

        Vector3fc pos = options.getPosition();
        alSourcei(source, AL_SOURCE_RELATIVE, options.getMode() == SpatialMode.RELATIVE ? AL_TRUE : AL_FALSE);
        alSource3f(source, AL_POSITION, pos.x(), pos.y(), pos.z());
        alSourcef(source, AL_PITCH, options.getPitch());
        alSourcef(source, AL_GAIN, options.getGain());
        alSourcei(source, AL_LOOPING, options.isLooping() ? AL_TRUE : AL_FALSE);

        AudioData data = options.getData();
        PlayingSound playing = null;
        if (data instanceof AudioSample) {
            AudioSample sample = (AudioSample) data;
            alSourcei(source, AL_BUFFER, sample.getBuffer());
            alSourcePlay(source);
            playing = new PlayingSample(source);
        } else if (data instanceof AudioStream) {
            AudioStream stream = (AudioStream) data;
            playing = new PlayingStream(source, stream);
        }

        thread.add(playing);
        return playing;
    }

    public void setListenerPosition(Vector3fc pos) {
        alListener3f(AL_POSITION, pos.x(), pos.y(), pos.z());
    }

    public void setListenerOrientation(Vector3fc forward, Vector3fc up) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.floats(
                    forward.x(), forward.y(), forward.z(),
                    up.x(), up.y(), up.z()
            );
            alListenerfv(AL_ORIENTATION, buf);
        }
    }

    public void setListenerTransform(Transform tx) {
        setListenerPosition(tx.position);
        setListenerOrientation(tx.getForward(), tx.getUp());
    }

    public void setListenerPose(PoseStack pose) {
        setListenerPosition(pose.getPosition());
        setListenerOrientation(pose.getForward(), pose.getUp());
    }

    public void setListenerVelocity(Vector3fc vel) {
        alListener3f(AL_VELOCITY, vel.x(), vel.y(), vel.z());
    }

    public void setListenerGain(float gain) {
        alListenerf(AL_GAIN, gain);
    }

    public void close() {
        try {
            thread.end();
            thread.join(1000);
        } catch (InterruptedException e) {}
        alcDestroyContext(context);
        alcCloseDevice(device);
    }
}
