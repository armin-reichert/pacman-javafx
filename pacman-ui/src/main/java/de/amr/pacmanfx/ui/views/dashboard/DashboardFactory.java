/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;


import de.amr.basics.Named;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.Optional;

public interface DashboardFactory {

    Optional<Named> identify(String id);

    GameDashboardSection createSection(GameDashboard dashboard, Named id, TranslationManager translations);
}
