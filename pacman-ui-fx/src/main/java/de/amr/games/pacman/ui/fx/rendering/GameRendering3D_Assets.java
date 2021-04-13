package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Assets (fonts, meshes, materials, ...) used in the 3D play scene.
 * 
 * @author Armin Reichert
 */
public class GameRendering3D_Assets {

	public static final Font ARCADE_FONT = Font
			.loadFont(GameRendering3D_Assets.class.getResourceAsStream("/emulogic.ttf"), 8);

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

}