/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.NumberBox3DRisingAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class HideGhostShowPointsAnimation extends ManagedAnimation {

    public HideGhostShowPointsAnimation(GameLevel3D gameLevel3D, byte killedGhostPersonality, int killIndex) {
        super("Hide ghost and show points");

        setFactory(() -> {
            final Ghost3D ghost3D = gameLevel3D.ghost3D(killedGhostPersonality).orElseThrow();
            final Image pointsImage = gameLevel3D.uiConfig().killedGhostPointsImage(killIndex);

            final NumberBox3D numberBox3D = new NumberBox3D(pointsImage);
            numberBox3D.setTranslateX(ghost3D.getTranslateX());
            numberBox3D.setTranslateY(ghost3D.getTranslateY());
            numberBox3D.setTranslateZ(ghost3D.getTranslateZ());

            gameLevel3D.entities().add(numberBox3D);
            gameLevel3D.getChildren().add(numberBox3D);

            //TODO Wrap into ManagedAnimation

            final Animation numberBoxRising = new NumberBox3DRisingAnimation(numberBox3D, (killIndex + 1) * 12).createAnimation();

            numberBoxRising.setOnFinished(_ -> {
                gameLevel3D.entities().remove(numberBox3D);
                gameLevel3D.getChildren().remove(numberBox3D);
                //TODO why do I get "duplicate children added" exceptions?
                if (!gameLevel3D.getChildren().contains(ghost3D)) {
                    gameLevel3D.getChildren().add(ghost3D);
                }
            });

            final Animation hideGhost3DForOneSecond = new Timeline(
                new KeyFrame(Duration.ZERO, _ -> gameLevel3D.getChildren().remove(ghost3D)),
                new KeyFrame(Duration.seconds(1), _ -> gameLevel3D.getChildren().add(ghost3D))
            );

            return new ParallelTransition(hideGhost3DForOneSecond, numberBoxRising);
        });
    }
}
