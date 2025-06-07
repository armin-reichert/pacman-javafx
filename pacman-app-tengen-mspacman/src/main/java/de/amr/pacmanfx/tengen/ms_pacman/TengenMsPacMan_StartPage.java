/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui.layout.StartPage;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
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
        var startButton = StartPagesView.createStartButton(Pos.BOTTOM_CENTER);
        startButton.setTranslateY(-50);
        startButton.setAction(PacManGames_Actions.SHOW_GAME_VIEW_AND_RESTART_GAME);
        getChildren().addAll(flyer, startButton);
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