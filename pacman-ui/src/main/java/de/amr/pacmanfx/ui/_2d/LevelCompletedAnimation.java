/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * Animation played when level is complete. Consists of the following steps:
 * <pre>
 *     Time (sec)         Action
 *     0.0                Start animation
 *     1.5                Hide ghosts
 *     2.0                Start n flashing cycles, each cycle takes 1/3 sec
 *     2.0 + n * 1/3 sec  Wait 1 sec
 * </pre>
 * After each flashing cycle, the flashing index is incremented. This is used by the Tengen play scene renderer to
 * draw a different map color for each flashing cycle (only for the non-ARCADE maps starting at level 28)
 */
public class LevelCompletedAnimation extends ManagedAnimation {

    private GameLevel gameLevel;
    private int singleFlashMillis;
    private int flashingIndex;
    private final BooleanProperty highlighted = new SimpleBooleanProperty(false);

    public LevelCompletedAnimation(AnimationRegistry animationRegistry) {
        super(animationRegistry, "Level_Completed");
        singleFlashMillis = 333;
    }

    @Override
    protected Animation createAnimationFX() {
        requireNonNull(gameLevel);
        int numFlashes = gameLevel.data().numFlashes();
        var flashingTimeline = new Timeline(
            new KeyFrame(Duration.millis(singleFlashMillis * 0.25), e -> highlighted.set(true)),
            new KeyFrame(Duration.millis(singleFlashMillis * 0.75), e -> highlighted.set(false)),
            new KeyFrame(Duration.millis(singleFlashMillis), e -> {
                if (flashingIndex + 1 < numFlashes) flashingIndex++;
            })
        );
        flashingTimeline.setCycleCount(numFlashes);
        return new SequentialTransition(
            pauseSec(1.5, () -> gameLevel.ghosts().forEach(Ghost::hide)),
            pauseSec(0.5),
            numFlashes > 0 ? flashingTimeline : pauseSec(0),
            pauseSec(1)
        );
    }

    public BooleanProperty highlightedProperty() {
        return highlighted;
    }

    @Override
    public void playFromStart() {
        flashingIndex = 0;
        super.playFromStart();
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
    }

    public void setSingleFlashMillis(int singleFlashMillis) {
        this.singleFlashMillis = singleFlashMillis;
    }

    public int flashingIndex() {
        return flashingIndex;
    }
}