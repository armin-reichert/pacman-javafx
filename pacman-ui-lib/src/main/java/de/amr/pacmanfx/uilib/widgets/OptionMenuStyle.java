/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
{}
