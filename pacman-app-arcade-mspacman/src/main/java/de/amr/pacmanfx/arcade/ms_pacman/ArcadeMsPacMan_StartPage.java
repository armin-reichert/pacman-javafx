/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage implements ResourceManager {

    private final Media flyerTextSpeech = loadMedia("sound/flyer-text.mp3");

    public ArcadeMsPacMan_StartPage() {
        super("Ms. Pac-Man (Arcade)");
        flyer.setImages(
            loadImage("graphics/flyer-page-1.jpg"),
            loadImage("graphics/flyer-page-2.jpg")
        );
    }

    @Override
    public final Class<?> resourceRootClass() {
        return ArcadeMsPacMan_StartPage.class;
    }

    @Override
    public void onEnterStartPage(Game context) {
        flyer.selectPage(0);
        context.ui().sounds().playVoice(flyerTextSpeech);
        context.selectGameVariant(GameVariant.ARCADE_MS_PACMAN.name());
    }
}