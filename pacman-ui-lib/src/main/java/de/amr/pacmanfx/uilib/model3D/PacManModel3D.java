/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;

public class PacManModel3D implements Disposable {

	private static final String MESH_ID_PAC_MAN_EYES = "PacMan.Eyes";
	private static final String MESH_ID_PAC_MAN_HEAD = "PacMan.Head";
	private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

	private final ObjFileContent model3D;

	public PacManModel3D() {
		model3D = Models3D.loadModelFromObjFile("/de/amr/pacmanfx/uilib/model3D/pacman.obj");
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh eyesMesh() {
		return Model3D.mesh(model3D, MESH_ID_PAC_MAN_EYES);
	}

	public Mesh headMesh() {
		return Model3D.mesh(model3D, MESH_ID_PAC_MAN_HEAD);
	}

	public Mesh palateMesh() {
		return Model3D.mesh(model3D, MESH_ID_PAC_MAN_PALATE);
	}

	public PacBody createPacBody(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new PacBody(size, headMesh(), headColor, eyesMesh(), eyesColor, palateMesh(), palateColor);
	}

	public PacBodyNoEyes createBlindPacBody(double size, Color headColor, Color palateColor) {
		return new PacBodyNoEyes(size, headMesh(), headColor, palateMesh(), palateColor);
	}

	public MsPacManFemaleParts createFemaleBodyParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
		return new MsPacManFemaleParts(pacSize, hairBowColor, hairBowPearlsColor, boobsColor);
	}

	public MsPacManBody createMsPacManBody(
			double size,
			Color headColor, Color eyesColor, Color palateColor,
			Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
		var body = createPacBody(size, headColor, eyesColor, palateColor);
		var femaleParts = createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
		return new MsPacManBody(body, femaleParts);
	}
}
