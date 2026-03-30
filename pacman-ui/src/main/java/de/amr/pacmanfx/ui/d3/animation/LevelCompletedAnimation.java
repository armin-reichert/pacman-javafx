/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.chance;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

/**
 * Full level‑completion animation including:
 * <ul>
 *   <li>ghosts hiding</li>
 *   <li>maze wall swinging</li>
 *   <li>Pac‑Man hiding</li>
 *   <li>maze spinning around a random axis</li>
 *   <li>house and walls disappearing</li>
 *   <li>sound effects</li>
 * </ul>
 * This is the long version used when a cutscene follows.
 */
public class LevelCompletedAnimation extends ManagedAnimation {

    private static final float SPINNING_SECONDS = 1.5f;

    /**
     * Creates an animation that briefly lowers and raises the maze wall base height,
     * producing a “swinging” or “bouncing” effect. Used during level completion.
     *
     * @param maze3D     the 3D maze whose walls are animated
     * @param numFlashes number of up/down cycles; if zero, a simple pause is returned
     * @return the animation
     */
    public static Animation createMazeWallsSwingingAnimation(Maze3D maze3D, int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        final var timeline = new Timeline(
            new KeyFrame(Duration.millis(0.5 * 250),
                new KeyValue(maze3D.wallBaseHeightProperty(), 0, Interpolator.EASE_BOTH)));
        timeline.setAutoReverse(true);
        timeline.setCycleCount(2 * numFlashes);
        return timeline;
    }

    private final GameLevel3D level3D;

    public LevelCompletedAnimation(GameLevel3D level3D) {
        super("Level Completed");
        this.level3D = requireNonNull(level3D);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        final GameLevel level = level3D.level();
        final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
        final Point3D rotationAxis = chance(0.5) ? Rotate.X_AXIS : Rotate.Z_AXIS;
        return new SequentialTransition(
            pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
            createMazeWallsSwingingAnimation(maze3D, level.numFlashes()),
            pauseSecThen(0.5, () -> level.pac().hide()),
            pauseSec(0.5),
            levelRotation(rotationAxis),
            pauseSecThen(0.5, () -> level3D.uiConfig().soundEffects().ifPresent(GameSoundEffects::playLevelCompleteSound)),
            pauseSec(0.5),
            mazeWallsAndHouseAnimation(maze3D),
            pauseSecThen(1.0, () -> level3D.uiConfig().soundEffects().ifPresent(GameSoundEffects::playLevelChangedSound))
        );
    }

    /**
     * Creates an animation that gradually lowers the house and maze walls until they disappear, then hides the maze.
     *
     * @param maze3D the maze whose walls and house are animated
     * @return the animation
     */
    private Animation mazeWallsAndHouseAnimation(Maze3D maze3D) {
        return new Timeline(
            new KeyFrame(Duration.seconds(0.5), new KeyValue(
                maze3D.house().wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(
                maze3D.wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.seconds(2.5), _ -> maze3D.setVisible(false))
        );
    }

    /**
     * Rotates the entire level around the given axis.
     *
     * @param axis rotation axis
     * @return the animation
     */
    private Animation levelRotation(Point3D axis) {
        final var rotation = new RotateTransition(Duration.seconds(SPINNING_SECONDS), level3D);
        rotation.setAxis(axis);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }
}
