/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a mutable ghost.
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
public class MutableGhost3D {

    public enum Look { NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<Look> lookPy = new SimpleObjectProperty<>(this, "look") {
        @Override
        protected void invalidated() {
            Look look = get();
            Logger.info("Ghost {} look changed to {}", ghost.id(), look);
            root.getChildren().setAll(look == Look.NUMBER ? numberCube : ghost3D.root());
            switch (look) {
                case NORMAL     -> ghost3D.appearNormal();
                case FRIGHTENED -> ghost3D.appearFrightened();
                case EYES       -> ghost3D.appearEyesOnly();
                case FLASHING   -> ghost3D.appearFlashing(numFlashes);
                case NUMBER     -> numberCubeRotation.playFromStart();
            }
        }
    };

    private final Ghost ghost;
    private final Group root = new Group();
    private final Ghost3D ghost3D;
    private final Box numberCube;
    private final RotateTransition numberCubeRotation;
    private final RotateTransition brakeAnimation;
    private final double size;
    private int numFlashes;

    public MutableGhost3D(GameVariant variant, Model3D model3D, AssetStorage assets, Ghost ghost, double size) {
        requireNonNull(variant);
        requireNonNull(model3D);
        requireNonNull(assets);
        requireNonNull(ghost);
        requirePositive(size);

        this.ghost = ghost;
        this.size = size;

        ghost3D = new Ghost3D(variant, model3D, assets, ghost.id(), size);
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

        lookPy.set(Look.NORMAL);
    }

    public void init(GameContext context) {
        brakeAnimation.stop();
        ghost3D.stopDressAnimation();
        numberCubeRotation.stop();
        updateTransform();
        updateLook(context.game());
    }

    public void update(GameContext context) {
        updateTransform();
        updateLook(context.game());
        updateAnimations();
        context.game().currentLevelData().ifPresent(level -> numFlashes = level.numFlashes());
    }

    public Group root() {
        return root;
    }

    public void setNumberImage(Image image) {
        var material = new PhongMaterial();
        material.setDiffuseMap(image);
        numberCube.setMaterial(material);
    }

    private void updateTransform() {
        Vector2f center = ghost.center();
        root.setTranslateX(center.x());
        root.setTranslateY(center.y());
        root.setTranslateZ(-0.5 * size - 2.0); // lift a bit over floor
        ghost3D.turnTo(Ufx.angle(ghost.wishDir()));
        boolean outside = center.x() < HTS || center.x() > ghost.world().map().terrain().numCols() * TS - HTS;
        root.setVisible(ghost.isVisible() && !outside);
    }

    private void updateAnimations() {
        if (lookPy.get() == Look.NUMBER) {
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
        }
    }

    private void updateLook(GameModel game) {
        if (ghost.state() == null) {
            // can this happen?
            lookPy.set(Look.NORMAL);
            return;
        }
        Look newLook = switch (ghost.state()) {
            case LEAVING_HOUSE, LOCKED ->
                // ghost that have been killed by current energizer will not look frightened
                game.powerTimer().isRunning() && !game.victims().contains(ghost)
                    ? frightenedOrFlashing(game) : Look.NORMAL;
            case FRIGHTENED -> frightenedOrFlashing(game);
            case ENTERING_HOUSE, RETURNING_HOME -> Look.EYES;
            case EATEN -> Look.NUMBER;
            default -> Look.NORMAL;
        };
        lookPy.set(newLook);
    }

    private Look frightenedOrFlashing(GameModel game) {
        return game.isPowerFading() ? Look.FLASHING : Look.FRIGHTENED;
    }
}