/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final String ROOT_PATH = "/de/amr/pacmanfx/tengenmspacman/";

    private static final ResourceManager RM = () -> TengenMsPacMan_StartPage.class;

    private static final Media VARIANT_NARRATION = RM.loadMedia(ROOT_PATH + "sound/flyer-text.mp3");

    private static final Image[] FLYER_IMAGES = {
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-1.png"),
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-2.png")
    };

    public TengenMsPacMan_StartPage() {
        super("Ms. Pac-Man (Tengen)"); //TODO localize
        flyer.setImages(FLYER_IMAGES);
        setVoice(VARIANT_NARRATION);
    }

    @Override
    public void onEnter() {
        game.selectGameVariant(GameVariantID.TENGEN_MS_PACMAN.name());
        flyer.selectPage(0);
        startTalking();
        Platform.runLater(startButton::requestFocus);
    }
}