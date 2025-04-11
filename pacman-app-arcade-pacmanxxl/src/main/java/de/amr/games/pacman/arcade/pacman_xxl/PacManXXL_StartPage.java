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

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage, ResourceManager {

    private final StackPane root = new StackPane();
    private final PacManXXL_OptionMenu menu = new PacManXXL_OptionMenu();

    public PacManXXL_StartPage() {
        Flyer flyer = new Flyer(loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        // scale menu to take 90% of start page height
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.numTiles() * TS));

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
    }

    @Override
    public Class<?> resourceRootClass() {
        return PacManXXL_StartPage.class;
    }

    @Override
    public void onEnter() {
        initMenuState();
        menu.startDrawing();
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

    private void initMenuState() {
        if (menu.gameVariant == null) {
            menu.setGameVariant(GameVariant.PACMAN_XXL);
        }
        GameModel game = THE_GAME_CONTROLLER.game(menu.gameVariant);
        menu.setPlay3D(PY_3D_ENABLED.get());
        menu.setCutScenesEnabled(game.isCutScenesEnabled());
        game.mapSelector().loadAllMaps(game);
        menu.setMapOrder(game.mapSelector().mapSelectionMode(), !game.mapSelector().customMaps().isEmpty());
        Logger.info("Option menu initialized");
        menu.logMenuState();
    }
}