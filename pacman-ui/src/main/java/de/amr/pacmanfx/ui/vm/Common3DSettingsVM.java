/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.ui.settings.ui.Common3DSettings;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.AttractionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.SwirlConfig;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

public class Common3DSettingsVM {

    public static final ParticlesAnimationConfig DEFAULT_PARTICLE_ANIMATION_CONFIG = new ParticlesAnimationConfig(
        new ExplosionConfig(
            new Vector3f(0, 0, 0.1f), // gravity
            500,        // num particles by explosion
            0.25f,      // mean particle radius
            0.1f, 0.4f, // min/max particle speed horizontally (xy-plane)
            1.5f, 6     // min/max particle speed horizontally (z-direction)
        ),
        new AttractionConfig(0.004f, 0.4f, 0.3f, 0.5f),
        new SwirlConfig(4, 20, 0.3f, 0.05f)
    );

    /* Whether 3D axes are visible in the 3D play scene. */
    public final BooleanProperty axesVisibleProperty;

    /* Currently active 3D camera perspective. */
    public final ObjectProperty<PerspectiveID> cameraPerspectiveIdProperty;

    /* Draw mode for 3D geometry (fill or wireframe). */
    public final ObjectProperty<DrawMode> drawModeProperty;

    /* Whether 3D rendering is enabled at all. */
    public final BooleanProperty view3DEnabledProperty;

    public Common3DSettingsVM() {
        axesVisibleProperty = new SimpleBooleanProperty();
        cameraPerspectiveIdProperty = new SimpleObjectProperty<>();
        drawModeProperty = new SimpleObjectProperty<>();
        view3DEnabledProperty = new SimpleBooleanProperty();
    }

    public void init(Common3DSettings settings) {
        axesVisibleProperty.set(settings.axesVisible());
        cameraPerspectiveIdProperty.set(settings.cameraPerspectiveId());
        drawModeProperty.set(settings.drawMode());
        view3DEnabledProperty.set(settings.view3DEnabled());
    }
}