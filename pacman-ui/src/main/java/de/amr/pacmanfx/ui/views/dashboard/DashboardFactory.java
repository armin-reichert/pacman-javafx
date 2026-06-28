/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;


import de.amr.basics.Identifier;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.Optional;

public interface DashboardFactory {

    Optional<Identifier> identify(String id);

    GameDashboardSection createSection(GameDashboard dashboard, Identifier id, TranslationManager translations);
}
