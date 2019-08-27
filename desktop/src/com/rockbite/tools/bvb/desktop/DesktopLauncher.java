package com.rockbite.tools.bvb.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.rockbite.tools.bvb.BvbEditor;

import java.awt.dnd.DropTarget;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VFX Editor 1.1.2";
		config.width = 1050; //900
		config.height = 880;
		config.resizable = false;
		BvbEditor editor = new BvbEditor();
		LwjglFrame frame = new LwjglFrame(editor, config);

		DropTarget dropTarget = new DropTarget(frame, editor);
	}
}
