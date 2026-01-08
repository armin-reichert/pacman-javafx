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
    AudioClip valueSelectedSound) {

    private static final ResourceManager RESOURCE_MANAGER = () -> OptionMenuStyle.class;

    private static final AudioClip DEFAULT_ENTRY_SELECTION_SOUND
        = RESOURCE_MANAGER.loadAudioClip("/de/amr/pacmanfx/uilib/sounds/menu-select1.wav");

    private static final AudioClip DEFAULT_VALUE_SELECTION_SOUND
        = RESOURCE_MANAGER.loadAudioClip("/de/amr/pacmanfx/uilib/sounds/menu-select2.wav");

    public static final Font DEFAULT_TITLE_FONT = Font.font("sans", FontWeight.BLACK, 30);
    public static final Font DEFAULT_TEXT_FONT = Font.font("sans", FontWeight.BOLD, 12);
    public static final Color DEFAULT_BACKGROUND_FILL = Color.valueOf("0c1568");
    public static final Color DEFAULT_BORDER_STROKE = Color.valueOf("fffeff");
    public static final Color DEFAULT_TITLE_TEXT_FILL = Color.valueOf("fffeff");
    public static final Color DEFAULT_ENTRY_TEXT_FILL = Color.valueOf("bcbe00");
    public static final Color DEFAULT_ENTRY_VALUE_FILL = Color.valueOf("fffeff");
    public static final Color DEFAULT_ENTRY_VALUE_DISABLED_FILL = Color.GRAY;
    public static final Color DEFAULT_HINT_TEXT_FILL = Color.valueOf("bcbe00");

    public static final OptionMenuStyle DEFAULT_OPTION_MENU_STYLE = OptionMenuStyle.builder()
        .titleFont(DEFAULT_TITLE_FONT)
        .textFont(DEFAULT_TEXT_FONT)
        .backgroundFill(DEFAULT_BACKGROUND_FILL)
        .borderStroke(DEFAULT_BORDER_STROKE)
        .titleTextFill(DEFAULT_TITLE_TEXT_FILL)
        .entryTextFill(DEFAULT_ENTRY_TEXT_FILL)
        .entryValueFill(DEFAULT_ENTRY_VALUE_FILL)
        .entryValueDisabledFill(DEFAULT_ENTRY_VALUE_DISABLED_FILL)
        .hintTextFill(DEFAULT_HINT_TEXT_FILL)
        .entrySelectedSound(DEFAULT_ENTRY_SELECTION_SOUND)
        .valueSelectedSound(DEFAULT_VALUE_SELECTION_SOUND)
        .build();

    // Builder API (created by Copilot)

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Font titleFont = DEFAULT_TITLE_FONT;
        private Font textFont = DEFAULT_TEXT_FONT;
        private Color backgroundFill = DEFAULT_BACKGROUND_FILL;
        private Color borderStroke = DEFAULT_BORDER_STROKE;
        private Color titleTextFill = DEFAULT_TITLE_TEXT_FILL;
        private Color entryTextFill = DEFAULT_ENTRY_TEXT_FILL;
        private Color entryValueFill = DEFAULT_ENTRY_VALUE_FILL;
        private Color entryValueDisabledFill = DEFAULT_ENTRY_VALUE_DISABLED_FILL;
        private Color hintTextFill = DEFAULT_HINT_TEXT_FILL;
        private AudioClip entrySelectedSound = DEFAULT_ENTRY_SELECTION_SOUND;
        private AudioClip valueSelectedSound = DEFAULT_VALUE_SELECTION_SOUND;

        private Builder() {}

        public Builder titleFont(Font titleFont) {
            this.titleFont = titleFont;
            return this;
        }

        public Builder textFont(Font textFont) {
            this.textFont = textFont;
            return this;
        }

        public Builder backgroundFill(Color backgroundFill) {
            this.backgroundFill = backgroundFill;
            return this;
        }

        public Builder borderStroke(Color borderStroke) {
            this.borderStroke = borderStroke;
            return this;
        }

        public Builder titleTextFill(Color titleTextFill) {
            this.titleTextFill = titleTextFill;
            return this;
        }

        public Builder entryTextFill(Color entryTextFill) {
            this.entryTextFill = entryTextFill;
            return this;
        }

        public Builder entryValueFill(Color entryValueFill) {
            this.entryValueFill = entryValueFill;
            return this;
        }

        public Builder entryValueDisabledFill(Color entryValueDisabledFill) {
            this.entryValueDisabledFill = entryValueDisabledFill;
            return this;
        }

        public Builder hintTextFill(Color hintTextFill) {
            this.hintTextFill = hintTextFill;
            return this;
        }

        public Builder entrySelectedSound(AudioClip entrySelectedSound) {
            this.entrySelectedSound = entrySelectedSound;
            return this;
        }

        public Builder valueSelectedSound(AudioClip valueSelectedSound) {
            this.valueSelectedSound = valueSelectedSound;
            return this;
        }

        public OptionMenuStyle build() {
            return new OptionMenuStyle(
                titleFont,
                textFont,
                backgroundFill,
                borderStroke,
                titleTextFill,
                entryTextFill,
                entryValueFill,
                entryValueDisabledFill,
                hintTextFill,
                entrySelectedSound,
                valueSelectedSound
            );
        }
    }
}