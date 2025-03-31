/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.model.MapSelector;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class PacManXXL_StartPage extends StackPane implements StartPage, ResourceManager {

    private final PacManXXL_OptionMenu menu;

    @Override
    public Class<?> resourceRootClass() {
        return PacManXXL_StartPage.class;
    }

    public PacManXXL_StartPage(GameVariant gameVariant) {
        setBackground(Background.fill(Color.BLACK));
        Flyer flyer = new Flyer(loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);
        menu = new PacManXXL_OptionMenu(36*TS);
        //TODO check this:
        menu.setState(true, gameVariant, false, MapSelectionMode.CUSTOM_MAPS_FIRST, true);
        menu.scalingProperty().bind(heightProperty().multiply(0.9).divide(menu.unscaledHeight()));

        getChildren().addAll(flyer, menu.root());
    }

    @Override
    public GameVariant currentGameVariant() {
        return menu.state().gameVariant;
    }

    @Override
    public Node root() {
        return this;
    }

    @Override
    public void requestFocus() {
        menu.root().requestFocus();
    }

    @Override
    public void onEnter() {
        switch (currentGameVariant()) {
            case MS_PACMAN_XXL, PACMAN_XXL -> {
                GameModel game = THE_GAME_CONTROLLER.game(currentGameVariant());
                MapSelector mapSelector = game.mapSelector();
                mapSelector.loadAllMaps(game);
                menu.setState(
                    GlobalProperties3d.PY_3D_ENABLED.get(),
                    currentGameVariant(),
                    game.isCutScenesEnabled(),
                    mapSelector.mapSelectionMode(),
                    !mapSelector.customMaps().isEmpty()
                );
                menu.root().requestFocus();
                menu.startDrawingLoop();
            }
            default -> throw new IllegalStateException("Illegal game variant for this start page: %s".formatted(currentGameVariant()));
        }
    }

    @Override
    public void onExit() {
        menu.stopDrawingLoop();
    }
}