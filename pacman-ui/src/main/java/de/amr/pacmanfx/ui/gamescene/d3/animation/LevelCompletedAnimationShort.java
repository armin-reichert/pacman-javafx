/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.d3.GameLevel3D;
import de.amr.pacmanfx.ui.gamescene.d3.Maze3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;

import static de.amr.basics.util.Ufx.pauseSec;
import static de.amr.basics.util.Ufx.pauseSecThen;
import static de.amr.pacmanfx.ui.gamescene.d3.animation.LevelCompletedAnimation.createMazeWallsSwingingAnimation;

/**
 * Shortened version of the level‑completion animation.
 * <p>
 * Used when no cutscene follows. Contains only:
 * <ul>
 *   <li>ghosts hiding</li>
 *   <li>maze wall swinging</li>
 *   <li>Pac‑Man hiding</li>
 * </ul>
 */
public class LevelCompletedAnimationShort extends ManagedAnimation {

    private final GameLevel3D level3D;

    public LevelCompletedAnimationShort(GameLevel3D level3D, int numFlashes) {
        super("Level Completed (Short Animation)");
        this.level3D = level3D;
        setAnimationFactory(() -> createAnimationFX(numFlashes));
    }

    private Animation createAnimationFX(int numFlashes) {
        final Maze3D maze3D = level3D.maze3D();
        final GameLevel level = level3D.level();
        return new SequentialTransition(
            pauseSecThen(0.5, () -> level.entities().ghosts().forEach(GameEntity::hide)),
            pauseSec(0.5),
            createMazeWallsSwingingAnimation(maze3D, numFlashes),
            pauseSecThen(0.5, () -> level.entities().pac().hide())
        );
    }
}
