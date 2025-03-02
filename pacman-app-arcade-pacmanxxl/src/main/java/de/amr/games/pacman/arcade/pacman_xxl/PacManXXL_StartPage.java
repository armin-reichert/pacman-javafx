/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

public class PacManXXL_StartPage extends StackPane implements StartPage {

    private final PacManGamesUI ui;
    private final PacManXXL_OptionsMenu menu;

    public PacManXXL_StartPage(PacManGamesUI ui) {
        this.ui = ui;
        ResourceManager rm = this::getClass;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);
        menu = new PacManXXL_OptionsMenu(ui);
        getChildren().addAll(flyer, menu.root());
        setBackground(Background.fill(Color.BLACK));
    }

    @Override
    public void requestFocus() {
        menu.root().requestFocus();
        initMenu();
    }

    @Override
    public void start() {}

    private void initMenu() {
        switch (ui.gameController().currentGameVariant()) {
            case MS_PACMAN_XXL -> {
                PacManXXL_MsPacMan_GameModel game = ui.game();
                menu.setMenuState(
                        ui.gameController().currentGameVariant(),
                        game.isCutScenesEnabled(),
                        game.mapSelector().mapSelectionMode());
            }
            case PACMAN_XXL -> {
                PacManXXL_PacMan_GameModel game = ui.game();
                menu.setMenuState(
                        ui.gameController().currentGameVariant(),
                        game.isCutScenesEnabled(),
                        game.mapSelector().mapSelectionMode());
            }
            default -> throw new IllegalStateException();
        }
        Logger.info("Menu initialized");
    }

    @Override
    public Node root() {
        return this;
    }
}