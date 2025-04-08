/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.ui.StartPagesView;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static de.amr.games.pacman.Globals.assertNotNull;

public class ArcadeMsPacMan_StartPage extends StackPane implements StartPage, ResourceManager {

    private final Flyer flyer;

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacMan_StartPage.class;
    }

    public ArcadeMsPacMan_StartPage(GameVariant gameVariant) {
        assertNotNull(gameVariant);
        flyer = new Flyer(loadImage("graphics/f1.jpg"), loadImage("graphics/f2.jpg"));
        flyer.setUserData(gameVariant);
        flyer.selectFlyerPage(0);
        getChildren().addAll(flyer, StartPagesView.createDefaultStartButton());
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
    }

    @Override
    public GameVariant currentGameVariant() {
        return (GameVariant) flyer.getUserData();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }
}