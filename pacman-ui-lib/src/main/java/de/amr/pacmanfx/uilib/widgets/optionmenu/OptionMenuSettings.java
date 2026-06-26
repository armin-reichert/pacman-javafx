package de.amr.pacmanfx.uilib.widgets.optionmenu;

import de.amr.pacmanfx.uilib.widgets.FontSettings;
import javafx.scene.paint.Color;

public record OptionMenuSettings(
    int minHeight,
    int maxHeight,
    float relHeight,
    int numTilesX,
    int numTilesY,
    int textColumn,
    int valueColumn,
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
