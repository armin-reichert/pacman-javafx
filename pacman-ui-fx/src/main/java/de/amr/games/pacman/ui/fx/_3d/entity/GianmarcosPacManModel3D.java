/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.entity;

import java.util.Map;
import java.util.stream.Stream;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * The original 3D-model has been generously provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart).
 * 
 * @author Armin Reichert
 */
public class GianmarcosPacManModel3D implements PacManModel3D {

	private static class ObjModel {

		Map<String, MeshView> meshViews;
		Map<String, PhongMaterial> materials;

		public ObjModel(String path) {
			ObjModelImporter objImporter = new ObjModelImporter();
			try {
				objImporter.read(getClass().getResource(path));
				meshViews = objImporter.getNamedMeshViews();
				materials = objImporter.getNamedMaterials();
				Logging.log("3D model '%s' loaded successfully", path);
			} catch (ImportException e) {
				e.printStackTrace();
			} finally {
				objImporter.close();
			}
		}
	}

	private ObjModel pacManModel;
	private ObjModel ghostModel;

	public GianmarcosPacManModel3D() {
		pacManModel = new ObjModel("/common/gianmarco/pacman-only.obj");
		ghostModel = new ObjModel("/common/gianmarco/ghost-only.obj");
	}

	@Override
	public Group createPacMan() {
		MeshView head = new MeshView(pacManModel.meshViews.get("Sphere_yellow_packman").getMesh());
		head.setMaterial(pacManModel.materials.get("yellow_packman"));

		MeshView eyes = new MeshView(pacManModel.meshViews.get("Sphere.008_Sphere.010_grey_wall").getMesh());
		eyes.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));

		MeshView palate = new MeshView(pacManModel.meshViews.get("Sphere_grey_wall").getMesh());
		palate.setMaterial(new PhongMaterial(Color.rgb(10, 10, 10)));

		Stream.of(head, eyes, palate).forEach(meshView -> meshView.drawModeProperty().bind(Env.$drawMode3D));

		// TODO: simplify
		Translate centering = Model3DHelper.centerNodeOverOrigin(head);
		eyes.getTransforms().add(centering);
		palate.getTransforms().add(centering);

		Group pacman = new Group(eyes, palate, head);
		pacman.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(pacman, 8);

		return pacman;
	}

	@Override
	public Group createGhost() {
		MeshView body = new MeshView(ghostModel.meshViews.get("Sphere.004_Sphere.034_light_blue_ghost").getMesh());
		body.setMaterial(ghostModel.materials.get("blue_ghost"));

		MeshView eyesOuter = new MeshView(ghostModel.meshViews.get("Sphere.009_Sphere.036_white").getMesh());
		eyesOuter.setMaterial(new PhongMaterial(Color.WHITE));

		MeshView eyesInner = new MeshView(ghostModel.meshViews.get("Sphere.010_Sphere.039_grey_wall").getMesh());
		eyesInner.setMaterial(new PhongMaterial(Color.BLACK));

		Stream.of(body, eyesOuter, eyesInner).forEach(meshView -> meshView.drawModeProperty().bind(Env.$drawMode3D));

		Translate centering = Model3DHelper.centerNodeOverOrigin(body);
		eyesOuter.getTransforms().add(centering);
		eyesInner.getTransforms().add(centering);

		Group ghost = new Group(body, eyesOuter, eyesInner);
		ghost.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(ghost, 8);

		return ghost;
	}

	@Override
	public Group createGhostEyes() {
		Group eyes = createGhost();
		eyes.getChildren().remove(0);
		Model3DHelper.centerNodeOverOrigin(eyes);
		return eyes;
	}
}