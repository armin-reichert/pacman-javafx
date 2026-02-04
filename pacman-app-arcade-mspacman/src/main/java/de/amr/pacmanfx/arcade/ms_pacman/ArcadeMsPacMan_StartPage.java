/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadeMsPacMan_StartPage.class;
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/flyer-text.mp3");

    public ArcadeMsPacMan_StartPage() {
        setTitle("Ms. Pac-Man (Arcade)");
        flyer.setImages(
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.jpg")
        );
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        flyer.selectPage(0);
        ui.voicePlayer().playVoice(VOICE);
        ui.context().gameVariantNameProperty().set(GameVariant.ARCADE_MS_PACMAN.name());
    }
}