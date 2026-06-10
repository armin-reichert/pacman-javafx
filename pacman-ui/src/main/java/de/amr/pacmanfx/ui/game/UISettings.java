package de.amr.pacmanfx.ui.game;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class UISettings {

    /** Default duration of flash messages. */
    public final Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    /**
     * Global property for the canvas background color.
     * <p>
     * Implementations should bind this to the rendering surface.
     */
    public final ObjectProperty<Color> canvasBackgroundColorProperty = new SimpleObjectProperty<>(Color.BLACK);

    /** Whether canvas font smoothing is enabled. */
    public final BooleanProperty canvasFontSmoothingProperty = new SimpleBooleanProperty(false);

    /** Whether debug information overlays are visible. */
    public final BooleanProperty debugInfoVisibleProperty = new SimpleBooleanProperty(false);

    /** Whether information about the currently pressed keys is displayed. */
    public final BooleanProperty keyboardMonitorVisibleProperty = new SimpleBooleanProperty(false);

    /** Height of the mini-view (in pixels). */
    public final IntegerProperty miniViewHeightProperty = new SimpleIntegerProperty(400);

    /** Whether the mini-view is currently visible. */
    public final BooleanProperty miniViewOnProperty = new SimpleBooleanProperty(false);

    /** Opacity of the mini-view (0–100%). */
    public final IntegerProperty miniViewOpacityPercentProperty = new SimpleIntegerProperty(69);

    /** Whether all audio output is muted. */
    public final BooleanProperty mutedProperty = new SimpleBooleanProperty(false);
}
