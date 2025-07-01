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

    private static final double GHOST_ELEVATION = 2.0;

    private static boolean isPositionOutsideWorld(GameLevel gameLevel, Vector2f center) {
        return center.x() < HTS || center.x() > gameLevel.worldMap().numCols() * TS - HTS;
    }

    private final ObjectProperty<GhostAppearance> appearanceProperty = new SimpleObjectProperty<>();

    private final Map<Image, PhongMaterial> textureCache = new WeakHashMap<>();
    private final Ghost ghost;
    private final Ghost3D ghost3D;
    private final Box numberBox;
    private final double size;

    private final ManagedAnimation brakeAnimation;
    private final ManagedAnimation pointsAnimation;

    public MutatingGhost3D(
        AnimationManager animationManager,
        AssetStorage assets, String assetPrefix,
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        Ghost ghost, double size, int numFlashes)
    {
        requireNonNull(animationManager);
        requireNonNull(assets);
        requireNonNull(assetPrefix);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNegative(numFlashes);

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

        ghost.positionProperty().addListener((py, ov, newPosition) -> updateTransform());
        ghost.wishDirProperty().addListener((py, ov, newWishDir) -> updateTransform());
        visibleProperty().bind(Bindings.createBooleanBinding(
                () -> ghost.isVisible() && !isPositionOutsideWorld(theGameLevel(), ghost.center()),
                ghost.visibleProperty(), ghost.positionProperty()
        ));

        appearanceProperty.addListener((property, oldValue, newValue) -> {
            Logger.info("Ghost {} now has appearance {}", ghost.name(), newValue);
            if (newValue == GhostAppearance.VALUE) {
                numberBox.setVisible(true);
                ghost3D.setVisible(false);
                ghost3D.dressAnimation().stop();
                pointsAnimation.play(ManagedAnimation.FROM_START);
            } else {
                numberBox.setVisible(false);
                ghost3D.setVisible(true);
                switch (newValue) {
                    case NORMAL     -> ghost3D.setNormalLook();
                    case FRIGHTENED -> ghost3D.setFrightenedLook();
                    case EATEN      -> ghost3D.setEyesOnlyLook();
                    case FLASHING   -> ghost3D.setFlashingLook(numFlashes);
                }
                pointsAnimation.stop();
                if (ghost.moveInfo().tunnelEntered) {
                    brakeAnimation.play(ManagedAnimation.FROM_START);
                }
            }
        });

        updateTransform();
        setAppearance(GhostAppearance.NORMAL);
    }

    public void destroy() {
        stopAllAnimations();
        //TODO...
    }

    public void stopAllAnimations() {
        brakeAnimation.stop();
        pointsAnimation.stop();
        ghost3D.dressAnimation().stop();
        ghost3D.flashingAnimation().stop();
    }

    public void init(GameLevel gameLevel) {
        stopAllAnimations();
        updateTransform();
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
            ghost3D.dressAnimation().play(ManagedAnimation.CONTINUE);
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
        if (!textureCache.containsKey(numberImage)) {
            var texture = new PhongMaterial();
            texture.setDiffuseMap(numberImage);
            textureCache.put(numberImage, texture);
        }
        numberBox.setMaterial(textureCache.get(numberImage));
    }

    private void updateTransform() {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - GHOST_ELEVATION); // a little bit over the floor
        ghost3D.turnTowards(ghost.wishDir());
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