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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PAC_LIGHT_ENABLED;

/**
 * @author Armin Reichert
 */
public abstract class AbstractPac3D implements Pac3D {

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    protected final Pac pac;
    protected final double size;
    protected final Model3D model3D;
    protected final PointLight light = new PointLight();
    protected final Rotate moveRotation = new Rotate();

    protected RotateTransition closeMouth;
    protected RotateTransition openMouth;
    protected Transition chewingAnimation;

    protected AbstractPac3D(Pac pac, double size, Model3D model3D) {
        this.pac = checkNotNull(pac);
        this.size = size;
        this.model3D = checkNotNull(model3D);
    }

    protected void updatePosition(Node root) {
        Vector2f center = pac.center();
        root().setTranslateX(center.x());
        root().setTranslateY(center.y());
        root().setTranslateZ(-0.5 * size);
        light.setTranslateX(center.x());
        light.setTranslateY(center.y());
        light.setTranslateZ(-1.5 * size);
    }

    protected void updateMoveRotation() {
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac.moveDir()));
    }

    protected void updateLight() {
        GameModel game = context().game();
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(PY_3D_PAC_LIGHT_ENABLED.get() && pac.isVisible() && hasPower);
    }

    protected void updateVisibility() {
        WorldMap map = context().game().world().map();
        boolean outsideWorld = root().getTranslateX() < HTS || root().getTranslateX() > TS * map.terrain().numCols() - HTS;
        root().setVisible(pac.isVisible() && !outsideWorld);
    }

    protected abstract GameContext context();
    protected abstract void stopChewingAnimation();
    protected abstract void stopWalkingAnimation();

    @Override
    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    @Override
    public PointLight light() {
        return light;
    }

    protected void createChewingAnimation(Node jaw) {
        final int openAngle = 0, closedAngle = -54;
        closeMouth = new RotateTransition(Duration.millis(100), jaw);
        closeMouth.setAxis(Rotate.Y_AXIS);
        closeMouth.setFromAngle(openAngle);
        closeMouth.setToAngle(closedAngle);
        closeMouth.setInterpolator(Interpolator.LINEAR);
        openMouth = new RotateTransition(Duration.millis(40), jaw);
        openMouth.setAxis(Rotate.Y_AXIS);
        openMouth.setFromAngle(closedAngle);
        openMouth.setToAngle(openAngle);
        openMouth.setInterpolator(Interpolator.LINEAR);
        chewingAnimation = new SequentialTransition(openMouth, Ufx.pauseSec(0.2), closeMouth);
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
    }
}