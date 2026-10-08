/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.ui.gamescene.playscene.GameLevelView3D;
import de.amr.pacmanfx.ui.gamescene.playscene.WorldMapView3D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.basics.math.RandomNumbers.chance;
import static de.amr.basics.util.Ufx.pauseSec;
import static de.amr.basics.util.Ufx.pauseSecThen;
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

    static Animation createMazeWallsSwingingAnimation(WorldMapView3D maze3D, int numFlashes) {
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

    private final GameLevelView3D level3D;

    public LevelCompletedAnimation(GameLevelView3D level3D, int numFlashes, PacManGameSoundEffects soundEffects) {
        super("Level Completed");
        this.level3D = requireNonNull(level3D);
        setAnimationFactory(() -> {
            final GameLevelEntitySet entitySet = level3D.level().entitySet();
            final House house = entitySet.entities().theOne(House.class);
            final Point3D rotationAxis = chance(0.5) ? Rotate.X_AXIS : Rotate.Z_AXIS;
            return new SequentialTransition(
                pauseSecThen(0.5, () -> entitySet.ghosts().forEach(GameEntity::hide)),

                createMazeWallsSwingingAnimation(level3D.maze3D(), numFlashes),

                pauseSecThen(0.5, () -> entitySet.pac().hide()),

                pauseSec(0.5),

                levelRotation(rotationAxis),

                pauseSecThen(0.5, soundEffects::playLevelCompleteSound),

                mazeWallsAndHouseDisappearAnimation(
                    level3D,
                    house.assertComponent(House3DViewComp.class).wallBaseHeightProperty(),
                    level3D.maze3D().wallBaseHeightProperty()),

                pauseSecThen(1.0, soundEffects::playLevelChangedSound)
            );
        });
    }

    private Animation mazeWallsAndHouseDisappearAnimation(
        GameLevelView3D level3D,
        DoubleProperty mazeWallHeight,
        DoubleProperty houseWallHeight)
    {
        return new Timeline(
            new KeyFrame(Duration.seconds(0.5), new KeyValue(houseWallHeight, 0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(mazeWallHeight, 0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.seconds(2.5), _ -> level3D.root().setVisible(false))
        );
    }

    private Animation levelRotation(Point3D axis) {
        final var rotation = new RotateTransition(Duration.seconds(SPINNING_SECONDS), level3D.root());
        rotation.setAxis(axis);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return rotation;
    }
}
