/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.uilib.objimport.ObjFileParserByCopilot;
import de.amr.pacmanfx.uilib.objimport.TriangleMeshBuilderByCopilot;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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

	private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pacman.obj";

    private static final String ID_EYES = "Material.Eyes";
	private static final String ID_HEAD = "Material.Head";
	private static final String ID_PALATE = "Material.Palate";

    private final Map<String, MeshView> meshesByMaterialName;

	private PacManModel3D() {
		try {
            URL url = getClass().getResource(OBJ_FILE);
            ObjFileParserByCopilot parser = new ObjFileParserByCopilot(url, StandardCharsets.UTF_8);
            TriangleMeshBuilderByCopilot builder = new TriangleMeshBuilderByCopilot(parser.objModel(), parser.materialLibsMap());
            meshesByMaterialName = builder.buildMeshViewsByMaterial();
			// fail fast
			meshOrFail(ID_HEAD);
			meshOrFail(ID_PALATE);
			meshOrFail(ID_EYES);
		} catch (Exception x) {
			throw new RuntimeException("Failed to load Pac-Man 3D model", x);
		}
	}

    @Override
    public void dispose() {
        meshesByMaterialName.clear();
    }

    private Mesh meshOrFail(String materialName) {
        final MeshView meshView = meshesByMaterialName.get(materialName);
        if (meshView != null) {
            return meshView.getMesh();
        }
        throw new IllegalArgumentException("Mesh view for material %s does not exist".formatted(materialName));
    }


    /**
	 * Creates a fully assembled Pac-Man body with head, eyes, and palate.
	 *
	 * @param config the Pac configuration
	 * @return a new Pac body group
	 */
	public Group createPacBody(PacConfig config) {
        final MeshView head = createHead(config);

        final var eyes = new MeshView(meshOrFail(ID_EYES));
        eyes.setMaterial(coloredPhongMaterial(config.colors().eyes()));

        final var palate = new MeshView(meshOrFail(ID_PALATE));
        palate.setMaterial((coloredPhongMaterial(config.colors().palate())));

		final Group body = new Group(head, eyes, palate);
		centerOverOrigin(head, eyes, palate);
		normalizeBodySize(body, config.size3D());
		fixBodyRotation(body); //TODO fix in obj file

        return body;
	}

	/**
	 * Creates a Pac-Man body without eyes (used for blinking or special effects).
	 *
	 * @param config the Pac configuration
	 * @return a new Pac body without eyeballs
	 */
	public Group createBlindPacBody(PacConfig config) {
		final MeshView head = createHead(config);

        final var palate = new MeshView(meshOrFail(ID_PALATE));
        palate.setMaterial(coloredPhongMaterial(config.colors().palate()));

		final Group body = new Group(head, palate);
		centerOverOrigin(head, palate);
		normalizeBodySize(body, config.size3D());
		fixBodyRotation(body); //TODO fix in obj file

        return body;
    }

	private MeshView createHead(PacConfig config) {
		final Mesh headMesh = meshOrFail(ID_HEAD);
		final var head = new MeshView(headMesh);

		// If we would like to use the material defined in the OBJ file:
		//final PhongMaterial headMaterial = model3D.modelMaterialAssignment(headMesh).orElse(coloredPhongMaterial(config.colors().head()));
		final PhongMaterial headMaterial = coloredPhongMaterial(config.colors().head());
		head.setMaterial(headMaterial);

		return head;
	}

	private static void centerOverOrigin(Node master, Node... slaves) {
		final Bounds masterBounds = master.getBoundsInLocal();
		final var centeredOverOrigin = new Translate(
			-masterBounds.getCenterX(),
			-masterBounds.getCenterY(),
			-masterBounds.getCenterZ());
		master.getTransforms().add(centeredOverOrigin);
		Stream.of(slaves).map(Node::getTransforms).forEach(tf -> tf.add(centeredOverOrigin));
	}

	private static void fixBodyRotation(Node body) {
		body.getTransforms().add(new Rotate(90,  Rotate.X_AXIS));
		body.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		body.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
	}

	private static void normalizeBodySize(Node body, float size) {
		final var bounds = body.getBoundsInLocal();
		body.getTransforms().add(new Scale(
			size / bounds.getWidth(),
			size / bounds.getHeight(),
			size / bounds.getDepth())
		);
	}

	/**
	 * Creates the additional female parts used for Ms. Pac-Man (hair bow, pearls, etc.).
	 *
	 * @param config Pac configuration
	 * @return a new female parts group
	 */
	public Group createFemaleBodyParts(PacConfig config) {
        final int sphereDivisions = 16; // 64 is default

        final PhongMaterial bowMaterial = coloredPhongMaterial(config.msColors().hairBow());

        final Sphere bowLeft = new Sphere(1.2, sphereDivisions);
        bowLeft.setMaterial(bowMaterial);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -config.size3D() * 0.55));

        final Sphere bowRight = new Sphere(1.2, sphereDivisions);
        bowRight.setMaterial(bowMaterial);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -config.size3D() * 0.55));

        final PhongMaterial pearlMaterial = coloredPhongMaterial(config.msColors().hairBowPearls());

        final Sphere pearlLeft = new Sphere(0.4, sphereDivisions);
        pearlLeft.setMaterial(pearlMaterial);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -config.size3D() * 0.58));

        final Sphere pearlRight = new Sphere(0.4, sphereDivisions);
        pearlRight.setMaterial(pearlMaterial);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -config.size3D() * 0.58));

        final PhongMaterial beautySpotMaterial = coloredPhongMaterial(Color.rgb(120, 120, 120));
        final Sphere beautySpot = new Sphere(0.5, sphereDivisions);
        beautySpot.getTransforms().addAll(new Translate(-0.33 * config.size3D(), -0.4 * config.size3D(), -0.14 * config.size3D()));
        beautySpot.setMaterial(beautySpotMaterial);

        final PhongMaterial silicone = coloredPhongMaterial(config.msColors().boobs());

        final double bx = -0.2 * config.size3D(); // forward
        final double by = 1.6; // or - 1.6 // sidewards
        final double bz = 0.4 * config.size3D(); // up/down

        final Sphere boobLeft = new Sphere(1.8, sphereDivisions);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        final Sphere boobRight = new Sphere(1.8, sphereDivisions);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

	/**
	 * Creates a complete Ms. Pac-Man body consisting of a Pac-Man base body
	 * plus the additional female parts.
	 *
	 * @param config Pac configuration
	 * @return a new Ms Pac-Man body instance
	 */
	public Group createMsPacManBody(PacConfig config) {
		return new Group(createPacBody(config), createFemaleBodyParts(config));
	}
}
