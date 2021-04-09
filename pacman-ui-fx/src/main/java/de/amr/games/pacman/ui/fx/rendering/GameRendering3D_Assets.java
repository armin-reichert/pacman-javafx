package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.Map;
import java.util.Random;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
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

	public static MeshView ghostMeshTemplate;
	public static Map<String, MeshView> guyMeshTemplates;

	static {
		ARCADE_FONT = Font.loadFont(url("/emulogic.ttf"), TS);

		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(url("/common/ghost.obj"));
			ghostMeshTemplate = objImporter.getNamedMeshViews().get("Ghost_Sphere.001");

			objImporter.read(url("/common/pacman1.obj"));
			guyMeshTemplates = objImporter.getNamedMeshViews();

			guyMeshTemplates.keySet().stream().sorted().forEach(key -> log("Mesh '%s': %s", key, guyMeshTemplates.get(key)));
			log("Mesh views loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}
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

	public static PhongMaterial ghostSkin(int ghostID) {
		return new PhongMaterial(getGhostColor(ghostID));
	}

	public static PhongMaterial foodMaterial(GameVariant gameType, int mazeNumber) {
		return new PhongMaterial(getFoodColor(gameType, mazeNumber));
	}

	public static AmbientLight ambientLight(GameVariant gameType, int mazeNumber) {
		return new AmbientLight(getMazeWallColor(gameType, mazeNumber));
	}

	public static MeshView createGhostMeshView(int ghostID) {
		MeshView shape = new MeshView(ghostMeshTemplate.getMesh());
		centerOverOrigin(shape);
		scale(shape, 8);
		shape.drawModeProperty().bind(Env.$drawMode);
		return shape;
	}

	public static Image randomArea(Image src, int w, int h) {
		int x = new Random().nextInt((int) src.getWidth() - w);
		int y = new Random().nextInt((int) src.getHeight() - h);
		WritableImage result = new WritableImage(w, h);
		result.getPixelWriter().setPixels(0, 0, w, h, src.getPixelReader(), x, y);
		return result;
	}
}