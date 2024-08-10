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
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.level.Pac3D.createChewingAnimation;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D implements Pac3D {

    private static class HipSwayingAnimation {

        static final short ANGLE_FROM = -20;
        static final short ANGLE_TO = 20;
        static final Duration DURATION = Duration.seconds(0.4);

        private final RotateTransition swaying;

        public HipSwayingAnimation(Node target) {
            swaying = new RotateTransition(DURATION, target);
            swaying.setAxis(Rotate.Z_AXIS);
            swaying.setCycleCount(Animation.INDEFINITE);
            swaying.setAutoReverse(true);
            swaying.setInterpolator(Interpolator.EASE_BOTH);
            setWinnetouchMode(false);
        }

        // Note: Winnetouch is the gay twin-brother of Abahachi
        public void setWinnetouchMode(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            swaying.stop();
            swaying.setFromAngle(ANGLE_FROM * amplification);
            swaying.setToAngle(ANGLE_TO * amplification);
            swaying.setRate(rate);
        }

        public void play() {
            swaying.play();
        }

        public void stop() {
            swaying.stop();
            swaying.getNode().setRotationAxis(swaying.getAxis());
            swaying.getNode().setRotate(0);
        }
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final PointLight light = new PointLight();
    private final Rotate moveRotation = new Rotate();
    private final Pac msPacMan;
    private final Group bodyGroup = new Group();
    private final HipSwayingAnimation hipSwayingAnimation;
    private final Animation chewingAnimation;
    private final double initialZ;

    /**
     * Creates a 3D Ms. Pac-Man.
     *
     * @param msPacMan Ms. Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset map
     */
    public MsPacMan3D(Pac msPacMan, double size, AssetMap assets) {
        this.msPacMan = checkNotNull(msPacMan);
        initialZ = -0.5 * size;

        Model3D model3D = assets.get("model3D.pacman");

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color("ms_pacman.color.head"),
            assets.color("ms_pacman.color.eyes"),
            assets.color("ms_pacman.color.palate"));

        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        Group femaleParts = PacModel3D.createFemaleParts(size,
            assets.color("ms_pacman.color.hairbow"),
            assets.color("ms_pacman.color.hairbow.pearls"),
            assets.color("ms_pacman.color.boobs"));

        Node jaw = PacModel3D.createPacSkull(
            model3D, size,
            assets.color("pacman.color.head"),
            assets.color("pacman.color.palate"));

        meshViewById(jaw,  PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw,  PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        bodyGroup.getChildren().addAll(body, femaleParts, jaw);
        bodyGroup.getTransforms().add(moveRotation);
        bodyGroup.setTranslateZ(initialZ);

        light.translateXProperty().bind(bodyGroup.translateXProperty());
        light.translateYProperty().bind(bodyGroup.translateYProperty());
        light.setTranslateZ(initialZ - size);

        chewingAnimation = createChewingAnimation(jaw);
        hipSwayingAnimation = new HipSwayingAnimation(bodyGroup);
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

    @Override
    public void init() {
        updatePosition();
        bodyGroup.setVisible(msPacMan.isVisible());
        hipSwayingAnimation.stop();
        hipSwayingAnimation.setWinnetouchMode(false);
        chewingAnimation.stop();
    }

    @Override
    public void update(GameContext context) {
        if (msPacMan.isAlive()) {
            updatePosition();
            updateVisibility(context.game());
            updateLight(context.game());
        }
        if (msPacMan.isAlive() && !msPacMan.isStandingStill()) {
            hipSwayingAnimation.play();
            chewingAnimation.play();
        } else {
            hipSwayingAnimation.stop();
            chewingAnimation.stop();
        }
    }

    @Override
    public void setPower(boolean power) {
        hipSwayingAnimation.setWinnetouchMode(power);
    }

    @Override
    public Animation createDyingAnimation() {
        var spin = new RotateTransition(Duration.seconds(0.25), bodyGroup);
        spin.setAxis(Rotate.Z_AXIS);
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        return new SequentialTransition(pauseSec(1), spin, pauseSec(1.5));
    }

    private void updatePosition() {
        Vector2f center = msPacMan.center();
        bodyGroup.setTranslateX(center.x());
        bodyGroup.setTranslateY(center.y());
        bodyGroup.setTranslateZ(initialZ);
        updateMoveRotation();
    }

    private void updateMoveRotation() {
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(msPacMan.moveDir()));
    }

    private void updateLight(GameModel game) {
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(PY_3D_PAC_LIGHT_ENABLED.get() && msPacMan.isVisible() && hasPower);
    }

    private void updateVisibility(GameModel game) {
        WorldMap map = game.world().map();
        boolean outsideWorld = bodyGroup.getTranslateX() < HTS || bodyGroup.getTranslateX() > TS * map.terrain().numCols() - HTS;
        bodyGroup.setVisible(msPacMan.isVisible() && !outsideWorld);
    }
}