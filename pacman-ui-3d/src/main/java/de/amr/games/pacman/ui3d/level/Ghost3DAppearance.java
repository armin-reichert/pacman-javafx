/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.lib.Ufx;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
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
public class Ghost3DAppearance {

    public enum Appearance { COLORED_GHOST, FRIGHTENED_GHOST, FLASHING_GHOST, GHOST_EYES, NUMBER }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<Appearance> appearancePy = new SimpleObjectProperty<>(this, "appearance") {
        @Override
        protected void invalidated() { changeAppearance(getValue()); }
    };

    private final Ghost ghost;
    private final Group root = new Group();
    private final Ghost3D ghost3D;
    private final Box numberCube;
    private final RotateTransition numberCubeRotation;
    private final RotateTransition brakeAnimation;
    private final double size;
    private final int numFlashes;

    public Ghost3DAppearance(
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        AssetStorage assets, String assetPrefix, Ghost ghost, double size, int numFlashes) {

        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNull(assets);
        requireNonNull(ghost);
        assertNonNegative(size);
        assertNonNegative(numFlashes);

        this.ghost = ghost;
        this.size = size;
        this.numFlashes = numFlashes;

        drawModePy.bind(GlobalProperties3d.PY_3D_DRAW_MODE);

        ghost3D = new Ghost3D(ghost.id(), dressShape, pupilsShape, eyeballsShape, assets, assetPrefix, size);
        ghost3D.drawModePy.bind(drawModePy);

        numberCube = new Box(14, 8, 8);
        numberCubeRotation = new RotateTransition(Duration.seconds(1), numberCube);
        numberCubeRotation.setAxis(Rotate.X_AXIS);
        numberCubeRotation.setFromAngle(0);
        numberCubeRotation.setToAngle(360);
        numberCubeRotation.setInterpolator(Interpolator.LINEAR);
        numberCubeRotation.setRate(0.75);

        brakeAnimation = new RotateTransition(Duration.seconds(0.5), root);
        brakeAnimation.setAxis(Rotate.Y_AXIS);
        brakeAnimation.setByAngle(35);
        brakeAnimation.setAutoReverse(true);
        brakeAnimation.setCycleCount(2);

        appearancePy.set(Appearance.COLORED_GHOST);
    }

    public Group root() {
        return root;
    }

    public void init(GameContext context) {
        stopAllAnimations();
        updateTransform();
        updateAppearance(context);
    }

    public void update(GameContext context) {
        updateTransform();
        updateAppearance(context);
        updateAnimations();
    }

    public void setNumberImage(Image numberImage) {
        var texture = new PhongMaterial();
        texture.setDiffuseMap(numberImage);
        numberCube.setMaterial(texture);
    }

    private void updateTransform() {
        Vector2f center = ghost.position().plus(HTS, HTS);
        root.setTranslateX(center.x());
        root.setTranslateY(center.y());
        root.setTranslateZ(-0.5 * size - 2.0); // a little bit over the floor
        ghost3D.turnTo(Ufx.angle(ghost.wishDir()));
        boolean outsideTerrain = center.x() < HTS || center.x() > ghost.world().map().terrain().numCols() * TS - HTS;
        root.setVisible(ghost.isVisible() && !outsideTerrain);
    }

    private void stopAllAnimations() {
        brakeAnimation.stop();
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
                switch (ghost.moveDir()) {
                    case LEFT -> {
                        brakeAnimation.setByAngle(-40);
                        brakeAnimation.playFromStart();
                    }
                    case RIGHT -> {
                        brakeAnimation.setByAngle(40);
                        brakeAnimation.playFromStart();
                    }
                    default -> {}
                }
            }
            if (ghost.moveInfo().tunnelLeft) {
                brakeAnimation.stop();
            }
        }
    }

    private void changeAppearance(Appearance appearance) {
        root.getChildren().setAll(appearance == Appearance.NUMBER ? numberCube : ghost3D.root());
        switch (appearance) {
            case COLORED_GHOST -> ghost3D.appearNormal();
            case FRIGHTENED_GHOST -> ghost3D.appearFrightened();
            case GHOST_EYES -> ghost3D.appearEyesOnly();
            case FLASHING_GHOST -> ghost3D.appearFlashing(numFlashes);
            case NUMBER     -> numberCubeRotation.playFromStart();
        }
        Logger.info("Ghost {} appearance changed to {}", ghost.id(), appearance);
    }

    private void updateAppearance(GameContext context) {
        GameLevel level = context.level(); // assert not null
        if (ghost.state() == null) {
            // can this happen?
            appearancePy.set(Appearance.COLORED_GHOST);
            return;
        }
        Appearance nextAppearance = switch (ghost.state()) {
            case LEAVING_HOUSE, LOCKED ->
                // ghost that have been killed by current energizer will not look frightened
                level.powerTimer().isRunning() && !level.victims().contains(ghost)
                    ? frightenedOrFlashing(context.game().isPowerFading())
                        : Appearance.COLORED_GHOST;
            case FRIGHTENED -> frightenedOrFlashing(context.game().isPowerFading());
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