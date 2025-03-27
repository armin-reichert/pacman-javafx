/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.level;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.model3D.Model3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.uilib.Ufx.now;
import static de.amr.games.pacman.uilib.Ufx.pauseSec;
import static de.amr.games.pacman.uilib.model3D.Model3D.meshViewById;

/**
 * @author Armin Reichert
 */
public class MsPacMan3D implements Pac3D {

    private final Pac msPacMan;
    private final PacShape3D shape3D;
    private RotateTransition hipSwayingAnimation;

    /**
     * Creates aButtonKey 3D Ms. Pac-Man.
     *
     * @param variant game variant
     * @param msPacMan Ms. Pac-Man instance
     * @param size diameter of Pac-Man
     * @param assets asset storage
     * @param assetNamespace prefix of asset keys (depends on current game variant)
     */
    public MsPacMan3D(GameVariant variant, Pac msPacMan, double size, AssetStorage assets, String assetNamespace) {
        Globals.assertNotNull(variant);
        this.msPacMan = Globals.assertNotNull(msPacMan);
        Globals.assertNotNull(assets);
        Globals.assertNotNull(assetNamespace);

        Model3D model3D = assets.get("model3D.pacman");

        shape3D = new PacShape3D(model3D, size,
            assets.color(assetNamespace + ".pac.color.head"),
            assets.color(assetNamespace + ".pac.color.palate"));

        Group body = PacModel3D.createPacShape(
            model3D, size,
            assets.color(assetNamespace + ".pac.color.head"),
            assets.color(assetNamespace + ".pac.color.eyes"),
            assets.color(assetNamespace + ".pac.color.palate"));

        meshViewById(body, PacModel3D.MESH_ID_EYES).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_HEAD).drawModeProperty().bind(shape3D.drawModeProperty());
        meshViewById(body, PacModel3D.MESH_ID_PALATE).drawModeProperty().bind(shape3D.drawModeProperty());

        Group femaleParts = PacModel3D.createFemaleParts(size,
            assets.color(assetNamespace + ".pac.color.hairbow"),
            assets.color(assetNamespace + ".pac.color.hairbow.pearls"),
            assets.color(assetNamespace + ".pac.color.boobs"));

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
    public void update() {
        THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
            if (msPacMan.isAlive()) {
                shape3D.updatePosition(msPacMan);
                shape3D.updateLight(msPacMan, level);
                shape3D.updateVisibility(msPacMan, level);
            }
            if (msPacMan.isAlive() && !msPacMan.isStandingStill()) {
                swayHips();
                shape3D.chew();
            } else {
                stopSwayingHips();
                shape3D.stopChewingAndOpenMouth();
            }
        });
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
        return new SequentialTransition(pauseSec(1), now(THE_UI.sound()::playPacDeathSound), spinning, pauseSec(1.5));
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