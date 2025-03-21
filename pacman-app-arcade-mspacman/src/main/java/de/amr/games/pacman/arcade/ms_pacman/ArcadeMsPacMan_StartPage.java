/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class ArcadeMsPacMan_StartPage extends StackPane implements StartPage {

    private final PacManGamesUI ui;
    private final Flyer flyer;

    public ArcadeMsPacMan_StartPage(PacManGamesUI ui) {
        this.ui = ui;

        ResourceManager rm = this::getClass;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.jpg"),
            rm.loadImage("graphics/f2.jpg")
        );
        flyer.setUserData(GameVariant.MS_PACMAN);
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

    private Node startButton() {
        ResourceManager rm = () -> PacManGamesUI.class;
        Font startButtonFont = rm.loadFont("fonts/emulogic.ttf", 30);
        Node btnStart = Ufx.createFancyButton(startButtonFont, ui.locText("play_button"), ui::showGameView);
        btnStart.setTranslateY(-50);
        StackPane.setAlignment(btnStart, Pos.BOTTOM_CENTER);
        return btnStart;
    }

    @Override
    public void start() {
        ui.showGameView();
    }

    @Override
    public Node root() {
        return this;
    }
}
