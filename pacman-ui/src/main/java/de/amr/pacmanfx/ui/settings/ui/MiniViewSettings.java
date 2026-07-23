package de.amr.pacmanfx.ui.settings.ui;

public record MiniViewSettings(
    int minHeight,
    int maxHeight,
    int height,
    boolean active,
    int opacityPercentage,
    float slideInSeconds,
    float slideOutSeconds
) {}
