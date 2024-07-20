/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;

/**
 * @author Armin Reichert
 */
public abstract class AbstractPac3D implements Pac3D {

    protected final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    protected final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    protected final DoubleProperty lightRangePy = new SimpleDoubleProperty(this, "lightRange", 0);
    protected final Rotate rotation = new Rotate();
    protected final Group jaw;
    protected final RotateTransition jawRotation;
    protected final Transition chewingAnimation;
    protected final Pac pac;
    protected final double size;
    protected final Model3D model3D;

    protected AbstractPac3D(Pac pac, double size, Theme theme) {
        this.size = size;
        this.pac = pac;
        this.model3D = theme.get("model3D.pacman");
        jaw = PacModel3D.createPacHead(model3D, size, theme.color("pacman.color.head"), theme.color("pacman.color.palate"));
        jawRotation = new RotateTransition(Duration.seconds(0.4));
        jawRotation.setAutoReverse(true);
        jawRotation.setAxis(Rotate.Y_AXIS);
        jawRotation.setInterpolator(Interpolator.EASE_IN);
        jawRotation.setFromAngle(0);
        jawRotation.setToAngle(-55);
        chewingAnimation = new SequentialTransition(jawRotation, Ufx.pauseSec(0.1));
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void init(GameContext context) {
        node().setScaleX(1.0);
        node().setScaleY(1.0);
        node().setScaleZ(1.0);
        node().setTranslateZ(-0.5 * size);
        updatePosition();
        updateRotation();
    }

    protected void updatePosition() {
        Vector2f center = pac.center();
        node().setTranslateX(center.x());
        node().setTranslateY(center.y());
        node().setTranslateZ(-0.5 * size);
    }

    protected void updateRotation() {
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setAngle(angle(pac.moveDir()));
    }

    protected void updateLight(GameContext context) {
        GameModel game = context.game();
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        lightRangeProperty().set(range);
        lightOnProperty().set(PY_3D_PAC_LIGHT_ENABLED.get() && pac.isVisible() && hasPower);
    }

    protected void updateVisibility(GameContext context) {
        GameWorld world = context.game().world();
        boolean outsideWorld = node().getTranslateX() < HTS || node().getTranslateX() > TS * world.map().terrain().numCols() - HTS;
        node().setVisible(pac.isVisible() && !outsideWorld);
    }

    @Override
    public void updateAlive(GameContext context) {
        updatePosition();
        updateRotation();
        updateVisibility(context);
        updateLight(context);
        updateAliveAnimation();
    }

    protected void stopChewing() {
        chewingAnimation.stop();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }

    protected abstract void updateAliveAnimation();

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    @Override
    public BooleanProperty lightOnProperty() {
        return lightOnPy;
    }

    @Override
    public DoubleProperty lightRangeProperty() {
        return lightRangePy;
    }
}