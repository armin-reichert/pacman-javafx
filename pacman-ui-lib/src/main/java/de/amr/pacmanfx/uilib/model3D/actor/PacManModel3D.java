/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.model3D.Model3DException;
import de.amr.pacmanfx.uilib.objimport.Model3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
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

    private static class LazyThreadSafeSingletonHolder {
        static final PacManModel3D SINGLETON = new PacManModel3D();
    }

    public static PacManModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

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
	private PacManModel3D() {
		try {
			model3D = Model3D.importObjFile(getClass().getResource("/de/amr/pacmanfx/uilib/model3D/pacman.obj"));
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
	 * @return a new Pac body without eyeballs
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
	 * @return a new female parts group
	 */
	public Group createFemaleBodyParts(PacConfig pacConfig) {
        final Group parts = new Group();

        final int sphereDivisions = 16; // 64 is default

        final PhongMaterial bowMaterial = coloredPhongMaterial(pacConfig.msColors().hairBow());

        final Sphere bowLeft = new Sphere(1.2, sphereDivisions);
        bowLeft.setMaterial(bowMaterial);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacConfig.size3D() * 0.55));

        final Sphere bowRight = new Sphere(1.2, sphereDivisions);
        bowRight.setMaterial(bowMaterial);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacConfig.size3D() * 0.55));

        final PhongMaterial pearlMaterial = coloredPhongMaterial(pacConfig.msColors().hairBowPearls());

        final Sphere pearlLeft = new Sphere(0.4, sphereDivisions);
        pearlLeft.setMaterial(pearlMaterial);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacConfig.size3D() * 0.58));

        final Sphere pearlRight = new Sphere(0.4, sphereDivisions);
        pearlRight.setMaterial(pearlMaterial);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacConfig.size3D() * 0.58));

        final PhongMaterial beautySpotMaterial = coloredPhongMaterial(Color.rgb(120, 120, 120));
        final Sphere beautySpot = new Sphere(0.5, sphereDivisions);
        beautySpot.getTransforms().addAll(new Translate(-0.33 * pacConfig.size3D(), -0.4 * pacConfig.size3D(), -0.14 * pacConfig.size3D()));
        beautySpot.setMaterial(beautySpotMaterial);

        final PhongMaterial silicone = coloredPhongMaterial(pacConfig.msColors().boobs());

        final double bx = -0.2 * pacConfig.size3D(); // forward
        final double by = 1.6; // or - 1.6 // sidewards
        final double bz = 0.4 * pacConfig.size3D(); // up/down

        final Sphere boobLeft = new Sphere(1.8, sphereDivisions);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        final Sphere boobRight = new Sphere(1.8, sphereDivisions);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        parts.getChildren().addAll(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
        return parts;
    }

	/**
	 * Creates a complete Ms. Pac-Man body consisting of a Pac-Man base body
	 * plus the additional female parts.
	 *
	 * @param pacConfig Pac configuration
	 * @return a new Ms Pac-Man body instance
	 */
	public Group createMsPacManBody(PacConfig pacConfig) {
		return new Group(createPacBody(pacConfig), createFemaleBodyParts(pacConfig));
	}
}
