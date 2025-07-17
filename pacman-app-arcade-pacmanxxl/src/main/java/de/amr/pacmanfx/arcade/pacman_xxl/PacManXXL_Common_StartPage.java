/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.layout.StartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.theUI;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_Common_StartPage implements StartPage {

    private final StackPane root = new StackPane();
    private final PacManXXL_Common_StartPageMenu menu;

    public PacManXXL_Common_StartPage(GameContext gameContext) {
        ResourceManager rm = () -> PacManXXL_Common_StartPage.class;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        menu = new PacManXXL_Common_StartPageMenu(gameContext);
        // scale menu to take 90% of start page height
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.numTilesY() * TS));

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
        root.focusedProperty().addListener((py,ov,nv) -> {
            if (root.isFocused()) {
                Logger.info("Focus now on {}, passing to {}", root, menu);
                menu.canvas().requestFocus();
                if (menu.canvas().isFocused()) {
                    Logger.info("Focus now on {}", menu.canvas());
                }
                onEnter();
            }
        });
    }

    @Override
    public void onEnter() {
        String gameVariant = currentGameVariant();
        if (GameVariant.PACMAN_XXL.name().equals(gameVariant)) {
            Logger.info("Loading assets for game variant {}", gameVariant);
            theUI().config(gameVariant).storeAssets(theUI().theAssets());
        }
        else if (GameVariant.MS_PACMAN_XXL.name().equals(gameVariant)) {
            Logger.info("Loading assets for game variant {}", gameVariant);
            theUI().config(gameVariant).storeAssets(theUI().theAssets());
        }
        else {
            Logger.error("Invalid game variant: {}", gameVariant);
        }
        menu.soundEnabledProperty().bind(theUI().theSound().mutedProperty().not());
        menu.syncMenuState();
        menu.startAnimation();
    }

    @Override
    public void onExit() {
        menu.stopAnimation();
    }

    @Override
    public String currentGameVariant() {
        return menu.state().gameVariant;
    }

    @Override
    public Region layoutRoot() {
        return root;
    }
}