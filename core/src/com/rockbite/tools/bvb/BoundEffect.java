package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
public class BoundEffect {

    private String name;

    private ParticleEffect particleEffect = new ParticleEffect();

    private String boneName;

    private Vector2 offset = new Vector2(0, 0);

    private Vector2 currPosition = new Vector2();

    private Vector2 tmp = new Vector2();


    private boolean boundRotation;
    private boolean boundColor;
    private String startEvent = "";
    private String endEvent = "";

    private boolean stopped = false;

    public boolean isBehind = false;

    public float scale = 1f;

    public BoundEffect() {
        boneName = "root";
    }


    public void load(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        particleEffect.loadEmitters(fileHandle);
        particleEffect.loadEmitterImages(fileHandle.parent());

        name = fileHandle.nameWithoutExtension();
    }



    public void reloadEffect(String path) {
        particleEffect = new ParticleEffect();
        load(path);
        particleEffect.scaleEffect(scale);
    }

    public ParticleEffect getEffect() {
        return particleEffect;
    }

    public void bind(String boneName, float x, float y) {
        offset.set(x, y);
        this.boneName = boneName;
    }

    public String getBoneName() {
        return boneName;
    }

    public Vector2 getPosition() {
        return currPosition;
    }

    public void setPosition(float x, float y) {
        currPosition.set(x, y);
        particleEffect.setPosition(x, y);
    }

    public Vector2 getOffset() {
        return offset;
    }

    public Vector2 getPositionWithOffset() {
        tmp.set(currPosition).add(offset);
        return tmp;
    }

    public String getName() {
        return name;
    }

    public boolean isBoundRotation() {
        return boundRotation;
    }

    public void setBoundRotation(boolean boundRotation) {
        this.boundRotation = boundRotation;
    }

    public boolean isBoundColor() {
        return boundColor;
    }

    public void setBoundColor(boolean boundColor) {
        this.boundColor = boundColor;
    }

    public String getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(String startEvent) {
        this.startEvent = startEvent;
        if(!startEvent.equals("")) {
            particleEffect.reset(false);
            stopped = true;
        } else {
            particleEffect.start();
            stopped = false;
        }
    }

    public String getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(String endEvent) {
        this.endEvent = endEvent;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        stopped = false;
        particleEffect.start();
    }

    public void stop() {
        particleEffect.allowCompletion();
    }

    public void setScale(float val) {
        scale = val;
        particleEffect.reset(true);
        particleEffect.scaleEffect(scale);
    }

    public void setBehind(boolean checked) {
        isBehind = checked;
    }

    public boolean isBehind() {
        return isBehind;
    }
}
