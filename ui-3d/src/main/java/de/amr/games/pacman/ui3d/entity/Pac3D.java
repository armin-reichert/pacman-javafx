/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 *
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 *
 * @author Armin Reichert
 */
public abstract class Pac3D extends Group {

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

    protected final Rotate orientation = new Rotate();
    protected final PointLight light;
    protected Pac pac;
    protected double zStandingOnGround;

    protected Pac3D() {
        light = new PointLight();
        light.setMaxRange(2 * TS);
        light.translateXProperty().bind(translateXProperty());
        light.translateYProperty().bind(translateYProperty());
        light.setTranslateZ(-10);
    }

    public abstract Animation createDyingAnimation(GameContext context);

    public abstract void startWalkingAnimation();

    public abstract void stopWalkingAnimation();

    public abstract void setPower(boolean power);

    public PointLight light() {
        return light;
    }

    public void init(GameContext context) {
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        update(context);
    }

    public void update(GameContext context) {
        var game = context.game();
        var world = game.world();
        Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(zStandingOnGround);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pac.moveDir()));
        boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * world.map().terrain().numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
        if (pac.isStandingStill()) {
            stopWalkingAnimation();
        } else {
            startWalkingAnimation();
        }
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(lightedPy.get() && pac.isVisible() && hasPower);
    }
}