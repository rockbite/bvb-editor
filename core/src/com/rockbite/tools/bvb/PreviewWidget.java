package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import com.esotericsoftware.spine.*;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.rockbite.tools.bvb.data.ExportData;
import com.rockbite.tools.bvb.data.SFXExportData;
import com.rockbite.tools.bvb.data.VFXExportData;

import java.util.HashMap;
import java.util.Map;


public class PreviewWidget extends Actor {

    ShapeRenderer shapeRenderer;

    SkeletonRenderer renderer;
    Skeleton skeleton;
    AnimationState state;

    Array<String> eventList = new Array<String>();

    HashMap<String, Array<BoundEffect>> boundEffects = new HashMap<String, Array<BoundEffect>>();

    HashMap<String, Bone> boneMap = new HashMap<String, Bone>();

    Vector2 globalOffset = new Vector2();
    Vector2 tmp = new Vector2();
    Vector2 mousePos = new Vector2();
    Vector2 mouseScreenPos = new Vector2();

    // input controls
    Vector2 touchDownPos = new Vector2();
    Vector2 firstTouchPos = new Vector2();// this is for screen space
    Vector2 lastPos = new Vector2();
    Vector2 currPos = new Vector2();
    boolean wasTouched = false;
    boolean lastTouchWasSelecting = false;

    BoundEffect currentlyMovingEffect = null;
    BoundEffect currentlySelectedEffect = null;
    Bone targetBone = null;
    Vector2 targetOffset = new Vector2();

    Mode currentMode = Mode.PREVIEW;

    String currentAnimation;

    float speedMultiplier = 1f;

    private boolean paused = false;

    private ChangeListener changeListener;

    private boolean isSpineFileDropActive = false;

    private FXProperties fxProperties;
    private FXProperties sfxProperties;

    private boolean touchDisabled = false;

    // oh here we go jeez
    Viewport viewport;
    FrameBuffer fbo;

    private boolean shiftToggle = false;

    private void addBoundEffect(BoundEffect effect) {
        String animation = currentAnimation;
        if(!boundEffects.containsKey(animation)) {
            boundEffects.put(animation, new Array<BoundEffect>());
        }

        boundEffects.get(animation).add(effect);
    }

    private void removeBoundEffect(BoundEffect effect, boolean identity) {
        if(!boundEffects.containsKey(currentAnimation)) {
            boundEffects.put(currentAnimation, new Array<BoundEffect>());
        }

        boundEffects.get(currentAnimation).removeValue(effect, identity);
    }

    public boolean isSpineLoaded() {
        if(skeleton == null) return false;

        return true;
    }

    private Array<BoundEffect> getBoundEffects() {
        if(!boundEffects.containsKey(currentAnimation)) {
            boundEffects.put(currentAnimation, new Array<BoundEffect>());
        }

        return boundEffects.get(currentAnimation);
    }

    public void setActivateForDrop(boolean activate) {
        isSpineFileDropActive = activate;
    }

    public void setPropertiesPanel(VFXProperties vfxWidget, SFXProperties sfxWidget) {
        this.fxProperties = vfxWidget;
        this.sfxProperties = sfxWidget;
    }

    public void play() {
        paused = false;
        toolTip("Resume animation");
    }

    public void pause() {
        paused = true;
        toolTip("Stop All Motor Functions!");
    }

    public void deleteSelectedVFX() {
        if(currentlySelectedEffect != null) {
            removeBoundEffect(currentlySelectedEffect, true);
        }
    }

    public void setCanvas(float canvasOffsetX, float canvasOffsetY) {
        viewport.getCamera().position.set(canvasOffsetX, canvasOffsetY, 0);
    }

    public void disableTouch() {
        touchDisabled = true;
    }

    public void enableTouch() {
        touchDisabled = false;
    }

    public void scrolled(int amount) {
        if(touchDisabled) return;
        ((OrthographicCamera)viewport.getCamera()).zoom += amount*0.05f;
        if(((OrthographicCamera)viewport.getCamera()).zoom < 0) ((OrthographicCamera)viewport.getCamera()).zoom = 0.1f;
    }

    public void resetPosition() {
        viewport.getCamera().position.set(0, 0, 0);
        ((OrthographicCamera)viewport.getCamera()).zoom = 1f;
    }

    public void setSpeed(float value) {
        speedMultiplier = value/100f;
    }


    public interface ChangeListener {
        void onAnimationChanged(Skeleton skeleton);
    }

    public void setListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    private enum Mode {
        PREVIEW,
        OFFSET,
        BONE_SWITCH
    }

    public PreviewWidget() {
        shapeRenderer  = new ShapeRenderer();
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(false); // PMA results in correct blending without outlines. (actually should be true, not sure why this ruins scene2d later, probably blend screwup, will check later)

        setSize(700, 855);

        viewport = new FitViewport(700, 855);
        fbo = new FrameBuffer(Pixmap.Format.RGB888, 700, 855, false);
    }

    public boolean initSpine(String animPath, String planBPath) {
        FileHandle jsonFileHandle = Gdx.files.absolute(animPath);

        if(!jsonFileHandle.exists()) {
            // lets look in same directory at least of this project
            FileHandle projPath = Gdx.files.absolute(planBPath);
            String dirPath = projPath.parent().path();
            String newPath = dirPath + "\\" +  jsonFileHandle.name();

            jsonFileHandle = Gdx.files.absolute(newPath);
        }

        if(!jsonFileHandle.extension().equals("json")) {
            Dialogs.showErrorDialog (getStage(), "Spine file must have .json extension");
            return false;
        }

        boundEffects.clear();

        FileHandle atlasFileHandle = Gdx.files.absolute(jsonFileHandle.pathWithoutExtension() + ".atlas");

        TextureAtlas atlas = new TextureAtlas(atlasFileHandle);
        SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
        json.setScale(0.3968f);
        SkeletonData skeletonData = json.readSkeletonData(jsonFileHandle);

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        skeleton.setPosition(0, 0);

        // traverse animations list
        changeListener.onAnimationChanged(skeleton);

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
        state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        state.setTimeScale(1f); // Slow all animations down to 50% speed.

        // Queue animations on track 0.
        state.setAnimation(0, skeleton.getData().getAnimations().get(0).getName(), true);
        currentAnimation = skeleton.getData().getAnimations().get(0).getName();

        eventList.clear();
        for(EventData eventData : skeletonData.getEvents()) {
            eventList.add(eventData.getName());
        }

        fxProperties.updateEventList(eventList);
        sfxProperties.updateEventList(eventList);

        state.update(0.1f); // Update the animation time.
        state.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.\
        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        state.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                animationEvent(event.toString());
                super.event(entry, event);
            }
        });

        boneMap.clear();
        for(Bone bone: skeleton.getBones()) {
            boneMap.put(bone.getData().getName(), bone);
        }

        return true;
    }

    private void animationEvent(String eventName) {
        // react to VFX stop and start events

        toolTip("Event Fired: " + eventName);

        for(BoundEffect effect: getBoundEffects()) {
           if(effect.getStartEvent().equals(eventName)) {
               effect.start();
           }
            if(effect.getEndEvent().equals(eventName)) {
                effect.stop();
            }
        }

        ((MainStage)getStage()).spineEvent(eventName);
    }

    public void changeAnimation(Animation targetAnim) {
        state.setAnimation(0, targetAnim, true);
        currentAnimation = targetAnim.getName();
        toolTip("Animation Changed: " + targetAnim.getName());

    }

    public void addParticle(VFXListModel model) {
        if(skeleton == null) {
            Dialogs.showErrorDialog (getStage(), "Please Load spine animation first");
            return;
        }

        float x = mousePos.x;
        float y = mousePos.y;

        System.out.println("x: " + x + "  y + " + y);

        BoundEffect effect = new BoundEffect();
        effect.load(model.path);
        effect.setScale(50f);
        effect.getEffect().start();


        Vector2 offset = new Vector2();
        // let's find closest position
        effect.setPosition(x, y);
        float minDist = 100000f;
        Bone closestBone = skeleton.getBones().get(0);
        for (Bone bone : skeleton.getBones()) {
            tmp.set(bone.getWorldX(), bone.getWorldY());
            float dist = tmp.dst(effect.getPosition());
            if (dist < minDist) {
                minDist = dist;
                closestBone = bone;
                offset.set(tmp.sub(effect.getPosition()));
            }
        }

        System.out.println(minDist);

        effect.bind(closestBone.getData().getName(), offset.x, offset.y);

        selectEffect(effect);

        addBoundEffect(effect);
    }


    public void reloadVFX(String effectName) {
        if(skeleton == null) {
            return;
        }

        for (String key : boundEffects.keySet()) {
            Array<BoundEffect> arr = boundEffects.get(key);
            for(BoundEffect eff : arr) {
                eff.reloadEffect(((MainStage)getStage()).loadedVFX.get(eff.getName()).path);
            }
        }
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        detectInputs();

        for(BoundEffect effect: getBoundEffects()) {
            if(!effect.isStopped()) {
                effect.getEffect().update(delta * speedMultiplier);
            }
        }

        if(currentlySelectedEffect != null) {
            fxProperties.updateValues();
        }
    }

    private void toolTip(String text) {
        ((MainStage)getStage()).toolTip(text);
    }

    private Bone getClosestBone(float x, float y, float maxDist) {
        //System.out.println("mouse " + x + "," + y + "   bone: " + skeleton.getRootBone().getWorldX() + "," + skeleton.getRootBone().getWorldY() );
        float minDist = 100000f;
        float dist = 0;
        if(skeleton == null) return null;

        Bone closestBone = skeleton.getRootBone();
        for (Bone bone : skeleton.getBones()) {
            tmp.set(bone.getWorldX(), bone.getWorldY());
            dist = tmp.dst(x, y);
            if(dist < minDist) {
                minDist = dist;
                closestBone = bone;
            }
        }

        if(minDist > maxDist) return null;

        return closestBone;
    }

    private void detectInputs() {

        MainStage mainStage = (MainStage) getStage();

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetPosition();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
            deleteSelectedVFX();
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.N) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            mainStage.newProject();
            return;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.O) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            mainStage.openProject();
            return;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            mainStage.saveProject();
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.E) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            mainStage.exportProject();
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            paused = !paused;

            if(paused == true) {
                toolTip("Stop All Motor Functions!");
            } else {
                toolTip("Resume animation");
            }
        }

        setMousePos();

        if(Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            shiftToggle = !shiftToggle;
        }

        if(shiftToggle) {
            currentMode = Mode.BONE_SWITCH;
            Bone closestBone = getClosestBone(mousePos.x, mousePos.y, 20f);
            if(closestBone != null) {
                mainStage.setHintText(closestBone.toString());
            } else {
                mainStage.setHintText("");
            }
        } else {
             currentMode = Mode.PREVIEW;
        }

        Mode origMode = currentMode;
        if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            currentMode = Mode.OFFSET;
        } else {
            currentMode = origMode;
        }

        if(touchDisabled) return;

        if(Gdx.input.getX() < 188 || getStage().getHeight() - Gdx.input.getY() > getStage().getHeight() - 30) {
            return;
        }

        if(Gdx.input.justTouched()) {
            if(mouseScreenPos.x > fxProperties.getX() && mouseScreenPos.x < fxProperties.getX() + fxProperties.getWidth() &&
                    mouseScreenPos.y > fxProperties.getY() && mouseScreenPos.y < fxProperties.getY() + fxProperties.getHeight()) {
                return;
            }
        }

        boolean justUntouched = false;
        if(!Gdx.input.isTouched() && wasTouched) {
            //mouse up detected

            if(currentlyMovingEffect != null && currentMode == Mode.BONE_SWITCH) {
                currentlyMovingEffect.bind(targetBone.getData().getName().toString(), -targetOffset.x, -targetOffset.y);
            }
            targetBone = null;
            justUntouched = true;
        }

        if(justUntouched) {
            if(mousePos.x > fxProperties.getX() && mousePos.x < fxProperties.getX() + fxProperties.getWidth() &&
                    mousePos.y > fxProperties.getY() && mousePos.y < fxProperties.getY() + fxProperties.getHeight()) {
                wasTouched = false;
                return; // protect the DIALOG at all costs, (jesus it has come to this)
            }
        }

        if(!Gdx.input.isTouched()) {
            wasTouched = false;
            currentlyMovingEffect = null;
            touchDownPos.set(0, 0);
            firstTouchPos.set(0, 0);
        }

        if(Gdx.input.justTouched()) {
            // first touch
            wasTouched = true;
            touchDownPos.set(mousePos);
            firstTouchPos.set(mouseScreenPos);
        }

        // check for non drag behaviour when a particle effect is being moved
        if(Gdx.input.justTouched()) {
            lastTouchWasSelecting = false;
            for(BoundEffect effect : getBoundEffects()) {
                tmp.set(effect.getPosition());
                if(mousePos.dst(tmp) < 10f) {
                    currentlyMovingEffect = effect;
                    selectEffect(effect);
                    lastTouchWasSelecting = true;
                }
            }
        }

        if(justUntouched && !lastTouchWasSelecting) {
            unselectEffect();
        }

        if(Gdx.input.isTouched() && Gdx.input.getX() > 188) {
            currPos.set(mouseScreenPos); // let's use screen coords here
            currPos.sub(lastPos); // curr pos is now offset
            // are we panning?

            if(currentlyMovingEffect != null) {
                // no we are moving an effect
                currentlyMovingEffect.getOffset().add(currPos);
            } else if( firstTouchPos.x > 200) {
                // yeah we are panning
                float zoom = ((OrthographicCamera)viewport.getCamera()).zoom;
                globalOffset.sub(currPos.scl(zoom));
                viewport.getCamera().position.set(globalOffset.x, globalOffset.y, 0f);
            }
        }

        lastPos.set(mouseScreenPos);
    }

    private void setMousePos() {
        tmp.set(Gdx.input.getX()-200, Gdx.input.getY());
        viewport.unproject(tmp);
        mousePos.set(tmp);


        mouseScreenPos.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    private void unselectEffect() {
        currentlySelectedEffect = null;


        fxProperties.setEffect(null);
    }

    private void selectEffect(BoundEffect effect) {
        currentlySelectedEffect = effect;
        fxProperties.setEffect(effect);
        sfxProperties.hide();
    }

    public void drawFBO(Batch batch, float parentAlpha) {
        fbo.begin();
        viewport.update(700, 855);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderGrid(batch);
        if(skeleton != null) {
            batch.getProjectionMatrix().set(viewport.getCamera().combined);
            batch.begin();
            renderBackVFX(batch);
            renderSpine(batch);
            renderVFX(batch);
            batch.end();
            renderTools(batch);
        }

        fbo.end();
    }

    public void draw (Batch batch, float parentAlpha) {

        Texture fboTExture = fbo.getColorBufferTexture();
        Sprite sprite  = new Sprite(fboTExture);
        sprite.setPosition(200, 0);
        sprite.setSize(700, 855);
        sprite.flip(false, true);
        sprite.draw(batch);
    }

    private void renderGrid(Batch batch) {

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        shapeRenderer.setColor(1f, 1f, 1f, 0.1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if(isSpineFileDropActive) {
            shapeRenderer.setColor(1f, 1f, 1f, 0.1f);
            shapeRenderer.rect(-getWidth()/2f + viewport.getCamera().position.x, -getHeight()/2f + viewport.getCamera().position.y, getWidth(), getHeight());
        }

        float lineCount = 40;

        for(int i = 0; i < lineCount; i++) {


            float tileSize = 50f;
            float zoom = ((OrthographicCamera)viewport.getCamera()).zoom;
            int zoomInt = (int) Math.floor(zoom);
            if(zoomInt < 1) zoomInt = 1;
            tileSize *= zoomInt;

            float offsetX = -viewport.getCamera().position.x % tileSize + viewport.getCamera().position.x;
            float offsetY = -viewport.getCamera().position.y % tileSize + viewport.getCamera().position.y;

            float lineThickness = 1f * ((OrthographicCamera)viewport.getCamera()).zoom;

            float finalPos = (i - lineCount/2f) * tileSize;

            float alpha = 0.1f;
            if(i % 2 == 0) alpha = 0.2f;
            shapeRenderer.setColor(1f, 1f, 1f, alpha);

            // horizontal lines
            shapeRenderer.rectLine(
                    -getWidth()*zoom/2f - tileSize + offsetX,
                    finalPos + offsetY,
                    getWidth()*zoom/2f + tileSize + offsetX,
                    finalPos + offsetY,
                    lineThickness);

            // vertical lines
            shapeRenderer.rectLine(
                    finalPos + offsetX,
                    -getHeight()*zoom/2f - tileSize + offsetY,
                    finalPos + offsetX,
                    getHeight()*zoom/2f + tileSize + offsetY,
                    lineThickness);
        }

        shapeRenderer.end();
    }

    private void renderSpine(Batch batch) {

        // this should go to act
        if(!paused) {
            state.update(Gdx.graphics.getDeltaTime() * speedMultiplier); // Update the animation time.
            state.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.\
        }

        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        renderer.draw(batch, skeleton); // Draw the skeleton images.
    }

    private void renderBackVFX(Batch batch) {
        for(BoundEffect effect: getBoundEffects()) {
            if(!effect.isBehind()) continue;
            Bone bone = boneMap.get(effect.getBoneName());
            effect.setPosition(bone.getWorldX() + effect.getOffset().x, bone.getWorldY() + effect.getOffset().y);

            effect.getEffect().draw(batch);
        }
    }

    private void renderVFX(Batch batch) {
        for(BoundEffect effect: getBoundEffects()) {
            if(effect.isBehind()) continue;
            Bone bone = boneMap.get(effect.getBoneName());
            effect.setPosition(bone.getWorldX() + effect.getOffset().x, bone.getWorldY() + effect.getOffset().y);

            effect.getEffect().draw(batch);
        }
    }

    private void renderTools(Batch batch) {
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1f, 0.05f, 0.05f, 0.4f);

        if(currentMode == Mode.BONE_SWITCH) {
            float minDist = 100000f;
            Bone closestBone = skeleton.getRootBone();
            for (Bone bone : skeleton.getBones()) {
                shapeRenderer.circle(bone.getWorldX(), bone.getWorldY(), 3, 10);
                // let's also find the closest bone and draw connection to it
                if(currentlySelectedEffect != null) {
                    tmp.set(bone.getWorldX(), bone.getWorldY());
                    float dist = tmp.dst(currentlySelectedEffect.getPosition());
                    if(dist < minDist) {
                        minDist = dist;
                        closestBone = bone;
                        targetBone = closestBone;
                        targetOffset.set(tmp.sub(currentlySelectedEffect.getPosition()));
                    }
                }
            }

            if(currentlySelectedEffect != null && currentlyMovingEffect != null) {
                // let's draw line to closest bone to current effect
                shapeRenderer.setColor(1f, 1f, 1f, 1f);
                shapeRenderer.rectLine(closestBone.getWorldX(), closestBone.getWorldY(), currentlySelectedEffect.getPosition().x, currentlySelectedEffect.getPosition().y, 2f);
            }
        }
        if(currentMode == Mode.OFFSET) {
            // draw currently bound bone for currently translating effect
            if(currentlySelectedEffect != null) {
                Bone currBone = boneMap.get(currentlySelectedEffect.getBoneName());
                shapeRenderer.setColor(1f, 0.05f, 1f, 0.8f);
                shapeRenderer.circle(currBone.getWorldX(), currBone.getWorldY(), 3, 10);

                // also let's draw a line
                shapeRenderer.rectLine(currBone.getWorldX(), currBone.getWorldY(), currBone.getWorldX() + currentlySelectedEffect.getOffset().x, currBone.getWorldY() + currentlySelectedEffect.getOffset().y, 2f);
            }
        }


        if(currentMode != Mode.PREVIEW) {
            shapeRenderer.setColor(1f, 1f, 0.05f, 1f);
            // render the particle toosl
            for (BoundEffect effect : getBoundEffects()) {
                Bone bone = boneMap.get(effect.getBoneName());
                shapeRenderer.circle(bone.getWorldX() + effect.getOffset().x, bone.getWorldY() + effect.getOffset().y, 6, 10);

            }
        }


        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public ExportData getExportData() {
        ExportData exportData = new ExportData();

        for (Map.Entry<String, Array<BoundEffect>> entry : boundEffects.entrySet()) {
            String animName = entry.getKey();
            Array<BoundEffect> value = entry.getValue();

            if(!exportData.boundVFXList.containsKey(animName)) {
                exportData.boundVFXList.put(animName, new Array<VFXExportData>());
            }

            for(BoundEffect be: value) {
                VFXExportData vfx = new VFXExportData();
                vfx.vfxName = be.getName();
                vfx.boneName = be.getBoneName();
                vfx.offset = new Vector2(be.getOffset());
                vfx.startEvent = be.getStartEvent();
                vfx.endEvent = be.getEndEvent();
                vfx.isBehind = be.isBehind;
                vfx.scale = be.scale;

                exportData.boundVFXList.get(animName).add(vfx);
            }
        }

        exportData.sfxExportData.clear();
        HashMap<String, SFXListModel> sfxList = ((MainStage)getStage()).getSFXList();
        for (SFXListModel model : sfxList.values()) {
            SFXExportData sfx = new SFXExportData();
            sfx.sfxName = model.getName();
            sfx.sfxExtension = "ogg";
            sfx.playEvent = model.getPlayEvent();
            exportData.sfxExportData.add(sfx);
        }

        return exportData;
    }

    public void initData(ExportData exportData) {
        boundEffects.clear();

        MainStage mainStage = (MainStage) getStage();

        for (Map.Entry<String,Array<VFXExportData>> entry : exportData.boundVFXList.entrySet()) {
            String animName = entry.getKey();
            Array<VFXExportData> vfxList = entry.getValue();

            if(!boundEffects.containsKey(animName)) {
                boundEffects.put(animName, new Array<BoundEffect>());
            }

            for(VFXExportData dt: vfxList) {
                BoundEffect be = new BoundEffect();
                be.load(mainStage.loadedVFX.get(dt.vfxName).path);
                be.bind(dt.boneName, dt.offset.x, dt.offset.y);
                be.setStartEvent(dt.startEvent);
                be.setEndEvent(dt.endEvent);
                be.isBehind = dt.isBehind;
                be.setScale(dt.scale);
                boundEffects.get(animName).add(be);
            }
        }

        String projectPath = mainStage.getProjectPath();


        for(SFXExportData dt: exportData.sfxExportData) {
            mainStage.dropSFX(projectPath + "\\" + dt.sfxName + "." + dt.sfxExtension);
            if(mainStage.loadedSFX.get(dt.sfxName) != null) {
                mainStage.loadedSFX.get(dt.sfxName).setPlayEvent(dt.playEvent);
            }
        }

    }

}
