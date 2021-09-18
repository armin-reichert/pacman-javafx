package de.amr.games.pacman.ui.fx._3d.entity;

import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * The imported 3D model has been generously provided by Gianmarco Cavallaccio
 * (https://www.artstation.com/gianmart). Thanks Gianmarco!
 * 
 * <p>
 * Unfortunately, I have neither a model with animations nor a Ms. Pac-Man model
 * yet.
 * 
 * @author Armin Reichert
 */
public class GianmarcosPacManModel3D implements PacManModel3D {

	static final String PATH_TO_OBJ_FILE = "/common/gianmarco/pacman.obj";

	public Map<String, MeshView> meshViewsByName;
	public Map<String, PhongMaterial> materialsByName;

	public GianmarcosPacManModel3D() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource(PATH_TO_OBJ_FILE));
			meshViewsByName = objImporter.getNamedMeshViews();
			materialsByName = objImporter.getNamedMaterials();
		} catch (ImportException e) {
			e.printStackTrace();
		} finally {
			objImporter.close();
		}
	}

	@Override
	public Group createPacMan() {
		MeshView body = new MeshView(meshViewsByName.get("Sphere_yellow_packman").getMesh());
		body.setMaterial(materialsByName.get("yellow_packman"));
		body.drawModeProperty().bind(Env.$drawMode3D);
		Translate centering = Model3DHelper.centerNodeOverOrigin(body);

		MeshView eyes = new MeshView(meshViewsByName.get("Sphere.008_Sphere.010").getMesh());
		eyes.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));
		eyes.drawModeProperty().bind(Env.$drawMode3D);
		eyes.getTransforms().add(centering);

		Group group = new Group(eyes, body);
		group.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(group, 8);
		return group;
	}

	@Override
	public Group createGhost() {
		MeshView body = new MeshView(meshViewsByName.get("Sphere.004_Sphere.034").getMesh());
		body.setMaterial(materialsByName.get("blue_ghost"));
		body.drawModeProperty().bind(Env.$drawMode3D);
		Translate centering = Model3DHelper.centerNodeOverOrigin(body);

		MeshView eyesOuter = new MeshView(meshViewsByName.get("Sphere.009_Sphere.036").getMesh());
		eyesOuter.setMaterial(new PhongMaterial(Color.WHITE));
		eyesOuter.drawModeProperty().bind(Env.$drawMode3D);
		eyesOuter.getTransforms().add(centering);

		MeshView eyesInner = new MeshView(meshViewsByName.get("Sphere.010_Sphere.039").getMesh());
		eyesInner.setMaterial(new PhongMaterial(Color.BLACK));
		eyesInner.drawModeProperty().bind(Env.$drawMode3D);
		eyesInner.getTransforms().add(centering);

		Group group = new Group(body, eyesOuter, eyesInner);
		group.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(group, 8);
		return group;
	}

	@Override
	public Group createGhostEyes() {
		Group eyes = createGhost();
		eyes.getChildren().remove(0);
		Model3DHelper.centerNodeOverOrigin(eyes);
		return eyes;
	}
}