package com.dg.ssrl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.dg.ssrl.Components;
import com.dg.ssrl.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 320;
		config.height = 480;

		Components.Position[] debugScreenSizes = {
				new Components.Position(320, 480),
				new Components.Position(480 / 2, 800 / 2),
				new Components.Position(480 / 2, 854 / 2),
				new Components.Position(540 / 2, 960 / 2)
		};

		new LwjglApplication(new Game(debugScreenSizes), config);
	}
}
