package de.amr.games.pacman.ui.fx.mspacman;

import javafx.scene.paint.Color;

public class MsPacMan_Constants {

	public static Color getMazeFoodColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(255, 255, 0);
		case 3:
			return Color.rgb(255, 0, 0);
		case 4:
			return Color.rgb(222, 222, 255);
		case 5:
			return Color.rgb(0, 255, 255);
		case 6:
			return Color.rgb(222, 222, 255);
		default:
			return Color.WHITE;
		}
	}

	public static Color getMazeWallColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(255, 183, 174);
		case 2:
			return Color.rgb(71, 183, 255);
		case 3:
			return Color.rgb(222, 151, 81);
		case 4:
			return Color.rgb(33, 33, 255);
		case 5:
			return Color.rgb(255, 183, 255);
		case 6:
			return Color.rgb(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}
}