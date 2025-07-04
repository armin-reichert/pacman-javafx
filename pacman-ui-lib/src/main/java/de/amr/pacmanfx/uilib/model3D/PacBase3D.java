/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class for Pac-Man and Ms. Pac-Man 3D representations.
 */
public class PacBase3D {

    protected double size;
    protected Pac pac;
    protected PointLight light = new PointLight();
    protected Group root = new Group();
    protected PacBody body;
    protected Group jaw;
    protected Rotate moveRotation = new Rotate();

    protected AnimationManager animationManager;
    protected ManagedAnimation chewingAnimation;
    protected ManagedAnimation movementAnimation;
    protected ManagedAnimation dyingAnimation;

    protected PacBase3D(
        Model3DRepository model3DRepository,
        AnimationManager animationManager,
        Pac pac,
        double size,
        AssetStorage assets,
        String ans)
    {
        requireNonNull(model3DRepository);
        this.animationManager = requireNonNull(animationManager);
        this.pac = requireNonNull(pac);
        this.size = size;
        requireNonNull(assets);
        requireNonNull(ans);

        body = model3DRepository.createPacBody(
            size,
            assets.color(ans + ".pac.color.head"),
            assets.color(ans + ".pac.color.eyes"),
            assets.color(ans + ".pac.color.palate"));

        jaw = model3DRepository.createBlindPacBody(
            size,
            assets.color(ans + ".pac.color.head"),
            assets.color(ans + ".pac.color.palate"));

        root.getChildren().addAll(jaw, body);
        root.getTransforms().add(moveRotation);
        root.setTranslateZ(-0.5 * size);

        chewingAnimation = new ManagedAnimation(animationManager, "PacMan_Chewing") {
            @Override
            protected Animation createAnimation() {
                var closed = new KeyValue[] {
                        new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
                        new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
                };
                var open = new KeyValue[] {
                        new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
                        new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
                };
                var timeline = new Timeline(
                        new KeyFrame(Duration.ZERO,        "Open on Start", open),
                        new KeyFrame(Duration.millis(100), "Start Closing", open),
                        new KeyFrame(Duration.millis(130), "Closed",        closed),
                        new KeyFrame(Duration.millis(200), "Start Opening", closed),
                        new KeyFrame(Duration.millis(280), "Open",          open)
                );
                timeline.setCycleCount(Animation.INDEFINITE);
                return timeline;
            }

            @Override
            public void stop() {
                Animation animation = getOrCreateAnimation();
                animation.stop();
                if (jaw != null) {
                    // open mouth when stopped
                    jaw.setRotationAxis(Rotate.Y_AXIS);
                    jaw.setRotate(0);
                }
            }
        };

        light.translateXProperty().bind(root.translateXProperty());
        light.translateYProperty().bind(root.translateYProperty());
        light.setTranslateZ(-30);
    }

    public Node root() {
        return root;
    }

    public LightBase light() {
        return light;
    }

    public ManagedAnimation dyingAnimation() {
        return dyingAnimation;
    }

    public void setMovementPowerMode(boolean power) {}

    public void updateMovementAnimation() {}

    public void destroy() {
        if (body != null) {
            body.destroy();
            body = null;
        }
        if (jaw != null) {
            jaw.getChildren().clear();
            jaw = null;
        }
        if (root != null) {
            root.getChildren().clear();
        }
        if (chewingAnimation != null) {
            chewingAnimation.stop();
            chewingAnimation = null;
        }
        if (movementAnimation != null) {
            movementAnimation.stop();
            movementAnimation = null;
        }
        if (dyingAnimation != null) {
            dyingAnimation.stop();
            dyingAnimation = null;
        }
    }

    public void init() {
        requireNonNull(movementAnimation);
        requireNonNull(chewingAnimation);

        root.setVisible(pac.isVisible());
        root.setScaleX(1.0);
        root.setScaleY(1.0);
        root.setScaleZ(1.0);

        updatePosition();
        chewingAnimation.stop();
        movementAnimation.stop();
        setMovementPowerMode(false);
    }

    public void update(GameLevel level) {
        requireNonNull(movementAnimation);
        requireNonNull(chewingAnimation);

        if (pac.isAlive()) {
            updatePosition();
            updateVisibility(level);
            updateMovementAnimation();
            updateLight();
        }
        if (pac.isAlive() && !pac.isStandingStill()) {
            movementAnimation.playOrContinue();
            chewingAnimation.playOrContinue();
        } else {
            movementAnimation.stop();
            chewingAnimation.stop();
        }
    }

    protected void updatePosition() {
        Vector2f center = pac.center();
        root.setTranslateX(center.x());
        root.setTranslateY(center.y());
        root.setTranslateZ(-0.5 * size);
        moveRotation.setAxis(Rotate.Z_AXIS);
        double angle = switch (pac.moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
        moveRotation.setAngle(angle);
    }

    protected void updateVisibility(GameLevel level) {
        WorldMap worldMap = level.worldMap();
        boolean outsideWorld = root.getTranslateX() < HTS || root.getTranslateX() > TS * worldMap.numCols() - HTS;
        root.setVisible(pac.isVisible() && !outsideWorld);
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    protected void updateLight() {
        TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible()) {
            light.setLightOn(true);
            double remaining = powerTimer.remainingTicks();
            double maxRange = (remaining / powerTimer.durationTicks()) * 60 + 30;
            light.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remaining, maxRange);
        } else {
            light.setLightOn(false);
        }
    }
}