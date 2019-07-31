package com.rockbite.tools.bvb;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import javax.swing.tree.ExpandVetoException;

public class FXListAdapter<ItemT extends IFXListModel> extends ArrayAdapter<ItemT, VisTable> {
    private final FXListAdapter.FXListAdapterStyle style;

    private MainStage stage;

    public FXListAdapter(MainStage stage, Array<ItemT> array) {
        this(stage, array, "default");
    }

    public FXListAdapter(MainStage stage, Array<ItemT> array, String styleName) {
        this(stage, array, VisUI.getSkin().get(styleName, FXListAdapter.FXListAdapterStyle.class));
    }

    public FXListAdapter(MainStage stage, Array<ItemT> array, FXListAdapter.FXListAdapterStyle style) {
        super(array);
        this.style = style;
        this.stage = stage;
    }

    @Override
    protected VisTable createView (final ItemT item) {
        VisTable table = new VisTable();
        table.left();
        final ModelLabel label = new ModelLabel(item);
        label.setColor(item.getColor());
        table.add(label);


        label.addListener((new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ModelLabel ml = (ModelLabel)(event.getTarget());
                IFXListModel model = ml.getModel();
                if(!model.isVFX()) {
                    // is SFX
                    stage.showSFXProperties((SFXListModel) model);
                }
            }
        }));

        // init left panel drag drop
        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.addSource(new DragAndDrop.Source(label) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(item);

                payload.setDragActor(new Label(label.getText(), stage.getSkin()));

                return payload;
            }
        });
        dragAndDrop.addTarget(new DragAndDrop.Target(stage.previewWidget) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                //stage.spineDropTargetActivate(true);
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Object object = payload.getObject();
                if(object instanceof IFXListModel) {
                    IFXListModel fx = (IFXListModel) object;
                    if(fx.isVFX()) {
                        VFXListModel vfx = (VFXListModel) fx;
                        stage.instantiateVFX((vfx).toString(), x , y);
                    }
                }
            }
        });


        return table;
    }

    class ModelLabel extends VisLabel {

        private IFXListModel payload;

        public ModelLabel(IFXListModel model) {
            super(model.toString());
            this.payload = model;
        }

        public IFXListModel getModel() {
            return payload;
        }
    }

    @Override
    protected void selectView (VisTable view) {
        view.setBackground(style.selection);
    }

    @Override
    protected void deselectView (VisTable view) {
        view.setBackground(style.background);
    }

    public static class FXListAdapterStyle {
        public Drawable background;
        public Drawable selection;

        public FXListAdapterStyle () {
        }

        public FXListAdapterStyle (Drawable background, Drawable selection) {
            this.background = background;
            this.selection = selection;
        }

        public FXListAdapterStyle (SimpleListAdapter.SimpleListAdapterStyle style) {
            this.background = style.background;
            this.selection = style.selection;
        }
    }
}
