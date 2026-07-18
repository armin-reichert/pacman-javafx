package de.amr.pacmanfx.ui.settings.ui;

public record MiniViewSettings(
    int height,
    boolean active,
    int opacityPercentage,
    float slideInSeconds,
    float slideOutSeconds
) {}
