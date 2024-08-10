/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.PointLight;
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

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    protected final PointLight light = new PointLight();
    protected final Rotate moveRotation = new Rotate();
    private Animation chewingAnimation;

    protected abstract Pac pac();

    protected void createChewingAnimation(Node jaw) {
        final int openAngle = 0, closedAngle = -54;
        var closeMouth = new RotateTransition(Duration.millis(30), jaw);
        closeMouth.setAxis(Rotate.Y_AXIS);
        closeMouth.setFromAngle(openAngle);
        closeMouth.setToAngle(closedAngle);
        closeMouth.setInterpolator(Interpolator.LINEAR);
        var openMouth = new RotateTransition(Duration.millis(90), jaw);
        openMouth.setAxis(Rotate.Y_AXIS);
        openMouth.setFromAngle(closedAngle);
        openMouth.setToAngle(openAngle);
        openMouth.setInterpolator(Interpolator.LINEAR);
        chewingAnimation = new SequentialTransition(openMouth, Ufx.pauseSec(0.1), closeMouth);
        chewingAnimation.setCycleCount(Animation.INDEFINITE);
    }

    protected void playChewingAnimation() {
        chewingAnimation.play();
    }

    protected void stopChewingAnimation(Node jaw) {
        chewingAnimation.stop();
        jaw.setRotationAxis(Rotate.Y_AXIS);
        jaw.setRotate(0);
    }

    protected void updateMoveRotation() {
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(Ufx.angle(pac().moveDir()));
    }

    protected void updateLight(GameModel game) {
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(PY_3D_PAC_LIGHT_ENABLED.get() && pac().isVisible() && hasPower);
    }

    protected void updateVisibility(GameModel game) {
        WorldMap map = game.world().map();
        boolean outsideWorld = root().getTranslateX() < HTS || root().getTranslateX() > TS * map.terrain().numCols() - HTS;
        root().setVisible(pac().isVisible() && !outsideWorld);
    }

    @Override
    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    @Override
    public PointLight light() {
        return light;
    }
}