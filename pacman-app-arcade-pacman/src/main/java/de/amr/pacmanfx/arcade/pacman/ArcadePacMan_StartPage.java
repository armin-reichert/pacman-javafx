/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager RM = () -> ArcadePacMan_StartPage.class;

    private static final Media VOICE = RM.loadMedia("/de/amr/pacmanfx/arcade/pacman/sound/flyer-text.mp3");

    public ArcadePacMan_StartPage() {
        super("Pac-Man (Arcade)");
        flyer.setImages(
            RM.loadImage("/de/amr/pacmanfx/arcade/pacman/graphics/flyer-page-1.jpg"),
            RM.loadImage("/de/amr/pacmanfx/arcade/pacman/graphics/flyer-page-2.jpg"),
            RM.loadImage("/de/amr/pacmanfx/arcade/pacman/graphics/flyer-page-3.jpg")
        );
    }

    @Override
    public void onEnterStartPage(Game game) {
        game.selectGameVariant(GameVariantID.ARCADE_PACMAN.name());
        flyer.selectPage(0);
        game.ui().sounds().playVoice(VOICE);
    }
}