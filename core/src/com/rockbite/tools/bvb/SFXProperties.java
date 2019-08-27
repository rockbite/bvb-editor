package com.rockbite.tools.bvb;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class SFXProperties extends FXProperties<SFXListModel> {

    Label title;

    SelectBox<String> startEventValue;

    ChangeListener l1;

    SFXListModel effect;

    public SFXProperties(Skin skin) {

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

        final Label startEventLabel = new Label("Play Event:", getSkin(), "small-font", "white");
        startEventValue = new SelectBox<String>(getSkin());

        Table seTbl = new Table();
        seTbl.add(startEventLabel).left().expandX();
        seTbl.add().width(15);
        seTbl.add(startEventValue).right().width(105);
        body.add(seTbl).colspan(2).expandX().padBottom(4).padTop(4);
        body.row();

        body.add().expand().fill().grow();

        // listeners for other non select box stuff

        l1 = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(effect == null) return;
                effect.setPlayEvent(startEventValue.getSelected());
            }
        };

    }

    public void hide() {
        addAction(Actions.fadeOut(0.2f));
    }

    public void show() {
        addAction(Actions.fadeIn(0.2f));
    }

    @Override
    public void setEffect(SFXListModel effect) {
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
            title.setText(effect.getName() + ".ogg");
        }

        startEventValue.setSelected(effect.getPlayEvent());
    }

    @Override
    public void updateEventList(Array<String> eventList) {
        startEventValue.removeListener(l1);

        Array<String> tmp = new Array<String>();
        tmp.add("");
        tmp.addAll(eventList);

        startEventValue.clearItems();
        startEventValue.setItems(tmp);

        // listeners
        startEventValue.addListener(l1);
    }

    private String f(float f) {
        return String.format("%.02f", f);
    }

    private String s(String s) {
        if(s.length() < 11) return s;
        return s.substring(0, 11);
    }
}
