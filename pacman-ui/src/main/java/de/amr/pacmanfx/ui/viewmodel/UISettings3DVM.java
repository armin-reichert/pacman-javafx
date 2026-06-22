package de.amr.pacmanfx.ui.viewmodel;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.AttractionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.gamescene.d3.animation.energizer.SwirlConfig;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public class UISettings3DVM {

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

    /* Floor color used in 3D mode. */
    public final ObjectProperty<Color> mazeFloorColorProperty;

    /* Light color used in 3D mode. */
    public final ObjectProperty<Color> mazeLightColorProperty;

    /* Height of 3D walls (in world units). */
    public final DoubleProperty mazeWallHeightProperty;

    /* Opacity of 3D walls (0.0–1.0). */
    public final DoubleProperty mazeWallOpacityProperty;

    public UISettings3DVM(
        BooleanProperty axesVisibleProperty,
        ObjectProperty<PerspectiveID> cameraPerspectiveIdProperty,
        ObjectProperty<DrawMode> drawModeProperty,
        BooleanProperty view3DEnabledProperty,
        ObjectProperty<Color> mazeFloorColorProperty,
        ObjectProperty<Color> mazeLightColorProperty,
        DoubleProperty mazeWallHeightProperty,
        DoubleProperty mazeWallOpacityProperty)
    {
            this.axesVisibleProperty = axesVisibleProperty;
            this.cameraPerspectiveIdProperty = cameraPerspectiveIdProperty;
            this.drawModeProperty = drawModeProperty;
            this.view3DEnabledProperty = view3DEnabledProperty;
            this.mazeFloorColorProperty = mazeFloorColorProperty;
            this.mazeLightColorProperty = mazeLightColorProperty;
            this.mazeWallHeightProperty = mazeWallHeightProperty;
            this.mazeWallOpacityProperty = mazeWallOpacityProperty;
        }
    }