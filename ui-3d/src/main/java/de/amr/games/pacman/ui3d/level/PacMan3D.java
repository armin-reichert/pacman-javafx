/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.level.Pac3D.createChewingAnimation;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class PacMan3D implements Pac3D {

    private static class HeadBangingAnimation {

        private static final float POWER_AMPLIFICATION = 2;
        private static final short ANGLE_FROM = -15;
        private static final short ANGLE_TO = 20;
        private static final Duration DURATION = Duration.seconds(0.3);

        private final RotateTransition banging;

        public HeadBangingAnimation(Node target) {
            banging = new RotateTransition(DURATION, target);
            banging.setAxis(Rotate.X_AXIS);
            banging.setCycleCount(Animation.INDEFINITE);
            banging.setAutoReverse(true);
            banging.setInterpolator(Interpolator.EASE_BOTH);
            setStrokeMode(false);
        }

        // Note: Massive headbanging can lead to a stroke!
        public void setStrokeMode(boolean power) {
            banging.stop();
            float rate = power ? POWER_AMPLIFICATION : 1;
            banging.setFromAngle(ANGLE_FROM * rate);
            banging.setToAngle(ANGLE_TO * rate);
            banging.setRate(rate);
        }

        public void update(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
            } else {
                Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
                if (!axis.equals(banging.getAxis())) {
                    banging.stop();
                    banging.setAxis(axis);
                }
                banging.play();
            }
        }

        public void stop() {
            banging.stop();
            banging.getNode().setRotationAxis(banging.getAxis());
            banging.getNode().setRotate(0);
        }
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final PointLight light = new PointLight();
    private final Rotate moveRotation = new Rotate();
    private final Pac pacMan;
    private final Group bodyGroup = new Group();
    private final HeadBangingAnimation headBangingAnimation;
    private final Animation chewingAnimation;
    private final double initialZ;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param pacMan Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset map
     */
    public PacMan3D(Pac pacMan, double size, AssetMap assets) {
        this.pacMan = checkNotNull(pacMan);
        initialZ = -0.5 * size;

        Model3D model3D = assets.get("model3D.pacman");

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.eyes"),
            assets.color("pacman.color.palate")
        );
        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        Node jaw = PacModel3D.createPacSkull(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.palate"));
        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        bodyGroup.getChildren().addAll(body, jaw);
        bodyGroup.getTransforms().add(moveRotation);
        bodyGroup.setTranslateZ(initialZ);

        light.translateXProperty().bind(bodyGroup.translateXProperty());
        light.translateYProperty().bind(bodyGroup.translateYProperty());
        light.setTranslateZ(initialZ - size);

        chewingAnimation = createChewingAnimation(jaw);
        headBangingAnimation = new HeadBangingAnimation(bodyGroup);
    }

    @Override
    public Group root() {
        return bodyGroup;
    }


    @Override
    public PointLight light() {
        return light;
    }

    @Override
    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    private void updatePosition() {
        Vector2f center = pacMan.center();
        bodyGroup.setTranslateX(center.x());
        bodyGroup.setTranslateY(center.y());
        bodyGroup.setTranslateZ(initialZ);
        updateMoveRotation();
    }

    @Override
    public void init() {
        updatePosition();
        bodyGroup.setVisible(pacMan.isVisible());
        bodyGroup.setScaleX(1.0);
        bodyGroup.setScaleY(1.0);
        bodyGroup.setScaleZ(1.0);
        headBangingAnimation.stop();
        headBangingAnimation.setStrokeMode(false);
        chewingAnimation.stop();
    }

    @Override
    public void update(GameContext context) {
        if (pacMan.isAlive()) {
            updatePosition();
            updateVisibility(context.game());
            updateLight(context.game());
        }
        if (pacMan.isAlive() && !pacMan.isStandingStill()) {
            headBangingAnimation.update(pacMan);
            chewingAnimation.play();
        } else {
            headBangingAnimation.stop();
            chewingAnimation.stop();
        }
    }

    @Override
    public void setPower(boolean power) {
        headBangingAnimation.setStrokeMode(power);
    }

    @Override
    public Animation createDyingAnimation() {
        Duration duration = Duration.seconds(1.0);
        byte numSpins = 6;

        var spins = new RotateTransition(duration.divide(numSpins), bodyGroup);
        spins.setAxis(Rotate.Z_AXIS);
        spins.setByAngle(360);
        spins.setCycleCount(numSpins);
        spins.setInterpolator(Interpolator.LINEAR);

        var shrinks = new ScaleTransition(duration.multiply(0.5), bodyGroup);
        shrinks.setToX(0.25);
        shrinks.setToY(0.25);
        shrinks.setToZ(0.02);

        var expands = new ScaleTransition(duration.multiply(0.5), bodyGroup);
        expands.setToX(0.75);
        expands.setToY(0.75);

        var sinks = new TranslateTransition(duration, bodyGroup);
        sinks.setToZ(0);

        return new SequentialTransition(
            now(this::init), // TODO check this
            pauseSec(0.5),
            new ParallelTransition(spins, new SequentialTransition(shrinks, expands), sinks),
            doAfterSec(1.0, () -> bodyGroup.setVisible(false))
        );
    }

    private void updateMoveRotation() {
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pacMan.moveDir()));
    }

    private void updateLight(GameModel game) {
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(PY_3D_PAC_LIGHT_ENABLED.get() && pacMan.isVisible() && hasPower);
    }

    private void updateVisibility(GameModel game) {
        WorldMap map = game.world().map();
        boolean outsideWorld = root().getTranslateX() < HTS || root().getTranslateX() > TS * map.terrain().numCols() - HTS;
        bodyGroup.setVisible(pacMan.isVisible() && !outsideWorld);
    }
}