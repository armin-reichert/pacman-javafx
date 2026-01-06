/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_StartPage.class;

    public TengenMsPacMan_StartPage() {
        super(
            StandardGameVariant.TENGEN_MS_PACMAN.name(),
            "Ms. Pac-Man (Tengen)",
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.png"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.png")
        );
    }
}