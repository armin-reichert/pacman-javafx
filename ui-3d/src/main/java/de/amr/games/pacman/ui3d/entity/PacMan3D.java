package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.scene.Group;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

public class PacMan3D extends Pac3D {

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance, may be NULL
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        super(pacMan);
        zPosGround = -0.5 * size;

        var body = Pac3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );

        var shapeGroup = new Group(body);
        shapeGroup.getTransforms().setAll(position, orientation);
        getChildren().add(shapeGroup);

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
                .map(id -> meshView(shapeGroup, id))
                .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));

        if (pacMan != null) {
            setFemaleBehavior(false);
            light.setColor(theme.color("pacman.color.head").desaturate());
        }
    }
}
