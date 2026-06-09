/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RM = () -> ArcadeMsPacMan_StartPage.class;

    private static final Media VOICE = RM.loadMedia("/de/amr/pacmanfx/arcade/ms_pacman/sound/flyer-text.mp3");

    public ArcadeMsPacMan_StartPage() {
        super("Ms. Pac-Man (Arcade)");
        flyer.setImages(
            RM.loadImage("/de/amr/pacmanfx/arcade/ms_pacman/graphics/flyer-page-1.jpg"),
            RM.loadImage("/de/amr/pacmanfx/arcade/ms_pacman/graphics/flyer-page-2.jpg")
        );
    }

    @Override
    public void onEnterStartPage(Game game) {
        flyer.selectPage(0);
        game.ui().sounds().playVoice(VOICE);
        game.selectGameVariant(GameVariant.ARCADE_MS_PACMAN.name());
    }
}