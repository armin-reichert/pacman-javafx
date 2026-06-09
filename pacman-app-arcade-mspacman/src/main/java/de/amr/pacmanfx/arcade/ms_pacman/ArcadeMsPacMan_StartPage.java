/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RM = () -> ArcadeMsPacMan_StartPage.class;

    private static final Media VOICE = RM.loadMedia("/de/amr/pacmanfx/arcade/ms_pacman/sound/flyer-text.mp3");

    private static final Image[] FLYER_IMAGES = {
        RM.loadImage("/de/amr/pacmanfx/arcade/ms_pacman/graphics/flyer-page-1.jpg"),
        RM.loadImage("/de/amr/pacmanfx/arcade/ms_pacman/graphics/flyer-page-2.jpg")
    };

    public ArcadeMsPacMan_StartPage() {
        super("Ms. Pac-Man (Arcade)");
        flyer.setImages(FLYER_IMAGES);
    }

    @Override
    public void onEnterStartPage(Game game) {
        game.selectGameVariant(GameVariantID.ARCADE_MS_PACMAN.name());
        game.ui().sounds().playVoice(VOICE);
        flyer.selectPage(0);
    }
}