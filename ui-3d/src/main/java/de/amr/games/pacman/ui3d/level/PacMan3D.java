/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class PacMan3D implements Pac3D {

    private final Pac pacMan;
    private final PacShape3D shape3D;
    private RotateTransition headBanging;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param pacMan Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset map
     */
    public PacMan3D(Pac pacMan, double size, AssetMap assets) {
        this.pacMan = checkNotNull(pacMan);
        checkNotNull(assets);

        Model3D model3D = assets.get("model3D.pacman");

        shape3D = new PacShape3D(model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.palate"));

        Group body = PacModel3D.createPacShape(model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.eyes"),
            assets.color("pacman.color.palate")
        );
        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(shape3D.drawModeProperty());
        shape3D.getChildren().add(body);

        createHeadBangingAnimation(shape3D);
    }

    @Override
    public PacShape3D shape3D() {
        return shape3D;
    }

    @Override
    public void init() {
        shape3D.init(pacMan);
        stopHeadBanging();
        setStrokeMode(false);
    }

    @Override
    public void update(GameContext context) {
        if (pacMan.isAlive()) {
            shape3D.updatePosition(pacMan);
            shape3D.updateLight(pacMan, context.game());
            shape3D.updateVisibility(pacMan, context.game().world());
        }
        if (pacMan.isAlive() && !pacMan.isStandingStill()) {
            updateHeadBanging(pacMan);
            shape3D.chew();
        } else {
            stopHeadBanging();
            shape3D.stopChewingAndOpenMouth();
        }
    }

    @Override
    public void setPowerMode(boolean on) {
        setStrokeMode(on);
    }

    @Override
    public Animation createDyingAnimation() {
        Duration duration = Duration.seconds(1.5);
        byte numSpins = 6;

        var spins = new RotateTransition(duration.divide(numSpins), shape3D);
        spins.setAxis(Rotate.Z_AXIS);
        spins.setByAngle(360);
        spins.setCycleCount(numSpins);
        spins.setInterpolator(Interpolator.LINEAR);

        var shrinks = new ScaleTransition(duration.multiply(0.5), shape3D);
        shrinks.setToX(0.25);
        shrinks.setToY(0.25);
        shrinks.setToZ(0.02);

        var expands = new ScaleTransition(duration.multiply(0.5), shape3D);
        expands.setToX(0.75);
        expands.setToY(0.75);

        var sinks = new TranslateTransition(duration, shape3D);
        sinks.setToZ(0);

        return new SequentialTransition(
            now(this::init), // TODO check this
            pauseSec(0.5),
            new ParallelTransition(spins, new SequentialTransition(shrinks, expands), sinks),
            doAfterSec(1.0, () -> shape3D.setVisible(false))
        );
    }

    // Head banging animation

    static final float POWER_AMPLIFICATION = 2;
    static final short ANGLE_FROM = -10;
    static final short ANGLE_TO = 15;
    static final Duration DURATION = Duration.seconds(0.3);

    private void createHeadBangingAnimation(Node target) {
        headBanging = new RotateTransition(DURATION, target);
        headBanging.setAxis(Rotate.X_AXIS);
        headBanging.setCycleCount(Animation.INDEFINITE);
        headBanging.setAutoReverse(true);
        headBanging.setInterpolator(Interpolator.EASE_BOTH);
        setStrokeMode(false);
    }

    // Note: Massive headbanging can lead to a stroke!
    private void setStrokeMode(boolean on) {
        headBanging.stop();
        float rate = on ? POWER_AMPLIFICATION : 1;
        headBanging.setFromAngle(ANGLE_FROM * rate);
        headBanging.setToAngle(ANGLE_TO * rate);
        headBanging.setRate(rate);
    }

    private void updateHeadBanging(Pac pac) {
        if (pac.isStandingStill()) {
            stopHeadBanging();
        } else {
            Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(headBanging.getAxis())) {
                headBanging.stop();
                headBanging.setAxis(axis);
            }
            headBanging.play();
        }
    }

    private void stopHeadBanging() {
        headBanging.stop();
        headBanging.getNode().setRotationAxis(headBanging.getAxis());
        headBanging.getNode().setRotate(0);
    }
}