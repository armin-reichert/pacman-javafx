/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public record OptionMenuStyle(
    Font titleFont,
    Font textFont,
    Color backgroundFill,
    Color borderStroke,
    Color titleTextFill,
    Color entryTextFill,
    Color entryValueFill,
    Color entryValueDisabledFill,
    Color hintTextFill,
    AudioClip entrySelectedSound,
    AudioClip valueSelectedSound)
{
    public static final OptionMenuStyle DEFAULT_OPTION_MENU_STYLE;
    static {
        ResourceManager rm = () -> OptionMenuStyle.class;
        DEFAULT_OPTION_MENU_STYLE = new OptionMenuStyle(
            Font.font("sans", FontWeight.BLACK, 30),
            Font.font("sans", FontWeight.BOLD, 12),
            Color.valueOf("0c1568"),
            Color.valueOf("fffeff"),
            Color.valueOf("fffeff"),
            Color.valueOf("bcbe00"),
            Color.valueOf("fffeff"),
            Color.GRAY,
            Color.valueOf("bcbe00"),
            rm.loadAudioClip("/de/amr/pacmanfx/uilib/sounds/menu-select1.wav"),
            rm.loadAudioClip("/de/amr/pacmanfx/uilib/sounds/menu-select2.wav")
        );
    }
}