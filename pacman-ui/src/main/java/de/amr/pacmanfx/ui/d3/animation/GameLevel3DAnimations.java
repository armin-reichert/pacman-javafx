/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.Maze3D;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.SequentialTransition;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_WALL_HEIGHT;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;
import static java.util.Objects.requireNonNull;

/**
 * Container for all 3D animations used during a {@link GameLevel} lifecycle.
 * <p>
 * This class instantiates and manages:
 * <ul>
 *   <li>wall flashing animations</li>
 *   <li>maze wall swinging effects</li>
 *   <li>short and full level‑completion sequences</li>
 *   <li>a ghost spotlight animation</li>
 * </ul>
 * All animations are registered in the {@link AnimationRegistry} of the owning {@link GameLevel3D}.
 */
public class GameLevel3DAnimations {

    private final WallColorFlashingAnimation wallColorFlashingAnimation;
    private final LevelCompletedAnimation levelCompletedFullAnimation;
    private final LevelCompletedAnimationShort levelCompletedShortAnimation;
    private final GhostLightAnimation ghostLightAnimation;

    /**
     * Creates all animations associated with the given 3D level.
     *
     * @param level3D the 3D level representation
     * @param colorScheme the used map color scheme
     * @param soundEffects the playing sound effects
     */
    public GameLevel3DAnimations(GameLevel3D level3D, WorldMapColorScheme colorScheme, GamePlaySoundEffects soundEffects) {
        requireNonNull(level3D);
        requireNonNull(soundEffects);
        final AnimationRegistry animationRegistry = level3D.animationRegistry();
        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry, level3D, colorScheme);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry, level3D, soundEffects);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry, level3D);
        ghostLightAnimation = new GhostLightAnimation(animationRegistry, level3D.ghostAppearances3D());
    }

    /** @return the ghost‑spotlight animation */
    public GhostLightAnimation ghostLightAnimation() {
        return ghostLightAnimation;
    }

    /** @return the wall‑color flashing animation */
    public ManagedAnimation wallColorFlashingAnimation() {
        return wallColorFlashingAnimation;
    }

    /**
     * Plays the level completion animation sequence and resets game timer.
     *
     * @param maze3D the 3D maze to be animated
     * @param level the completed level (used to determine animation details)
     * @param state the current game state (used to determine cut-scene follow-up)
     */
    public void playLevelEndAnimation(Maze3D maze3D, GameLevel level, State<Game> state) {
        final boolean cutSceneFollows = level.cutSceneNumber() != 0;
        final PerspectiveID perspectiveBeforeAnimation = GameUI.PROPERTY_3D_PERSPECTIVE_ID.get();

        final var seq = new SequentialTransition(
            pauseSecThen(2, () -> {
                GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
                maze3D.wallBaseHeightProperty().unbind();
            }),
            (cutSceneFollows ? levelCompletedShortAnimation : levelCompletedFullAnimation).animationFX(),
            pauseSec(0.25)
        );

        seq.setOnFinished(_ -> {
            GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(perspectiveBeforeAnimation);
            maze3D.wallBaseHeightProperty().bind(PROPERTY_3D_WALL_HEIGHT);
            state.timer().expire();
        });

        state.timer().resetIndefiniteTime(); // freeze game control until animation ends
        seq.play();
    }
}
