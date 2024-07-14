/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 *
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 *
 * @author Armin Reichert
 */
public abstract class Pac3D extends Group {

    protected static final String MESH_ID_EYES   = "PacMan.Eyes";
    protected static final String MESH_ID_HEAD   = "PacMan.Head";
    protected static final String MESH_ID_PALATE = "PacMan.Palate";

    public static Group createPacShape(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
        var head = new MeshView(model3D.mesh(MESH_ID_HEAD));
        head.setId(Model3D.toCSS_ID(MESH_ID_HEAD));
        head.setMaterial(coloredMaterial(headColor));

        var eyes = new MeshView(model3D.mesh(MESH_ID_EYES));
        eyes.setId(Model3D.toCSS_ID(MESH_ID_EYES));
        eyes.setMaterial(coloredMaterial(eyesColor));

        var palate = new MeshView(model3D.mesh(MESH_ID_PALATE));
        palate.setId(Model3D.toCSS_ID(MESH_ID_PALATE));
        palate.setMaterial(coloredMaterial(palateColor));

        var centeredOverOrigin = Model3D.centeredOverOrigin(head);
        Stream.of(head, eyes, palate).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        var root = new Group(head, eyes, palate);
        root.getTransforms().add(Model3D.scaled(root, size));

        // TODO check/fix Pac-Man mesh position and rotation in .obj file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        return root;
    }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

    protected final Translate position = new Translate();
    protected final Rotate orientation = new Rotate();
    protected final PointLight light;
    protected Pac pac;
    protected double zStandingOnGround;

    protected Pac3D() {
        light = new PointLight();
        light.setMaxRange(2 * TS);
        light.translateXProperty().bind(position.xProperty());
        light.translateYProperty().bind(position.yProperty());
        light.setTranslateZ(-10);
    }

    public abstract Animation createDyingAnimation(GameContext context);

    public abstract void startWalkingAnimation();

    public abstract void stopWalkingAnimation();

    public abstract void setPower(boolean power);

    public Translate position() {
        return position;
    }

    public PointLight light() {
        return light;
    }

    public void init(GameContext context) {
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        update(context);
    }

    public void update(GameContext context) {
        var game = context.game();
        var world = game.world();
        Vector2f center = pac.center();
        position.setX(center.x());
        position.setY(center.y());
        position.setZ(zStandingOnGround);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pac.moveDir()));
        boolean outsideWorld = position.getX() < HTS || position.getX() > TS * world.map().numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
        if (pac.isStandingStill()) {
            stopWalkingAnimation();
        } else {
            startWalkingAnimation();
        }
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(lightedPy.get() && pac.isVisible() && hasPower);
    }
}