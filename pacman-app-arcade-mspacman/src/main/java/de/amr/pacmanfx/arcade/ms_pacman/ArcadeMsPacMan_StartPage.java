/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage implements ResourceManager {

    private final Media flyerTextSpeech = loadMedia("sound/flyer-text.mp3");

    public ArcadeMsPacMan_StartPage() {
        setTitle("Ms. Pac-Man (Arcade)");
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
    public void onEnterStartPage(GameUI ui) {
        flyer.selectPage(0);
        ui.voicePlayer().playVoice(flyerTextSpeech);
        ui.gameContext().gameVariantNameProperty().set(GameVariant.ARCADE_MS_PACMAN.name());
    }
}