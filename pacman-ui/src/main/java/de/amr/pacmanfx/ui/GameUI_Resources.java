/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.text.Font;

import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.createImageBackground;

/**
 * Central repository for static UI resources (fonts, media, backgrounds, etc.).
 */
public interface GameUI_Resources {
    /**
     * Resource manager for UI assets (fonts, images, sounds).
     */
    ResourceManager UI_RESOURCES = () -> GameUI.class;
    /**
     * Localized text bundle for the UI.
     */
    ResourceBundle LOCALIZED_TEXTS = UI_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");
    /**
     * Path to the main UI stylesheet.
     */
    String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";
    /**
     * Background image using Pac-Man wallpaper.
     */
    Background BACKGROUND_PAC_MAN_WALLPAPER = createImageBackground(UI_RESOURCES.loadImage("graphics/pacman_wallpaper.png"));
    /**
     * Arcade font size 6.
     */
    Font FONT_ARCADE_6 = UI_RESOURCES.loadFont("fonts/emulogic.ttf", 6);
    /**
     * Arcade font size 8.
     */
    Font FONT_ARCADE_8 = UI_RESOURCES.loadFont("fonts/emulogic.ttf", 8);
    /**
     * Handwriting-style font for messages.
     */
    Font FONT_HANDWRITING = UI_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9);
    /**
     * Monospaced font for debug/info text.
     */
    Font FONT_MONOSPACED = UI_RESOURCES.loadFont("fonts/fantasquesansmono-bold.otf", 12);
    /**
     * Condensed monospace font for UI elements.
     */
    Font FONT_CONDENSED = UI_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12);
    /**
     * Standard Pac-Man font.
     */
    Font FONT_PAC_FONT = UI_RESOURCES.loadFont("fonts/Pacfont.ttf", 8);
    /**
     * Good Pac-Man font (alternative variant).
     */
    Font FONT_PAC_FONT_GOOD = UI_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8);
    /**
     * Voice media for autopilot deactivation.
     */
    Media VOICE_AUTOPILOT_OFF = UI_RESOURCES.loadMedia("sound/voice/autopilot-off.mp3");
    /**
     * Voice media for autopilot activation.
     */
    Media VOICE_AUTOPILOT_ON = UI_RESOURCES.loadMedia("sound/voice/autopilot-on.mp3");
    /**
     * Voice media for game start explanation.
     */
    Media VOICE_EXPLAIN_GAME_START = UI_RESOURCES.loadMedia("sound/voice/press-key.mp3");
    /**
     * Voice media for immunity deactivation.
     */
    Media VOICE_IMMUNITY_OFF = UI_RESOURCES.loadMedia("sound/voice/immunity-off.mp3");
    /**
     * Voice media for immunity activation.
     */
    Media VOICE_IMMUNITY_ON = UI_RESOURCES.loadMedia("sound/voice/immunity-on.mp3");
}
