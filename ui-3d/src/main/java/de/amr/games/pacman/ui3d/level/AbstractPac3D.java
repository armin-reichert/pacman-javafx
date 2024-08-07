/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PAC_LIGHT_ENABLED;

/**
 * @author Armin Reichert
 */
public abstract class AbstractPac3D implements Pac3D {

    protected final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    protected final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    protected final DoubleProperty lightRangePy = new SimpleDoubleProperty(this, "lightRange", 0);

    protected final GameContext context;
    protected final Pac pac;
    protected final double size;
    protected final Model3D model3D;

    protected final Rotate rotation = new Rotate();

    protected RotateTransition closeMouth;
    protected RotateTransition openMouth;
    protected Transition chewingAnimation;

    protected AbstractPac3D(GameContext context, Pac pac, double size, Model3D model3D) {
        this.context = context;
        this.pac = pac;
        this.size = size;
        this.model3D = model3D;
    }

    @Override
    public GameContext context() {
        return context;
    }

    protected void updatePosition() {
        Vector2f center = pac.center();
        root().setTranslateX(center.x());
        root().setTranslateY(center.y());
        root().setTranslateZ(-0.5 * size);
    }

    protected void updateRotation() {
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    protected void updateLight() {
        GameModel game = context.game();
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        lightRangePy.set(range);
        lightOnPy.set(PY_3D_PAC_LIGHT_ENABLED.get() && pac.isVisible() && hasPower);
    }

    protected void updateVisibility() {
        WorldMap map = context.game().world().map();
        boolean outsideWorld = root().getTranslateX() < HTS || root().getTranslateX() > TS * map.terrain().numCols() - HTS;
        root().setVisible(pac.isVisible() && !outsideWorld);
    }

    @Override
    public void update() {
        if (pac.isAlive()) {
            updatePosition();
            updateRotation();
            updateVisibility();
            updateLight();
            updateAliveAnimation();
        } else {
            stopChewingAnimation();
            stopWalkingAnimation();
        }
    }

    @Override
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

    protected void createChewingAnimation(Node jaw) {
        closeMouth = new RotateTransition(Duration.millis(100), jaw);
        closeMouth.setAxis(Rotate.Y_AXIS);
        closeMouth.setFromAngle(0);
        closeMouth.setToAngle(-54);
        closeMouth.setInterpolator(Interpolator.LINEAR);
        openMouth = new RotateTransition(Duration.millis(25), jaw);
        openMouth.setAxis(Rotate.Y_AXIS);
        openMouth.setFromAngle(-54);
        openMouth.setToAngle(0);
        openMouth.setInterpolator(Interpolator.LINEAR);
        chewingAnimation = new SequentialTransition(openMouth, Ufx.pauseSec(0.1), closeMouth);
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
    }
}