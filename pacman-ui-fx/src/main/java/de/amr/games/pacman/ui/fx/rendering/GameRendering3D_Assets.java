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
	public static final Image WALL_TEXTURE;
	public static final PhongMaterial wallMaterials[];
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

		WALL_TEXTURE = new Image(url("/common/stone-texture.png"));
		wallMaterials = new PhongMaterial[1];
		for (int i = 0; i < wallMaterials.length; ++i) {
			PhongMaterial m = new PhongMaterial(Color.CORNFLOWERBLUE);
//			Image texture = randomArea(WALL_TEXTURE, 128, 128);
//			m.setBumpMap(texture);
//			m.setDiffuseMap(texture);
			wallMaterials[i] = m;
		}
	}

	public static Color mazeGroundColor(GameVariant gameType, int mazeNumber) {
		return gameType == GameVariant.PACMAN ? Color.BLACK : GameRendering2D_Assets.getMazeWallColor(mazeNumber);
	}

	public static Color mazeWallColor(GameVariant gameType, int mazeNumber) {
		return gameType == GameVariant.PACMAN ? Color.BLUE : GameRendering2D_Assets.getMazeWallBorderColor(mazeNumber);
	}

	public static Color foodColor(GameVariant gameType, int mazeNumber) {
		return gameType == GameVariant.PACMAN ? Color.rgb(250, 185, 176) : GameRendering2D_Assets.getFoodColor(mazeNumber);
	}

	public static Color ghostColor(int id) {
		return id == 0 ? Color.TOMATO : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	public static PhongMaterial ghostSkin(int ghostID) {
		return new PhongMaterial(ghostColor(ghostID));
	}

	public static PhongMaterial foodMaterial(GameVariant gameType, int mazeNumber) {
		return new PhongMaterial(foodColor(gameType, mazeNumber));
	}

	public static PhongMaterial randomWallMaterial() {
		return wallMaterials[new Random().nextInt(wallMaterials.length)];
	}

	public static AmbientLight ambientLight(GameVariant gameType, int mazeNumber) {
		return new AmbientLight(mazeWallColor(gameType, mazeNumber));
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