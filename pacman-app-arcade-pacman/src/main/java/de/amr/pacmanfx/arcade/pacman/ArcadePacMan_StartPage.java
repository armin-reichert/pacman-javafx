/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.layout.StartPage;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_BOOT_SHOW_GAME_VIEW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_StartPage extends StackPane implements StartPage {

    public ArcadePacMan_StartPage(GameContext gameContext, String gameVariant) {
        requireNonNull(gameContext);
        setUserData(requireNonNull(gameVariant));

        ResourceManager rm = () -> ArcadePacMan_StartPage.class;
        var flyer = new Flyer(rm.loadImage("graphics/f1.jpg"), rm.loadImage("graphics/f2.jpg"), rm.loadImage("graphics/f3.jpg"));
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
        flyer.selectPage(0);

        var startButton = StartPagesView.createStartButton(Pos.BOTTOM_CENTER);
        startButton.setAction(() -> GameAction.executeIfEnabled(theUI(), gameContext, ACTION_BOOT_SHOW_GAME_VIEW));
        startButton.setTranslateY(-50);
        getChildren().addAll(flyer, startButton);
    }

    @Override
    public String currentGameVariant() {
        return (String) getUserData();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }
}