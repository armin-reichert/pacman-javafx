/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/ms_pacman/";

    private static final ResourceManager RM = () -> ArcadeMsPacMan_StartPage.class;

    private static final Media VOICE = RM.loadMedia(ROOT_PATH + "sound/flyer-text.mp3");

    private static final Image[] FLYER_IMAGES = {
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-1.jpg"),
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-2.jpg")
    };

    public ArcadeMsPacMan_StartPage()  {
        super("Ms. Pac-Man (Arcade)"); // TODO; localize
        flyer.setImages(FLYER_IMAGES);
        setVoice(VOICE);
    }

    @Override
    public void onEnter() {
        game.selectGameVariant(GameVariantID.ARCADE_MS_PACMAN.name());
        flyer.selectPage(0);
        startTalking();
    }
}