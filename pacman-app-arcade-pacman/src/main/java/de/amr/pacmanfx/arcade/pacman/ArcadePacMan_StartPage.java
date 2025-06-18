/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.layout.StartPage;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.ACTION_BOOT_SHOW_GAME_VIEW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_StartPage extends StackPane implements StartPage, ResourceManager {

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_StartPage.class;
    }

    public ArcadePacMan_StartPage(String gameVariant) {
        setUserData(requireNonNull(gameVariant));
        var flyer = new Flyer(loadImage("graphics/f1.jpg"), loadImage("graphics/f2.jpg"), loadImage("graphics/f3.jpg"));
        flyer.selectPage(0);
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
        var startButton = StartPagesView.createStartButton(Pos.BOTTOM_CENTER);
        startButton.setAction(() -> GameAction.executeIfEnabled(theUI(), ACTION_BOOT_SHOW_GAME_VIEW));
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

    @Override
    public void onEnter() {
        Logger.info("onEnter {}", this);
    }

    @Override
    public void onExit() {
        Logger.info("onExit {}", this);
    }
}