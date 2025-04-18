/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.assets.AssetStorage;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui.Globals.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Ufx.now;
import static de.amr.games.pacman.uilib.Ufx.pauseSec;
import static de.amr.games.pacman.uilib.model3D.Model3D.meshViewById;
import static java.util.Objects.requireNonNull;

public class MsPacMan3D implements Pac3D {
    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Pac pac;
    private final PacShape3D shape3D;
    private final Node jaw;
    private final PointLight light = new PointLight();
    private final Animation chewingAnimation;
    private RotateTransition hipSwayingAnimation;

    public MsPacMan3D(Pac pac, double size, Model3D model3D, AssetStorage assets, String ans) {
        this.pac = requireNonNull(pac);
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
            assets.color(ans + ".pac.color.palate"));

        Group femaleParts = PacModel3D.createFemaleParts(size,
            assets.color(ans + ".pac.color.hairbow"),
            assets.color(ans + ".pac.color.hairbow.pearls"),
            assets.color(ans + ".pac.color.boobs"));

        shape3D = new PacShape3D(jaw, size);
        shape3D.getChildren().addAll(body, femaleParts);

        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        chewingAnimation = PacMan3D.createChewingAnimation(jaw);
        createHipSwayingAnimation(shape3D);

        light.translateXProperty().bind(shape3D.translateXProperty());
        light.translateYProperty().bind(shape3D.translateYProperty());
        light.setTranslateZ(-30);
    }

    @Override
    public PacShape3D shape3D() {
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
        shape3D.updatePosition(pac);
        shape3D.setVisible(pac.isVisible());
        shape3D.setScaleX(1.0);
        shape3D.setScaleY(1.0);
        shape3D.setScaleZ(1.0);

        stopChewingAndOpenMouth();
        stopSwayingHips();
        setWinnetouchMode(false);
    }

    @Override
    public void update(GameLevel level) {
        if (pac.isAlive()) {
            shape3D.updatePosition(pac);
            shape3D.updateVisibility(pac, level);
            updateLight(pac, level);
        }
        if (pac.isAlive() && !pac.isStandingStill()) {
            swayHips();
            chew();
        } else {
            stopSwayingHips();
            stopChewingAndOpenMouth();
        }
    }

    @Override
    public void setPowerMode(boolean on) {
        setWinnetouchMode(on);
    }

    @Override
    public Animation createDyingAnimation() {
        //TODO use Timeline?
        var spinning = new RotateTransition(Duration.seconds(0.25), shape3D);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setFromAngle(0);
        spinning.setToAngle(360);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.setCycleCount(4);
        return new SequentialTransition(pauseSec(1), now(THE_SOUND::playPacDeathSound), spinning, pauseSec(1.5));
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    private void updateLight(Pac pac, GameLevel level) {
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

    // Hip swaying animation

    static final short ANGLE_FROM = -20;
    static final short ANGLE_TO = 20;
    static final Duration DURATION = Duration.seconds(0.4);

    private void createHipSwayingAnimation(Node target) {
        hipSwayingAnimation = new RotateTransition(DURATION, target);
        hipSwayingAnimation.setAxis(Rotate.Z_AXIS);
        hipSwayingAnimation.setCycleCount(Animation.INDEFINITE);
        hipSwayingAnimation.setAutoReverse(true);
        hipSwayingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        setWinnetouchMode(false);
    }

    // Note: Winnetouch is the gay twin-brother of Abahachi
    private void setWinnetouchMode(boolean on) {
        double amplification = on ? 1.5 : 1;
        double rate = on ? 2 : 1;
        hipSwayingAnimation.stop();
        hipSwayingAnimation.setFromAngle(ANGLE_FROM * amplification);
        hipSwayingAnimation.setToAngle(ANGLE_TO * amplification);
        hipSwayingAnimation.setRate(rate);
    }

    private void swayHips() {
        hipSwayingAnimation.play();
    }

    private void stopSwayingHips() {
        hipSwayingAnimation.stop();
        hipSwayingAnimation.getNode().setRotationAxis(hipSwayingAnimation.getAxis());
        hipSwayingAnimation.getNode().setRotate(0);
    }
}