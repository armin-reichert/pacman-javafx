/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.Ufx;
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

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * A 3D ghost can have one of the following appearances:
 * <ul>
 * <li>{@link Appearance#COLORED_GHOST}: colored ghost with blue eyes,
 * <li>{@link Appearance#FRIGHTENED_GHOST}: blue ghost with empty pinkish eyes (ghost looking blind),
 * <li>{@link Appearance#FLASHING_GHOST}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link Appearance#GHOST_EYES} blue eyes only,
 * <li>{@link Appearance#NUMBER}: number cube showing eaten ghost's value.
 * </ul>
 *
 * @author Armin Reichert
 */
public class Ghost3DAppearance extends Group {

    public enum Appearance { COLORED_GHOST, FRIGHTENED_GHOST, FLASHING_GHOST, GHOST_EYES, NUMBER }

    private final ObjectProperty<Appearance> appearancePy = new SimpleObjectProperty<>(this, "appearance") {
        @Override
        protected void invalidated() { changeAppearance(getValue()); }
    };

    private final Ghost ghost;
    private final Ghost3D ghost3D;
    private final Box numberCube;
    private final RotateTransition numberCubeRotation;
    private final double size;
    private final int numFlashes;
    private RotateTransition brakeAnimation;

    public Ghost3DAppearance(
        AssetStorage assets,
        String assetPrefix,
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        Ghost ghost, double size, int numFlashes) {

        requireNonNull(assetPrefix);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNull(ghost);
        Validations.requireNonNegative(size);
        Validations.requireNonNegative(numFlashes);

        this.ghost = ghost;
        this.size = size;
        this.numFlashes = numFlashes;

        ghost3D = new Ghost3D(assets, assetPrefix, ghost.id(), dressShape, pupilsShape, eyeballsShape, size);

        numberCube = new Box(14, 8, 8);
        numberCubeRotation = new RotateTransition(Duration.seconds(1), numberCube);
        numberCubeRotation.setAxis(Rotate.X_AXIS);
        numberCubeRotation.setFromAngle(0);
        numberCubeRotation.setToAngle(360);
        numberCubeRotation.setInterpolator(Interpolator.LINEAR);
        numberCubeRotation.setRate(0.75);

        appearancePy.set(Appearance.COLORED_GHOST);
    }

    private void playBrakeAnimation() {
        if (ghost.moveDir().isHorizontal()) {
            brakeAnimation = new RotateTransition(Duration.seconds(0.5), this);
            brakeAnimation.setAxis(Rotate.Y_AXIS);
            brakeAnimation.setByAngle(ghost.moveDir() == Direction.LEFT ? -35 : 35);
            brakeAnimation.setAutoReverse(true);
            brakeAnimation.setCycleCount(2);
            brakeAnimation.setInterpolator(Interpolator.EASE_OUT);
            brakeAnimation.play();
        }
    }

    private void endBrakeAnimation() {
        if (brakeAnimation != null) {
            brakeAnimation.stop();
            setRotationAxis(Rotate.Y_AXIS);
            setRotate(0);
        }
    }

    public void init(GameLevel level) {
        stopAllAnimations();
        updateTransform();
        updateAppearance(level);
    }

    public void update(GameLevel level) {
        updateTransform();
        updateAppearance(level);
        updateAnimations();
    }

    public void setNumberImage(Image numberImage) {
        var texture = new PhongMaterial();
        texture.setDiffuseMap(numberImage);
        numberCube.setMaterial(texture);
    }

    private void updateTransform() {
        Vector2f center = ghost.position().plus(HTS, HTS);
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - 2.0); // a little bit over the floor
        ghost3D.turnTo(Ufx.angle(ghost.wishDir()));
        boolean outsideTerrain = center.x() < HTS || center.x() > ghost.level().worldMap().numCols() * TS - HTS;
        setVisible(ghost.isVisible() && !outsideTerrain);
    }

    private void stopAllAnimations() {
        endBrakeAnimation();
        ghost3D.stopDressAnimation();
        numberCubeRotation.stop();
    }

    private void updateAnimations() {
        if (appearancePy.get() == Appearance.NUMBER) {
            ghost3D.stopDressAnimation();
        } else {
            numberCubeRotation.stop();
            ghost3D.playDressAnimation();
            if (ghost.moveInfo().tunnelEntered) {
                playBrakeAnimation();
            }
        }
    }

    private void changeAppearance(Appearance appearance) {
        getChildren().setAll(appearance == Appearance.NUMBER ? numberCube : ghost3D.root());
        switch (appearance) {
            case COLORED_GHOST -> ghost3D.appearNormal();
            case FRIGHTENED_GHOST -> ghost3D.appearFrightened();
            case GHOST_EYES -> ghost3D.appearEyesOnly();
            case FLASHING_GHOST -> ghost3D.appearFlashing(numFlashes);
            case NUMBER     -> numberCubeRotation.playFromStart();
        }
        Logger.info("Ghost {} appearance changed to {}", ghost.id(), appearance);
    }

    private void updateAppearance(GameLevel level) {
        if (ghost.state() == null) { // TODO: can this happen?
            appearancePy.set(Appearance.COLORED_GHOST);
            return;
        }
        Appearance nextAppearance = switch (ghost.state()) {
            case LEAVING_HOUSE, LOCKED ->
                // ghost that have been killed by current energizer will not look frightened
                level.pac().powerTimer().isRunning() && !level.victims().contains(ghost)
                        ? frightenedOrFlashing(level.pac().isPowerFading())
                        : Appearance.COLORED_GHOST;
            case FRIGHTENED -> frightenedOrFlashing(level.pac().isPowerFading());
            case ENTERING_HOUSE, RETURNING_HOME -> Appearance.GHOST_EYES;
            case EATEN -> Appearance.NUMBER;
            default -> Appearance.COLORED_GHOST;
        };
        appearancePy.set(nextAppearance);
    }

    private Appearance frightenedOrFlashing(boolean powerFading) {
        return powerFading ? Appearance.FLASHING_GHOST : Appearance.FRIGHTENED_GHOST;
    }
}