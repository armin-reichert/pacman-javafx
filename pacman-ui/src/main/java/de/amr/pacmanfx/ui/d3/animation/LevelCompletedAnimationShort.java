/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;

import static de.amr.pacmanfx.ui.d3.animation.LevelCompletedAnimation.createMazeWallsSwingingAnimation;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;

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

    public LevelCompletedAnimationShort(GameLevel3D level3D) {
        super("Level Completed (Short Animation)");
        this.level3D = level3D;
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
        final GameLevel level = level3D.level();
        return new SequentialTransition(
            pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
            pauseSec(0.5),
            createMazeWallsSwingingAnimation(maze3D, level.numFlashes()),
            pauseSecThen(0.5, () -> level.pac().hide())
        );
    }
}
