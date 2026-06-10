package de.amr.pacmanfx.ui.d3;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.ui.d3.animation.energizer.AttractionConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.SwirlConfig;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public final class UISettings3D {

    public final ParticlesAnimationConfig DEFAULT_PARTICLE_ANIMATION_CONFIG = new ParticlesAnimationConfig(
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

    /** Whether 3D axes are visible in the 3D play scene. */
    public final BooleanProperty axesVisibleProperty = new SimpleBooleanProperty(false);

    /** Currently active 3D camera perspective. */
    public final ObjectProperty<PerspectiveID> cameraPerspectiveIdProperty = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);

    /** Draw mode for 3D geometry (fill or wireframe). */
    public final ObjectProperty<DrawMode> drawModeProperty = new SimpleObjectProperty<>(DrawMode.FILL);

    /** Whether 3D rendering is enabled at all. */
    public final BooleanProperty d3EnabledProperty = new SimpleBooleanProperty(false);

    /** Floor color used in 3D mode. */
    public final ObjectProperty<Color> mazeFloorColorProperty = new SimpleObjectProperty<>(Color.rgb(20, 20, 20));

    /** Light color used in 3D mode. */
    public final ObjectProperty<Color> mazeLightColorProperty = new SimpleObjectProperty<>(Color.WHITE);

    /** Height of 3D walls (in world units). */
    public final DoubleProperty mazeWallHeightProperty = new SimpleDoubleProperty();

    /** Opacity of 3D walls (0.0–1.0). */
    public final DoubleProperty mazeWallOpacityProperty = new SimpleDoubleProperty(1.0);
}
