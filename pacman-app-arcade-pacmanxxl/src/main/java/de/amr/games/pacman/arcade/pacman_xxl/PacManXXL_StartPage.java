/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.UIGlobals.THE_UI;

public class PacManXXL_StartPage extends StackPane implements StartPage {

    private final PacManXXL_OptionMenu menu;

    public PacManXXL_StartPage() {
        ResourceManager rm = this::getClass;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);

        menu = new PacManXXL_OptionMenu();
        getChildren().addAll(flyer, menu.root());

        setBackground(Background.fill(Color.BLACK));

        //TODO find a more elegant way to start/stop the animation loop of the menu
        THE_UI.startPageSelectionView().selectedIndexProperty().addListener((py, ov, nv) -> {
            THE_UI.startPageSelectionView().currentSlide().ifPresent(startPage -> {
                if (startPage == this) {
                    menu.startDrawingLoop();
                } else {
                    menu.stopDrawingLoop();
                }
            });
        });
        THE_UI.viewProperty().addListener((py, ov, view) -> {
            if (view == THE_UI.startPageSelectionView()
                    && THE_UI.startPageSelectionView().currentSlide().isPresent()
                    && THE_UI.startPageSelectionView().currentSlide().get() == this) {
                menu.startDrawingLoop();
            } else {
                menu.stopDrawingLoop();
            }
        });
    }

    private void initMenuState() {
        switch (THE_GAME_CONTROLLER.selectedGameVariant()) {
            case MS_PACMAN_XXL, PACMAN_XXL -> {
                THE_GAME_CONTROLLER.game().mapSelector().loadAllMaps(THE_GAME_CONTROLLER.game());
                menu.setState(
                    GlobalProperties3d.PY_3D_ENABLED.get(),
                        THE_GAME_CONTROLLER.selectedGameVariant(),
                        THE_GAME_CONTROLLER.game().isCutScenesEnabled(),
                        THE_GAME_CONTROLLER.game().mapSelector().mapSelectionMode(),
                    !THE_GAME_CONTROLLER.game().mapSelector().customMaps().isEmpty()
                );
            }
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public void requestFocus() {
        menu.root().requestFocus();
        initMenuState();
    }

    @Override
    public void start() {}

    @Override
    public Node root() {
        return this;
    }
}