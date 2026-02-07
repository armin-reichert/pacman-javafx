/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class TengenMsPacMan_StartPage extends FlyerStartPage implements ResourceManager {

    private final Media flyerTextSpeech = loadMedia("sound/flyer-text.mp3");

    public TengenMsPacMan_StartPage() {
        setTitle("Ms. Pac-Man (Tengen)");
        flyer.setImages(
            loadImage("graphics/flyer-page-1.png"),
            loadImage("graphics/flyer-page-2.png")
        );
    }

    @Override
    public final Class<?> resourceRootClass() {
        return TengenMsPacMan_StartPage.class;
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        flyer.selectPage(0);
        ui.voicePlayer().playVoice(flyerTextSpeech);
        ui.gameContext().gameVariantNameProperty().set(GameVariant.TENGEN_MS_PACMAN.name());
    }
}