/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.meshbuilder.MeshBuilder;
import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.pacmanfx.uilib.model3D.actor.GhostMeshSet;
import de.amr.pacmanfx.uilib.model3D.actor.PacConfig;
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
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * Pac-Man game 3D model provided by fellow 3D artist Gianmarco Cavallaccio
 * (<a href="https://www.artstation.com/gianmart">Homepage</a>).
 * Only the Pac-Man and one ghost from that 3D model are used though.
 * For the pellets, another model is used, and the maze is procedurally generated from the map data.
 */
public class PacManWorld3D {

    private static class LazyThreadSafeSingletonHolder {
        static final PacManWorld3D SINGLETON = create();
    }

    private static PacManWorld3D create() {
        try {
            return new PacManWorld3D();
        } catch (IOException x) {
            Logger.error(x, "An error occurred on creation of the Pac-Man game 3D model");
            throw new ExceptionInInitializerError(x);
        }
    }

    public static PacManWorld3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static final String PAC_MAN_WORLD_OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pacman_marco/pacman.obj";

    // Strange IDs but it is what it is and it isn't what it isn't.

    // Blue ghost behind Pac-Man in the OBJ file
    private static final String ID_GHOST_DRESS    = "GhostCyanHead.GhostCyanHead_light_blue_ghost";
    private static final String ID_GHOST_EYEBALLS = "GhostCyanEyeballs.GhostCyanEyeballs_white";
    private static final String ID_GHOST_PUPILS   = "GhostCyanPupils.GhostCyanPupils_grey_wall";

    // Pac-Man mesh IDs
    private static final String ID_PAC_HEAD   = "PacManHead.PacManHead_yellow_pacman";
    private static final String ID_PAC_EYES   = "PacManEyes.PacManEyes_grey_wall";
    private static final String ID_PAC_PALATE = "PacManPalate.PacManPalate_grey_wall";

    // Pellet 3D model
    private static final String PELLET_OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pellet.obj";
    private static final String ID_PELLET = "Object.Pellet";

    /**
     * Rotates Pac-Man / the used ghost to fit into the 3D play scene.
     */
    public static <T extends Node> T fixShapeOrientation(T node) {
        node.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        return node;
    }

    private Map<String, MeshView> pacManWorldMeshViews;
    private Map<String, PhongMaterial> pacManWorldMaterials;
    private Mesh pelletMesh;

    private PacManWorld3D() throws IOException {
        loadPacManWorldModel();
        loadPelletModel();
    }

    private void loadPacManWorldModel() throws IOException {
        final URL url = getClass().getResource(PAC_MAN_WORLD_OBJ_FILE);
        if (url == null) {
            throw new ExceptionInInitializerError("Unable to create 3D model from .obj file " + PAC_MAN_WORLD_OBJ_FILE);
        }
        final ObjModel objModel = new ObjFileParser(url, StandardCharsets.UTF_8).parse();
        final MeshBuilder meshBuilder = new MeshBuilder(objModel);
        pacManWorldMeshViews = meshBuilder.buildMeshViewsByGroup();
        pacManWorldMaterials = new HashMap<>(meshBuilder.materials());
    }

    private void loadPelletModel() throws IOException {
        final URL url = getClass().getResource(PELLET_OBJ_FILE);
        if (url == null) {
            throw new ExceptionInInitializerError("Unable to create 3D model from .obj file " + PELLET_OBJ_FILE);
        }
        final ObjModel objModel = new ObjFileParser(url, StandardCharsets.UTF_8).parse();
        final Map<String, MeshView> meshViews = MeshBuilder.build(objModel, MeshBuilder.BuildMode.BY_OBJECT);
        pelletMesh = requireNonNull(meshViews.get(ID_PELLET)).getMesh();
    }

    public Map<String, PhongMaterial> materials() {
        return pacManWorldMaterials;
    }

    public MeshView ghostDress() {
        return assertMeshViewExists(ID_GHOST_DRESS);
    }

    public MeshView ghostEyeballs() {
        return assertMeshViewExists(ID_GHOST_EYEBALLS);
    }

    public MeshView ghostPupils() {
        return assertMeshViewExists(ID_GHOST_PUPILS);
    }

    public GhostMeshSet createGhostMeshSet() {
        return new GhostMeshSet(ghostDress().getMesh(), ghostPupils().getMesh(), ghostEyeballs().getMesh());
    }

    public MeshView pacHead() {
        return assertMeshViewExists(ID_PAC_HEAD);
    }

    public MeshView pacPalate() {
        return assertMeshViewExists(ID_PAC_PALATE);
    }

    public MeshView pacEyes() {
        return assertMeshViewExists(ID_PAC_EYES);
    }

    public Mesh pelletMesh() { return pelletMesh; }

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @return a new Pac body group
     */
    public Group createPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config, true);
        final MeshView eyes = createPacEyes(config);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, eyes, palate);
        centerOverOrigin(head, List.of(eyes, palate));
        return fixShapeOrientation(resize(body, config.size3D()));
    }

    /**
     * Creates a Pac-Man body without eyes (used for jaw open/close animation).
     *
     * @param config the Pac configuration
     * @return a Pac body without eyes
     */
    public Group createBlindPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config, true);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, palate);
        centerOverOrigin(head, List.of(palate));
        return fixShapeOrientation(resize(body, config.size3D()));
    }

    /**
     * Creates the additional female parts used for Ms. Pac-Man (hair bow, pearls, etc.).
     *
     * @param config Pac configuration
     * @return a new female parts group
     */
    public Group createFemalePacBodyParts(PacConfig config) {
        requireNonNull(config);

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
        return new Group(createPacBody(config), createFemalePacBodyParts(config));
    }

    // private

    private MeshView createPacHead(PacConfig config, boolean boring) {
        final PhongMaterial boringMaterial = coloredPhongMaterial(config.colors().head());
        return createMeshView(pacHead().getMesh(), boring
            ? boringMaterial
            : pacManWorldMaterials.getOrDefault("yellow_pacman", boringMaterial));
    }

    private MeshView createPacPalate(PacConfig config) {
        return createMeshView(pacPalate().getMesh(), coloredPhongMaterial(config.colors().palate()));
    }

    private MeshView createPacEyes(PacConfig config) {
        return createMeshView(pacEyes().getMesh(), coloredPhongMaterial(config.colors().eyes()));
    }

    private static MeshView createMeshView(Mesh mesh, PhongMaterial material) {
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    private MeshView assertMeshViewExists(String name) {
        final MeshView meshView = pacManWorldMeshViews.get(name);
        if (meshView != null) {
            return meshView;
        }
        throw new IllegalArgumentException("Mesh view for group name %s does not exist".formatted(name));
    }

    private static void centerOverOrigin(Node master, List<Node> slaves) {
        final Bounds b = master.getBoundsInLocal();
        final var centerOverOrigin = new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
        master.getTransforms().add(centerOverOrigin);
        slaves.stream().map(Node::getTransforms).forEach(tf -> tf.add(centerOverOrigin));
    }

    private static <T extends Node> T resize(T body, float size) {
        final Bounds b = body.getBoundsInLocal();
        body.getTransforms().add(new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth()));
        return body;
    }
}
