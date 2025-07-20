/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;

import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.uilib.Ufx.doNow;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

public class LevelCompletedAnimation extends ManagedAnimation {

    public static final int FLASH_DURATION_MILLIS = 250;

    private final GameUI ui;
    private GameLevel3D gameLevel3D;

    private ManagedAnimation wallsDisappearingAnimation;

    public LevelCompletedAnimation(GameUI ui, AnimationManager animationManager, GameLevel3D gameLevel3D) {
        super(animationManager, "Level_Complete");
        this.ui = requireNonNull(ui);
        this.gameLevel3D = requireNonNull(gameLevel3D);

        wallsDisappearingAnimation = new ManagedAnimation(animationManager, "Maze_WallsDisappearing") {
            @Override
            protected Animation createAnimation() {
                var totalDuration = Duration.seconds(1);
                var houseDisappears = new Timeline(
                        new KeyFrame(totalDuration.multiply(0.33),
                                new KeyValue(gameLevel3D.houseBaseHeightProperty(), 0, Interpolator.EASE_IN)));
                var obstaclesDisappear = new Timeline(
                        new KeyFrame(totalDuration.multiply(0.33),
                                new KeyValue(gameLevel3D.obstacleBaseHeightProperty(), 0, Interpolator.EASE_IN)));
                var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
                animation.setOnFinished(e -> gameLevel3D.maze3D().setVisible(false));
                return animation;
            }
        };
    }

    @Override
    protected Animation createAnimation() {
        if (ui.theGameContext().optGameLevel().isPresent()) {
            GameLevel gameLevel = ui.theGameContext().theGameLevel();
            return new SequentialTransition(
                    doNow(() -> {
                        gameLevel3D.livesCounter3D().light().setLightOn(false);
                        sometimesShowLevelCompleteFlashMessage(gameLevel.number());
                    }),
                    pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                    pauseSec(0.5),
                    createWallsFlashAnimation(),
                    pauseSec(0.5, () -> gameLevel.pac().hide()),
                    pauseSec(0.5),
                    createSpinningAnimation(),
                    pauseSec(0.5, () -> ui.theSound().play(SoundID.LEVEL_COMPLETE)),
                    pauseSec(0.5),
                    wallsDisappearingAnimation.getOrCreateAnimation(),
                    pauseSec(1.0, () -> ui.theSound().play(SoundID.LEVEL_CHANGED))
            );
        } else {
            Logger.error("Could not create animation '{}', no game level exists!", label);
            return null;
        }
    }

    @Override
    public void destroy() {
        gameLevel3D = null;
        if (wallsDisappearingAnimation != null) {
            animationManager.destroyAnimation(wallsDisappearingAnimation);
            wallsDisappearingAnimation = null;
        }
    }

    private void sometimesShowLevelCompleteFlashMessage(int levelNumber) {
        boolean showFlashMessage = randomInt(1, 1000) < 250; // every 4th time also show a message
        if (showFlashMessage) {
            String message = ui.theAssets().localizedLevelCompleteMessage(levelNumber);
            ui.showFlashMessageSec(3, message);
        }
    }

    private Animation createSpinningAnimation() {
        var spin360 = new RotateTransition(Duration.seconds(1.5), gameLevel3D.root());
        spin360.setAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        spin360.setFromAngle(0);
        spin360.setToAngle(360);
        spin360.setInterpolator(Interpolator.LINEAR);
        return spin360;
    }

    private Animation createWallsFlashAnimation() {
        if (ui.theGameContext().optGameLevel().isPresent()) {
            int numFlashes = ui.theGameContext().theGameLevel().data().numFlashes();
            if (numFlashes == 0) {
                return pauseSec(1.0);
            }
            var flashingTimeline = new Timeline(
                    new KeyFrame(Duration.millis(0.5 * FLASH_DURATION_MILLIS),
                            new KeyValue(gameLevel3D.obstacleBaseHeightProperty(), 0, Interpolator.EASE_BOTH)
                    )
            );
            flashingTimeline.setAutoReverse(true);
            flashingTimeline.setCycleCount(2 * numFlashes);
            return flashingTimeline;
        } else {
            Logger.error("Could not create animation '{}', no game level exists!", label);
            return null;
        }
    }
}
