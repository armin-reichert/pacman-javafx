/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class TengenMsPacMan_StartPage extends StackPane {

    private final Flyer flyer;

    public TengenMsPacMan_StartPage(PacManGamesUI ui) {
        ResourceManager rm = this::getClass;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.png"),
            rm.loadImage("graphics/f2.png")
        );
        flyer.setUserData(GameVariant.MS_PACMAN_TENGEN);
        flyer.selectFlyerPage(0);

        getChildren().add(flyer);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ENTER -> ui.selectGamePage();
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
    }
}
