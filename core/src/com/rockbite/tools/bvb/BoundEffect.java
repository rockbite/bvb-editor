package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.assets.TextureAtlasAssetProvider;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;

public class BoundEffect {

    private static ScopePayload scope = new ScopePayload();
    Label leftTime, rightTime, comparedTime;

    Array<Long> leftTimes = new Array<Long>();
    Array<Long> rightTimes = new Array<Long>();
    private float talosAverageTimeMS;
    private float legacyAverageTimeMS;


    public TalosActor talosActor;


    private String name;

//    private ParticleEffect particleEffect = new ParticleEffect();

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

    public class TalosActor extends Actor {

        ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
        ParticleEffectInstance particleEffect;
        ParticleRenderer renderer;

        public TalosActor (FileHandle effect, AssetProvider assetProvider, ParticleRenderer renderer) {
            this.renderer = renderer;
            particleEffectDescriptor.setAssetProvider(assetProvider);
            particleEffectDescriptor.load(effect);
            particleEffect = particleEffectDescriptor.createEffectInstance();
            particleEffect.setScope(scope);
            particleEffect.loopable = true;
        }

        @Override
        public void act (float delta) {
            long nano = TimeUtils.nanoTime();
            super.act(delta);
            particleEffect.setPosition(getX(), getY());
            particleEffect.update(Gdx.graphics.getDeltaTime());

            long diff = TimeUtils.nanoTime() - nano;
            rightTimes.add(diff);
            if (rightTimes.size > 60)
                rightTimes.removeIndex(0);
            long sum = 0;
            for (int i = 0; i < rightTimes.size; i++) {
                sum += rightTimes.get(i);
            }
            long avg = sum / rightTimes.size;
            talosAverageTimeMS = avg / 1000000f;
            rightTime.setText(talosAverageTimeMS + "ms");

            comparedTime.setText((int)(talosAverageTimeMS / legacyAverageTimeMS * 100) + "%");
        }

        @Override
        public void draw (Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            particleEffect.render(renderer);
        }
    }

    public BoundEffect(String path, AssetProvider assetProvider, ParticleRenderer renderer) {
        boneName = "root";
        FileHandle fileHandle = Gdx.files.absolute(path);
        talosActor = new TalosActor(fileHandle, assetProvider, renderer);
//        particleEffect.loadEmitters(fileHandle);
//        particleEffect.loadEmitterImages(fileHandle.parent());
        name = fileHandle.nameWithoutExtension();

    }
//
//
//    public void load(String path) {
//        FileHandle fileHandle = Gdx.files.absolute(path);
//        particleEffect.loadEmitters(fileHandle);
//        particleEffect.loadEmitterImages(fileHandle.parent());
//
//        name = fileHandle.nameWithoutExtension();
//    }



    public void reloadEffect(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        talosActor.particleEffectDescriptor.load(fileHandle);
        talosActor.particleEffect.restart();
//        particleEffect = new ParticleEffect();
//        load(path);
//        particleEffect.scaleEffect(scale);
    }

    public ParticleEffectInstance getEffect() {
        return talosActor.particleEffect;
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
        talosActor.particleEffect.setPosition(x, y);
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
            talosActor.particleEffect.loopable = false;
            stopped = true;
        } else {
            talosActor.particleEffect.loopable = true;
            talosActor.particleEffect.restart();
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
        talosActor.particleEffect.restart();
    }

    public void stop() {
        talosActor.particleEffect.allowCompletion();
    }

//    public void setScale(float val) {
//        scale = val;
//        particleEffect.reset(true);
//        particleEffect.scaleEffect(scale);
//    }

    public void setBehind(boolean checked) {
        isBehind = checked;
    }

    public boolean isBehind() {
        return isBehind;
    }
}
