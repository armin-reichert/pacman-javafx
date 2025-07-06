package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.doAfterSec;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

public class LevelCompletedAnimationBeforeCutScene extends ManagedAnimation {

    private GameLevel gameLevel;
    private GameLevel3D gameLevel3D;

    public LevelCompletedAnimationBeforeCutScene(AnimationManager animationManager, GameLevel3D gameLevel3D, GameLevel gameLevel) {
        super(animationManager, "Level_Complete_Before_CutScene");
        this.gameLevel3D = requireNonNull(gameLevel3D);
        this.gameLevel = requireNonNull(gameLevel);
    }

    @Override
    protected Animation createAnimation() {
        return new SequentialTransition(
                doAfterSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
                doAfterSec(0.5, createMazeFlashAnimation(gameLevel.data().numFlashes(), 250)),
                doAfterSec(0.5, () -> gameLevel.pac().hide())
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