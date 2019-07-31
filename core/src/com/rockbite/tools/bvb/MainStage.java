package com.rockbite.tools.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.Skeleton;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.tools.bvb.data.ExportData;
import com.rockbite.tools.bvb.data.ProjectData;
import com.rockbite.tools.bvb.data.SFXExportData;
import com.rockbite.tools.bvb.data.VFXExportData;

import java.io.*;
import java.util.HashMap;


public class MainStage extends Stage {

    Kryo kryo;

    PreviewWidget previewWidget;
    PopupMenu animationsListMenu;
    Table leftTable;
    TextureAtlas atlas;
    Table rightTable;

    HashMap<String, VFXListModel> loadedVFX = new HashMap<String, VFXListModel>();
    HashMap<String, SFXListModel> loadedSFX = new HashMap<String, SFXListModel>();

    FXListAdapter<IFXListModel> fxListAdapter;

    Skin skin;
    private String spineJsonPath;

    Label leftHintLabel;

    VFXProperties effectWidget;
    SFXProperties sfxWidget;

    float timePassed = 0;
    private String projectPath;
    private String projectFilePath;
    private String exportFilePath;

    public MainStage() {
        super(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()),
                new PolygonSpriteBatch());
        //setDebugAll(true); // remove this later

        kryo = new Kryo();

        kryo.register(Array.class, new ArraySerializer(), 10);
        kryo.register(ProjectData.class, 11);
        kryo.register(ExportData.class, 12);
        kryo.register(VFXExportData.class, 13);
        kryo.register(HashMap.class, 14);
        kryo.register(Vector2.class, 15);
        kryo.register(SFXExportData.class, 16);

        Gdx.input.setInputProcessor(this);

        /**
         * Initializing UI
         */
        atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.addRegions(atlas);

        VisUI.load(skin);

        Table mainTable = new Table();
        mainTable.setFillParent(true);

        /**
         * Layout
         */

        Table topTable = new Table();
        leftTable = new Table();
        rightTable = new Table();

        mainTable.add(topTable).expandX().top().colspan(2).fillX();
        mainTable.row();
        SplitPane splitPane = new SplitPane(leftTable, rightTable, false, skin);
        splitPane.setSplitAmount(0.22f);
        mainTable.add(splitPane).expand().fill();

        addListener(new InputListener() {
            public boolean scrolled (InputEvent event, float x, float y, int amount) {
                previewWidget.scrolled(amount);
                return false;
            }
        });

        /**
         * Content
         */

        //top panel

        ChangeListener menuListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String text = ((MenuItem)actor).getLabel().getText().toString();

                // Bunch of crap because I was lazy to read the docs

                if(text.equals("New Project")) {
                    newProject();
                } else if(text.equals("Open Project")) {
                    openProject();
                } else if(text.equals("Save Project")) {
                   saveProject();
                } else if(text.equals("Export Binding File")) {
                   exportProject();
                } else if(text.equals("Exit")) {
                    Gdx.app.exit();
                }


                if(text.equals("Play")) {
                    previewWidget.play();
                }
                if(text.equals("Pause")) {
                    previewWidget.pause();
                }

                if(text.equals("Delete Selected VFX")) {
                    previewWidget.deleteSelectedVFX();
                }

                if(text.equals("Camera Reset")) {
                    previewWidget.resetPosition();
                }

                if(text.equals("Help")) {
                    Dialogs.showOKDialog (MainStage.this, "Help", " Drag & Drop .json to the right and .p files to the left panels \n to load Spine and VFX effects. \n\n Hold Shift for Bone Binding Mode \n Hold Alt for bone Offset Mode \n\n ");
                }
                if(text.equals("About")) {
                    Dialogs.showOKDialog (MainStage.this, "About BVB Editor V 1.0", "Au, Au, Au. EGEGEY");
                }
            }
        };

        MenuBar menuBar = new MenuBar();
        topTable.add(menuBar.getTable()).fillX().expandX();


        Menu projectMenu = new Menu("File");
        //projectMenu.addItem(new MenuItem("New Project", new Image(atlas.findRegion("ic-file-new")), menuListener).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.N));
        projectMenu.addItem(new MenuItem("Open Project", new Image(atlas.findRegion("ic-folder")), menuListener).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.O));
        projectMenu.addItem(new MenuItem("Save Project", new Image(atlas.findRegion("ic-save")), menuListener).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.S));
        projectMenu.addSeparator();
        projectMenu.addItem(new MenuItem("Export Binding File", new Image(atlas.findRegion("ic-download")), menuListener).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.E));
        projectMenu.addSeparator();
        projectMenu.addItem(new MenuItem("Exit", menuListener));
        menuBar.addMenu(projectMenu);

        Menu viewMenu = new Menu("View");
        viewMenu.addItem(new MenuItem("Camera Reset", menuListener).setShortcut(Input.Keys.ENTER));
        menuBar.addMenu(viewMenu);

        final Menu animationMenu = new Menu("Animation");
        MenuItem animations = new MenuItem("Animations");
        animationsListMenu = new PopupMenu();
        animations.setSubMenu(animationsListMenu);
        animationMenu.addItem(animations);
        animationMenu.addSeparator();
        animationMenu.addItem(new MenuItem("Play", menuListener).setShortcut(Input.Keys.SPACE));
        animationMenu.addItem(new MenuItem("Pause", menuListener).setShortcut(Input.Keys.SPACE));
        animationMenu.addItem(new MenuItem("Delete Selected VFX", menuListener).setShortcut(Input.Keys.FORWARD_DEL));
        menuBar.addMenu(animationMenu);

        Menu helpMenu = new Menu("Help");
        helpMenu.addItem(new MenuItem("Help", menuListener));
        helpMenu.addItem(new MenuItem("About", menuListener));
        menuBar.addMenu(helpMenu);


        // left panel
        fxListAdapter = new FXListAdapter<IFXListModel>(this, new Array<IFXListModel>());
        ListView<IFXListModel> view = new ListView<IFXListModel>(fxListAdapter);
        leftTable.add(view.getMainTable()).top().expandY().grow();
        view.getMainTable().setTouchable(Touchable.enabled);
        view.setItemClickListener(new ListView.ItemClickListener<IFXListModel>() {
            @Override
            public void clicked(IFXListModel item) {
                //System.out.println(item);
            }
        });


        // right widget
        previewWidget = new PreviewWidget();
        rightTable.add(previewWidget).fill().expand();

        // adding the effect widget as pnael View
        effectWidget = new VFXProperties(skin);
        sfxWidget = new SFXProperties(skin);
        previewWidget.setPropertiesPanel(effectWidget, sfxWidget);

        leftHintLabel = new Label("", skin);

        final VisSlider visSlider = new VisSlider(25, 400, 25, false);
        visSlider.setValue(100);
        visSlider.setPosition(getWidth() - visSlider.getWidth() - 10, getHeight() - visSlider.getHeight());
        final Label speed = new Label("speed: 100%", skin);
        speed.setPosition(visSlider.getX() - speed.getWidth() - 5, visSlider.getY());

        /**
         * finalizing
         */

        addActor(mainTable);

        addActor(effectWidget); effectWidget.hide();
        addActor(sfxWidget); sfxWidget.hide();

        addActor(leftHintLabel);

        addActor(visSlider);
        addActor(speed);


        // listeners
        previewWidget.setListener(new PreviewWidget.ChangeListener() {
            @Override
            public void onAnimationChanged(Skeleton skeleton) {
                animationsListMenu.clear();
                for(Animation anim : skeleton.getData().getAnimations()) {
                    final Animation targetAnim = anim;
                    animationsListMenu.addItem(new MenuItem(anim.getName(), new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            previewWidget.changeAnimation(targetAnim);
                        }
                    }));
                }
            }
        });

        visSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                speed.setText("speed: " + visSlider.getValue() + "%");
                speed.setPosition(visSlider.getX() - speed.getWidth() - 5, visSlider.getY());
                previewWidget.setSpeed(visSlider.getValue());
            }
        });
    }

    @Override
    public void draw () {
        previewWidget.drawFBO(getBatch(), 1f);

        super.draw();
    }

    public void openProject() {
        previewWidget.disableTouch();
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setHeight(400);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                previewWidget.enableTouch();
                loadProjectFile(file.get(0).path());
            }
        });
        addActor(fileChooser.fadeIn());
    }

    public void newProject() {

    }

    public void exportProject() {
        if((exportFilePath == null || exportFilePath.equals("")) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            previewWidget.disableTouch();
            FileChooser fileChooser = new FileChooser(FileChooser.Mode.SAVE);
            fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setHeight(400);
            fileChooser.setListener(new FileChooserAdapter() {
                @Override
                public void selected(Array<FileHandle> file) {
                    previewWidget.enableTouch();
                    String path = file.get(0).path();
                    exportFilePath = path;
                    exportToFile(path);
                }
            });
            addActor(fileChooser.fadeIn());
        } else {
            exportToFile(exportFilePath);
        }

    }


    public void saveProject() {
        if((projectFilePath == null || projectFilePath.equals(""))) {
            previewWidget.disableTouch();
            FileChooser fileChooser = new FileChooser(FileChooser.Mode.SAVE);
            fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setHeight(400);
            fileChooser.setListener(new FileChooserAdapter() {
                @Override
                public void selected (Array<FileHandle> file) {
                    previewWidget.enableTouch();
                    String path = file.get(0).path();
                    projectFilePath = path;
                    saveProjectFile(path);
                }
            });
            addActor(fileChooser.fadeIn());
        } else {
            saveProjectFile(projectFilePath);
        }
    }

    @Override
    public void dispose () {
        super.dispose();
        VisUI.dispose();
        previewWidget.dispose();
    }

    public void vfxDropTargetActivate(boolean activate) {
        // do nothing because whatever
        if(activate) {
            leftTable.setBackground(new TextureRegionDrawable(atlas.findRegion("slider-knob-over")).tint(new Color(1f, 1f, 1f, 0.2f)));
        } else {
            leftTable.setBackground(new TextureRegionDrawable(atlas.findRegion("slider-knob-over")).tint(new Color(1f, 1f, 1f, 0.0f)));
        }

    }

    public void spineDropTargetActivate(boolean activate) {
        previewWidget.setActivateForDrop(activate);
    }

    public void dropMainFile(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        if(fileHandle.extension().equals("json")) {
            dropSpine(path);
        } else if(fileHandle.extension().equals("bvb")) {
            dropBVB(path);
        } else {
            Dialogs.showErrorDialog (this, "You can only drop Spine(.json) or Project(.bvb) files here");
        }
    }

    public void dropBVB(String path) {
        loadProjectFile(path);
    }

    public void dropSpine(String path) {
        previewWidget.initSpine(path, path);
        spineJsonPath = path;
    }

    public void dropLibraryFile(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        if(fileHandle.extension().equals("p")) {
            dropVFX(path);
        }

        if(fileHandle.extension().equals("ogg")) {
            dropSFX(path);
        }
    }

    public void dropVFX(String path) {
        VFXListModel model = new VFXListModel(this, path);
        if(loadedVFX.get(model.toString()) != null) return;
        if(!model.wasLoaded()) return;
        fxListAdapter.add(model);

        loadedVFX.put(model.toString(), model);
    }

    public void dropSFX(String path) {
        SFXListModel model = new SFXListModel(this, path);
        if(loadedVFX.get(model.toString()) != null) return;
        if(!model.wasLoaded()) return;
        fxListAdapter.add(model);

        loadedSFX.put(model.toString(), model);
    }

    public Skin getSkin() {
        return skin;
    }

    public void instantiateVFX(String vfxName, float x, float y) {
        VFXListModel model = loadedVFX.get(vfxName);

        previewWidget.addParticle(model);
    }

    public void toolTip(String text) {

        final Label label = new Label(text, skin);
        label.setScale(2f);
        addActor(label);

        label.setPosition(getWidth()/2f - label.getWidth()/2f + 90, (float) (10f + Math.random()*20f));

        label.addAction(Actions.parallel(Actions.fadeOut(2f), Actions.sequence(
                Actions.moveBy(0, 100f, 2.2f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.remove();
                    }
                })
        )));
    }

    public ProjectData getProjectData() {
        ProjectData projectData = new ProjectData();

        projectData.exportData = previewWidget.getExportData();

        projectData.spineJsonPath = spineJsonPath;

        projectData.vfxPaths = new Array<String>();

        for (VFXListModel model : loadedVFX.values()) {
            projectData.vfxPaths.add(model.path);
        }

        projectData.canvasOffsetX = previewWidget.getX();
        projectData.canvasOffsetY = previewWidget.getY();

        return projectData;
    }

    public void saveProjectFile(String path) {
        ProjectData projectData = getProjectData();

        try {
            Output output = new Output(new FileOutputStream(path));
            kryo.writeObject(output, projectData);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void exportToFile(String path) {
        ExportData exportData = previewWidget.getExportData();

       FileHandle fileHandle = Gdx.files.absolute(path);
       fileHandle.writeBytes(exportData.writeXML().getBytes(), false);
    }

    public void loadProjectFile(final String path) {
        try {
            com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(new FileInputStream(path));
            final ProjectData projectData = kryo.readObject(input, ProjectData.class);
            input.close();


            FileHandle prj = Gdx.files.absolute(path);
            projectPath = prj.parent().path();
            projectFilePath = path;

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    // now let's use this project data
                    spineJsonPath = projectData.spineJsonPath;

                    previewWidget.initSpine(spineJsonPath, path);

                    // now load VFX list
                    fxListAdapter.clear();
                    loadedVFX.clear();
                    for(String vfxPath: projectData.vfxPaths) {
                        VFXListModel model = new VFXListModel(MainStage.this, vfxPath);
                        if(loadedVFX.get(model.toString()) != null) return;
                        if(!model.exists) return;

                        fxListAdapter.add(model);
                        loadedVFX.put(model.toString(), model);
                    }

                    // Now need to reload bounds
                    previewWidget.initData(projectData.exportData);

                    previewWidget.setCanvas(projectData.canvasOffsetX, projectData.canvasOffsetY);
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Dialogs.showErrorDialog (MainStage.this, "Unable to open this project file, either wrong file or older format. either way you are screwed.");
        }
    }

    @Override
    public void act (float delta) {
        timePassed += delta;
        if(timePassed > 1f) {
            timePassed = 0f;
            checkModifiedFiles();
        }
        super.act(delta);
    }

    private void checkModifiedFiles() {
        for (VFXListModel model : loadedVFX.values()) {
            String path = model.path;
            File file = new File(path);
            long date = file.lastModified();
            if(date > model.lastModified) {
                reloadVFX(model);
            }
        }
    }

    public void setHintText(String text) {
        leftHintLabel.setText(text);
        leftHintLabel.setPosition(205, getHeight() - leftHintLabel.getHeight() - 35);
    }

    private void reloadVFX(VFXListModel oldModel) {
        fxListAdapter.removeValue(oldModel, true);

        String path = oldModel.path;

        VFXListModel model = new VFXListModel(this, path);

        if(!model.wasLoaded()) return;

        fxListAdapter.add(model);

        loadedVFX.put(model.toString(), model);

        // need to reload all bone stuff with new VFX
        previewWidget.reloadVFX(model.getName());
    }



    public static void copyFile(File from, File to)  throws IOException {

        File copied = to;
        InputStream in = new BufferedInputStream( new FileInputStream(from));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(copied));

        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, lengthRead);
            out.flush();
        }
    }

    public void showSFXProperties(SFXListModel sfx) {
        if(!previewWidget.isSpineLoaded()) return;

        effectWidget.hide();
        sfxWidget.setEffect(sfx);
    }

    public void spineEvent(String eventName) {
        // sounds
        for (SFXListModel model : loadedSFX.values()) {
            if(model.getPlayEvent().equals(eventName)) {
                model.play();
            }
        }
    }

    public HashMap<String, SFXListModel> getSFXList() {
        return loadedSFX;
    }

    public String getProjectPath() {
        return projectPath;
    }
}



