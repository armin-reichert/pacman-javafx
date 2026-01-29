/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Random;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSecThen;

public class GameLevel3DAnimations implements Disposable {

    private final GameUI ui;
    private final GameLevel3D level3D;

    private WallColorFlashingAnimation wallColorFlashingAnimation;
    private LevelCompletedAnimation levelCompletedFullAnimation;
    private LevelCompletedAnimationShort levelCompletedShortAnimation;
    private GhostLightAnimation ghostLightAnimation;

    private PointLight ghostLight;

    private Animation wallsSwinging(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var timeline = new Timeline(
                new KeyFrame(Duration.millis(0.5 * 250),
                        new KeyValue(level3D.wallBaseHeightProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(2 * numFlashes);
        return timeline;
    }

    private class LevelCompletedAnimation extends RegisteredAnimation {
        private static final int MESSAGE_FREQUENCY = 20; // 20% of cases
        private static final float SPINNING_SECONDS = 1.5f;

        public LevelCompletedAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Completed");
        }

        @Override
        protected Animation createAnimationFX() {
            final GameLevel level = level3D.level();
            return new SequentialTransition(
                    //doNow(() -> sometimesLevelCompleteMessage(level.number())),
                    pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                    wallsSwinging(level.numFlashes()),
                    pauseSecThen(0.5, () -> level.pac().hide()),
                    pauseSec(0.5),
                    levelSpinningAroundAxis(new Random().nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS),
                    pauseSecThen(0.5, () -> ui.soundManager().play(SoundID.LEVEL_COMPLETE)),
                    pauseSec(0.5),
                    wallsAndHouseDisappearing(),
                    pauseSecThen(1.0, () -> ui.soundManager().play(SoundID.LEVEL_CHANGED))
            );
        }

        private Animation wallsAndHouseDisappearing() {
            return new Timeline(
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(level3D.house3D().wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(1.5), new KeyValue(level3D.wallBaseHeightProperty(), 0, Interpolator.EASE_IN)),
                    new KeyFrame(Duration.seconds(2.5), _ -> level3D.maze3D().setVisible(false))
            );
        }

        /*
        private void sometimesLevelCompleteMessage(int levelNumber) {
            if (randomInt(0, 100) < MESSAGE_FREQUENCY) {
                String message = translatedLevelCompleteMessage(localizedTextsAccessor, levelNumber);
                ui.showFlashMessage(Duration.seconds(3), message);
            }
        }
         */

        private Animation levelSpinningAroundAxis(Point3D axis) {
            var spin360 = new RotateTransition(Duration.seconds(SPINNING_SECONDS), level3D);
            spin360.setAxis(axis);
            spin360.setFromAngle(0);
            spin360.setToAngle(360);
            spin360.setInterpolator(Interpolator.LINEAR);
            return spin360;
        }
    }

    private class LevelCompletedAnimationShort extends RegisteredAnimation {

        public LevelCompletedAnimationShort(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Level_Complete_Short_Animation");
        }

        @Override
        protected Animation createAnimationFX() {
            final GameLevel level = level3D.level();
            return new SequentialTransition(
                    pauseSecThen(0.5, () -> level.ghosts().forEach(Ghost::hide)),
                    pauseSec(0.5),
                    wallsSwinging(level.numFlashes()),
                    pauseSecThen(0.5, () -> level.pac().hide())
            );
        }
    }

    private class WallColorFlashingAnimation extends RegisteredAnimation {

        private final Color fromColor = Color.valueOf(level3D.colorScheme().wallFill());
        private final Color toColor = Color.valueOf(level3D.colorScheme().wallStroke());

        public WallColorFlashingAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "WallColorFlashing");
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
                    level3D.wallTopMaterial().setDiffuseColor(color);
                    level3D.wallTopMaterial().setSpecularColor(color.brighter());
                }
            };
        }

        @Override
        public void stop() {
            super.stop();
            // reset wall colors
            level3D.wallTopMaterial().setDiffuseColor(Color.valueOf(level3D.colorScheme().wallFill()));
            level3D.wallTopMaterial().setSpecularColor(Color.valueOf(level3D.colorScheme().wallFill()).brighter());
        }
    }

    /**
     * A light animation that switches from ghost to ghost (JavaFX can only display a limited amount of lights per scene).
     */
    private class GhostLightAnimation extends RegisteredAnimation {

        private byte currentGhostID;

        public GhostLightAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "GhostLight");
            currentGhostID = RED_GHOST_SHADOW;
            ghostLight = new PointLight(Color.WHITE);
            ghostLight.setMaxRange(30);
            ghostLight.lightOnProperty().addListener((_, _, on) -> Logger.info("Ghost light {}", on ? "ON" : "OFF"));
        }

        private static byte nextGhostID(byte id) {
            return (byte) ((id + 1) % 4);
        }

        private void illuminateGhost(byte ghostID) {
            MutableGhost3D ghost3D = level3D.ghosts3D().get(ghostID);
            ghostLight.setColor(ghost3D.colorSet().normal().dress());
            ghostLight.translateXProperty().bind(ghost3D.translateXProperty());
            ghostLight.translateYProperty().bind(ghost3D.translateYProperty());
            ghostLight.setTranslateZ(-25);
            ghostLight.setLightOn(true);
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
                ghostLight.setLightOn(false);
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
            ghostLight.setLightOn(false);
            super.stop();
        }
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

    public GameLevel3DAnimations(GameUI ui, GameLevel3D level3D) {
        this.ui = ui;
        this.level3D = level3D;
        wallColorFlashingAnimation = new WallColorFlashingAnimation(level3D.animationRegistry());
        levelCompletedFullAnimation = new LevelCompletedAnimation(level3D.animationRegistry());
        levelCompletedShortAnimation = new LevelCompletedAnimationShort(level3D.animationRegistry());
        ghostLightAnimation = new GhostLightAnimation(level3D.animationRegistry());
    }

    public PointLight ghostLight() {
        return ghostLight;
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
