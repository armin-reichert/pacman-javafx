package de.amr.pacmanfx.ui.gamescene.d2;

/**
 * State of the flashing part of the level-complete animation. Used by renderers to query the current flashing status.
 */
public record FlashingState(
    /* Tells if the map should be drawn highlighted (bright) in the current frame */
    boolean isHighlighted,
    /* Tells if the flashing animation is currently running */
    boolean isFlashing,
    /* The current flashing cycle index (0-based) */
    int flashingIndex) {
}
