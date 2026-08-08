/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.bonus.anim.NumberBoxRisingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.world.NumberBox3D;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HideGhost3DRiseNumberBoxAnimation extends ManagedAnimation {

    public HideGhost3DRiseNumberBoxAnimation(Ghost3DViewComp ghost3DView, NumberBox3D numberBox3D, double risingHeight) {
        super("Hide ghost and show points");

        setAnimationFactory(() -> {
            final var hideGhostAfterShortTime = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(ghost3DView.root().visibleProperty(), false)),
                new KeyFrame(Duration.seconds(1), new KeyValue(ghost3DView.root().visibleProperty(), true))
            );
            final var numberBoxRising = new NumberBoxRisingAnimation3D(numberBox3D, risingHeight).createAnimation();

            return new ParallelTransition(hideGhostAfterShortTime, numberBoxRising);
        });
    }
}
