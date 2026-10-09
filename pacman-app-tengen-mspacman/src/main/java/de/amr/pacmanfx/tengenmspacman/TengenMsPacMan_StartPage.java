/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.ui.assets.ResourceManager;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RESOURCE_MANAGER = () -> TengenMsPacMan_StartPage.class;

    public TengenMsPacMan_StartPage() {
        super(RESOURCE_MANAGER.url("startpage.json"));
    }
}