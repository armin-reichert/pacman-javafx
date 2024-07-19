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
    private final Pac pac;
    private final HeadBanging headBanging;
    private final double size;
    private final Rotate rotation = new Rotate();

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        this.size = size;
        this.pac = checkNotNull(pacMan);
        checkNotNull(theme);

        headBanging = new HeadBanging(this);
        headBanging.setStrokeMode(false);

        Group body = PacModel3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate")
        );
        getChildren().add(body);

        getTransforms().add(rotation);
        setTranslateZ(-0.5 * size);

        Stream.of(PacModel3D.MESH_ID_EYES, PacModel3D.MESH_ID_HEAD, PacModel3D.MESH_ID_PALATE)
            .map(id -> meshView(body, id))
            .forEach(meshView -> meshView.drawModeProperty().bind(drawModePy));
    }

    @Override
    public void init(GameContext context) {
        headBanging.stop();
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        updatePosition();
        updateRotation();
    }

    private void updatePosition() {
        Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size);
    }

    private void updateRotation() {
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setAngle(angle(pac.moveDir()));
    }

    private void updateLight(GameContext context) {
        GameModel game = context.game();
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        lightRangeProperty().set(range);
        lightOnProperty().set(PY_3D_PAC_LIGHT_ENABLED.get() && pac.isVisible() && hasPower);
    }

    private void updateVisibility(GameContext context) {
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
        if (pac.isStandingStill()) {
            headBanging.stop();
        } else {
            headBanging.apply(pac);
        }
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
        shrinking.setToX(0.25);
        shrinking.setToY(0.25);
        shrinking.setToZ(0.02);
        shrinking.setOnFinished(e -> {
            setScaleX(0.75);
            setScaleX(0.75);
        });

        var falling = new TranslateTransition(duration, this);
        falling.setToZ(0);

        return new SequentialTransition(
            now(() -> init(context)),
            pauseSec(0.5),
            new ParallelTransition(spinning, shrinking, falling),
            doAfterSec(1.0, () -> setVisible(false))
        );
    }
}