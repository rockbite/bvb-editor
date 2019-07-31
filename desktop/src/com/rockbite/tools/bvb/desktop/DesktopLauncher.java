package com.rockbite.tools.bvb.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.rockbite.tools.bvb.BvbEditor;

import java.awt.dnd.DropTarget;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VFX to Bone Binder - version 1.0.0";
		config.width = 900;
		config.height = 680;
		config.resizable = false;
		BvbEditor editor = new BvbEditor();
		LwjglFrame frame = new LwjglFrame(editor, config);

		DropTarget dropTarget = new DropTarget(frame, editor);
	}
}
