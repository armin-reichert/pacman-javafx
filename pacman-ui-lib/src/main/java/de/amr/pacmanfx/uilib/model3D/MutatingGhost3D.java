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
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

/**
 * Appearances of a 3D ghost. One of:
 * <ul>
 * <li>{@link GhostAppearance#NORMAL}: colored ghost with blue eyes,
 * <li>{@link GhostAppearance#FRIGHTENED}: blue ghost with empty, "pinkish" eyes (looking blind),
 * <li>{@link GhostAppearance#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link GhostAppearance#EATEN} eyes only,
 * <li>{@link GhostAppearance#VALUE}: showing eaten ghost's value.
 * </ul>
 */
public class MutatingGhost3D extends Group implements Disposable {

    private static final double GHOST_OVER_FLOOR_DIST = 2.0;

    private static boolean isPositionOutsideWorld(GameLevel gameLevel, Vector2f center) {
        return center.x() < HTS || center.x() > gameLevel.worldMap().numCols() * TS - HTS;
    }

    private final ObjectProperty<GhostAppearance> appearanceProperty = new SimpleObjectProperty<>();

    private final Map<Image, PhongMaterial> numberMaterialCache = new WeakHashMap<>();
    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private final double size;
    private final int numFlashes;

    private Ghost3D ghost3D;
    private Box numberBox;

    private RegisteredAnimation brakeAnimation;
    private RegisteredAnimation pointsAnimation;

    public MutatingGhost3D(
        AnimationRegistry animationRegistry,
        GameLevel gameLevel,
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

        pointsAnimation = new RegisteredAnimation(animationRegistry, "Ghost_Points_%s".formatted(ghost.name())) {
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
        };

        brakeAnimation = new RegisteredAnimation(animationRegistry, "Ghost_Braking_%s".formatted(ghost.name())) {
            @Override
            protected Animation createAnimationFX() {
                var rotateTransition = new RotateTransition(Duration.seconds(0.5), MutatingGhost3D.this);
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
        };

        ghost.positionProperty().addListener(this::handleGhostPositionChange);
        ghost.wishDirProperty().addListener(this::handleGhostWishDirChange);

        visibleProperty().bind(Bindings.createBooleanBinding(
                () -> ghost.isVisible() && !isPositionOutsideWorld(gameLevel, ghost.center()),
                ghost.visibleProperty(), ghost.positionProperty()
        ));

        appearanceProperty.addListener(this::handleAppearanceChange);

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

    @Override
    public void dispose() {
        if (ghost != null) {
            ghost.positionProperty().removeListener(this::handleGhostPositionChange);
            ghost.wishDirProperty().removeListener(this::handleGhostWishDirChange);
        }
        visibleProperty().unbind();
        appearanceProperty.removeListener(this::handleAppearanceChange);
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

    public GhostAppearance appearance() { return appearanceProperty.get(); }

    public void setAppearance(GhostAppearance newAppearance) {
        requireNonNull(newAppearance);
        if (newAppearance != appearance()) {
            appearanceProperty.set(newAppearance);
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
        GhostAppearance appearance = GhostAppearanceSelector.selectAppearance(
            ghost.state(),
            powerActive,
            powerFading,
            killedDuringCurrentPhase);
        setAppearance(appearance);
    }
}