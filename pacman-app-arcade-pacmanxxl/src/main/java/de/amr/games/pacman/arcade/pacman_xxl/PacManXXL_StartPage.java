/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._2d.StartPageSelectionView;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PacManXXL_StartPage extends StackPane implements StartPage {

    private final PacManGamesUI ui;
    private final PacManXXL_OptionMenu menu;

    public PacManXXL_StartPage(PacManGamesUI ui) {
        this.ui = ui;

        ResourceManager rm = this::getClass;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);

        menu = new PacManXXL_OptionMenu(ui);
        getChildren().addAll(flyer, menu.root());

        setBackground(Background.fill(Color.BLACK));

        //TODO find a more elegant way to start/stop the animation loop of the menu
        final StartPageSelectionView carousel = ui.startPageSelectionView();
        carousel.selectedIndexProperty().addListener((py, ov, nv) -> {
            carousel.currentSlide().ifPresent(startPage -> {
                if (startPage == this) {
                    menu.startDrawingLoop();
                } else {
                    menu.stopDrawingLoop();
                }
            });
        });
        ui.viewProperty().addListener((py, ov, page) -> {
            if (page == carousel && carousel.currentSlide().isPresent() && carousel.currentSlide().get() == this) {
                menu.startDrawingLoop();
            } else {
                menu.stopDrawingLoop();
            }
        });
    }

    private void initMenuState() {
        switch (ui.gameVariant()) {
            case MS_PACMAN_XXL, PACMAN_XXL -> {
                ui.game().mapSelector().loadAllMaps(ui.game());
                menu.setState(
                    GlobalProperties3d.PY_3D_ENABLED.get(),
                    ui.gameVariant(),
                    ui.game().isCutScenesEnabled(),
                    ui.game().mapSelector().mapSelectionMode(),
                    !ui.game().mapSelector().customMaps().isEmpty()
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