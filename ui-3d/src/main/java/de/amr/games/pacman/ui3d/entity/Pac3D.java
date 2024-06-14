/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
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
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 *
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 *
 * @author Armin Reichert
 */
public class Pac3D extends Group {

    public interface Walking {
        void walk();
        void stop();
        void setPower(boolean power);
    }

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
    protected final Pac pac;
    protected final PointLight light;
    protected double zPosGround;
    protected Walking walking;

    protected Pac3D(Pac pac) {
        this.pac = pac;
        light = new PointLight();
        light.setMaxRange(2 * TS);
        light.translateXProperty().bind(position.xProperty());
        light.translateYProperty().bind(position.yProperty());
        light.setTranslateZ(-10);
    }

    public void setPower(boolean power) {
        walking.setPower(power);
    }

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
        Vector2f center = pac.center();
        position.setX(center.x());
        position.setY(center.y());
        position.setZ(zPosGround);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pac.moveDir()));
        setVisible(pac.isVisible() && !outsideWorld(pac.world()));
        if (pac.isStandingStill()) {
            walking.stop();
        } else {
            walking.walk();
        }
        updateLight(context);
    }

    private void updateLight(GameContext context) {
        var game = context.game();
        double radius = 0;
        if (game.powerTimer().duration() > 0) {
            double t = (double) game.powerTimer().remaining() / game.powerTimer().duration();
            radius = t * 6 * TS;
        }
        boolean hasPower = game.powerTimer().isRunning();
        light.setMaxRange(hasPower ? 2 * TS + radius : 0);
        light.setLightOn(lightedPy.get() && pac.isVisible() && hasPower);
    }

    private boolean outsideWorld(World world) {
        return position.getX() < HTS || position.getX() > TS * world.numCols() - HTS;
    }

    public Animation createMsPacManDyingAnimation() {
        var spin = new RotateTransition(Duration.seconds(0.5), this);
        spin.setAxis(Rotate.X_AXIS); //TODO check this
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setRate(2);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(
            spin,
            pauseSec(2)
        );
    }

    public Animation createPacManDyingAnimation(GameContext context) {
        Duration duration = Duration.seconds(1.0);
        short numSpins = 6;

        var spinning = new RotateTransition(duration.divide(numSpins), this);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        var shrinking = new ScaleTransition(duration, this);
        shrinking.setToX(0.5);
        shrinking.setToY(0.5);
        shrinking.setToZ(0.0);
        shrinking.setInterpolator(Interpolator.LINEAR);

        var falling = new TranslateTransition(duration, this);
        falling.setToZ(4);
        falling.setInterpolator(Interpolator.EASE_IN);

        //TODO does not yet work as I want to
        return new SequentialTransition(
            now(() -> init(context)),
            pauseSec(0.5),
            new ParallelTransition(spinning, shrinking, falling),
            doAfterSec(1.0, () -> {
                setVisible(false);
                setTranslateZ(0);
            })
        );
    }
}