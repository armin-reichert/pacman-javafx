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
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.model3D.Ghost3D;
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

    private final AnimationManager animationMgr;
    private final Ghost ghost;
    private final Ghost3D ghost3D;
    private final Box numberBox;
    private final RotateTransition numberBoxRotation;
    private final double size;
    private final int numFlashes;
    private RotateTransition brakeAnimation;

    public MutatingGhost3D(
        AnimationManager animationManager,
        AssetStorage assets, String assetPrefix,
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        Ghost ghost, double size, int numFlashes)
    {
        requireNonNull(assets);
        requireNonNull(assetPrefix);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNegative(numFlashes);

        this.animationMgr = requireNonNull(animationManager);
        this.ghost = requireNonNull(ghost);
        this.size = requireNonNegative(size);
        this.numFlashes = numFlashes;

        ghost3D = new Ghost3D(animationManager, assets, assetPrefix, ghost.personality(), dressShape, pupilsShape, eyeballsShape, size);

        numberBox = new Box(14, 8, 8);
        numberBoxRotation = new RotateTransition(Duration.seconds(1), numberBox);
        numberBoxRotation.setAxis(Rotate.X_AXIS);
        numberBoxRotation.setFromAngle(0);
        numberBoxRotation.setToAngle(360);
        numberBoxRotation.setInterpolator(Interpolator.LINEAR);
        numberBoxRotation.setRate(0.75);

        setAppearance(Appearance.NORMAL);
    }

    public Appearance appearance() { return appearancePy.get(); }

    public void setAppearance(Appearance appearance) { appearancePy.set(requireNonNull(appearance)); }

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

    public void setNumberTexture(Image numberTexture) {
        var material = new PhongMaterial();
        material.setDiffuseMap(numberTexture);
        numberBox.setMaterial(material);
    }

    private void updateTransform(GameLevel level) {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - 2.0); // a little bit over the floor
        double angle = switch (ghost.wishDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
        ghost3D.setRotation(angle);
        boolean outsideTerrain = center.x() < HTS || center.x() > level.worldMap().numCols() * TS - HTS;
        setVisible(ghost.isVisible() && !outsideTerrain);
    }

    private void stopAllAnimations() {
        endBrakeAnimation();
        ghost3D.stopDressAnimation();
        numberBoxRotation.stop();
    }

    private void updateAnimations() {
        if (appearance() == Appearance.VALUE) {
            ghost3D.stopDressAnimation();
        } else {
            numberBoxRotation.stop();
            ghost3D.playDressAnimation();
            if (ghost.moveInfo().tunnelEntered) {
                playBrakeAnimation();
            }
        }
    }

    private void playBrakeAnimation() {
        if (ghost.moveDir().isHorizontal()) {
            brakeAnimation = new RotateTransition(Duration.seconds(0.5), this);
            brakeAnimation.setAxis(Rotate.Y_AXIS);
            brakeAnimation.setByAngle(ghost.moveDir() == Direction.LEFT ? -35 : 35);
            brakeAnimation.setAutoReverse(true);
            brakeAnimation.setCycleCount(2);
            brakeAnimation.setInterpolator(Interpolator.EASE_OUT);
            animationMgr.registerAndPlayFromStart(this, "GhostBraking", brakeAnimation);
        }
    }

    private void endBrakeAnimation() {
        if (brakeAnimation != null) {
            brakeAnimation.stop();
            setRotationAxis(Rotate.Y_AXIS);
            setRotate(0);
        }
    }

    private void onAppearanceChanged(Appearance appearance) {
        getChildren().setAll(appearance == Appearance.VALUE ? numberBox : ghost3D.root());
        switch (appearance) {
            case NORMAL -> ghost3D.setNormalAppearance();
            case FRIGHTENED -> ghost3D.setFrightenedAppearance();
            case EATEN -> ghost3D.setEyesOnlyAppearance();
            case FLASHING -> ghost3D.setFlashingAppearance(numFlashes);
            case VALUE -> numberBoxRotation.playFromStart();
        }
        Logger.trace("Ghost {} appearance changed to {}", ghost.personality(), appearance);
    }

    private void updateAppearance(GameLevel level) {
        Appearance nextAppearance = switch (ghost.state()) {
            case LEAVING_HOUSE, LOCKED ->
                // ghost that have been killed by current energizer will not look frightened
                level.pac().powerTimer().isRunning() && !level.victims().contains(ghost)
                        ? frightenedOrFlashing(level.pac().isPowerFading(level))
                        : Appearance.NORMAL;
            case FRIGHTENED -> frightenedOrFlashing(level.pac().isPowerFading(level));
            case ENTERING_HOUSE, RETURNING_HOME -> Appearance.EATEN;
            case EATEN -> Appearance.VALUE;
            default -> Appearance.NORMAL;
        };
        setAppearance(nextAppearance);
    }

    private Appearance frightenedOrFlashing(boolean powerFading) {
        return powerFading ? Appearance.FLASHING : Appearance.FRIGHTENED;
    }
}