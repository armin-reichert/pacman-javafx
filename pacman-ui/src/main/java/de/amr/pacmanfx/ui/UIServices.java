package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

// So many managers? I think I should fire some!
public record UIServices(
    UIConfigManager configurations,
    GameSceneManager gameScenes,
    PreferencesManager prefs,
    SoundManager sounds,
    TranslationManager translations,
    ViewManager views) {
}
