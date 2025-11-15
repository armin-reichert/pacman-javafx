/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.PredefinedGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.StartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_Common_StartPage implements StartPage {

    private static final String BACKGROUND_IMAGE_PATH = "graphics/screenshot.png";

    private final StackPane root = new StackPane();
    private final PacManXXL_Common_StartPageMenu menu;

    public PacManXXL_Common_StartPage(GameUI ui) {
        ResourceManager rm = () -> PacManXXL_Common_StartPage.class;
        Flyer flyer = new Flyer(rm.loadImage(BACKGROUND_IMAGE_PATH));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        menu = new PacManXXL_Common_StartPageMenu(ui);
        // scale menu to take 90% of start page height
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.numTilesY() * TS));

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
        root.focusedProperty().addListener((py,ov,nv) -> {
            if (root.isFocused()) {
                Logger.info("Focus now on {}, passing to {}", root, menu);
                menu.canvas().requestFocus();
                menu.draw();
                if (menu.canvas().isFocused()) {
                    Logger.info("Focus now on {}", menu.canvas());
                }
                onEnter(ui);
            }
        });
    }

    @Override
    public void onEnter(GameUI ui) {
        ui.selectGameVariant(menu.state().gameVariant);
        menu.soundEnabledProperty().bind(ui.soundManager().mutedProperty().not());
        menu.syncMenuState();
        menu.startAnimation();
    }

    @Override
    public void onExit(GameUI ui) {
        menu.stopAnimation();
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public String title() {
        String gameVariant = menu.state().gameVariant;
        if (PredefinedGameVariant.PACMAN_XXL.name().equals(gameVariant)) {
            return "Pac-Man XXL"; //TODO localize
        }
        if (PredefinedGameVariant.MS_PACMAN_XXL.name().equals(gameVariant)) {
            return "Ms. Pac-Man XXL"; //TODO localize
        }
        return "Unknown game variant";
    }

}