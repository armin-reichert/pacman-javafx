package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class GameRendering2D_Assets {

	private GameRendering2D_Assets() {
	}

	public static final Font ARCADE_FONT = Font
			.loadFont(GameRendering2D_Assets.class.getResourceAsStream("/emulogic.ttf"), 8);

	public static Color getMazeWallColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? GameRendering2D_Assets.getPacManMazeWallColor(mazeNumber)
				: GameRendering2D_Assets.getMsPacManMazeWallColor(mazeNumber);
	}

	public static Color getFoodColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? GameRendering2D_Assets.getPacManFoodColor(mazeNumber)
				: GameRendering2D_Assets.getMsPacManFoodColor(mazeNumber);
	}

	public static Color getGhostColor(int ghostID) {
		return ghostID == 0 ? Color.TOMATO : ghostID == 1 ? Color.PINK : ghostID == 2 ? Color.CYAN : Color.ORANGE;
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