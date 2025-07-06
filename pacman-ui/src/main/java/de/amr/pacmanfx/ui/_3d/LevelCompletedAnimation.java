package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.theRNG;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.uilib.Ufx.*;
import static java.util.Objects.requireNonNull;

public class LevelCompletedAnimation extends ManagedAnimation {

    private GameLevel gameLevel;
    private GameLevel3D gameLevel3D;
    private ManagedAnimation wallsDisappearingAnimation;

    public LevelCompletedAnimation(AnimationManager animationManager, GameLevel3D gameLevel3D, GameLevel gameLevel) {
        super(animationManager, "Level_Complete");
        this.gameLevel3D = requireNonNull(gameLevel3D);
        this.gameLevel = requireNonNull(gameLevel);

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
        int levelNumber = gameLevel.number();
        int numMazeFlashes = gameLevel.data().numFlashes();
        boolean showFlashMessage = randomInt(1, 1000) < 250; // every 4th time also show a message
        return new SequentialTransition(
                now(() -> {
                    gameLevel3D.livesCounter3D().light().setLightOn(false);
                    if (showFlashMessage) {
                        theUI().showFlashMessageSec(3, theAssets().localizedLevelCompleteMessage(levelNumber));
                    }
                }),
                doAfterSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                doAfterSec(0.5, createMazeFlashAnimation(numMazeFlashes, 250)),
                doAfterSec(0.5, () -> gameLevel.pac().hide()),
                doAfterSec(0.5, () -> {
                    var spin360 = new RotateTransition(Duration.seconds(1.5), gameLevel3D);
                    spin360.setAxis(theRNG().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
                    spin360.setFromAngle(0);
                    spin360.setToAngle(360);
                    spin360.setInterpolator(Interpolator.LINEAR);
                    return spin360;
                }),
                doAfterSec(0.5, () -> theSound().playLevelCompleteSound()),
                doAfterSec(0.5, wallsDisappearingAnimation.getOrCreateAnimation()),
                doAfterSec(1.0, () -> theSound().playLevelChangedSound())
        );
    }

    @Override
    public void destroy() {
    }

    private Animation createMazeFlashAnimation(int numFlashes, int flashDurationMillis) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var flashing = new Timeline(
                new KeyFrame(Duration.millis(0.5 * flashDurationMillis),
                        new KeyValue(gameLevel3D.obstacleBaseHeightProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
        flashing.setAutoReverse(true);
        flashing.setCycleCount(2 * numFlashes);
        return flashing;
    }
}
