/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.model3D.Model3DException;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

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
	 * @param pacConfig the Pac configuration
	 * @return a new Pac body group
	 */
	public Group createPacBody(PacConfig pacConfig) {
        final Group body = new Group();

        final var head = new MeshView(headMesh());
        head.setMaterial(coloredPhongMaterial(pacConfig.colors().head()));

        final var eyes = new MeshView(eyesMesh());
        eyes.setMaterial(coloredPhongMaterial(pacConfig.colors().eyes()));

        final var palate = new MeshView(palateMesh());
        palate.setMaterial((coloredPhongMaterial(pacConfig.colors().palate())));

        body.getChildren().addAll(head, eyes, palate);

        final var headBounds = head.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-headBounds.getCenterX(), -headBounds.getCenterY(), -headBounds.getCenterZ());
        Stream.of(head, eyes, palate).forEach(mv -> mv.getTransforms().add(centeredOverOrigin));

        body.getTransforms().add(new Rotate(90,  Rotate.X_AXIS));
        body.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        body.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        final var bounds = body.getBoundsInLocal();
        final float size = pacConfig.size3D();
        final var scaleToSize = new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
        body.getTransforms().add(scaleToSize);

        return body;
	}

	/**
	 * Creates a Pac-Man body without eyes (used for blinking or special effects).
	 *
	 * @param pacConfig the Pac configuration
	 * @return a new {@link PacBodyNoEyes} instance
	 */
	public Group createBlindPacBody(PacConfig pacConfig) {
		//return new PacBodyNoEyes(pacConfig, headMesh(), palateMesh());
        final Group body = new Group();

        final PhongMaterial headMaterial = coloredPhongMaterial(pacConfig.colors().head());
        final var headMeshView = new MeshView(headMesh());
        headMeshView.setMaterial(headMaterial);

        final PhongMaterial palateMaterial = coloredPhongMaterial(pacConfig.colors().palate());
        final var palateMeshView = new MeshView(palateMesh());
        palateMeshView.setMaterial(palateMaterial);

        final var headBounds = headMeshView.getBoundsInLocal();
        final var centeredOverOrigin = new Translate(-headBounds.getCenterX(), -headBounds.getCenterY(), -headBounds.getCenterZ());
        Stream.of(headMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        body.getChildren().addAll(headMeshView, palateMeshView);

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        body.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        body.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        body.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        final var bodyBounds = body.getBoundsInLocal();
        final float size = pacConfig.size3D();
        body.getTransforms().add(
            new Scale(size / bodyBounds.getWidth(), size / bodyBounds.getHeight(), size / bodyBounds.getDepth()));

        return body;
    }

	/**
	 * Creates the additional female parts used for Ms. Pac-Man (hair bow, pearls, etc.).
	 *
	 * @param pacConfig Pac configuration
	 * @return a new {@link MsPacManFemaleParts} instance
	 */
	public MsPacManFemaleParts createFemaleBodyParts(PacConfig pacConfig) {
		return new MsPacManFemaleParts(pacConfig);
	}

	/**
	 * Creates a complete Ms. Pac-Man body consisting of a Pac-Man base body
	 * plus the additional female parts.
	 *
	 * @param pacConfig Pac configuration
	 * @return a new {@link MsPacManBody} instance
	 */
	public MsPacManBody createMsPacManBody(PacConfig pacConfig) {
		return new MsPacManBody(createPacBody(pacConfig), createFemaleBodyParts(pacConfig));
	}
}
