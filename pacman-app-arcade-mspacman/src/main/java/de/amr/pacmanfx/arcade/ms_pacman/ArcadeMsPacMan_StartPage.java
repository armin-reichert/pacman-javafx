/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.StartPage;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_BOOT_SHOW_PLAY_VIEW;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_StartPage extends StackPane implements StartPage {

    private final Flyer flyer;

    public ArcadeMsPacMan_StartPage(GameUI ui) {
        requireNonNull(ui);
        ResourceManager rm = () -> ArcadeMsPacMan_StartPage.class;
        flyer = new Flyer(rm.loadImage("graphics/f1.jpg"), rm.loadImage("graphics/f2.jpg"));
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
        addEventHandler(ScrollEvent.SCROLL, e-> {
            if (e.getDeltaY() < 0) {
                flyer.nextFlyerPage();
            } else if (e.getDeltaY() > 0) {
                flyer.prevFlyerPage();
            }
        });

        Node startButton = StartPagesCarousel.createDefaultStartButton(ui, () -> {
            ui.soundManager().playVoice(SoundID.VOICE_EXPLAIN, 0);
            ACTION_BOOT_SHOW_PLAY_VIEW.executeIfEnabled(ui);
        });
        startButton.setTranslateY(-50);
        getChildren().addAll(flyer, startButton);
    }

    @Override
    public void onEnter(GameUI ui) {
        ui.selectGameVariant(StandardGameVariant.MS_PACMAN.name());
        flyer.selectPage(0);
    }

    @Override
    public Region layoutRoot() {
        return this;
    }

    @Override
    public String title() {
        return "Arcade Ms. Pac-Man";
    }
}