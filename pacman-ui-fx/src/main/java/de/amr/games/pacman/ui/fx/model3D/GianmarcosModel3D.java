package de.amr.games.pacman.ui.fx.model3D;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

/**
 * This 3D model has been provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart)
 * from Rome, Italy. Thanks Gianmarco!
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

	public MeshView getPacManMesh() {
		MeshView mesh = new MeshView(meshViewsByName.get("Sphere_yellow_packman").getMesh());
//		GameRendering3D_Assets.centerOverOrigin(mesh);
//		GameRendering3D_Assets.scale(mesh, 8);

		mesh.setMaterial(materialsByName.get("yellow_packman"));
//		mesh.setMaterial(new PhongMaterial(Color.YELLOW));
		mesh.setVisible(true);
//		mesh.setDrawMode(DrawMode.LINE);
		return mesh;
	}

	public static void main(String[] args) {
		new GianmarcosModel3D();
	}
}
