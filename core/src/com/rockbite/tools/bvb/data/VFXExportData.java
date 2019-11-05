package com.rockbite.tools.bvb.data;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class VFXExportData implements Json.Serializable {

    public String vfxName;

    public String boneName;

    public Vector2 offset;

    public String startEvent;

    public String endEvent;

    public boolean isBehind;

    public float scale;


    @Override
    public void write (Json json) {
        json.writeValue("effectName", vfxName);
        json.writeValue("isBehind", isBehind);
        Json.Serializable serializable = new Json.Serializable() {

            @Override
            public void write (Json json) {
                json.writeValue("type", "ATTACHED");
                json.writeValue("attachmentType", "POSITION");
                json.writeValue("boneName", boneName);

                final Json.Serializable offsetJson = new Json.Serializable() {
                    @Override
                    public void write (Json json) {
                        json.writeValue("x", offset.x);
                        json.writeValue("y", offset.y);
                    }

                    @Override
                    public void read (Json json, JsonValue jsonData) {

                    }
                };

                json.writeValue("offset", offsetJson);
            }

            @Override
            public void read (Json json, JsonValue jsonData) {

            }
        };
        json.writeValue("positionAttachment", serializable);
        json.writeArrayStart("valueAttachments");
        json.writeArrayEnd();
        json.writeValue("startEvent", startEvent);
        json.writeValue("endEvent", endEvent);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {

    }
}