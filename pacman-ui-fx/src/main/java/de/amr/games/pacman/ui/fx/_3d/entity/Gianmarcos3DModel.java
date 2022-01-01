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
 * The imported 3D model has been generously provided to me by Gianmarco Cavallaccio
 * (https://www.artstation.com/gianmart). Currently, I just use Pac-Man and one ghost from this
 * model, the rest is waste for me. What I really need is an animated 3D model of Pac-Man and a
 * single ghost.
 * 
 * @author Armin Reichert
 */
public class Gianmarcos3DModel implements PacManModel3D {

	public Map<String, MeshView> meshViewsByName;
	public Map<String, PhongMaterial> materialsByName;

	public Gianmarcos3DModel(String pathToObjFile) {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource(pathToObjFile));
			meshViewsByName = objImporter.getNamedMeshViews();
			materialsByName = objImporter.getNamedMaterials();
		} catch (ImportException e) {
			Logging.log("3D model '%s' could not be loaded", getClass().getName());
			e.printStackTrace();
		}
		objImporter.close();
		Logging.log("3D model '%s' loaded successfully", getClass().getName());
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