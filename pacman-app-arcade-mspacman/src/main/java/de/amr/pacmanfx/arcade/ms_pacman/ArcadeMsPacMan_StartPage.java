/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RESOURCE_MANAGER = () -> ArcadeMsPacMan_StartPage.class;

    public ArcadeMsPacMan_StartPage()  {
        super(RESOURCE_MANAGER.url("startpage.json"));
    }
}