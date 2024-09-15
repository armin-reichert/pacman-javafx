/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.util.Ufx.now;
import static de.amr.games.pacman.ui2d.util.Ufx.pauseSec;
import static de.amr.games.pacman.ui3d.model.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D implements Pac3D {

    private final Pac msPacMan;
    private final PacShape3D shape3D;
    private RotateTransition hipSwayingAnimation;

    /**
     * Creates a 3D Ms. Pac-Man.
     *
     * @param msPacMan Ms. Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset map
     */
    public MsPacMan3D(Pac msPacMan, double size, AssetStorage assets) {
        this.msPacMan = checkNotNull(msPacMan);
        checkNotNull(assets);

        Model3D model3D = assets.get("model3D.pacman");

        shape3D = new PacShape3D(model3D, size,
            assets.color("ms_pacman.color.head"),
            assets.color("ms_pacman.color.palate"));

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color("ms_pacman.color.head"),
            assets.color("ms_pacman.color.eyes"),
            assets.color("ms_pacman.color.palate"));

        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(shape3D.drawModeProperty());

        Group femaleParts = PacModel3D.createFemaleParts(size,
            assets.color("ms_pacman.color.hairbow"),
            assets.color("ms_pacman.color.hairbow.pearls"),
            assets.color("ms_pacman.color.boobs"));

        shape3D.getChildren().addAll(body, femaleParts);
        createHipSwayingAnimation(shape3D);
    }

    @Override
    public PacShape3D shape3D() {
        return shape3D;
    }

    @Override
    public void init() {
        shape3D.init(msPacMan);
        stopSwayingHips();
        setWinnetouchMode(false);
    }

    @Override
    public void update(GameContext context) {
        if (msPacMan.isAlive()) {
            shape3D.updatePosition(msPacMan);
            shape3D.updateLight(msPacMan, context.game());
            shape3D.updateVisibility(msPacMan, context.game().world());
        }
        if (msPacMan.isAlive() && !msPacMan.isStandingStill()) {
            swayHips();
            shape3D.chew();
        } else {
            stopSwayingHips();
            shape3D.stopChewingAndOpenMouth();
        }
    }

    @Override
    public void setPowerMode(boolean on) {
        setWinnetouchMode(on);
    }

    @Override
    public Animation createDyingAnimation() {
        //TODO use Timeline?
        var spinning = new RotateTransition(Duration.seconds(0.25), shape3D);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setFromAngle(0);
        spinning.setToAngle(360);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.setCycleCount(4);
        return new SequentialTransition(pauseSec(1), now(GameSounds::playPacDeathSound), spinning, pauseSec(1.5));
    }

    // Hip swaying animation

    static final short ANGLE_FROM = -20;
    static final short ANGLE_TO = 20;
    static final Duration DURATION = Duration.seconds(0.4);

    private void createHipSwayingAnimation(Node target) {
        hipSwayingAnimation = new RotateTransition(DURATION, target);
        hipSwayingAnimation.setAxis(Rotate.Z_AXIS);
        hipSwayingAnimation.setCycleCount(Animation.INDEFINITE);
        hipSwayingAnimation.setAutoReverse(true);
        hipSwayingAnimation.setInterpolator(Interpolator.EASE_BOTH);
        setWinnetouchMode(false);
    }

    // Note: Winnetouch is the gay twin-brother of Abahachi
    private void setWinnetouchMode(boolean on) {
        double amplification = on ? 1.5 : 1;
        double rate = on ? 2 : 1;
        hipSwayingAnimation.stop();
        hipSwayingAnimation.setFromAngle(ANGLE_FROM * amplification);
        hipSwayingAnimation.setToAngle(ANGLE_TO * amplification);
        hipSwayingAnimation.setRate(rate);
    }

    private void swayHips() {
        hipSwayingAnimation.play();
    }

    private void stopSwayingHips() {
        hipSwayingAnimation.stop();
        hipSwayingAnimation.getNode().setRotationAxis(hipSwayingAnimation.getAxis());
        hipSwayingAnimation.getNode().setRotate(0);
    }
}