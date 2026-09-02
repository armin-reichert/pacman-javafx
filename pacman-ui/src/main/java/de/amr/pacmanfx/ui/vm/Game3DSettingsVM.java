/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.vm;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.AttractionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.SwirlConfig;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.settings.ui.Game3DSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

public class Game3DSettingsVM {

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
    private final BooleanProperty axesVisible;

    /* Currently active 3D camera perspective. */
    private final ObjectProperty<PerspectiveID> cameraPerspectiveID;

    /* Draw mode for 3D geometry (fill or wireframe). */
    private final ObjectProperty<DrawMode> drawMode;

    /* Whether 3D rendering is enabled at all. */
    private final BooleanProperty view3DEnabled;

    public Game3DSettingsVM() {
        axesVisible = new SimpleBooleanProperty();
        cameraPerspectiveID = new SimpleObjectProperty<>();
        drawMode = new SimpleObjectProperty<>();
        view3DEnabled = new SimpleBooleanProperty();
    }

    public void init(Game3DSettings settings) {
        axesVisible.set(settings.axesVisible());
        cameraPerspectiveID.set(settings.cameraPerspectiveId());
        drawMode.set(settings.drawMode());
        view3DEnabled.set(settings.view3DEnabled());
    }

    public BooleanProperty axesVisibleProperty() {
        return axesVisible;
    }

    public ObjectProperty<PerspectiveID> cameraPerspectiveIDProperty() {
        return cameraPerspectiveID;
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

    public BooleanProperty view3DEnabledProperty() {
        return view3DEnabled;
    }
}