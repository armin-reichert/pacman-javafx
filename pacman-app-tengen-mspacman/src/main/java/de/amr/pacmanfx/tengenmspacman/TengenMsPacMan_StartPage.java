/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_StartPage.class;
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/flyer-text.mp3");

    public TengenMsPacMan_StartPage() {
        setTitle("Ms. Pac-Man (Tengen)");
        flyer.setImages(
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.png"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.png")
        );
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        flyer.selectPage(0);
        ui.voicePlayer().playVoice(VOICE);
        ui.gameContext().gameVariantNameProperty().set(GameVariant.TENGEN_MS_PACMAN.name());
    }
}