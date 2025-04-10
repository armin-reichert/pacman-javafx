/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_KEYBOARD;

public class PacManXXL_StartPage implements StartPage, ResourceManager {

    private final StackPane root = new StackPane();
    private final PacManXXL_OptionMenu menu;

    @Override
    public Class<?> resourceRootClass() {
        return PacManXXL_StartPage.class;
    }

    public PacManXXL_StartPage() {
        root.setBackground(Background.fill(Color.BLACK));

        Flyer flyer = new Flyer(loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);

        menu = new PacManXXL_OptionMenu();
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.tilesY() * TS));
        initMenuState(THE_GAME_CONTROLLER.selectedGameVariant());

        root.getChildren().addAll(flyer, menu.root());
    }

    private void initMenuState(GameVariant gameVariant) {
        if (gameVariant != GameVariant.MS_PACMAN_XXL && gameVariant != GameVariant.PACMAN_XXL) {
            Logger.warn("Game variant {} is not allowed in option menu, using {} instead", gameVariant, GameVariant.PACMAN_XXL);
            gameVariant = GameVariant.PACMAN_XXL;
        }
        GameModel game = THE_GAME_CONTROLLER.game(gameVariant);
        game.mapSelector().loadAllMaps(game);
        menu.setGameVariant(gameVariant);
        menu.setPlay3D(PY_3D_ENABLED.get());
        menu.setCutScenesEnabled(game.isCutScenesEnabled());
        menu.setMapOrder(game.mapSelector().mapSelectionMode(), !game.mapSelector().customMaps().isEmpty());
    }

    @Override
    public void onEnter() {
        initMenuState(currentGameVariant());
        menu.startDrawing();
        THE_KEYBOARD.logCurrentBindings();
    }

    @Override
    public GameVariant currentGameVariant() {
        return menu.gameVariant();
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public void requestFocus() {
        menu.requestFocus();
    }

    @Override
    public void onExit() {
        menu.stopDrawing();
    }
}