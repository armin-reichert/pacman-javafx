/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

public class LevelCompletedAnimationShort extends ManagedAnimation {

    public static final int FLASH_DURATION_MILLIS = 250;

    private final GameLevel gameLevel;
    private GameLevel3D gameLevel3D;

    public LevelCompletedAnimationShort(AnimationManager animationManager, GameLevel3D gameLevel3D, GameLevel gameLevel) {
        super(animationManager, "Level_Complete_Short_Animation");
        this.gameLevel3D = requireNonNull(gameLevel3D);
        this.gameLevel = requireNonNull(gameLevel);
    }

    @Override
    protected Animation createAnimation() {
        return new SequentialTransition(
            pauseSec(0.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
            pauseSec(0.5),
            mazeFlashAnimation(gameLevel.data().numFlashes()),
            pauseSec(0.5, () -> gameLevel.pac().hide())
        );
    }

    @Override
    public void destroy() {
        animationManager.stopAnimation(this);
        animationManager.destroyAnimation(this);
        gameLevel3D = null;
    }

    private Animation mazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var flashing = new Timeline(
                new KeyFrame(Duration.millis(0.5 * FLASH_DURATION_MILLIS),
                        new KeyValue(gameLevel3D.obstacleBaseHeightProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
        flashing.setAutoReverse(true);
        flashing.setCycleCount(2 * numFlashes);
        return flashing;
    }
}