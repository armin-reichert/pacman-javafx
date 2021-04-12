package de.amr.games.pacman.ui.fx.model3D;

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
 * 3D model from things I found somewhere on the Internet. Will be replaced soon.
 * 
 * @author Armin Reichert
 */
public class JustAnotherModel3D {

	public static final JustAnotherModel3D IT = new JustAnotherModel3D();

	private Map<String, MeshView> ghostMeshViewsByName;

	private Map<String, MeshView> playerMeshViewsByName;
	private Map<String, PhongMaterial> playerMaterialsByName;

	private JustAnotherModel3D() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/temp/ghost.obj"));
			ghostMeshViewsByName = objImporter.getNamedMeshViews();
		} catch (ImportException e) {
			e.printStackTrace();
		}
		try {
			objImporter.read(getClass().getResource("/common/temp/pacman1.obj"));
			playerMeshViewsByName = objImporter.getNamedMeshViews();
			playerMaterialsByName = objImporter.getNamedMaterials();
		} catch (ImportException e) {
			e.printStackTrace();
		} finally {
			objImporter.close();
		}
	}

	public PhongMaterial getPlayerMaterial(String name) {
		return playerMaterialsByName.get(name);
	}

	public MeshView createGhostMesh() {
		MeshView meshView = new MeshView(ghostMeshViewsByName.get("Ghost_Sphere.001").getMesh());
		GameRendering3D_Assets.centerOverOrigin(meshView);
		GameRendering3D_Assets.scale(meshView, 8);
		meshView.drawModeProperty().bind(Env.$drawMode);
		return meshView;
	}

	public Group createPlayerMesh() {
		MeshView body = new MeshView(playerMeshViewsByName.get("Sphere_Sphere.002_Material.001").getMesh());
		body.setMaterial(new PhongMaterial(Color.YELLOW));
		body.drawModeProperty().bind(Env.$drawMode);

		MeshView glasses = new MeshView(playerMeshViewsByName.get("Sphere_Sphere.002_Material.002").getMesh());
		glasses.setMaterial(new PhongMaterial(Color.rgb(60, 60, 60)));
		glasses.drawModeProperty().bind(Env.$drawMode);

		Translate centering = GameRendering3D_Assets.centerOverOrigin(body);
		glasses.getTransforms().add(centering);

		Group root = new Group(body, glasses);
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		GameRendering3D_Assets.scale(root, 8);
		return root;
	}
}
