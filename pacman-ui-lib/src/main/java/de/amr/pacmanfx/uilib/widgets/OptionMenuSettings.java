package de.amr.pacmanfx.uilib.widgets;

import javafx.scene.paint.Color;

public record OptionMenuSettings(
    FontSettings titleFont,
    FontSettings textFont,
    Color backgroundFill,
    Color borderStroke,
    Color titleTextFill,
    Color entryTextFill,
    Color entryValueFill,
    Color entryValueDisabledFill,
    Color usageTextFill)
{}
