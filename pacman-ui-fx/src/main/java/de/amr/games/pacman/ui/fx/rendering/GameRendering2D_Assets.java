package de.amr.games.pacman.ui.fx.rendering;

import javafx.scene.paint.Color;

public final class GameRendering2D_Assets {

	private GameRendering2D_Assets() {
	}

	public static Color getPacManMazeWallColor(int mazeNumber) {
		return Color.rgb(33, 33, 255);
	}

	public static Color getPacManFoodColor(int mazeNumber) {
		return Color.rgb(250, 185, 176);
	}

	public static Color getMsPacManFoodColor(int mazeNumber) {
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
			throw new IllegalArgumentException();
		}
	}

	public static Color getMsPacManMazeWallColor(int mazeNumber) {
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
			throw new IllegalArgumentException();
		}
	}

	public static Color getMsPacManMazeWallBorderColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(255, 0, 0);
		case 2:
			return Color.rgb(222, 222, 255);
		case 3:
			return Color.rgb(222, 222, 255);
		case 4:
			return Color.rgb(255, 183, 81);
		case 5:
			return Color.rgb(255, 255, 0);
		case 6:
			return Color.rgb(255, 0, 0);
		default:
			throw new IllegalArgumentException();
		}
	}
}
