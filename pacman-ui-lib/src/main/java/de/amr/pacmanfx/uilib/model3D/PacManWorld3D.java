/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.meshbuilder.MeshBuilder;
import de.amr.objparser.ObjFileParser;
import de.amr.objparser.ObjModel;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.shape.Mesh;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Pac-Man game 3D model provided by fellow 3D artist Gianmarco Cavallaccio
 * (<a href="https://www.artstation.com/gianmart">Homepage</a>).
 * <p>
 * Only the Pac-Man and one ghost from that 3D model are used though. The materials defined in the OBJ file are also
 * not used. Instead, we use colored materials created according to the color scheme needed for the games.
 * <p>
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
            Logger.error(x);
            throw new ExceptionInInitializerError("An error occurred on creation of the Pac-Man game 3D model");
        }
    }

    public static final  Rotate ORIENTATION_ADJUSTMENT = new Rotate(270, Rotate.X_AXIS);

    public static PacManWorld3D instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static final String PAC_MAN_WORLD_OBJ_FILE = "/de/amr/pacmanfx/uilib/model3D/pacmanworld/pacman.obj";

    // Strange IDs but it is what it is and it isn't what it isn't.

    private static final String ID_GHOST_DRESS    = "GhostCyanHead.GhostCyanHead_light_blue_ghost";
    private static final String ID_GHOST_EYEBALLS = "GhostCyanEyeballs.GhostCyanEyeballs_white";
    private static final String ID_GHOST_PUPILS   = "GhostCyanPupils.GhostCyanPupils_grey_wall";
    private static final String ID_PAC_HEAD       = "PacManHead.PacManHead_yellow_pacman";
    private static final String ID_PAC_EYES       = "PacManEyes.PacManEyes_grey_wall";
    private static final String ID_PAC_PALATE     = "PacManPalate.PacManPalate_grey_wall";

    private static final Set<String> MESH_IDs = Set.of(
        ID_GHOST_DRESS,
        ID_GHOST_EYEBALLS,
        ID_GHOST_PUPILS,
        ID_PAC_HEAD,
        ID_PAC_EYES,
        ID_PAC_PALATE
    );

    private Map<String, Mesh> meshes;

    private PacManWorld3D() throws IOException {
        Ufx.measureDuration("3D model loading", this::loadPacManWorldModel);
        MESH_IDs.forEach(meshID -> requireNonNull(meshes.get(meshID)));
    }

    private void loadPacManWorldModel() {
        final URL url = getClass().getResource(PAC_MAN_WORLD_OBJ_FILE);
        if (url == null) {
            throw new ExceptionInInitializerError("Unable to create 3D model from .obj file " + PAC_MAN_WORLD_OBJ_FILE);
        }
        try {
            final ObjModel objModel = new ObjFileParser(url, StandardCharsets.UTF_8).parse();
            final MeshBuilder meshBuilder = new MeshBuilder(objModel);
            meshes = meshBuilder.buildMeshViewsByGroup(MESH_IDs::contains)
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getMesh()));
        } catch (IOException x) {
            Logger.error(x, "3D model loading failed.");
        }
    }

    public Mesh ghostDressMesh() {
        return meshes.get(ID_GHOST_DRESS);
    }

    public Mesh ghostEyeballsMesh() {
        return meshes.get(ID_GHOST_EYEBALLS);
    }

    public Mesh ghostPupilsMesh() {
        return meshes.get(ID_GHOST_PUPILS);
    }

    public Mesh pacHeadMesh() {
        return meshes.get(ID_PAC_HEAD);
    }

    public Mesh pacPalateMesh() {
        return meshes.get(ID_PAC_PALATE);
    }

    public Mesh pacEyesMesh() {
        return meshes.get(ID_PAC_EYES);
    }
}
