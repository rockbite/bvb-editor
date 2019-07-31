package com.rockbite.tools.bvb;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;

public class SFXListModel implements IFXListModel {

    public String path;

    public String effectName;

    public boolean exists = false;

    private MainStage stage;

    private Sound sound;

    private String playEvent = "";

    public SFXListModel(MainStage stage, String path) {
        this.stage = stage;

        this.path = path;

        FileHandle handle = Gdx.files.absolute(path);

        effectName = handle.nameWithoutExtension();

        sound = Gdx.audio.newSound(handle);
    }

    public String getPlayEvent() {
        return playEvent;
    }

    public void setPlayEvent(String event) {
        playEvent = event;
    }

    public void play() {
        sound.play();
    }

    @Override
    public boolean wasLoaded() {
        return true;
    }

    @Override
    public String getName() {
        return effectName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isVFX() {
        return false;
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public String toString() {
        return effectName;
    }
}
