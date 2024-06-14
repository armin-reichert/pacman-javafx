package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.scene.Group;

import java.util.stream.Stream;

import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

public class MsPacMan3D extends Pac3D {

    /**
     * Creates a 3D Ms. Pac-Man.
     * @param size diameter of Pac-Man
     * @param msPacMan Ms. Pac-Man instance, may be NULL
     * @param theme the theme
     */
    public MsPacMan3D(double size, Pac msPacMan, Theme theme) {
        super(msPacMan);
        zPosGround = -0.5 * size;

        var body = Pac3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("ms_pacman.color.head"),
            theme.color("ms_pacman.color.eyes"),
            theme.color("ms_pacman.color.palate"));

        var femaleParts = Pac3D.createFemaleParts(size,
            theme.color("ms_pacman.color.hairbow"),
            theme.color("ms_pacman.color.hairbow.pearls"),
            theme.color("ms_pacman.color.boobs"));

        var shapeGroup = new Group(body, femaleParts);
        shapeGroup.getTransforms().setAll(position, orientation);
        getChildren().add(shapeGroup);

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
                .map(id -> meshView(shapeGroup, id))
                .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));

        if (msPacMan != null) {
            setFemaleBehavior(true);
            light.setColor(theme.color("ms_pacman.color.head").desaturate());
        }
    }
}
