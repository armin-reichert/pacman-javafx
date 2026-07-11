/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.animation;

import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * Animation that continuously interpolates the maze wall color between
 * the fill and stroke colors of the current color scheme.
 * <p>
 * Used during energizer mode to create a flashing effect.
 * Automatically restores the original wall colors when stopped.
 */
public class WallColorFlashingAnimation extends ManagedAnimation {

    private final Color fromColor;
    private final Color toColor;

    public WallColorFlashingAnimation(WorldMapColorScheme colorScheme, PhongMaterial wallMaterial) {
        super("Wall Color Flashing");
        this.fromColor = Color.valueOf(colorScheme.wallFill());
        this.toColor = Color.valueOf(colorScheme.wallStroke());
        setFactory(() -> new Transition() {
            {
                setAutoReverse(true);
                setCycleCount(Animation.INDEFINITE);
                setCycleDuration(Duration.seconds(0.25));
            }

            @Override
            protected void interpolate(double t) {
                final Color color = fromColor.interpolate(toColor, t);
                wallMaterial.setDiffuseColor(color);
                wallMaterial.setSpecularColor(color.brighter());
            }

            @Override
            public void stop() {
                super.stop();
                // reset wall colors
                final Color wallFillColor = Color.valueOf(colorScheme.wallFill());
                wallMaterial.setDiffuseColor(wallFillColor);
                wallMaterial.setSpecularColor(wallFillColor.brighter());
            }
        });
    }
}
