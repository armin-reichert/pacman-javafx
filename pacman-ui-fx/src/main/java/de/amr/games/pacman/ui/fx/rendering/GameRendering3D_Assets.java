package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.model.common.GameVariant;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Assets (fonts, meshes, materials, ...) used in the 3D play scene.
 * 
 * @author Armin Reichert
 */
public class GameRendering3D_Assets {

	public static Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		Translate centering = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
		node.getTransforms().add(centering);
		return centering;
	}

	public static void scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}

	public static String url(String path) {
		return GameRendering3D_Assets.class.getResource(path).toExternalForm();
	}

	public static final Font ARCADE_FONT;
	public static final PhongMaterial livesCounterOn = new PhongMaterial(Color.YELLOW);
	public static final PhongMaterial livesCounterOff = new PhongMaterial(Color.GRAY);

	static {
		ARCADE_FONT = Font.loadFont(url("/emulogic.ttf"), TS);
	}

	public static Color getMazeWallColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? Color.rgb(33, 33, 255)
				: GameRendering2D_Assets.getMazeWallColor(mazeNumber);
	}

	public static Color getFoodColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? Color.rgb(250, 185, 176)
				: GameRendering2D_Assets.getFoodColor(mazeNumber);
	}

	public static Color getGhostColor(int ghostID) {
		return ghostID == 0 ? Color.TOMATO : ghostID == 1 ? Color.PINK : ghostID == 2 ? Color.CYAN : Color.ORANGE;
	}

}