/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
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
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;
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
public class Ghost3D {

    private static final Duration BRAKE_DURATION = Duration.seconds(0.4);

    public enum Look { NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final GameLevel level;
    private final Ghost ghost;
    private final Group root;
    private final Group coloredGhostGroup;
    private final ColoredGhost3D coloredGhost3D;
    private final Box numberQuad;
    private final Rotate orientation = new Rotate();
    private final RotateTransition brakeAnimation;
    private final RotateTransition dressAnimation;
    private final RotateTransition numberRotation;
    private Image numberImage;
    private Look currentLook;

    public Ghost3D(GameLevel level, Ghost ghost, Model3D model3D, Theme theme, double size) {
        checkLevelNotNull(level);
        requireNonNull(ghost);
        requireNonNull(model3D);
        requirePositive(size, "Ghost3D size must be positive but is %f");

        this.level = level;
        this.ghost = ghost;

        coloredGhost3D = new ColoredGhost3D(model3D, theme, ghost.id(), size);
        coloredGhost3D.dressShape().drawModeProperty().bind(drawModePy);
        coloredGhost3D.eyeballsShape().drawModeProperty().bind(drawModePy);
        coloredGhost3D.pupilsShape().drawModeProperty().bind(drawModePy);

        coloredGhostGroup = new Group(coloredGhost3D.getRoot());
        coloredGhostGroup.getTransforms().add(orientation);

        numberQuad = new Box(14, 8, 8);

        root = new Group(coloredGhostGroup);

        numberRotation = new RotateTransition(Duration.seconds(1), numberQuad);
        numberRotation.setAxis(Rotate.X_AXIS);
        numberRotation.setFromAngle(0);
        numberRotation.setToAngle(360);
        numberRotation.setInterpolator(Interpolator.LINEAR);
        numberRotation.setRate(0.75);

        brakeAnimation = new RotateTransition(BRAKE_DURATION, coloredGhost3D.getRoot());
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

    public Node root() {
        return root;
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
        root.setTranslateX(position.x());
        root.setTranslateY(position.y());
        root.setTranslateZ(-5);
        // TODO: make transition to new wish dir if changed
        orientation.setAngle(Turn.angle(ghost.wishDir()));
        boolean outsideWorld = position.x() < 0 || position.x() > level.world().numCols() * TS;
        root.setVisible(ghost.isVisible() && !outsideWorld);
    }

    private void updateAnimations() {
        if (currentLook == Look.NUMBER) {
            dressAnimation.stop();
        } else {
            numberRotation.stop();
            if (ghost.enteredTunnel()) {
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
            case LOCKED, LEAVING_HOUSE -> ghost.killable(level.pac()) ? frightenedOrFlashingLook() : Look.NORMAL;
            case FRIGHTENED -> frightenedOrFlashingLook();
            case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
            case EATEN -> Look.NUMBER;
            default -> Look.NORMAL;
        };
    }

    private void setLook(Look newLook) {
        currentLook = newLook;
        if (currentLook == Look.NUMBER) {
            root.getChildren().setAll(numberQuad);
        } else {
            root.getChildren().setAll(coloredGhostGroup);
        }
        switch (newLook) {
            case NORMAL     -> coloredGhost3D.appearNormal();
            case FRIGHTENED -> coloredGhost3D.appearFrightened();
            case EYES       -> coloredGhost3D.appearEyesOnly();
            case FLASHING -> {
                if (level.data().numFlashes() > 0) {
                    coloredGhost3D.appearFlashing(level.data().numFlashes(), 1.0);
                } else {
                    coloredGhost3D.appearFrightened();
                }
            }
            case NUMBER -> {
                var material = new PhongMaterial();
                material.setBumpMap(numberImage);
                material.setDiffuseMap(numberImage);
                numberQuad.setMaterial(material);
                if (numberRotation.getStatus() != Status.RUNNING) {
                    numberRotation.playFromStart();
                }
            }
        }
    }

    private Look frightenedOrFlashingLook() {
        return level.pac().isPowerFading() ? Look.FLASHING : Look.FRIGHTENED;
    }

    public void setNumberImage(Image numberImage) {
        this.numberImage = numberImage;
    }
}