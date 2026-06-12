/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RM = () -> TengenMsPacMan_StartPage.class;

    private static final Media VOICE = RM.loadMedia("/de/amr/pacmanfx/tengenmspacman/sound/flyer-text.mp3");

    private static final Image[] FLYER_IMAGES = {
        RM.loadImage("/de/amr/pacmanfx/tengenmspacman/graphics/flyer-page-1.png"),
        RM.loadImage("/de/amr/pacmanfx/tengenmspacman/graphics/flyer-page-2.png")
    };

    public TengenMsPacMan_StartPage() {
        super("Ms. Pac-Man (Tengen)");
        flyer.setImages(FLYER_IMAGES);
    }

    @Override
    public void onEnter() {
        game.selectGameVariant(GameVariantID.TENGEN_MS_PACMAN.name());
        game.ui().sounds().playVoice(VOICE); // must be called after selecting game variant!
        flyer.selectPage(0);
    }
}