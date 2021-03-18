package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.Map;
import java.util.Random;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.common.Env;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_Constants;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Assets (fonts, meshes, materials, ...) used in the 3D play scene.
 * 
 * @author Armin Reichert
 */
class Assets {

	static final Font ARCADE_FONT;
	static final PhongMaterial wallMaterials[];
	static final PhongMaterial livesCounterOn = new PhongMaterial(Color.YELLOW);
	static final PhongMaterial livesCounterOff = new PhongMaterial(Color.GRAY);

	static MeshView ghostMeshView;
	static Map<String, MeshView> meshViews;

	static {
		ARCADE_FONT = Font.loadFont(Assets.class.getResource("/emulogic.ttf").toExternalForm(), TS);

		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(Assets.class.getResource("/common/ghost.obj"));
			ghostMeshView = objImporter.getNamedMeshViews().get("Ghost_Sphere.001");

			objImporter.read(Assets.class.getResource("/common/pacman1.obj"));
			meshViews = objImporter.getNamedMeshViews();

			meshViews.keySet().stream().sorted().forEach(key -> log("Mesh '%s': %s", key, meshViews.get(key)));
			log("Mesh views loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}

		Image wallTexture = new Image(Assets.class.getResource("/common/stone-texture.png").toExternalForm());
		wallMaterials = new PhongMaterial[10];
		for (int i = 0; i < wallMaterials.length; ++i) {
			PhongMaterial m = new PhongMaterial();
			Image texture = randomSubimage(wallTexture, 128, 128);
			m.setBumpMap(texture);
			m.setDiffuseMap(texture);
			wallMaterials[i] = m;
		}
	}

	static Color ghostColor(int id) {
		return id == 0 ? Color.TOMATO : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	static Color mazeColor(GameType gameType, int mazeNumber) {
		return gameType == GameType.PACMAN ? Color.BLUE : MsPacMan_Constants.getMazeWallColor(mazeNumber);
	}

	static Color foodColor(GameType gameType, int mazeNumber) {
		return gameType == GameType.PACMAN ? Color.rgb(250, 185, 176) : MsPacMan_Constants.getFoodColor(mazeNumber);
	}

	static PhongMaterial ghostSkin(int ghostID) {
		return new PhongMaterial(ghostColor(ghostID));
	}

	static PhongMaterial foodMaterial(GameType gameType, int mazeNumber) {
		return new PhongMaterial(foodColor(gameType, mazeNumber));
	}

	static PhongMaterial randomWallMaterial() {
		return wallMaterials[new Random().nextInt(wallMaterials.length)];
	}

	static AmbientLight ambientLight(GameType gameType, int mazeNumber) {
		return new AmbientLight(mazeColor(gameType, mazeNumber));
	}

	static Group playerShape() {
		MeshView body = meshViews.get("Sphere_Sphere.002_Material.001");
		PhongMaterial bodyMaterial = new PhongMaterial(Color.YELLOW);
		bodyMaterial.setDiffuseColor(Color.YELLOW.brighter());
		bodyMaterial.setSpecularPower(100);
		body.setMaterial(bodyMaterial);
		body.setDrawMode(Env.$drawMode.get());
		Translate shift = centerOverOrigin(body);

		MeshView glasses = meshViews.get("Sphere_Sphere.002_Material.002");
		glasses.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));
		glasses.setDrawMode(Env.$drawMode.get());
		glasses.getTransforms().add(shift);

		Group shape = new Group(body, glasses);
		shape.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		scale(shape, TS);
		return shape;
	}

	static MeshView ghostShape(int ghostID) {
		PhongMaterial material = new PhongMaterial(Assets.ghostColor(ghostID));
		MeshView shape = new MeshView(ghostMeshView.getMesh());
		shape.setMaterial(material);
//		shape.setDrawMode(DrawMode.LINE);
		shape.getTransforms().clear();
		shape.getTransforms().add(new Scale(4, 4, 4));
		return shape;
	}

	private static Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		Translate shift = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
		node.getTransforms().add(shift);
		return shift;
	}

	private static void scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}

	private static Image randomSubimage(Image src, int w, int h) {
		WritableImage result = new WritableImage(w, h);
		int x = new Random().nextInt((int) src.getWidth() - w);
		int y = new Random().nextInt((int) src.getHeight() - h);
		result.getPixelWriter().setPixels(0, 0, w, h, src.getPixelReader(), x, y);
		return result;
	}
}