/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.NumberBoxRisingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HideGhostShowPointsAnimation3D extends ManagedAnimation {

    public HideGhostShowPointsAnimation3D(Ghost3D ghost3D, NumberBox3D numberBox3D, double risingHeight) {
        super("Hide ghost and show points");

        setFactory(() -> {
            final var numberBoxRising = new NumberBoxRisingAnimation3D(numberBox3D, risingHeight).createAnimation();

            final var hideGhostShortly = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(ghost3D.visibleProperty(), false)),
                new KeyFrame(Duration.seconds(1), new KeyValue(ghost3D.visibleProperty(), true))
            );

            return new ParallelTransition(
                hideGhostShortly,
                numberBoxRising);
        });
    }
}
