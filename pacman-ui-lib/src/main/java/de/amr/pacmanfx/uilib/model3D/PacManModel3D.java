/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;

public class PacManModel3D implements Disposable {

	private static final String MESH_ID_PAC_MAN_EYES = "PacMan.Eyes";
	private static final String MESH_ID_PAC_MAN_HEAD = "PacMan.Head";
	private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

	private final Model3D model3D;

	public PacManModel3D() {
		try {
			model3D = Models3D.createFromObjFile(this::getClass, "/de/amr/pacmanfx/uilib/model3D/pacman.obj");
		} catch (Model3DException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void dispose() {
		model3D.dispose();
	}

	public Mesh eyesMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_EYES).orElseThrow();
	}

	public Mesh headMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_HEAD).orElseThrow();
	}

	public Mesh palateMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_PALATE).orElseThrow();
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
		Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
	{
		final var body = createPacBody(size, headColor, eyesColor, palateColor);
		final var femaleParts = createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
		return new MsPacManBody(body, femaleParts);
	}
}
