/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.assets.AssetStorage;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Ufx.doAfterSec;
import static de.amr.games.pacman.uilib.Ufx.now;
import static de.amr.games.pacman.uilib.model3D.Model3D.meshViewById;
import static java.util.Objects.requireNonNull;

public class PacMan3D implements Pac3D {

    public static Animation createChewingAnimation(Node jaw) {
        var closed = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
        };
        var open = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
        };
        Timeline animation = new Timeline(
            new KeyFrame(Duration.ZERO,        "Open on Start", open),
            new KeyFrame(Duration.millis(100), "Start Closing", open),
            new KeyFrame(Duration.millis(130), "Closed",        closed),
            new KeyFrame(Duration.millis(200), "Start Opening", closed),
            new KeyFrame(Duration.millis(280), "Open",          open)
        );
        animation.setCycleCount(Animation.INDEFINITE);
        return animation;
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Pac pac;
    private final double size;
    private final Group shape3D = new Group();
    private final Rotate moveRotation = new Rotate();
    private final Node jaw;
    private final PointLight light = new PointLight();
    private final Animation chewingAnimation;
    private RotateTransition headBanging;

    public PacMan3D(Pac pac, double size, Model3D model3D, AssetStorage assets, String ans) {
        this.pac = requireNonNull(pac);
        this.size = size;
        requireNonNull(model3D);
        requireNonNull(assets);
        requireNonNull(ans);

        jaw = PacModel3D.createPacSkull(
            model3D,
            size,
            assets.color(ans + ".pac.color.head"),
            assets.color(ans + ".pac.color.palate"));

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color(ans + ".pac.color.head"),
            assets.color(ans + ".pac.color.eyes"),
            assets.color(ans + ".pac.color.palate")
        );

        shape3D.getChildren().addAll(jaw, body);
        shape3D.setTranslateZ(-0.5 * size);
        shape3D.getTransforms().add(moveRotation);

        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        chewingAnimation = createChewingAnimation(jaw);
        createHeadBangingAnimation();

        light.translateXProperty().bind(shape3D.translateXProperty());
        light.translateYProperty().bind(shape3D.translateYProperty());
        light.setTranslateZ(-30);
    }

    @Override
    public Node shape3D() {
        return shape3D;
    }

    @Override
    public ObjectProperty<DrawMode> drawModeProperty() { return  drawModePy; }

    @Override
    public LightBase light() {
        return light;
    }

    @Override
    public void init() {
        shape3D.setVisible(pac.isVisible());
        shape3D.setScaleX(1.0);
        shape3D.setScaleY(1.0);
        shape3D.setScaleZ(1.0);

        updatePosition();
        stopChewingAndOpenMouth();
        stopHeadBanging();
        setExcited(false);
    }

    @Override
    public void update(GameLevel level) {
        if (pac.isAlive()) {
            updatePosition();
            updateVisibility(level);
            updateLight(level);
        }
        if (pac.isAlive() && !pac.isStandingStill()) {
            updateHeadBanging();
            chew();
        } else {
            stopHeadBanging();
            stopChewingAndOpenMouth();
        }
    }

    @Override
    public void setPowerMode(boolean on) {
        setExcited(on);
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
            doAfterSec(0.5, THE_SOUND::playPacDeathSound),
            new ParallelTransition(spins, new SequentialTransition(shrinks, expands), sinks),
            doAfterSec(1.0, () -> shape3D.setVisible(false))
        );
    }

    private void updatePosition() {
        Vector2f center = pac.position().plus(HTS, HTS);
        shape3D.setTranslateX(center.x());
        shape3D.setTranslateY(center.y());
        shape3D.setTranslateZ(-0.5 * size);
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    private void updateVisibility(GameLevel level) {
        WorldMap worldMap = level.worldMap();
        boolean outsideWorld = shape3D.getTranslateX() < HTS || shape3D.getTranslateX() > TS * worldMap.numCols() - HTS;
        shape3D.setVisible(pac.isVisible() && !outsideWorld);
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    private void updateLight(GameLevel level) {
        TickTimer powerTimer = level.powerTimer();
        if (PY_3D_PAC_LIGHT_ENABLED.get() && powerTimer.isRunning() && pac.isVisible()) {
            light.setLightOn(true);
            double remaining = powerTimer.remainingTicks();
            double maxRange = (remaining / powerTimer.durationTicks()) * 60 + 30;
            light.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remaining, maxRange);
        } else {
            light.setLightOn(false);
        }
    }

    // Chewing animation

    public void stopChewingAndOpenMouth() {
        stopChewing();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }

    public void chew() {
        chewingAnimation.play();
    }

    public void stopChewing() {
        chewingAnimation.stop();
    }

    // Head banging animation

    static final float POWER_AMPLIFICATION = 2;
    static final short ANGLE_FROM = -10;
    static final short ANGLE_TO = 15;
    static final Duration DURATION = Duration.seconds(0.3);

    private void createHeadBangingAnimation() {
        headBanging = new RotateTransition(DURATION, shape3D);
        headBanging.setAxis(Rotate.X_AXIS);
        headBanging.setCycleCount(Animation.INDEFINITE);
        headBanging.setAutoReverse(true);
        headBanging.setInterpolator(Interpolator.EASE_BOTH);
        setExcited(false);
    }

    // Note: Massive headbanging can lead to a stroke!
    private void setExcited(boolean on) {
        headBanging.stop();
        float rate = on ? POWER_AMPLIFICATION : 1;
        headBanging.setFromAngle(ANGLE_FROM * rate);
        headBanging.setToAngle(ANGLE_TO * rate);
        headBanging.setRate(rate);
    }

    private void updateHeadBanging() {
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