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
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;
import static de.amr.games.pacman.ui3d.model.Model3D.meshView;

/**
 * @author Armin Reichert
 */
public class PacMan3D extends Group implements AnimatedPac3D {

    private static class HeadBanging {

        private static final short DEFAULT_ANGLE_FROM = -25;
        private static final short DEFAULT_ANGLE_TO = 15;
        private static final Duration DEFAULT_DURATION = Duration.seconds(0.25);

        private final RotateTransition banging;

        public HeadBanging(Node target) {
            banging = new RotateTransition(DEFAULT_DURATION, target);
            banging.setAxis(Rotate.X_AXIS);
            banging.setCycleCount(Animation.INDEFINITE);
            banging.setAutoReverse(true);
            banging.setInterpolator(Interpolator.EASE_BOTH);
        }

        // Note: Massive headbanging can lead to a stroke!
        public void setStrokeMode(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            banging.stop();
            banging.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
            banging.setToAngle(DEFAULT_ANGLE_TO * amplification);
            banging.setRate(rate);
        }

        public void apply(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
            } else {
                Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
                if (!axis.equals(banging.getAxis())) {
                    banging.stop();
                    banging.setAxis(axis);
                }
                banging.play();
            }
        }

        public void stop() {
            banging.stop();
            banging.getNode().setRotationAxis(banging.getAxis());
            banging.getNode().setRotate(0);
        }
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    private final DoubleProperty lightRangePy = new SimpleDoubleProperty(this, "lightRange", 0);
    private final Rotate orientation = new Rotate();
    private final Pac pacMan;
    private final HeadBanging headBanging;
    private final double size;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        this.size = size;
        this.pacMan = checkNotNull(pacMan);
        checkNotNull(theme);

        headBanging = new HeadBanging(this);
        headBanging.setStrokeMode(false);

        Group body = PacModel3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );

        var shapeGroup = new Group(body);
        shapeGroup.getTransforms().setAll(orientation);
        getChildren().add(shapeGroup);

        setTranslateZ(-0.5 * size);

        Stream.of(PacModel3D.MESH_ID_EYES, PacModel3D.MESH_ID_HEAD, PacModel3D.MESH_ID_PALATE)
            .map(id -> meshView(body, id))
            .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));
    }

    @Override
    public void init(GameContext context) {
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        setTranslateZ(-0.5 * size);
        update(context);
    }

    @Override
    public void update(GameContext context) {
        GameModel game = context.game();
        GameWorld world = game.world();
        Vector2f center = pacMan.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pacMan.moveDir()));
        boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * world.map().terrain().numCols() - HTS;
        setVisible(pacMan.isVisible() && !outsideWorld);
        if (pacMan.isStandingStill()) {
            headBanging.stop();
        } else {
            headBanging.apply(pacMan);
        }
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;

        lightRangeProperty().set(range);
        lightOnProperty().set(PY_3D_PAC_LIGHT_ENABLED.get() && pacMan.isVisible() && hasPower);
    }

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

    @Override
    public void setPower(boolean power) {
        headBanging.setStrokeMode(power);
    }

    @Override
    public Animation createDyingAnimation(GameContext context) {
        Duration duration = Duration.seconds(1.0);
        short numSpins = 6;

        var spinning = new RotateTransition(duration.divide(numSpins), this);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        var shrinking = new ScaleTransition(duration, this);
        shrinking.setToY(0.1);
        shrinking.setToZ(0.01);
        shrinking.setInterpolator(Interpolator.EASE_OUT);

        var falling = new TranslateTransition(duration, this);
        falling.setToZ(0);
        falling.setInterpolator(Interpolator.EASE_IN);

        //TODO does not yet work as I want to
        return new SequentialTransition(
                now(() -> init(context)),
                pauseSec(0.5),
                new ParallelTransition(spinning, shrinking, falling),
                doAfterSec(1.0, () -> {
                    setVisible(false);
                    setTranslateZ(0);
                })
        );
    }
}