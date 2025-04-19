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
import static de.amr.games.pacman.uilib.model3D.Model3D.meshViewById;
import static java.util.Objects.requireNonNull;

public abstract class Pac3D {

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

    protected final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    protected final Pac pac;
    protected final PointLight light = new PointLight();
    protected final Group root = new Group();
    protected final double size;
    protected final Rotate moveRotation = new Rotate();
    protected RotateTransition movementAnimation;
    protected Animation chewingAnimation;
    protected Node jaw;

    public abstract Animation createDyingAnimation();
    public abstract void setMovementPowerMode(boolean power);

    protected abstract void createMovementAnimation();
    protected abstract void startMovementAnimation();
    protected abstract void stopMovementAnimation();
    protected abstract void updateMovementAnimation();

    protected Pac3D(Pac pac, double size, Model3D model3D, AssetStorage assets, String ans) {
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

        Node body = PacModel3D.createPacShape(
                model3D, size,
                assets.color(ans + ".pac.color.head"),
                assets.color(ans + ".pac.color.eyes"),
                assets.color(ans + ".pac.color.palate"));

        root.getChildren().addAll(jaw, body);
        root.setTranslateZ(-0.5 * size);
        root.getTransforms().add(moveRotation);

        meshViewById(jaw, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(jaw, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(drawModePy);

        createMovementAnimation();
        chewingAnimation = createChewingAnimation(jaw);

        light.translateXProperty().bind(root.translateXProperty());
        light.translateYProperty().bind(root.translateYProperty());
        light.setTranslateZ(-30);
    }

    public Node shape3D() {
        return root;
    }

    public ObjectProperty<DrawMode> drawModeProperty() { return  drawModePy; }

    public LightBase light() {
        return light;
    }

    public void init() {
        root.setVisible(pac.isVisible());
        root.setScaleX(1.0);
        root.setScaleY(1.0);
        root.setScaleZ(1.0);

        updatePosition();
        stopChewingAndOpenMouth();
        stopMovementAnimation();
        setMovementPowerMode(false);
    }

    public void update(GameLevel level) {
        if (pac.isAlive()) {
            updatePosition();
            updateVisibility(level);
            updateMovementAnimation();
            updateLight(level);
        }
        if (pac.isAlive() && !pac.isStandingStill()) {
            startMovementAnimation();
            chewingAnimation.play();
        } else {
            stopMovementAnimation();
            stopChewingAndOpenMouth();
        }
    }

    protected void updatePosition() {
        Vector2f center = pac.position().plus(HTS, HTS);
        root.setTranslateX(center.x());
        root.setTranslateY(center.y());
        root.setTranslateZ(-0.5 * size);
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    protected void updateVisibility(GameLevel level) {
        WorldMap worldMap = level.worldMap();
        boolean outsideWorld = root.getTranslateX() < HTS || root.getTranslateX() > TS * worldMap.numCols() - HTS;
        root.setVisible(pac.isVisible() && !outsideWorld);
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    protected void updateLight(GameLevel level) {
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

    protected void stopChewingAndOpenMouth() {
        chewingAnimation.stop();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }
}