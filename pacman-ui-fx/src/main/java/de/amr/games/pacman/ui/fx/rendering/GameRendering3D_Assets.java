package de.amr.games.pacman.ui.fx.rendering;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Assets (fonts, meshes, materials, ...) used in the 3D play scene.
 * 
 * @author Armin Reichert
 */
public class GameRendering3D_Assets {

	public static final Font ARCADE_FONT = Font
			.loadFont(GameRendering3D_Assets.class.getResourceAsStream("/emulogic.ttf"), 8);

	public static Translate centerNodeOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		Translate centering = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
		node.getTransforms().add(centering);
		return centering;
	}

	public static void scaleNode(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}

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