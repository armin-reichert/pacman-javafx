/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Map;
import java.util.WeakHashMap;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

/**
 * Represents the different 3D appearances of a ghost. One of:
 * <ul>
 * <li>{@link GhostAppearance#NORMAL}: colored ghost, blue eyes,
 * <li>{@link GhostAppearance#FRIGHTENED}: blue ghost, empty, "pinkish" eyes (looking blind),
 * <li>{@link GhostAppearance#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link GhostAppearance#EYES} eyes only,
 * <li>{@link GhostAppearance#NUMBER}: eaten ghost's point value.
 * </ul>
 */
public class MutableGhost3D extends Group implements Disposable {

    private static final int NUMBER_BOX_SIZE_X = 14;
    private static final int NUMBER_BOX_SIZE_Y = 8;
    private static final int NUMBER_BOX_SIZE_Z = 8;

    private static final double GHOST_OVER_FLOOR_DIST = 2.0;

    public static GhostAppearance selectAppearance(
        GhostState ghostState,
        boolean powerActive,
        boolean powerFading,
        boolean killedInCurrentPhase)
    {
        return switch (ghostState) {
            case null -> GhostAppearance.NORMAL; //TODO can this happen?
            case LEAVING_HOUSE, LOCKED -> powerActive && !killedInCurrentPhase ?
                    frightenedOrFlashing(powerFading) : GhostAppearance.NORMAL;
            case FRIGHTENED -> frightenedOrFlashing(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case EATEN -> GhostAppearance.NUMBER;
            default -> GhostAppearance.NORMAL;
        };
    }

    private final ObjectProperty<GhostAppearance> appearance = new SimpleObjectProperty<>();

    private final Map<Image, PhongMaterial> numberMaterialCache = new WeakHashMap<>();
    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private final double size;
    private final int numFlashes;

    private Ghost3D ghostShape3D;
    private Box numberShape3D;

    private class BrakeAnimation extends RegisteredAnimation {

        public BrakeAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_Braking_%s".formatted(ghost.name()));
        }

        @Override
        protected Animation createAnimationFX() {
            var rotateTransition = new RotateTransition(Duration.seconds(0.5), MutableGhost3D.this);
            rotateTransition.setAxis(Rotate.Y_AXIS);
            rotateTransition.setAutoReverse(true);
            rotateTransition.setCycleCount(2);
            rotateTransition.setInterpolator(Interpolator.EASE_OUT);
            return rotateTransition;
        }

        @Override
        public void playFromStart() {
            var rotateTransition = (RotateTransition) getOrCreateAnimationFX();
            rotateTransition.stop();
            rotateTransition.setByAngle(ghost.moveDir() == Direction.LEFT ? -35 : 35);
            rotateTransition.playFromStart();
        }

        @Override
        public void playOrContinue() {
            var rotateTransition = (RotateTransition) getOrCreateAnimationFX();
            rotateTransition.stop();
            rotateTransition.setByAngle(ghost.moveDir() == Direction.LEFT ? -35 : 35);
            rotateTransition.play();
        }

        @Override
        public void stop() {
            super.stop();
            setRotationAxis(Rotate.Y_AXIS);
            setRotate(0);
        }
    }

    private class PointsAnimation extends RegisteredAnimation {

        public PointsAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_Points_%s".formatted(ghost.name()));
        }

        @Override
        protected Animation createAnimationFX() {
            var numberBoxRotation = new RotateTransition(Duration.seconds(1), numberShape3D);
            numberBoxRotation.setAxis(Rotate.X_AXIS);
            numberBoxRotation.setFromAngle(0);
            numberBoxRotation.setToAngle(360);
            numberBoxRotation.setInterpolator(Interpolator.LINEAR);
            numberBoxRotation.setRate(0.75);
            return numberBoxRotation;
        }
    }

    private BrakeAnimation brakeAnimation;
    private PointsAnimation pointsAnimation;

    public MutableGhost3D(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostColorSet colorSet,
        MeshView dressShape,
        MeshView pupilsShape,
        MeshView eyeballsShape,
        double size,
        int numFlashes)
    {
        requireNonNull(animationRegistry);
        this.ghost = requireNonNull(ghost);
        this.colorSet = requireNonNull(colorSet);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        this.size = requireNonNegative(size);
        this.numFlashes = requireNonNegativeInt(numFlashes);

        ghostShape3D = new Ghost3D(animationRegistry, ghost, colorSet, dressShape, pupilsShape, eyeballsShape, size);
        numberShape3D = new Box(NUMBER_BOX_SIZE_X, NUMBER_BOX_SIZE_Y, NUMBER_BOX_SIZE_Z);
        getChildren().setAll(ghostShape3D, numberShape3D);

        pointsAnimation = new PointsAnimation(animationRegistry);
        brakeAnimation = new BrakeAnimation(animationRegistry);

        appearance.addListener(this::handleAppearanceChange);
        ghost.positionProperty().addListener(this::handleGhostPositionChange);
        ghost.wishDirProperty().addListener(this::handleGhostWishDirChange);

        update3DTransform();
        appearance.set(GhostAppearance.NORMAL);
    }

    public Ghost3D ghost3D() {
        return ghostShape3D;
    }

    public GhostColorSet colorSet() {
        return colorSet;
    }

    public void stopAllAnimations() {
        brakeAnimation.stop();
        pointsAnimation.stop();
        ghostShape3D.dressAnimation().stop();
        ghostShape3D.flashingAnimation().stop();
    }

    public void init(GameLevel gameLevel) {
        stopAllAnimations();
        update3DTransform();
        updateAppearance(gameLevel);
    }

    /**
     * Called on each clock tick (frame).
     *
     * @param gameLevel the game level
     */
    public void update(GameLevel gameLevel) {
        updateAppearance(gameLevel);
        if (ghost.isVisible()) {
            ghostShape3D.dressAnimation().playOrContinue();
        } else {
            ghostShape3D.dressAnimation().stop();
        }
    }

    public void setNumberImage(Image numberImage) {
        if (!numberMaterialCache.containsKey(numberImage)) {
            var numberMaterial = new PhongMaterial();
            numberMaterial.setDiffuseMap(numberImage);
            numberMaterialCache.put(numberImage, numberMaterial);
        }
        numberShape3D.setMaterial(numberMaterialCache.get(numberImage));
    }

    private void handleAppearanceChange(
        ObservableValue<? extends GhostAppearance> property,
        GhostAppearance oldAppearance,
        GhostAppearance newAppearance)
    {
        if (newAppearance == GhostAppearance.NUMBER) {
            numberShape3D.setVisible(true);
            ghostShape3D.setVisible(false);
            ghostShape3D.dressAnimation().stop();
            pointsAnimation.playFromStart();
        } else {
            numberShape3D.setVisible(false);
            ghostShape3D.setVisible(true);
            switch (newAppearance) {
                case NORMAL     -> ghostShape3D.setNormalLook();
                case FRIGHTENED -> ghostShape3D.setFrightenedLook();
                case EYES -> ghostShape3D.setEyesOnlyLook();
                case FLASHING   -> ghostShape3D.setFlashingLook(numFlashes);
            }
            pointsAnimation.stop();
            if (ghost.moveInfo().tunnelEntered) {
                brakeAnimation.playFromStart();
            }
        }
        Logger.info("{} 3D appearance set to {}", ghost.name(), newAppearance);
    }

    // Separate method such that listener can be removed
    private void handleGhostPositionChange(ObservableValue<? extends Vector2f> obs, Vector2f oldPosition, Vector2f newPosition) {
        update3DTransform();
    }

    // Separate method such that listener can be removed
    private void handleGhostWishDirChange(ObservableValue<? extends Direction> obs, Direction oldDir, Direction newDir) {
        update3DTransform();
    }

    private void update3DTransform() {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - GHOST_OVER_FLOOR_DIST);
        if (ghostShape3D != null) {
            ghostShape3D.turnTowards(ghost.wishDir());
        }
    }

    private void updateAppearance(GameLevel gameLevel) {
        boolean powerActive = gameLevel.pac().powerTimer().isRunning();
        boolean powerFading = gameLevel.pac().isPowerFading(gameLevel);
        // ghost that got already killed in the current power phase do not look frightened anymore
        boolean killedInCurrentPhase = gameLevel.energizerVictims().contains(ghost);
        GhostAppearance newAppearance = selectAppearance(
            ghost.state(),
            powerActive,
            powerFading,
            killedInCurrentPhase);
        appearance.set(newAppearance);
    }

    private static GhostAppearance frightenedOrFlashing(boolean powerFading) {
        return powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    @Override
    public void dispose() {
        if (ghost != null) {
            ghost.positionProperty().removeListener(this::handleGhostPositionChange);
            ghost.wishDirProperty().removeListener(this::handleGhostWishDirChange);
        }
        visibleProperty().unbind();
        appearance.removeListener(this::handleAppearanceChange);
        numberMaterialCache.clear();

        stopAllAnimations();
        if (brakeAnimation != null) {
            brakeAnimation.dispose();
            brakeAnimation = null;
        }
        if (pointsAnimation != null) {
            pointsAnimation.dispose();
            pointsAnimation = null;
        }
        getChildren().clear();
        if (ghostShape3D != null) {
            ghostShape3D.dispose();
            ghostShape3D = null;
        }
        numberShape3D = null;
    }
}