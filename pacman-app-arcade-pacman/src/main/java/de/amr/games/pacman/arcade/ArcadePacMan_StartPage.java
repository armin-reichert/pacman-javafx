/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.ui.StartPagesView;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static de.amr.games.pacman.Globals.assertNotNull;

public class ArcadePacMan_StartPage extends StackPane implements StartPage, ResourceManager {

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_StartPage.class;
    }

    public ArcadePacMan_StartPage(GameVariant gameVariant) {
        setUserData(assertNotNull(gameVariant));
        var flyer = new Flyer(loadImage("graphics/f1.jpg"), loadImage("graphics/f2.jpg"), loadImage("graphics/f3.jpg"));
        flyer.selectFlyerPage(0);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
        getChildren().addAll(flyer, StartPagesView.createDefaultStartButton());
    }

    @Override
    public GameVariant currentGameVariant() {
        return (GameVariant) getUserData();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }
}