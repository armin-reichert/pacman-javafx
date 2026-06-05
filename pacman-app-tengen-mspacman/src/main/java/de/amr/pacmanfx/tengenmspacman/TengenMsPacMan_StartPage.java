/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class TengenMsPacMan_StartPage extends FlyerStartPage implements ResourceManager {

    private final Media flyerTextSpeech = loadMedia("sound/flyer-text.mp3");

    public TengenMsPacMan_StartPage() {
        super("Ms. Pac-Man (Tengen)");
        flyer.setImages(
            loadImage("graphics/flyer-page-1.png"),
            loadImage("graphics/flyer-page-2.png")
        );
    }

    @Override
    public final Class<?> resourceRootClass() {
        return TengenMsPacMan_StartPage.class;
    }

    @Override
    public void onEnterStartPage(AppContext context) {
        flyer.selectPage(0);
        context.ui().sounds().playVoice(flyerTextSpeech);
        context.selectGameVariant(GameVariant.TENGEN_MS_PACMAN.name());
    }
}