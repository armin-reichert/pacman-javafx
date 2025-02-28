/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.Optional;

public class ArcadePacMan_StartPage extends StackPane implements StartPage {

    private final PacManGamesUI ui;
    private final Flyer flyer;

    public ArcadePacMan_StartPage(PacManGamesUI ui) {
        this.ui = ui;

        ResourceManager rm = () -> ResourceRoot.class;
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
                case ENTER -> ui.selectGamePage();
                case DOWN -> flyer.nextFlyerPage();
                case UP -> flyer.prevFlyerPage();
            }
        });
    }

    private Node startButton() {
        ResourceManager rm = () -> PacManGamesUI.class;
        Font startButtonFont = rm.loadFont("fonts/emulogic.ttf", 30);
        Node btnStart = Ufx.createFancyButton(startButtonFont, ui.locText("play_button"), ui::selectGamePage);
        btnStart.setTranslateY(-50);
        StackPane.setAlignment(btnStart, Pos.BOTTOM_CENTER);
        return btnStart;
    }

    @Override
    public void start() {
        ui.selectGamePage();
    }

    @Override
    public Node root() {
        return this;
    }
}
