/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.Maze3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Animation that continuously interpolates the maze wall color between
 * the fill and stroke colors of the current color scheme.
 * <p>
 * Used during energizer mode to create a flashing effect.
 * Automatically restores the original wall colors when stopped.
 */
public class WallColorFlashingAnimation extends ManagedAnimation {

    private final GameLevel3D level3D;
    private final WorldMapColorScheme colorScheme;
    private final Color fromColor;
    private final Color toColor;

    public WallColorFlashingAnimation(AnimationRegistry animationRegistry, GameLevel3D level3D, WorldMapColorScheme colorScheme) {
        super(animationRegistry, "WallColorFlashing");
        this.level3D = requireNonNull(level3D);
        this.colorScheme = requireNonNull(colorScheme);
        this.fromColor = Color.valueOf(colorScheme.wallFill());
        this.toColor = Color.valueOf(colorScheme.wallStroke());
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
        final Color wallFillColor = Color.valueOf(colorScheme.wallFill());
        final PhongMaterial wallTopMaterial = maze3D.materials().wallTop();
        wallTopMaterial.setDiffuseColor(wallFillColor);
        wallTopMaterial.setSpecularColor(wallFillColor.brighter());
    }
}
