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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;
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

        private final RotateTransition animation;

        public HeadBanging(Node target) {
            animation = new RotateTransition(DEFAULT_DURATION, target);
            animation.setAxis(Rotate.X_AXIS);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.setAutoReverse(true);
            animation.setInterpolator(Interpolator.EASE_BOTH);
        }

        public void setPower(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            animation.stop();
            animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
            animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
            animation.setRate(rate);
        }

        public void play(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
                animation.getNode().setRotate(0);
                return;
            }
            var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(animation.getAxis())) {
                animation.stop();
                animation.setAxis(axis);
            }
            animation.play();
        }

        public void stop() {
            animation.stop();
            animation.getNode().setRotationAxis(animation.getAxis());
            animation.getNode().setRotate(0);
        }
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);
    private final Rotate orientation = new Rotate();
    private final Pac pacMan;
    private final PointLight light;
    private final HeadBanging walkingAnimation;

    /**
     * Creates a 3D Pac-Man.
     *
     * @param size diameter of Pac-Man
     * @param pacMan Pac-Man instance
     * @param theme the theme
     */
    public PacMan3D(double size, Pac pacMan, Theme theme) {
        this.pacMan = checkNotNull(pacMan);
        checkNotNull(theme);

        walkingAnimation = new HeadBanging(this);
        walkingAnimation.setPower(false);

        light = new PointLight();
        light.setMaxRange(2 * TS);
        light.translateXProperty().bind(translateXProperty());
        light.translateYProperty().bind(translateYProperty());
        light.setTranslateZ(-size);
        light.setColor(theme.color("pacman.color.head").desaturate());

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
            stand();
        } else {
            walk();
        }
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        light.setMaxRange(range);
        light.setLightOn(lightedPy.get() && pacMan.isVisible() && hasPower);
    }

    @Override
    public Node node() {
        return this;
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawModePy;
    }

    @Override
    public Property<Boolean> lightedProperty() {
        return lightedPy;
    }

    @Override
    public PointLight light() {
        return light;
    }

    @Override
    public void walk() {
        walkingAnimation.play(pacMan);
    }

    @Override
    public void stand() {
        walkingAnimation.stop();
    }

    @Override
    public void setPower(boolean power) {
        walkingAnimation.setPower(power);
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
        shrinking.setToX(0.5);
        shrinking.setToY(0.5);
        shrinking.setToZ(0.0);
        shrinking.setInterpolator(Interpolator.LINEAR);

        var falling = new TranslateTransition(duration, this);
        falling.setToZ(4);
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