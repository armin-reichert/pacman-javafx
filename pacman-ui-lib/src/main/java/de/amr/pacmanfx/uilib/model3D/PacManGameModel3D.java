package de.amr.pacmanfx.uilib.model3D;

import de.amr.meshbuilder.MeshBuilder;
import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import javafx.scene.shape.MeshView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PacManGameModel3D {

    private static final String OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pacman_marco/pacman.obj";

    private static class LazyThreadSafeSingletonHolder {
        static final PacManGameModel3D SINGLETON = new PacManGameModel3D();
    }

    public static PacManGameModel3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    // Idiotic IDs. Blue ghost behind Pac-Man in the OBJ file has correct initial direction.
    private static final String GROUP_ID_DRESS   = "Sphere.004_Sphere.034.Group.anon_6";
    private static final String GROUP_ID_EYEBALLS = "Sphere.009_Sphere.036.Group.anon_7";
    private static final String GROUP_ID_PUPILS    = "Sphere.010_Sphere.039.Group.anon_8";

    private final Map<String, MeshView> meshViewsForGroups;

    private PacManGameModel3D() {
        final URL file = PacManGameModel3D.class.getResource(OBJ_FILE);
        if (file == null) {
            throw new IllegalStateException("Unable to locate " + OBJ_FILE);
        }
        final ObjFileParser parser = new ObjFileParser(file, StandardCharsets.UTF_8);
        try {
            final ObjModel objModel = parser.parse();
            meshViewsForGroups = MeshBuilder.build(objModel, MeshBuilder.BuildMode.BY_GROUP);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public MeshView dress() {
        return meshViewOrFail(GROUP_ID_DRESS);
    }

    public MeshView eyeballs() {
        return meshViewOrFail(GROUP_ID_EYEBALLS);
    }

    public MeshView pupils() {
        return meshViewOrFail(GROUP_ID_PUPILS);
    }

    private MeshView meshViewOrFail(String groupName) {
        final MeshView meshView = meshViewsForGroups.get(groupName);
        if (meshView != null) {
            return meshView;
        }
        throw new IllegalArgumentException("Mesh view for group name %s does not exist".formatted(groupName));
    }

}
