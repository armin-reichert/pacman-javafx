/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.app.AppContext;
import de.amr.pacmanfx.ui.subviews.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadePacMan_StartPage extends FlyerStartPage implements ResourceManager {

    private final Media flyerTextSpeech = loadMedia("sound/flyer-text.mp3");

    public ArcadePacMan_StartPage() {
        super("Pac-Man (Arcade)");
        flyer.setImages(
            loadImage("graphics/flyer-page-1.jpg"),
            loadImage("graphics/flyer-page-2.jpg"),
            loadImage("graphics/flyer-page-3.jpg")
        );
    }

    @Override
    public final Class<?> resourceRootClass() {
        return ArcadePacMan_StartPage.class;
    }

    @Override
    public void onEnterStartPage(AppContext context) {
        flyer.selectPage(0);
        context.ui().sounds().playVoice(flyerTextSpeech);
        context.selectGameVariant(GameVariant.ARCADE_PACMAN.name());
    }
}