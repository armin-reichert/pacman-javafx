package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.Map;
import java.util.Random;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.common.Env;
import de.amr.games.pacman.ui.fx.rendering.Assets2D;
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
class Assets3D {

	static Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		Translate centering = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
		node.getTransforms().add(centering);
		return centering;
	}

	static void scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}

	static String url(String path) {
		return Assets3D.class.getResource(path).toExternalForm();
	}

	static final Font ARCADE_FONT;
	static final PhongMaterial wallMaterials[];
	static final PhongMaterial livesCounterOn = new PhongMaterial(Color.YELLOW);
	static final PhongMaterial livesCounterOff = new PhongMaterial(Color.GRAY);

	static MeshView ghostMeshTemplate;
	static Map<String, MeshView> guyMeshTemplates;

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

		Image wallTexture = new Image(url("/common/stone-texture.png"));
		wallMaterials = new PhongMaterial[10];
		for (int i = 0; i < wallMaterials.length; ++i) {
			PhongMaterial m = new PhongMaterial();
			Image texture = randomArea(wallTexture, 128, 128);
			m.setBumpMap(texture);
			m.setDiffuseMap(texture);
			wallMaterials[i] = m;
		}
	}

	static Color mazeColor(GameVariant gameType, int mazeNumber) {
		return gameType == GameVariant.PACMAN ? Color.BLUE : Assets2D.getMazeWallColor(mazeNumber);
	}

	static Color foodColor(GameVariant gameType, int mazeNumber) {
		return gameType == GameVariant.PACMAN ? Color.rgb(250, 185, 176) : Assets2D.getFoodColor(mazeNumber);
	}

	static Color ghostColor(int id) {
		return id == 0 ? Color.TOMATO : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	static PhongMaterial ghostSkin(int ghostID) {
		return new PhongMaterial(ghostColor(ghostID));
	}

	static PhongMaterial foodMaterial(GameVariant gameType, int mazeNumber) {
		return new PhongMaterial(foodColor(gameType, mazeNumber));
	}

	static PhongMaterial randomWallMaterial() {
		return wallMaterials[new Random().nextInt(wallMaterials.length)];
	}

	static AmbientLight ambientLight(GameVariant gameType, int mazeNumber) {
		return new AmbientLight(mazeColor(gameType, mazeNumber));
	}

	static MeshView createGhostMeshView(int ghostID) {
		MeshView shape = new MeshView(ghostMeshTemplate.getMesh());
		centerOverOrigin(shape);
		scale(shape, 8);
		shape.drawModeProperty().bind(Env.$drawMode);
		return shape;
	}

	private static Image randomArea(Image src, int w, int h) {
		int x = new Random().nextInt((int) src.getWidth() - w);
		int y = new Random().nextInt((int) src.getHeight() - h);
		WritableImage result = new WritableImage(w, h);
		result.getPixelWriter().setPixels(0, 0, w, h, src.getPixelReader(), x, y);
		return result;
	}
}