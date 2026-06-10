package de.amr.pacmanfx.ui.game;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class UISettings {
    /**
     * Global property for the canvas background color.
     * <p>
     * Implementations should bind this to the rendering surface.
     */
    public final ObjectProperty<Color> PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);

    /** Whether canvas font smoothing is enabled. */
    public final BooleanProperty PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);

    /** Whether debug information overlays are visible. */
    public final BooleanProperty PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);

    /** Whether information about the currently pressed keys is displayed. */
    public final BooleanProperty PROPERTY_KEYBOARD_MONITOR_VISIBLE = new SimpleBooleanProperty(false);

    /** Height of the mini-view (in pixels). */
    public final IntegerProperty PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);

    /** Whether the mini-view is currently visible. */
    public final BooleanProperty PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);

    /** Opacity of the mini-view (0–100%). */
    public final IntegerProperty PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);

    /** Whether all audio output is muted. */
    public final BooleanProperty PROPERTY_MUTED = new SimpleBooleanProperty(false);

    /** Default duration of flash messages. */
    public final Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);
}
