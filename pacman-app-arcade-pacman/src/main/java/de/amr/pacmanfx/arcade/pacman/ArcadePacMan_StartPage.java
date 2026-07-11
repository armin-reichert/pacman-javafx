/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman/";

    private static final ResourceManager RM = () -> ArcadePacMan_StartPage.class;

    private static final Media VARIANT_NARRATION = RM.loadMedia(ROOT_PATH + "sound/flyer-text.mp3");

    private static final Image[] FLYER_IMAGES = {
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-1.jpg"),
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-2.jpg"),
        RM.loadImage(ROOT_PATH + "graphics/flyer-page-3.jpg")
    };

    public ArcadePacMan_StartPage() {
        super(GameVariantID.ARCADE_PACMAN.name());
        title = "Pac-Man (Arcade)"; // TODO: localize
        flyer.setImages(FLYER_IMAGES);
        setVoice(VARIANT_NARRATION);
    }
}