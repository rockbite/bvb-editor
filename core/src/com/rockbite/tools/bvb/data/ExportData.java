package com.rockbite.tools.bvb.data;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ExportData {

    public HashMap<String, Array<VFXExportData>> boundVFXList = new HashMap<String, Array<VFXExportData>>();
    public transient HashMap<String, HashMap<String, Array<VFXExportData>>> newBoundVFXList = new HashMap<String, HashMap<String, Array<VFXExportData>>>();

    public Array<SFXExportData> sfxExportData = new Array<SFXExportData>();

    public String writeXML() {
        StringWriter writer = new StringWriter();
        XmlWriter xml = new XmlWriter(writer);

        try {
            xml.element("bindings");
            xml.element("vfx");

            for (Map.Entry<String,Array<VFXExportData>> entry : boundVFXList.entrySet()) {
                String animName = entry.getKey();
                Array<VFXExportData> vfxArray = entry.getValue();
                for(VFXExportData dt: vfxArray) {
                    xml.element("effect").attribute("skin", "default")
                            .attribute("animation", animName)
                    .attribute("asset", dt.vfxName)
                    .attribute("bone", dt.boneName)
                    .attribute("offsetX", dt.offset.x)
                    .attribute("offsetY", dt.offset.y)
                    .attribute("startEvent", dt.startEvent)
                    .attribute("endEvent", dt.endEvent)
                    .attribute("behind", dt.isBehind)
                    .attribute("scale", dt.scale);

                    xml.pop();
                }
            }
            xml.pop();

            xml.element("sfx");
            for(SFXExportData sfx :sfxExportData) {
                xml.element("sound").attribute("asset", sfx.sfxName)
                .attribute("extension", sfx.sfxExtension)
                .attribute("event", sfx.playEvent);
                xml.pop();
            }
            xml.pop();

            xml.pop();

            xml.flush();

            return writer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
