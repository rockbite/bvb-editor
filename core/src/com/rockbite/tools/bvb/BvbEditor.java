package com.rockbite.tools.bvb;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kotcrab.vis.ui.VisUI;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BvbEditor extends ApplicationAdapter implements DropTargetListener{

	MainStage mainStage;

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		mainStage.resize(width, height);
	}

	@Override
	public void create () {
		mainStage = new MainStage();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mainStage.act();
		mainStage.draw();
	}
	
	@Override
	public void dispose () {
		mainStage.dispose();
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if(dtde.getLocation().x < 184) {
			mainStage.vfxDropTargetActivate(true);
			mainStage.spineDropTargetActivate(false);
		}

		if(dtde.getLocation().x > 188) {
			mainStage.vfxDropTargetActivate(false);
			mainStage.spineDropTargetActivate(true);
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		mainStage.vfxDropTargetActivate(false);
		mainStage.spineDropTargetActivate(false);
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

		Transferable t= dtde.getTransferable();
		if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

			List<File> list = null;
			try {
				list = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] paths = new String[list.size()];
				for(int i = 0; i < list.size(); i++) {
					paths[i] = list.get(i).getAbsolutePath();
				}

				if(paths.length == 1) {
					dtde.dropComplete(true);

					if(dtde.getLocation().x < 184+150) {
						mainStage.dropLibraryFile(paths[0]);
					}

					if(dtde.getLocation().x > 188+150) {
						mainStage.dropMainFile(paths[0]);
					}

				} else {
					dtde.dropComplete(false);
				}

		}

		mainStage.spineDropTargetActivate(false);
		mainStage.vfxDropTargetActivate(false);
	}
}
