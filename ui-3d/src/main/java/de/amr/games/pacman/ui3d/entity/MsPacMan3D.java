/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED;
import static de.amr.games.pacman.ui3d.animation.Turn.angle;
import static de.amr.games.pacman.ui3d.entity.PacModel3D.*;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D extends Group implements AnimatedPac3D {

    private static class HipSwaying {

        private static final short ANGLE_FROM = -20;
        private static final short ANGLE_TO = 20;
        private static final Duration DURATION = Duration.seconds(0.4);

        private final RotateTransition swaying;

        public HipSwaying(Node target) {
            swaying = new RotateTransition(DURATION, target);
            swaying.setAxis(Rotate.Z_AXIS);
            swaying.setCycleCount(Animation.INDEFINITE);
            swaying.setAutoReverse(true);
            swaying.setInterpolator(Interpolator.EASE_BOTH);
        }

        // Winnetouch is the gay twin-brother of Abahachi
        public void setWinnetouchMode(boolean power) {
            double amplification = power ? 1.5 : 1;
            double rate = power ? 2 : 1;
            swaying.stop();
            swaying.setFromAngle(ANGLE_FROM * amplification);
            swaying.setToAngle(ANGLE_TO * amplification);
            swaying.setRate(rate);
        }

        public void update(Pac pac) {
            if (pac.isStandingStill()) {
                stop();
            } else {
                swaying.play();
            }
        }

        public void stop() {
            swaying.stop();
            swaying.getNode().setRotationAxis(swaying.getAxis());
            swaying.getNode().setRotate(0);
        }
    }

    private final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    private final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);
    private final DoubleProperty lightRangePy = new SimpleDoubleProperty(this, "lightRange", 0);
    private final Rotate orientation = new Rotate();
    private final Pac msPacMan;
    private final HipSwaying hipSwaying;
    private final double size;

    /**
     * Creates a 3D Ms. Pac-Man.
     * @param size diameter of Pac-Man
     * @param msPacMan Ms. Pac-Man instance
     * @param theme the theme
     */
    public MsPacMan3D(double size, Pac msPacMan, Theme theme) {
        this.size = size;
        this.msPacMan = checkNotNull(msPacMan);
        checkNotNull(theme);

        hipSwaying = new HipSwaying(this);
        hipSwaying.setWinnetouchMode(false);

        Group body = PacModel3D.createPacShape(
            theme.get("model3D.pacman"), size,
            theme.color("ms_pacman.color.head"),
            theme.color("ms_pacman.color.eyes"),
            theme.color("ms_pacman.color.palate"));

        Group femaleParts = PacModel3D.createFemaleParts(size,
            theme.color("ms_pacman.color.hairbow"),
            theme.color("ms_pacman.color.hairbow.pearls"),
            theme.color("ms_pacman.color.boobs"));

        var shapeGroup = new Group(body, femaleParts);
        shapeGroup.getTransforms().setAll(orientation);
        getChildren().add(shapeGroup);

        setTranslateZ(-0.5 * size);

        Stream.of(MESH_ID_EYES, MESH_ID_HEAD, MESH_ID_PALATE)
            .map(id -> Model3D.meshView(body, id))
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
        var game = context.game();
        var world = game.world();
        Vector2f center = msPacMan.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(msPacMan.moveDir()));
        boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * world.map().terrain().numCols() - HTS;
        setVisible(msPacMan.isVisible() && !outsideWorld);
        if (msPacMan.isStandingStill()) {
            hipSwaying.stop();
        } else {
            hipSwaying.update(msPacMan);
        }
        // When empowered, Pac-Man is lighted, light range shrinks with ceasing power
        boolean hasPower = game.powerTimer().isRunning();
        double range = hasPower && game.powerTimer().duration() > 0
            ? 2 * TS + ((double) game.powerTimer().remaining() / game.powerTimer().duration()) * 6 * TS
            : 0;
        lightRangeProperty().set(range);
        lightOnProperty().set(PY_3D_PAC_LIGHT_ENABLED.get() && msPacMan.isVisible() && hasPower);
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
        hipSwaying.setWinnetouchMode(power);
    }

    @Override
    public Animation createDyingAnimation(GameContext context) {
        var spin = new RotateTransition(Duration.seconds(0.5), this);
        spin.setAxis(Rotate.X_AXIS); //TODO check this
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setRate(2);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(spin, pauseSec(2));
    }
}