/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class ArcadePacManXXL_StartPage extends StackPane {

    private final Flyer flyer;

    public ArcadePacManXXL_StartPage(PacManGamesUI ui) {
        ResourceManager rm = this::getClass;
        flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.setUserData(GameVariant.PACMAN_XXL);
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);

        getChildren().add(flyer);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ENTER -> ui.selectGamePage();
            }
        });
    }
}
