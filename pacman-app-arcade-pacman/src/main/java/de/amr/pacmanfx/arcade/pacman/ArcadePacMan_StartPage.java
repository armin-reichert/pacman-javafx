/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RESOURCE_MANAGER = () -> ArcadePacMan_StartPage.class;

    public ArcadePacMan_StartPage() {
        super(RESOURCE_MANAGER.url("startpage.json"));
    }
}