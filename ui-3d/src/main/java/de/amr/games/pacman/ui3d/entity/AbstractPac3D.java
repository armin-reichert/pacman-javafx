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
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;

/**
 * @author Armin Reichert
 */
public abstract class AbstractPac3D extends Group implements AnimatedPac3D {

    protected final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    protected final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    protected final DoubleProperty lightRangePy = new SimpleDoubleProperty(this, "lightRange", 0);
    protected final Rotate rotation = new Rotate();
    protected Pac pac;
    protected double size;

    @Override
    public void init(GameContext context) {
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        setTranslateZ(-0.5 * size);
        updatePosition();
        updateRotation();
    }

    protected void updatePosition() {
        Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size);
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
        boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * world.map().terrain().numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
    }

    @Override
    public void updateAlive(GameContext context) {
        updatePosition();
        updateRotation();
        updateVisibility(context);
        updateLight(context);
        updateAliveAnimation();
    }

    protected abstract void updateAliveAnimation();

    @Override
    public Node node() {
        return this;
    }

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