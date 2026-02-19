/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;

/**
 * Loads and provides access to the 3D Pac-Man model and its individual mesh parts.
 * <p>
 * The model is loaded from a Wavefront OBJ file and split into named sub-meshes
 * (head, eyes, palate). This class also provides convenience factory methods for
 * creating fully assembled Pac-Man and Ms. Pac-Man 3D bodies.
 * <p>
 * Instances must be disposed via {@link #dispose()} to release mesh resources.
 */
public class PacManModel3D implements Disposable {

	private static final String MESH_ID_PAC_MAN_EYES   = "PacMan.Eyes";
	private static final String MESH_ID_PAC_MAN_HEAD   = "PacMan.Head";
	private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

	/** The loaded 3D model containing all Pac-Man mesh parts. */
	private final Model3D model3D;

	/**
	 * Loads the Pac-Man OBJ model from the classpath.
	 * <p>
	 * The model file must contain mesh groups with the IDs:
	 * <ul>
	 *   <li>{@code PacMan.Head}</li>
	 *   <li>{@code PacMan.Eyes}</li>
	 *   <li>{@code PacMan.Palate}</li>
	 * </ul>
	 *
	 * @throws RuntimeException if the model cannot be loaded
	 */
	public PacManModel3D() {
		try {
			model3D = Model3D.fromWavefrontFile(
					getClass().getResource("/de/amr/pacmanfx/uilib/model3D/pacman.obj")
			);
		} catch (Model3DException x) {
			throw new RuntimeException("Failed to load Pac-Man 3D model", x);
		}
	}

	/**
	 * Releases all resources associated with the underlying model.
	 */
	@Override
	public void dispose() {
		model3D.dispose();
	}

	/**
	 * @return the mesh representing Pac-Man's eyes
	 * @throws java.util.NoSuchElementException if the mesh is missing
	 */
	public Mesh eyesMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_EYES).orElseThrow();
	}

	/**
	 * @return the mesh representing Pac-Man's head
	 * @throws java.util.NoSuchElementException if the mesh is missing
	 */
	public Mesh headMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_HEAD).orElseThrow();
	}

	/**
	 * @return the mesh representing Pac-Man's palate (mouth interior)
	 * @throws java.util.NoSuchElementException if the mesh is missing
	 */
	public Mesh palateMesh() {
		return model3D.mesh(MESH_ID_PAC_MAN_PALATE).orElseThrow();
	}

	/**
	 * Creates a fully assembled Pac-Man body with head, eyes, and palate.
	 *
	 * @param size        the overall size (scale) of the model
	 * @param headColor   color of the head mesh
	 * @param eyesColor   color of the eyes mesh
	 * @param palateColor color of the palate mesh
	 * @return a new {@link PacBody} instance
	 */
	public PacBody createPacBody(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new PacBody(size, headMesh(), headColor, eyesMesh(), eyesColor, palateMesh(), palateColor);
	}

	/**
	 * Creates a Pac-Man body without eyes (used for blinking or special effects).
	 *
	 * @param size        the overall size (scale)
	 * @param headColor   color of the head mesh
	 * @param palateColor color of the palate mesh
	 * @return a new {@link PacBodyNoEyes} instance
	 */
	public PacBodyNoEyes createBlindPacBody(double size, Color headColor, Color palateColor) {
		return new PacBodyNoEyes(size, headMesh(), headColor, palateMesh(), palateColor);
	}

	/**
	 * Creates the additional female parts used for Ms. Pac-Man (hair bow, pearls, etc.).
	 *
	 * @param pacSize              size of the base Pac-Man body
	 * @param hairBowColor         color of the hair bow
	 * @param hairBowPearlsColor   color of the bow pearls
	 * @param boobsColor           color of the chest decorations
	 * @return a new {@link MsPacManFemaleParts} instance
	 */
	public MsPacManFemaleParts createFemaleBodyParts(
			double pacSize,
			Color hairBowColor,
			Color hairBowPearlsColor,
			Color boobsColor)
	{
		return new MsPacManFemaleParts(pacSize, hairBowColor, hairBowPearlsColor, boobsColor);
	}

	/**
	 * Creates a complete Ms. Pac-Man body consisting of a Pac-Man base body
	 * plus the additional female parts.
	 *
	 * @param size                overall size (scale)
	 * @param headColor           color of the head mesh
	 * @param eyesColor           color of the eyes mesh
	 * @param palateColor         color of the palate mesh
	 * @param hairBowColor        color of the hair bow
	 * @param hairBowPearlsColor  color of the bow pearls
	 * @param boobsColor          color of the chest decorations
	 * @return a new {@link MsPacManBody} instance
	 */
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
