package com.rockbite.tools.bvb;

import com.badlogic.gdx.graphics.Color;

public interface IFXListModel {
    public boolean wasLoaded();

    public String getName();

    public String getPath();

    public boolean isVFX();

    public Color getColor();
}
