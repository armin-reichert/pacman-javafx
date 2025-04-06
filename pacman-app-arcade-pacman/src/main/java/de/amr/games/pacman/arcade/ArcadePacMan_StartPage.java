/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class ArcadePacMan_StartPage extends StackPane implements StartPage {

    private final GameVariant gameVariant;
    private final Flyer flyer;

    public ArcadePacMan_StartPage(GameVariant gameVariant) {
        this.gameVariant = assertNotNull(gameVariant);

        ResourceManager rm = this::getClass;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.jpg"),
            rm.loadImage("graphics/f2.jpg"),
            rm.loadImage("graphics/f3.jpg")
        );
        flyer.setUserData(GameVariant.PACMAN);
        flyer.selectFlyerPage(0);

        Node startButton = startButton();
        getChildren().addAll(flyer, startButton);

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
    }

    @Override
    public GameVariant currentGameVariant() {
        return gameVariant;
    }

    private Node startButton() {
        ResourceManager rm = () -> PacManGamesUI.class;
        Font startButtonFont = rm.loadFont("fonts/emulogic.ttf", 30);
        Node btnStart = Ufx.createFancyButton(startButtonFont, THE_ASSETS.text("play_button"), THE_UI::showGameView);
        btnStart.setTranslateY(-50);
        StackPane.setAlignment(btnStart, Pos.BOTTOM_CENTER);
        return btnStart;
    }

    @Override
    public Node root() {
        return this;
    }
}
