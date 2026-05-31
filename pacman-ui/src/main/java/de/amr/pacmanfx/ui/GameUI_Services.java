/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.List;
import java.util.Optional;

public record GameUI_Services(
    UIConfigManager configurations,
    GameSceneManager gameScenes,
    PreferencesManager prefs,
    SoundManager sounds,
    TranslationManager translations,
    ViewManager views)
{
    public void configureDashboard(List<CommonDashboardID> dashboardIDList) {
        views().playView().dashboard().addCommonSections(translations(), dashboardIDList);
    }

    public Optional<GameSoundEffects> optSoundEffects(String gameVariantName) {
        return configurations().getOrCreateUIConfig(gameVariantName).optSoundEffects();
    }
}
