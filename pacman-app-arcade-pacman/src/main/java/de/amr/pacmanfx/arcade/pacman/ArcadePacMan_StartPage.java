/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.media.Media;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadePacMan_StartPage.class;
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/flyer-text.mp3");

    public ArcadePacMan_StartPage() {
        super(
            "Pac-Man (Arcade)",
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-3.jpg")
        );
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        flyer.selectPage(0);
        ui.voicePlayer().play(VOICE);
        ui.context().gameVariantNameProperty().set(GameVariant.ARCADE_PACMAN.name());
    }
}