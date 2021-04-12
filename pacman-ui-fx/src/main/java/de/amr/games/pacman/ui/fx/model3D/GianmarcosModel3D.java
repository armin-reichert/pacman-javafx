package de.amr.games.pacman.ui.fx.model3D;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * This 3D model has been generously provided by Gianmarco Cavallaccio
 * (https://www.artstation.com/gianmart). Thanks Gianmarco!
 * 
 * @author Armin Reichert
 */
public class GianmarcosModel3D {

	public static final GianmarcosModel3D IT = new GianmarcosModel3D();

	private Map<String, MeshView> meshViewsByName;
	private Map<String, PhongMaterial> materialsByName;

	private GianmarcosModel3D() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/gianmarco/pacman.obj"));
			meshViewsByName = objImporter.getNamedMeshViews();
			log("MeshViews:");
			meshViewsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
			materialsByName = objImporter.getNamedMaterials();
			log("");
			log("Materials:");
			materialsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
			log("Pac-Man 3D model loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}
		objImporter.close();
	}

	public Group createPacMan() {
		MeshView head = new MeshView(meshViewsByName.get("Sphere_yellow_packman").getMesh());
		head.setMaterial(materialsByName.get("yellow_packman"));
		head.drawModeProperty().bind(Env.$drawMode);
		Translate centering = GameRendering3D_Assets.centerNodeOverOrigin(head);

		MeshView eyes = new MeshView(meshViewsByName.get("Sphere.008_Sphere.010").getMesh());
//		eyes.setMaterial(materialsByName.get("yellow_emission"));
		eyes.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));
		eyes.drawModeProperty().bind(Env.$drawMode);
		eyes.getTransforms().add(centering);

		Group group = new Group(eyes, head);
		group.setTranslateX(0);
		group.setTranslateY(0);
		group.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		GameRendering3D_Assets.scaleNode(group, 8);
		return group;
	}

	public static void main(String[] args) {
		new GianmarcosModel3D();
	}
}
