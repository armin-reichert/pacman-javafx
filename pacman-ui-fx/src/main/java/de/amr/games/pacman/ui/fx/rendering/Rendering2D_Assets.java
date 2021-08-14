package de.amr.games.pacman.ui.fx.rendering;

import java.util.Map;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Provides rendering data extracted from the sprite sheets.
 * 
 * @author Armin Reichert
 */
public final class Rendering2D_Assets {

	public static final Font ARCADE_FONT = Font.loadFont(//
			Rendering2D_Assets.class.getResourceAsStream("/emulogic.ttf"), 8);

	public static Image image(String path) {
		return new Image(Rendering2D_Assets.class.getResource(path).toExternalForm());
	}

	private static final Color PACMAN_FOOD_COLOR = Color.rgb(250, 185, 176);

	private static final Color PACMAN_MAZE_WALL_SIDE_COLOR = Color.rgb(33, 33, 255);

	private static final Color MS_PACMAN_FOOD_COLOR[] = { //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(0, 255, 255), //
			Color.rgb(222, 222, 255),//
	};

	private static final Color MS_PACMAN_MAZE_WALL_TOP_COLOR[] = { //
			Color.rgb(255, 183, 174), //
			Color.rgb(71, 183, 255), //
			Color.rgb(222, 151, 81), //
			Color.rgb(33, 33, 255), //
			Color.rgb(255, 183, 255), //
			Color.rgb(255, 183, 174),//
	};

	private static final Color MS_PACMAN_MAZE_WALL_SIDE_COLOR[] = { //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 183, 81), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0),//
	};

	public static Color getMazeWallTopColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? getPacManMazeWallColor(mazeNumber)
				: getMsPacManMazeWallTopColor(mazeNumber);
	}

	public static Color getMazeWallSideColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? Color.WHITE : getMsPacManMazeWallSideColor(mazeNumber);
	}

	public static Color getFoodColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? getPacManFoodColor(mazeNumber) : getMsPacManFoodColor(mazeNumber);
	}

	public static Color getGhostColor(int ghostID) {
		return ghostID == 0 ? Color.RED : ghostID == 1 ? Color.PINK : ghostID == 2 ? Color.CYAN : Color.ORANGE;
	}

	public static Color getGhostBlueColor() {
		return Color.CORNFLOWERBLUE;
	}

	public static Color getPacManMazeWallColor(int mazeNumber) {
		return PACMAN_MAZE_WALL_SIDE_COLOR;
	}

	public static Color getPacManFoodColor(int mazeNumber) {
		return PACMAN_FOOD_COLOR;
	}

	private static Color getMsPacManFoodColor(int mazeNumber) {
		return MS_PACMAN_FOOD_COLOR[mazeNumber - 1];
	}

	public static Color getMsPacManMazeWallTopColor(int mazeNumber) {
		return MS_PACMAN_MAZE_WALL_TOP_COLOR[mazeNumber - 1];
	}

	public static Color getMsPacManMazeWallSideColor(int mazeNumber) {
		return MS_PACMAN_MAZE_WALL_SIDE_COLOR[mazeNumber - 1];
	}

	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}
}