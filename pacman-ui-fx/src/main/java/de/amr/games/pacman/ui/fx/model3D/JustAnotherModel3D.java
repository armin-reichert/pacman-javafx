package de.amr.games.pacman.ui.fx.model3D;

import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.shape.MeshView;

/**
 * 3D model from things I found somewhere on the Internet. Will be replaced soon.
 * 
 * @author Armin Reichert
 */
public class JustAnotherModel3D {

	public static final JustAnotherModel3D IT = new JustAnotherModel3D();

	private Map<String, MeshView> ghostMeshViewsByName;

	private JustAnotherModel3D() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/temp/ghost.obj"));
			ghostMeshViewsByName = objImporter.getNamedMeshViews();
		} catch (ImportException e) {
			e.printStackTrace();
		}
	}

	public MeshView createGhost() {
		MeshView meshView = new MeshView(ghostMeshViewsByName.get("Ghost_Sphere.001").getMesh());
		Model3DHelper.centerNodeOverOrigin(meshView);
		Model3DHelper.scaleNode(meshView, 8);
		meshView.drawModeProperty().bind(Env.$drawMode);
		return meshView;
	}
}