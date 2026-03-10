/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.chance;
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

    /**
     * Creates an animation that briefly lowers and raises the maze wall base height,
     * producing a “swinging” or “bouncing” effect. Used during level completion.
     *
     * @param maze3D     the 3D maze whose walls are animated
     * @param numFlashes number of up/down cycles; if zero, a simple pause is returned
     * @return the animation
     */
    private static Animation createMazeWallsSwingingAnimation(Maze3D maze3D, int numFlashes) {
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
    private static class LevelCompletedAnimation extends ManagedAnimation {

        private static final float SPINNING_SECONDS = 1.5f;

        private final GameLevel3D level3D;
        private final GamePlaySoundEffects soundEffects;

        public LevelCompletedAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D, GamePlaySoundEffects soundEffects) {
            super(animationRegistry, "Level_Completed");
            this.level3D = requireNonNull(level3D);
            this.soundEffects = requireNonNull(soundEffects);
            setFactory(() -> createAnimationFX(level3D.level(), level3D.maze3D()));
        }

        private Animation createAnimationFX(GameLevel level, Maze3D maze3D) {
            final Point3D rotationAxis = chance(0.5) ? Rotate.X_AXIS : Rotate.Z_AXIS;
            return new SequentialTransition(
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                createMazeWallsSwingingAnimation(maze3D, level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide()),
                pauseSec(0.5),
                levelRotation(rotationAxis),
                pauseSecThen(0.5, soundEffects::playLevelCompleteSound),
                pauseSec(0.5),
                mazeWallsAndHouseAnimation(maze3D),
                pauseSecThen(1.0, soundEffects::playLevelChangedSound)
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
    private static class LevelCompletedAnimationShort extends ManagedAnimation {

        private final GameLevel3D level3D;

        public LevelCompletedAnimationShort(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "Level_Complete_Short_Animation");
            this.level3D = level3D;
            setFactory(this::createAnimationFX);
        }

        private Animation createAnimationFX() {
            final GameLevel level = level3D.level();
            return new SequentialTransition(
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                createMazeWallsSwingingAnimation(level3D.maze3D(), level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide())
            );
        }
    }

    /**
     * Animation that continuously interpolates the maze wall color between
     * the fill and stroke colors of the current color scheme.
     * <p>
     * Used during energizer mode to create a flashing effect.
     * Automatically restores the original wall colors when stopped.
     */
    private static class WallColorFlashingAnimation extends ManagedAnimation {

        private final GameLevel3D level3D;
        private final Color fromColor;
        private final Color toColor;

        public WallColorFlashingAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "WallColorFlashing");
            this.level3D = level3D;
            this.fromColor = Color.valueOf(level3D.maze3D().colorScheme().wallFill());
            this.toColor = Color.valueOf(level3D.maze3D().colorScheme().wallStroke());
            setFactory(this::createAnimationFX);
        }

        private Animation createAnimationFX() {
            return new Transition() {
                {
                    setAutoReverse(true);
                    setCycleCount(Animation.INDEFINITE);
                    setCycleDuration(Duration.seconds(0.25));
                }

                @Override
                protected void interpolate(double t) {
                    Color color = fromColor.interpolate(toColor, t);
                    level3D.maze3D().materials().wallTop().setDiffuseColor(color);
                    level3D.maze3D().materials().wallTop().setSpecularColor(color.brighter());
                }
            };
        }

        @Override
        public void stop() {
            super.stop();
            // reset wall colors
            final Maze3D maze3D = level3D.maze3D();
            final Color wallFillColor = Color.valueOf(maze3D.colorScheme().wallFill());
            final PhongMaterial wallTopMaterial = maze3D.materials().wallTop();
            wallTopMaterial.setDiffuseColor(wallFillColor);
            wallTopMaterial.setSpecularColor(wallFillColor.brighter());
        }
    }

    /**
     * Animation that periodically transfers a point light to the ghost currently
     * hunting Pac‑Man. The light follows the ghost’s position and adopts its color.
     * <p>
     * If no ghost is hunting, the light is turned off.
     */
    public static class GhostLightAnimation extends ManagedAnimation {

        private final List<MutableGhost3D> ghosts3D;
        private final PointLight light;
        private byte currentGhostID;

        public GhostLightAnimation(AnimationRegistry animationRegistry, List<MutableGhost3D> ghosts3D) {
            super(animationRegistry, "GhostLight");
            this.ghosts3D = requireNonNull(ghosts3D);

            currentGhostID = RED_GHOST_SHADOW;

            light = new PointLight();
            light.setColor(Color.WHITE);
            light.setMaxRange(30);
            light.lightOnProperty().addListener((_, _, on) ->
                Logger.info("Ghost light {}", on ? "ON" : "OFF"));

            setFactory(this::createAnimationFX);
        }

        public PointLight light() {
            return light;
        }

        private static byte nextGhostID(byte id) {
            return (byte) ((id + 1) % 4);
        }

        /**
         * Moves the spotlight to the given ghost and updates its color.
         */
        private void illuminateGhost(byte ghostID) {
            final MutableGhost3D ghost3D = ghosts3D.get(ghostID);
            light.setColor(ghost3D.colorSet().normal().dress());
            light.translateXProperty().bind(ghost3D.translateXProperty());
            light.translateYProperty().bind(ghost3D.translateYProperty());
            light.setTranslateZ(-25);
            light.setLightOn(true);
            currentGhostID = ghostID;
            Logger.debug("Ghost light passed to ghost {}", currentGhostID);
        }

        private Animation createAnimationFX() {
            var timeline = new Timeline(new KeyFrame(Duration.millis(3000), _ -> {
                Logger.debug("Try to pass light from ghost {} to next", currentGhostID);
                byte id = nextGhostID(currentGhostID);
                while (id != currentGhostID) {
                    final Ghost ghost = ghosts3D.get(id).ghost();
                    if (ghost.state() == GhostState.HUNTING_PAC) {
                        illuminateGhost(id);
                        return;
                    }
                    id = nextGhostID(id);
                }
                light.setLightOn(false);
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            return timeline;
        }

        @Override
        public void playFromStart() {
            illuminateGhost(RED_GHOST_SHADOW);
            super.playFromStart();
        }

        @Override
        public void stop() {
            light.setLightOn(false);
            super.stop();
        }
    }

    private final WallColorFlashingAnimation wallColorFlashingAnimation;
    private final LevelCompletedAnimation levelCompletedFullAnimation;
    private final LevelCompletedAnimationShort levelCompletedShortAnimation;
    private final GhostLightAnimation ghostLightAnimation;

    /**
     * Creates all animations associated with the given 3D level.
     *
     * @param level3D the 3D level representation
     * @param soundEffects the playing sound effects
     */
    public GameLevel3DAnimations(GameLevel3D level3D, GamePlaySoundEffects soundEffects) {
        requireNonNull(level3D);
        requireNonNull(soundEffects);
        final AnimationRegistry animationRegistry = level3D.animationRegistry();
        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry, level3D);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry, level3D, soundEffects);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry, level3D);
        ghostLightAnimation = new GhostLightAnimation(animationRegistry, level3D.ghosts3D());
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
