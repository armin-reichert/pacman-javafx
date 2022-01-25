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
package de.amr.games.pacman.ui.fx._3d.model;

import static de.amr.games.pacman.ui.fx._3d.model.PacManModel3D.bindDrawMode;
import static de.amr.games.pacman.ui.fx._3d.model.PacManModel3D.centerOverOrigin;
import static de.amr.games.pacman.ui.fx._3d.model.PacManModel3D.scale;

import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

/**
 * The original 3D-model has been provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart). I extracted
 * the meshes for Pac-Man and a ghost into separate obj files.
 * 
 * @author Armin Reichert
 */
public class GianmarcosModel3D implements PacManModel3D {

	private static GianmarcosModel3D instance;

	public static GianmarcosModel3D get() {
		if (instance == null) {
			instance = new GianmarcosModel3D();
		}
		return instance;
	}

	private ObjModel pacManModel;
	private ObjModel ghostModel;

	private GianmarcosModel3D() {
		pacManModel = new ObjModel(getClass().getResource("/common/gianmarco/pacman.obj"));
		ghostModel = new ObjModel(getClass().getResource("/common/gianmarco/ghost.obj"));
	}

	@Override
	public Group createPacMan() {
		MeshView head = pacManModel.createMeshView("Sphere_yellow_packman");
		head.setMaterial(new PhongMaterial(Color.YELLOW));

		MeshView eyes = pacManModel.createMeshView("Sphere.008_Sphere.010_grey_wall");
		eyes.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));

		MeshView palate = pacManModel.createMeshView("Sphere_grey_wall");
		palate.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		centerOverOrigin(head, eyes, palate);
		bindDrawMode(Env.$drawMode3D, head, eyes, palate);

		Group pacman = new Group(head, eyes, palate);
		pacman.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		scale(pacman, 8);

		return pacman;
	}

	@Override
	public Group createGhost() {
		MeshView body = ghostModel.createMeshView("Sphere.004_Sphere.034_light_blue_ghost");
		body.setMaterial(ghostModel.getMaterial("blue_ghost"));

		MeshView eyesOuter = ghostModel.createMeshView("Sphere.009_Sphere.036_white");
		eyesOuter.setMaterial(new PhongMaterial(Color.WHITE));

		MeshView eyesInner = ghostModel.createMeshView("Sphere.010_Sphere.039_grey_wall");
		eyesInner.setMaterial(new PhongMaterial(Color.BLACK));

		centerOverOrigin(body, eyesOuter, eyesInner);
		bindDrawMode(Env.$drawMode3D, body, eyesOuter, eyesInner);

		Group ghost = new Group(body, eyesOuter, eyesInner);
		ghost.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		scale(ghost, 8);

		return ghost;
	}

	@Override
	public Group createGhostEyes() {
		Group eyes = createGhost();
		eyes.getChildren().remove(0);
		centerOverOrigin(eyes);
		return eyes;
	}
}