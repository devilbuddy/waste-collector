package com.dg.ssrl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.dg.ssrl.Game;
import com.dg.ssrl.Point;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 320;
		config.height = 480;

		Point[] debugScreenSizes = {
				new Point(320, 480),
				new Point(480 / 2, 800 / 2),
				new Point(480 / 2, 854 / 2),
				new Point(540 / 2, 960 / 2)
		};

		new LwjglApplication(new Game(debugScreenSizes), config);
	}
}
