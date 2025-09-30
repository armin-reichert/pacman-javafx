/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.Vector2f;
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
 * <li>{@link GhostAppearance#EATEN} eyes only,
 * <li>{@link GhostAppearance#VALUE}: eaten ghost's point value.
 * </ul>
 */
public class MutableGhost3D extends Group implements Disposable {

    private static final double GHOST_OVER_FLOOR_DIST = 2.0;

    public static GhostAppearance selectAppearance(
        GhostState ghostState,
        boolean powerActive,
        boolean powerFading,
        boolean killedDuringCurrentPhase)
    {
        if (ghostState == null) {
            return GhostAppearance.NORMAL; //TODO can this happen?
        }
        return switch (ghostState) {
            case LEAVING_HOUSE, LOCKED -> powerActive && !killedDuringCurrentPhase
                    ? frightenedOrFlashing(powerFading)
                    : GhostAppearance.NORMAL;
            case FRIGHTENED -> frightenedOrFlashing(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EATEN;
            case EATEN -> GhostAppearance.VALUE;
            default -> GhostAppearance.NORMAL;
        };
    }

    private final ObjectProperty<GhostAppearance> appearance = new SimpleObjectProperty<>();

    private final Map<Image, PhongMaterial> numberMaterialCache = new WeakHashMap<>();
    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private final double size;
    private final int numFlashes;

    private Ghost3D ghost3D;
    private Box numberBox;

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
            var numberBoxRotation = new RotateTransition(Duration.seconds(1), numberBox);
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

        ghost3D = new Ghost3D(animationRegistry, ghost, colorSet, dressShape, pupilsShape, eyeballsShape, size);
        numberBox = new Box(14, 8, 8);
        getChildren().setAll(ghost3D, numberBox);

        pointsAnimation = new PointsAnimation(animationRegistry);
        brakeAnimation = new BrakeAnimation(animationRegistry);

        ghost.positionProperty().addListener(this::handleGhostPositionChange);
        ghost.wishDirProperty().addListener(this::handleGhostWishDirChange);

        appearance.addListener(this::handleAppearanceChange);

        update3DTransform();
        setAppearance(GhostAppearance.NORMAL);
    }

    public Ghost3D ghost3D() {
        return ghost3D;
    }

    public GhostColorSet colorSet() {
        return colorSet;
    }

    private void handleAppearanceChange(
            ObservableValue<? extends GhostAppearance> property,
            GhostAppearance oldAppearance,
            GhostAppearance newAppearance)
    {
        Logger.debug("Ghost {} now has appearance {}", ghost.name(), newAppearance);
        if (newAppearance == GhostAppearance.VALUE) {
            numberBox.setVisible(true);
            ghost3D.setVisible(false);
            ghost3D.dressAnimation().stop();
            pointsAnimation.playFromStart();
        } else {
            numberBox.setVisible(false);
            ghost3D.setVisible(true);
            switch (newAppearance) {
                case NORMAL     -> ghost3D.setNormalLook();
                case FRIGHTENED -> ghost3D.setFrightenedLook();
                case EATEN      -> ghost3D.setEyesOnlyLook();
                case FLASHING   -> ghost3D.setFlashingLook(numFlashes);
            }
            pointsAnimation.stop();
            if (ghost.moveInfo().tunnelEntered) {
                brakeAnimation.playFromStart();
            }
        }
    }

    public void stopAllAnimations() {
        brakeAnimation.stop();
        pointsAnimation.stop();
        ghost3D.dressAnimation().stop();
        ghost3D.flashingAnimation().stop();
    }

    public void init(GameLevel gameLevel) {
        stopAllAnimations();
        update3DTransform();
        selectAppearance(gameLevel);
    }

    /**
     * Called on each clock tick (frame).
     *
     * @param gameLevel the game level
     */
    public void update(GameLevel gameLevel) {
        selectAppearance(gameLevel);
        if (ghost.isVisible()) {
            ghost3D.dressAnimation().playOrContinue();
        } else {
            ghost3D.dressAnimation().stop();
        }
    }

    public GhostAppearance appearance() { return appearance.get(); }

    public void setAppearance(GhostAppearance newAppearance) {
        requireNonNull(newAppearance);
        if (newAppearance != appearance()) {
            appearance.set(newAppearance);
        }
    }

    public void setNumberImage(Image numberImage) {
        if (!numberMaterialCache.containsKey(numberImage)) {
            var numberMaterial = new PhongMaterial();
            numberMaterial.setDiffuseMap(numberImage);
            numberMaterialCache.put(numberImage, numberMaterial);
        }
        numberBox.setMaterial(numberMaterialCache.get(numberImage));
    }

    private void handleGhostPositionChange(
        ObservableValue<? extends Vector2f> property,
        Vector2f oldPosition,
        Vector2f newPosition)
    {
        update3DTransform();
    }

    private void handleGhostWishDirChange(
        ObservableValue<? extends Direction> property,
        Direction oldDir,
        Direction newDir)
    {
        update3DTransform();
    }

    private void update3DTransform() {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - GHOST_OVER_FLOOR_DIST);
        if (ghost3D != null) {
            ghost3D.turnTowards(ghost.wishDir());
        }
    }

    private void selectAppearance(GameLevel gameLevel) {
        boolean powerFading = gameLevel.pac().isPowerFading(gameLevel);
        boolean powerActive = gameLevel.pac().powerTimer().isRunning();
        // ghost that got killed already during the current power phase do not look frightened anymore
        boolean killedDuringCurrentPhase = gameLevel.victims().contains(ghost);
        GhostAppearance appearance = selectAppearance(
            ghost.state(),
            powerActive,
            powerFading,
            killedDuringCurrentPhase);
        setAppearance(appearance);
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
        if (ghost3D != null) {
            ghost3D.dispose();
            ghost3D = null;
        }
        numberBox = null;
    }
}