package com.rockbite.tools.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public abstract class FXProperties<T> extends Table {

    abstract void updateEventList(Array<String> eventList);
    abstract void updateValues();
    abstract void setEffect(T object);

    void hide() {

    }
}
