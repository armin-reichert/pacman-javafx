/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.animation.Turn;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 * <p>
 * A ghost is displayed in one of the following modes:
 * <ul>
 * <li>{@link Look#NORMAL}: colored ghost with blue eyes,
 * <li>{@link Look#FRIGHTENED}: blue ghost with empty pinkish eyes (ghost looking blind),
 * <li>{@link Look#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link Look#EYES} blue eyes only,
 * <li>{@link Look#NUMBER}: number cube showing eaten ghost's value.
 * </ul>
 *
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

    private static final Duration BRAKE_DURATION = Duration.seconds(0.5);

    public enum Look { NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final Ghost ghost;
    private final int numFlashes;
    private final Pac pac;
    private final Group coloredGhostGroup;
    private final ColoredGhost3D coloredGhost3D;
    private final Box numberQuad;
    private final Rotate orientation = new Rotate();
    private final RotateTransition brakeAnimation;
    private final RotateTransition dressAnimation;
    private final RotateTransition numberRotation;
    private Look currentLook;

    public Ghost3D(Model3D model3D, Theme theme, Ghost ghost, Pac pac, int numFlashes, double size) {
        requireNonNull(model3D);
        requireNonNull(theme);
        requireNonNull(ghost);
        requireNonNull(pac);

        this.ghost = ghost;
        this.numFlashes = numFlashes;
        this.pac = pac;

        coloredGhost3D = new ColoredGhost3D(model3D, theme, ghost.id(), size);
        coloredGhost3D.dressShape().drawModeProperty().bind(drawModePy);
        coloredGhost3D.eyeballsShape().drawModeProperty().bind(drawModePy);
        coloredGhost3D.pupilsShape().drawModeProperty().bind(drawModePy);

        coloredGhostGroup = new Group(coloredGhost3D);
        coloredGhostGroup.getTransforms().add(orientation);

        numberQuad = new Box(14, 8, 8);

        getChildren().add(coloredGhostGroup);

        numberRotation = new RotateTransition(Duration.seconds(1), numberQuad);
        numberRotation.setAxis(Rotate.X_AXIS);
        numberRotation.setFromAngle(0);
        numberRotation.setToAngle(360);
        numberRotation.setInterpolator(Interpolator.LINEAR);
        numberRotation.setRate(0.75);

        brakeAnimation = new RotateTransition(BRAKE_DURATION, coloredGhost3D);
        brakeAnimation.setAxis(Rotate.Y_AXIS);
        brakeAnimation.setFromAngle(0);
        brakeAnimation.setToAngle(-35);
        brakeAnimation.setAutoReverse(true);
        brakeAnimation.setCycleCount(2);

        dressAnimation = new RotateTransition(Duration.seconds(0.3), coloredGhost3D.getDressGroup());
        // TODO I expected this should be the z-axis but... (maybe my expectations are wrong)
        dressAnimation.setAxis(Rotate.Y_AXIS);
        dressAnimation.setFromAngle(-15);
        dressAnimation.setToAngle(15);
        dressAnimation.setCycleCount(Animation.INDEFINITE);
        dressAnimation.setAutoReverse(true);

        setLook(Look.NORMAL);
    }

    public void setNumberImage(Image numberImage) {
        var material = new PhongMaterial();
        material.setDiffuseMap(numberImage);
        numberQuad.setMaterial(material);
    }

    public void init() {
        brakeAnimation.stop();
        dressAnimation.stop();
        numberRotation.stop();
        updateTransform();
        updateLook();
    }

    public void update() {
        updateTransform();
        updateLook();
        updateAnimations();
    }

    private void updateTransform() {
        Vector2f position = ghost.center();
        setTranslateX(position.x());
        setTranslateY(position.y());
        setTranslateZ(-5);
        // TODO: make transition to new wish dir if changed
        orientation.setAngle(Turn.angle(ghost.wishDir()));
        boolean outside = position.x() < HTS || position.x() > ghost.world().numCols() * TS - HTS;
        setVisible(ghost.isVisible() && !outside);
    }

    private void updateAnimations() {
        if (currentLook == Look.NUMBER) {
            dressAnimation.stop();
        } else {
            numberRotation.stop();
            if (ghost.hasEnteredTunnel()) {
                brakeAnimation.playFromStart();
            }
            if (dressAnimation.getStatus() != Status.RUNNING) {
                dressAnimation.play();
            }
        }
    }

    private void updateLook() {
        var newLook = computeLook();
        if (currentLook != newLook) {
            setLook(newLook);
        }
    }

    private Look computeLook() {
        return switch (ghost.state()) {
            case LOCKED, LEAVING_HOUSE -> pac.powerTimer().isRunning()? frightenedOrFlashingLook() : Look.NORMAL;
            case FRIGHTENED -> frightenedOrFlashingLook();
            case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
            case EATEN -> Look.NUMBER;
            default -> Look.NORMAL;
        };
    }

    private void setLook(Look look) {
        currentLook = look;
        if (currentLook == Look.NUMBER) {
            getChildren().setAll(numberQuad);
        } else {
            getChildren().setAll(coloredGhostGroup);
        }
        switch (look) {
            case NORMAL     -> coloredGhost3D.appearNormal();
            case FRIGHTENED -> coloredGhost3D.appearFrightened();
            case EYES       -> coloredGhost3D.appearEyesOnly();
            case FLASHING -> {
                if (numFlashes > 0) {
                    coloredGhost3D.appearFlashing(numFlashes, 1.0);
                } else {
                    coloredGhost3D.appearFrightened();
                }
            }
            case NUMBER -> numberRotation.playFromStart();
        }
    }

    private Look frightenedOrFlashingLook() {
        return pac.isPowerFading() ? Look.FLASHING : Look.FRIGHTENED;
    }
}