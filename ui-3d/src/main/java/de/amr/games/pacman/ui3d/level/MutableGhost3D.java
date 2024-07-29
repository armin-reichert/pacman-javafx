/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
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
public class MutableGhost3D extends Group {

    public enum Look { NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER }

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<Look> lookPy = new SimpleObjectProperty<>(this, "look") {
        @Override
        protected void invalidated() {
            onLookChanged(get());
        }
    };

    private final Ghost ghost;
    private final Group ghostGroup;
    private final Ghost3D ghost3D;
    private final NumberCube3D numberCube;
    private final Rotate orientation = new Rotate();
    private final RotateTransition brakeAnimation;
    private final RotateTransition dressAnimation;
    private final double size;
    private int numFlashes;

    public MutableGhost3D(Model3D model3D, Theme theme, Ghost ghost, double size) {
        requireNonNull(model3D);
        requireNonNull(theme);
        requireNonNull(ghost);

        this.ghost = ghost;
        this.size = size;

        ghost3D = new Ghost3D(model3D, theme, ghost.id(), size);
        ghost3D.dressShape().drawModeProperty().bind(drawModePy);
        ghost3D.eyeballsShape().drawModeProperty().bind(drawModePy);
        ghost3D.pupilsShape().drawModeProperty().bind(drawModePy);

        ghostGroup = new Group(ghost3D);
        ghostGroup.getTransforms().add(orientation);

        numberCube = new NumberCube3D(14, 8, 8);

        getChildren().add(ghostGroup);

        brakeAnimation = new RotateTransition(Duration.seconds(0.5), ghost3D);
        brakeAnimation.setAxis(Rotate.Y_AXIS);
        brakeAnimation.setFromAngle(0);
        brakeAnimation.setToAngle(-35);
        brakeAnimation.setAutoReverse(true);
        brakeAnimation.setCycleCount(2);

        dressAnimation = new RotateTransition(Duration.seconds(0.3), ghost3D.dressGroup());
        // TODO I expected this should be the z-axis but... (transforms messed-up?)
        dressAnimation.setAxis(Rotate.Y_AXIS);
        dressAnimation.setByAngle(30);
        dressAnimation.setCycleCount(Animation.INDEFINITE);
        dressAnimation.setAutoReverse(true);

        lookPy.set(Look.NORMAL);
    }

    public void init(GameContext context) {
        brakeAnimation.stop();
        dressAnimation.stop();
        numberCube.stopRotation();
        updateTransform();
        updateLook(context.game());
    }

    public void update(GameContext context) {
        updateTransform();
        updateLook(context.game());
        updateAnimations();
        context.game().level().ifPresent(level -> numFlashes = level.numFlashes());
    }

    public void setNumberImage(Image image) {
        numberCube.setImage(image);
    }

    private void updateTransform() {
        Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - 2.0);
        // TODO: make transition to new wish dir if changed
        orientation.setAngle(Ufx.angle(ghost.wishDir()));
        boolean outside = center.x() < HTS || center.x() > ghost.world().map().terrain().numCols() * TS - HTS;
        setVisible(ghost.isVisible() && !outside);
    }

    private void updateAnimations() {
        if (look() == Look.NUMBER) {
            dressAnimation.stop();
        } else {
            numberCube.stopRotation();
            if (ghost.moveInfo().tunnelEntered) {
                brakeAnimation.playFromStart();
            }
            if (dressAnimation.getStatus() != Status.RUNNING) {
                dressAnimation.play();
            }
        }
    }

    private void updateLook(GameModel game) {
        var newLook = Look.NORMAL;
        if (ghost.state() != null) {
            newLook = switch (ghost.state()) {
                case LEAVING_HOUSE, LOCKED ->
                    game.powerTimer().isRunning() && !game.victims().contains(ghost)
                        ? frightenedOrFlashing(game) : Look.NORMAL;
                case FRIGHTENED -> frightenedOrFlashing(game);
                case ENTERING_HOUSE, RETURNING_HOME -> Look.EYES;
                case EATEN -> Look.NUMBER;
                default -> Look.NORMAL;
            };
        }
        lookPy.set(newLook);
    }

    private void onLookChanged(Look look) {
        Logger.info("Ghost {} gets new look: {}", ghost.id(), look);
        getChildren().setAll(look == Look.NUMBER ? numberCube : ghostGroup);
        switch (look) {
            case NORMAL     -> ghost3D.appearNormal();
            case FRIGHTENED -> ghost3D.appearFrightened();
            case EYES       -> ghost3D.appearEyesOnly();
            case FLASHING   -> ghost3D.appearFlashing(numFlashes);
            case NUMBER     -> numberCube.startRotation();
        }
    }

    public Look look() {
        return lookPy.get();
    }

    private Look frightenedOrFlashing(GameModel game) {
        return game.isPowerFading() ? Look.FLASHING : Look.FRIGHTENED;
    }
}