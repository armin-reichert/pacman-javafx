/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;

public class GameLevel3DAnimations implements Disposable {

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

    private static class LevelCompletedAnimation extends RegisteredAnimation {

        private static final float SPINNING_SECONDS = 1.5f;

        private final GameLevel3D level3D;

        public LevelCompletedAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "Level_Completed");
            this.level3D = level3D;
        }

        @Override
        protected Animation createAnimationFX() {
            final GameLevel level = level3D.level();
            return new SequentialTransition(
                //doNow(() -> sometimesLevelCompleteMessage(level.number())),
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                createMazeWallsSwingingAnimation(level3D.maze3D(), level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide()),
                pauseSec(0.5),
                levelSpinningAroundAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                pauseSecThen(0.5, () -> level3D.soundManager().play(SoundID.LEVEL_COMPLETE)),
                pauseSec(0.5),
                wallsAndHouseDisappearing(),
                pauseSecThen(1.0, () -> level3D.soundManager().play(SoundID.LEVEL_CHANGED))
            );
        }

        private Animation wallsAndHouseDisappearing() {
            return new Timeline(
                new KeyFrame(Duration.seconds(0.5),
                    new KeyValue(level3D.maze3D().house().wallBaseHeightProperty(),
                        0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(1.5),
                    new KeyValue(level3D.maze3D().wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(2.5),
                    _ -> level3D.maze3D().setVisible(false))
            );
        }

        private Animation levelSpinningAroundAxis(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), level3D);
            spin360.setAxis(axis);
            spin360.setFromAngle(0);
            spin360.setToAngle(360);
            spin360.setInterpolator(Interpolator.LINEAR);
            return spin360;
        }
    }

    private static class LevelCompletedAnimationShort extends RegisteredAnimation {

        private final GameLevel3D level3D;

        public LevelCompletedAnimationShort(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "Level_Complete_Short_Animation");
            this.level3D = level3D;
        }

        @Override
        protected Animation createAnimationFX() {
            final GameLevel level = level3D.level();
            return new SequentialTransition(
                pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                pauseSec(0.5),
                createMazeWallsSwingingAnimation(level3D.maze3D(), level.numFlashes()),
                pauseSecThen(0.5, () -> level.pac().hide())
            );
        }
    }

    private static class WallColorFlashingAnimation extends RegisteredAnimation {

        private final GameLevel3D level3D;
        private final Color fromColor;
        private final Color toColor;

        public WallColorFlashingAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "WallColorFlashing");
            this.level3D = level3D;
            this.fromColor = Color.valueOf(level3D.maze3D().colorScheme().wallFill());
            this.toColor = Color.valueOf(level3D.maze3D().colorScheme().wallStroke());
        }

        @Override
        protected Animation createAnimationFX() {
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
     * A light animation that switches from ghost to ghost (JavaFX can only display a limited amount of lights per scene).
     */
    private static class GhostLightAnimation extends RegisteredAnimation {

        private final GameLevel3D level3D;
        private byte currentGhostID;

        public GhostLightAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D) {
            super(animationRegistry, "GhostLight");
            this.level3D = level3D;

            currentGhostID = RED_GHOST_SHADOW;
            level3D.ghostLight().setColor(Color.WHITE);
            level3D.ghostLight().setMaxRange(30);
            level3D.ghostLight().lightOnProperty().addListener((_, _, on) -> Logger.info("Ghost light {}", on ? "ON" : "OFF"));
        }

        private static byte nextGhostID(byte id) {
            return (byte) ((id + 1) % 4);
        }

        private void illuminateGhost(byte ghostID) {
            MutableGhost3D ghost3D = level3D.ghosts3D().get(ghostID);
            level3D.ghostLight().setColor(ghost3D.colorSet().normal().dress());
            level3D.ghostLight().translateXProperty().bind(ghost3D.translateXProperty());
            level3D.ghostLight().translateYProperty().bind(ghost3D.translateYProperty());
            level3D.ghostLight().setTranslateZ(-25);
            level3D.ghostLight().setLightOn(true);
            currentGhostID = ghostID;
            Logger.debug("Ghost light passed to ghost {}", currentGhostID);
        }

        @Override
        protected Animation createAnimationFX() {
            var timeline = new Timeline(new KeyFrame(Duration.millis(3000), _ -> {
                Logger.debug("Try to pass light from ghost {} to next", currentGhostID);
                // find the next hunting ghost, if exists, pass light to him
                byte candidate = nextGhostID(currentGhostID);
                while (candidate != currentGhostID) {
                    if (level3D.level().ghost(candidate).state() == GhostState.HUNTING_PAC) {
                        illuminateGhost(candidate);
                        return;
                    }
                    candidate = nextGhostID(candidate);
                }
                level3D.ghostLight().setLightOn(false);
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
            level3D.ghostLight().setLightOn(false);
            super.stop();
        }
    }

    private WallColorFlashingAnimation wallColorFlashingAnimation;
    private LevelCompletedAnimation levelCompletedFullAnimation;
    private LevelCompletedAnimationShort levelCompletedShortAnimation;
    private GhostLightAnimation ghostLightAnimation;

    public GameLevel3DAnimations(GameLevel3D level3D) {
        final AnimationRegistry animationRegistry = level3D.animationRegistry();
        wallColorFlashingAnimation = new WallColorFlashingAnimation(animationRegistry, level3D);
        levelCompletedFullAnimation = new LevelCompletedAnimation(animationRegistry, level3D);
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(animationRegistry, level3D);
        ghostLightAnimation = new GhostLightAnimation(animationRegistry, level3D);
    }

    @Override
    public void dispose() {
        if (wallColorFlashingAnimation != null) {
            wallColorFlashingAnimation.dispose();
            wallColorFlashingAnimation = null;
        }
        if (levelCompletedFullAnimation != null) {
            levelCompletedFullAnimation.dispose();
            levelCompletedFullAnimation = null;
        }
        if (levelCompletedShortAnimation != null) {
            levelCompletedShortAnimation.dispose();
            levelCompletedShortAnimation = null;
        }
        if (ghostLightAnimation != null) {
            ghostLightAnimation.dispose();
            ghostLightAnimation = null;
        }
    }

    public void playGhostLightAnimation() {
        ghostLightAnimation.playFromStart();
    }

    public void stopGhostLightAnimation() {
        ghostLightAnimation.stop();
    }

    public void playWallColorFlashing() {
        wallColorFlashingAnimation.playFromStart();
    }

    public void stopWallColorFlashing() {
        wallColorFlashingAnimation.stop();
    }

    public RegisteredAnimation getLevelCompleteAnimation(boolean cutSceneFollows) {
        return cutSceneFollows ? levelCompletedShortAnimation : levelCompletedFullAnimation;
    }
}
