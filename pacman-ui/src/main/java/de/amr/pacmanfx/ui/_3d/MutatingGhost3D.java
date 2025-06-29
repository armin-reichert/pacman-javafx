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

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * Appearances of a 3D ghost. One of:
 * <ul>
 * <li>{@link Appearance#NORMAL}: colored ghost with blue eyes,
 * <li>{@link Appearance#FRIGHTENED}: blue ghost with empty, "pinkish" eyes (looking blind),
 * <li>{@link Appearance#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link Appearance#EATEN} eyes only,
 * <li>{@link Appearance#VALUE}: showing eaten ghost's value.
 * </ul>
 */
public class MutatingGhost3D extends Group {

    public enum Appearance {NORMAL, FRIGHTENED, FLASHING, EATEN, VALUE}

    private final ObjectProperty<Appearance> appearancePy = new SimpleObjectProperty<>(this, "appearance") {
        @Override
        protected void invalidated() { onAppearanceChanged(getValue()); }
    };

    private final Ghost ghost;
    private final Ghost3D ghost3D;
    private final Box numberBox;
    private final double size;
    private final int numFlashes;

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
        this.numFlashes = numFlashes;
        this.ghost3D = new Ghost3D(animationManager, assets, assetPrefix,
            ghost.personality(), ghost.name(),
            dressShape, pupilsShape, eyeballsShape, size);
        this.numberBox = new Box(14, 8, 8);
        setAppearance(Appearance.NORMAL);

        pointsAnimation = new ManagedAnimation(animationManager, "Ghost_%s_Points".formatted(ghost.name())) {
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

        brakeAnimation = new ManagedAnimation(animationManager, "Ghost_%s_Braking".formatted(ghost.name())) {
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
    }

    public void init(GameLevel level) {
        stopAllAnimations();
        updateTransform(level);
        updateAppearance(level);
    }

    public void update(GameLevel level) {
        updateTransform(level);
        updateAppearance(level);
        updateAnimations();
    }

    public Appearance appearance() { return appearancePy.get(); }

    public void setAppearance(Appearance appearance) { appearancePy.set(requireNonNull(appearance)); }

    private void updateAppearance(GameLevel level) {
        boolean powerFading = level.pac().isPowerFading(level);
        boolean powerActive = level.pac().powerTimer().isRunning();
        setAppearance(switch (ghost.state()) {
            case LEAVING_HOUSE, LOCKED -> {
                // ghost that got killed already during the current power phase do not look frightened anymore
                boolean killedDuringCurrentPhase = level.victims().contains(ghost);
                yield powerActive && !killedDuringCurrentPhase ? frightenedOrFlashing(powerFading) : Appearance.NORMAL;
            }
            case FRIGHTENED -> frightenedOrFlashing(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> Appearance.EATEN;
            case EATEN -> Appearance.VALUE;
            default -> Appearance.NORMAL;
        });
    }

    public void setNumberTexture(Image numberImage) {
        var texture = new PhongMaterial();
        texture.setDiffuseMap(numberImage);
        numberBox.setMaterial(texture);
    }

    private void updateTransform(GameLevel level) {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - 2.0); // a little bit over the floor
        ghost3D.turnTowards(ghost.wishDir());
        boolean outsideTerrain = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
        setVisible(ghost.isVisible() && !outsideTerrain);
    }

    private void onAppearanceChanged(Appearance appearance) {
        getChildren().setAll(appearance == Appearance.VALUE ? numberBox : ghost3D.root());
        switch (appearance) {
            case NORMAL -> ghost3D.setNormalAppearance();
            case FRIGHTENED -> ghost3D.setFrightenedAppearance();
            case EATEN -> ghost3D.setEyesOnlyAppearance();
            case FLASHING -> ghost3D.setFlashingAppearance(numFlashes);
            case VALUE -> pointsAnimation.play(ManagedAnimation.FROM_START);
        }
        Logger.trace("Ghost {} appearance changed to {}", ghost.personality(), appearance);
    }

    private Appearance frightenedOrFlashing(boolean powerFading) {
        return powerFading ? Appearance.FLASHING : Appearance.FRIGHTENED;
    }

    // Animations

    public void stopAllAnimations() {
        brakeAnimation.stop();
        pointsAnimation.stop();
        ghost3D.dressAnimation().stop();
        ghost3D.flashingAnimation().stop();
    }

    private void updateAnimations() {
        if (appearance() == Appearance.VALUE) {
            ghost3D.dressAnimation().stop();
        } else {
            pointsAnimation.stop();
            if (ghost.isVisible()) {
                ghost3D.dressAnimation().play(ManagedAnimation.CONTINUE);
            } else {
                ghost3D.dressAnimation().stop();
            }
            if (ghost.moveInfo().tunnelEntered) {
                brakeAnimation.play(ManagedAnimation.FROM_START);
            }
        }
    }
}