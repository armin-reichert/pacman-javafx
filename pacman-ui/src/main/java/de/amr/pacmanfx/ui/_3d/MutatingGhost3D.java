/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.model3D.Ghost3D;
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
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Map;
import java.util.WeakHashMap;

import static de.amr.pacmanfx.Globals.*;
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
public class MutatingGhost3D extends Group {

    private static final double GHOST_OVER_FLOOR_DIST = 2.0;

    private static boolean isPositionOutsideWorld(GameLevel gameLevel, Vector2f center) {
        return center.x() < HTS || center.x() > gameLevel.worldMap().numCols() * TS - HTS;
    }

    private final ObjectProperty<GhostAppearance> appearanceProperty = new SimpleObjectProperty<>();

    private final Map<Image, PhongMaterial> numberMaterialCache = new WeakHashMap<>();
    private final Ghost ghost;
    private final double size;
    private final int numFlashes;

    private Ghost3D ghost3D;
    private Box numberBox;

    private final AnimationManager animationManager;
    private ManagedAnimation brakeAnimation;
    private ManagedAnimation pointsAnimation;

    public MutatingGhost3D(
        AnimationManager animationManager,
        AssetStorage assets, String assetPrefix,
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        Ghost ghost, double size, int numFlashes)
    {
        this.animationManager = requireNonNull(animationManager);
        requireNonNull(assets);
        requireNonNull(assetPrefix);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        this.numFlashes = requireNonNegativeInt(numFlashes);

        this.ghost = requireNonNull(ghost);
        this.size = requireNonNegative(size);

        ghost3D = new Ghost3D(animationManager,
            assets, assetPrefix,
            ghost,
            dressShape, pupilsShape, eyeballsShape, size);

        numberBox = new Box(14, 8, 8);

        getChildren().setAll(ghost3D, numberBox);

        pointsAnimation = new ManagedAnimation(animationManager, "Ghost_Points_%s".formatted(ghost.name())) {
            @Override
            protected Animation createAnimation() {
                var numberBoxRotation = new RotateTransition(Duration.seconds(1), numberBox);
                numberBoxRotation.setAxis(Rotate.X_AXIS);
                numberBoxRotation.setFromAngle(0);
                numberBoxRotation.setToAngle(360);
                numberBoxRotation.setInterpolator(Interpolator.LINEAR);
                numberBoxRotation.setRate(0.75);
                return numberBoxRotation;
            }
        };

        brakeAnimation = new ManagedAnimation(animationManager, "Ghost_Braking_%s".formatted(ghost.name())) {
            @Override
            protected Animation createAnimation() {
                var rotateTransition = new RotateTransition(Duration.seconds(0.5), MutatingGhost3D.this);
                rotateTransition.setAxis(Rotate.Y_AXIS);
                rotateTransition.setAutoReverse(true);
                rotateTransition.setCycleCount(2);
                rotateTransition.setInterpolator(Interpolator.EASE_OUT);
                return rotateTransition;
            }

            @Override
            public void play(boolean playMode) {
                var rotateTransition = (RotateTransition) getOrCreateAnimation();
                rotateTransition.stop();
                rotateTransition.setByAngle(ghost.moveDir() == Direction.LEFT ? -35 : 35);
                if (playMode == FROM_START) {
                    rotateTransition.playFromStart();
                } else {
                    rotateTransition.play();
                }
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
                () -> ghost.isVisible() && !isPositionOutsideWorld(theGameLevel(), ghost.center()),
                ghost.visibleProperty(), ghost.positionProperty()
        ));

        appearanceProperty.addListener(this::handleAppearanceChange);

        update3DTransform();
        setAppearance(GhostAppearance.NORMAL);
    }

    private void handleAppearanceChange(
            ObservableValue<? extends GhostAppearance> property,
            GhostAppearance oldAppearance,
            GhostAppearance newAppearance)
    {
        Logger.info("Ghost {} now has appearance {}", ghost.name(), newAppearance);
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

    public void destroy() {
        stopAllAnimations();
        if (ghost != null) {
            ghost.positionProperty().removeListener(this::handleGhostPositionChange);
            ghost.wishDirProperty().removeListener(this::handleGhostWishDirChange);
        }
        visibleProperty().unbind();
        appearanceProperty.removeListener(this::handleAppearanceChange);
        numberMaterialCache.clear();
        if (brakeAnimation != null) {
            animationManager.destroyAnimation(brakeAnimation);
            brakeAnimation = null;
        }
        if (pointsAnimation != null) {
            animationManager.destroyAnimation(pointsAnimation);
            pointsAnimation = null;
        }
        getChildren().clear();
        if (ghost3D != null) {
            ghost3D.destroy();
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

    public void setNumberTexture(Image numberImage) {
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

    private void selectAppearance(GameLevel level) {
        boolean powerFading = level.pac().isPowerFading(level);
        boolean powerActive = level.pac().powerTimer().isRunning();
        // ghost that got killed already during the current power phase do not look frightened anymore
        boolean killedDuringCurrentPhase = level.victims().contains(ghost);
        GhostAppearance appearance = GhostAppearanceSelector.selectAppearance(
                ghost.state(),
                powerActive,
                powerFading,
                killedDuringCurrentPhase);
        setAppearance(appearance);
    }
}