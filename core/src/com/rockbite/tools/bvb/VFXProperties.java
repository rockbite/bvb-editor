package com.rockbite.tools.bvb;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class VFXProperties extends FXProperties<BoundEffect> {

    Label title;

    Label offsetXValue;
    Label offsetYValue;
    Label boneNameValue;
    CheckBox bindRotationValue;
    CheckBox bindTransparencyValue;

    CheckBox isBehind;

    SelectBox<String> startEventValue;
    SelectBox<String> endEventValue;

    TextField scaleValue;

    ChangeListener l1, l2;

    BoundEffect effect;

    public VFXProperties(Skin skin) {

        setSkin(skin);

        setSize(198+150-10, 250);
        //setPosition(getWidth()-getWidth(), getHeight() - getHeight() - 27f);
        setPosition(-1, 0);

        title = new Label("test.p", getSkin());
        title.setColor(0.5f, 0.5f, 0.5f, 1f);
        title.setPosition(6, getHeight() - title.getHeight() - 2);
        addActor(title);

        setBackground(skin.getDrawable("em-item-frame"));

        Table body = new Table();
        add(body).expand().fill().grow();

        Label offsetXLabel = new Label("Offset:", getSkin(), "small-font", "white");
        offsetXValue = new Label("100", getSkin(), "small-font", "white"); offsetXValue.setAlignment(Align.right);
        Label offsetYLabel = new Label("Offset:", getSkin(), "small-font", "white");
        offsetYValue = new Label("100", getSkin(), "small-font", "white"); offsetYValue.setAlignment(Align.right);

        Label boneNameLabel = new Label("Bone:", getSkin(), "small-font", "white");
        boneNameValue = new Label("test_mest_b", getSkin(), "small-font", "white"); boneNameValue.setAlignment(Align.right);

        Label bindRotation = new Label("Bind Rotation:", getSkin(), "small-font", "white");
        bindRotationValue = new CheckBox("", getSkin());
        Label bindTransparency = new Label("Bind Alpha:", getSkin(), "small-font", "white");
        bindTransparencyValue = new CheckBox("", getSkin());

        Label startEventLabel = new Label("Start Event:", getSkin(), "small-font", "white");
        startEventValue = new SelectBox<String>(getSkin());
        Label endEventLabel = new Label("End Event:", getSkin(), "small-font", "white");
        endEventValue = new SelectBox(getSkin());

        Label scaleLabel = new Label("VFX Scale:", getSkin(), "small-font", "white");
        scaleValue = new TextField("0.6", getSkin());

        Label isBehindLabel = new Label("Is Behind:", getSkin(), "small-font", "white");
        isBehind = new CheckBox("", getSkin());


        body.add(offsetXLabel).left().expandX();
        body.add(offsetXValue).right().width(80);
        body.row();
        body.add(offsetYLabel).left().expandX();
        body.add(offsetYValue).right().width(80);
        body.row();
        body.add(boneNameLabel).left();
        body.add(boneNameValue).right().width(80);
        body.row().padBottom(5);
        /*
        body.add(bindRotation).left().expandX();
        body.add(bindRotationValue).right().width(50);
        body.row();
        body.add(bindTransparency).left().expandX();
        body.add(bindTransparencyValue).right().width(50);
        body.row();*/

        body.add(isBehindLabel).left().expandX();
        body.add(isBehind).right().width(50);
        body.row();

        Table seTbl = new Table();
        seTbl.add(startEventLabel).left().expandX();
        seTbl.add().width(15);
        seTbl.add(startEventValue).right().width(105+140);
        body.add(seTbl).colspan(2).expandX().padBottom(4).padTop(4);
        body.row();

        Table eeTbl = new Table();
        eeTbl.add(endEventLabel).left().expandX();
        eeTbl.add().width(19);
        eeTbl.add(endEventValue).right().width(105+140);
        body.add(eeTbl).colspan(2).expandX().padBottom(4);

        body.row();

        body.add(scaleLabel).left();
        body.add(scaleValue).right().width(80);
        body.row().padBottom(5);

        body.add().expand().fill().grow();

        // listeners for other non select box stuff

        l1 = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(effect == null) return;
                effect.setStartEvent(startEventValue.getSelected());
            }
        };

        l2 = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(effect == null) return;
                effect.setEndEvent(endEventValue.getSelected());
            }
        };

        scaleValue.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(effect == null) return;
                if(scaleValue.getText().equals("")) scaleValue.setText("0");
                float val = Float.parseFloat(scaleValue.getText());
                effect.scale = val;
                if(val == 0) return;
                effect.setScale(val);
            }
        });

        isBehind.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(effect == null) return;
                effect.setBehind(isBehind.isChecked());
            }
        });
    }

    public void hide() {
        addAction(Actions.fadeOut(0.2f));
    }

    public void show() {
        addAction(Actions.fadeIn(0.2f));
    }

    @Override
    void setEffect(BoundEffect effect) {
        if(effect != null) {
            show();
        } else {
            hide();
        }

        this.effect = effect;

        updateValues();
    }

    @Override
    public void updateValues() {
        if(effect != null) {
            title.setText(effect.getName() + ".p");
            offsetXValue.setText(f(effect.getOffset().x));
            offsetYValue.setText(f(effect.getOffset().y));

            boneNameValue.setText(s(effect.getBoneName()));

            try {
                if (Float.parseFloat(scaleValue.getText()) != effect.scale) {
                    scaleValue.setText(effect.scale + "");
                }
            } catch (Exception e) {

            }

            isBehind.setChecked(effect.isBehind);

            startEventValue.setSelected(effect.getStartEvent());
            endEventValue.setSelected(effect.getEndEvent());
        }
    }

    @Override
    public void updateEventList(Array<String> eventList) {
        startEventValue.removeListener(l1);
        endEventValue.removeListener(l2);

        Array<String> tmp = new Array<String>();
        tmp.add("");
        tmp.addAll(eventList);

        startEventValue.clearItems();
        startEventValue.setItems(tmp);

        endEventValue.clearItems();
        endEventValue.setItems(tmp);

        // listeners
        startEventValue.addListener(l1);

        endEventValue.addListener(l2);
    }

    private String f(float f) {
        return String.format("%.02f", f);
    }

    private String s(String s) {
        if(s.length() < 11) return s;
        return s.substring(0, 11);
    }
}
