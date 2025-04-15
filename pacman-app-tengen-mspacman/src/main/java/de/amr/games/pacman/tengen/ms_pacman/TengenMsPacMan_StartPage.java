/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.ui.StartPagesView;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.assets.ResourceManager;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_StartPage extends StackPane implements StartPage, ResourceManager {

    @Override
    public Class<?> resourceRootClass() {
        return TengenMsPacMan_StartPage.class;
    }

    public TengenMsPacMan_StartPage(GameVariant gameVariant) {
        setUserData(requireNonNull(gameVariant));
        var flyer = new Flyer(loadImage("graphics/f1.png"), loadImage("graphics/f2.png"));
        flyer.selectPage(0);
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