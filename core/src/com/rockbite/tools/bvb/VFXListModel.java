package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.util.dialog.Dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class VFXListModel implements IFXListModel {

    public String path;

    public String dirPath;

    public String effectName;

    public boolean exists = false;

    public long lastModified;

    private MainStage stage;

    public VFXListModel(MainStage stage, String pPath) {
        this.stage = stage;
        FileHandle handle = Gdx.files.absolute(pPath);
        if(!handle.extension().equals("p")) {
            Dialogs.showErrorDialog (stage, "VFX File must have .p extension");
            return;
        }

        if(handle.exists()) {
            exists = true;
        } else {
            return;
        }

        lastModified = handle.lastModified();

        path = pPath;
        dirPath = handle.parent().path();

        // check for missing images
        ObjectSet<String> errorPaths = new ObjectSet<String>();
        ParticleEffect tmp = new ParticleEffect();
        tmp.loadEmitters(handle);
        for(ParticleEmitter emitter : tmp.getEmitters()) {
            if (emitter.getImagePaths().size == 0) continue;
            for (String imagePath : emitter.getImagePaths()) {
                String imageName = new File(imagePath.replace('\\', '/')).getName();
                FileHandle imgHandle = Gdx.files.absolute(dirPath + "\\" + imageName);
                if(!imgHandle.exists()) {
                    errorPaths.add(imgHandle.path());
                }
            }
        }

        tryAndFixErrorPaths(errorPaths);

        if(errorPaths.size > 0) {
            exists = false;
            String details = "I was looking for the following paths, and they were not found: \n\n";
            for(String path : errorPaths) {
                details += path + "\n";
            }

            details += "\n please note that textures should be in the same location as the .p file.";

            Dialogs.showErrorDialog (stage, "Some Images from this VFX are not found, check details for more info", details);
            return;
        }

        effectName = handle.nameWithoutExtension();
    }

    public void tryAndFixErrorPaths(ObjectSet<String> errorPaths) {
        String userFolder = System.getProperty("user.home");

        File dir = new File(userFolder);

        File[] matches = dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith("Dropbox");
            }
        });

        Array<String> fixedPaths = new Array<String>();

        //String append = "\\Rockbite Games Team Folder\\projects\\sandship\\editor_project\\zed_project\\assets\\raws\\vfx\\particles";
        String append = "\\Rockbite Games Team Folder\\projects\\sandship\\RefactoredProduction\\Particles-Assets";
        for(int i = 0; i < matches.length; i++) {
            File finalDir = new File(matches[i].getAbsolutePath() + append);
            if(finalDir.exists()) {
                // start fixing errors
                for(String errPath : errorPaths) {
                    File err = new File(errPath);
                    File tmp = new File(finalDir + "\\" + err.getName());
                    if(tmp.exists()) {
                        // copy that file
                        Runtime rt = Runtime.getRuntime();
                        try {
                            //String command = "copy \"" + tmp.getAbsolutePath() + "\" \"" + err.getParent() + "\\" + err.getName()+"\"";
                            //System.out.println(command);
                            //Process pr = rt.exec(command);

                            stage.copyFile(tmp, err);
                            // remove from arr
                            fixedPaths.add(errPath);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                for(String pth: fixedPaths) {
                    errorPaths.remove(pth);
                }

                if(fixedPaths.size > 0) {
                    stage.toolTip("Missing files found in Dropbox and replaced, because ***magic****");
                }

                return;
            }
        }
    }

    @Override
    public String toString() {
        return effectName;
    }

    @Override
    public boolean wasLoaded() {
        return exists;
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
        return true;
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }
}