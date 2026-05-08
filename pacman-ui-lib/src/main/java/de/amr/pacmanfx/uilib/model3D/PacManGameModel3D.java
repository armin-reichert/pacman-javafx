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
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;

public class PacManGameModel3D {

    private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pacman_marco/pacman.obj";

    private static class LazyThreadSafeSingletonHolder {
        static final PacManGameModel3D SINGLETON = new PacManGameModel3D();
    }

    public static PacManGameModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    public static <T extends Node> T fixShapeOrientation(T shape) {
        shape.getTransforms().add(new Rotate(270,  Rotate.X_AXIS));
        return shape;
    }

    // Strange IDs

    // Blue ghost behind Pac-Man in the OBJ file has correct initial direction.
    private static final String GROUP_ID_GHOST_DRESS    = "GhostCyanHead.GhostCyanHead_light_blue_ghost";
    private static final String GROUP_ID_GHOST_EYEBALLS = "GhostCyanEyeballs.GhostCyanEyeballs_white";
    private static final String GROUP_ID_GHOST_PUPILS   = "GhostCyanPupils.GhostCyanPupils_grey_wall";

    private static final String GROUP_ID_PAC_EYES   = "PacManEyes.PacManEyes_grey_wall";
    private static final String GROUP_ID_PAC_HEAD   = "PacManHead.PacManHead_yellow_pacman";
    private static final String GROUP_ID_PAC_PALATE = "PacManPalate.PacManPalate_grey_wall";

    private final Map<String, MeshView> meshViews;
    private final Map<String, PhongMaterial> materials;

    private PacManGameModel3D() {
        final URL file = PacManGameModel3D.class.getResource(OBJ_FILE);
        if (file == null) {
            throw new IllegalStateException("Unable to locate " + OBJ_FILE);
        }
        final ObjFileParser parser = new ObjFileParser(file, StandardCharsets.UTF_8);
        try {
            final ObjModel objModel = parser.parse();
            final MeshBuilder builder = new MeshBuilder(objModel);
            meshViews = builder.buildMeshViewsByGroup();
            materials = new HashMap<>(builder.materials());
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public Map<String, PhongMaterial> materials() {
        return materials;
    }

    public MeshView ghostDress() {
        return assertMeshViewExists(GROUP_ID_GHOST_DRESS);
    }

    public MeshView ghostEyeballs() {
        return assertMeshViewExists(GROUP_ID_GHOST_EYEBALLS);
    }

    public MeshView ghostPupils() {
        return assertMeshViewExists(GROUP_ID_GHOST_PUPILS);
    }

    public GhostMeshSet createGhostMeshSet() {
        return new GhostMeshSet(ghostDress().getMesh(), ghostPupils().getMesh(), ghostEyeballs().getMesh());
    }

    public MeshView pacHead() {
        return assertMeshViewExists(GROUP_ID_PAC_HEAD);
    }

    public MeshView pacPalate() {
        return assertMeshViewExists(GROUP_ID_PAC_PALATE);
    }

    public MeshView pacEyes() {
        return assertMeshViewExists(GROUP_ID_PAC_EYES);
    }

    private MeshView assertMeshViewExists(String name) {
        final MeshView meshView = meshViews.get(name);
        if (meshView != null) {
            return meshView;
        }
        throw new IllegalArgumentException("Mesh view for group name %s does not exist".formatted(name));
    }

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @return a new Pac body group
     */
    public Group createPacBody(PacConfig config) {
        final MeshView head = createHead(config);

        final var eyes = new MeshView(pacEyes().getMesh());
        eyes.setMaterial(coloredPhongMaterial(config.colors().eyes()));

        final var palate = new MeshView(pacPalate().getMesh());
        palate.setMaterial((coloredPhongMaterial(config.colors().palate())));

        final Group body = new Group(head, eyes, palate);
        centerOverOrigin(head, eyes, palate);
        normalizeBodySize(body, config.size3D());

        return fixShapeOrientation(body);
    }

    /**
     * Creates a Pac-Man body without eyes (used for blinking or special effects).
     *
     * @param config the Pac configuration
     * @return a new Pac body without eyeballs
     */
    public Group createBlindPacBody(PacConfig config) {
        final MeshView head = createHead(config);

        final var palate = new MeshView(pacPalate().getMesh());
        palate.setMaterial(coloredPhongMaterial(config.colors().palate()));

        final Group body = new Group(head, palate);
        centerOverOrigin(head, palate);
        normalizeBodySize(body, config.size3D());

        return fixShapeOrientation(body);
    }

    private MeshView createHead(PacConfig config) {
        final var head = new MeshView(pacHead().getMesh());
        // to use Pac-Man material from OBJ file:
//        final PhongMaterial headMaterial = materials.getOrDefault("yellow_pacman", coloredPhongMaterial(config.colors().head()));
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
