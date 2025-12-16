/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_StartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_Common_StartPage implements GameUI_StartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_Common_StartPage.class;
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/game-description.mp3");

    private static final String BACKGROUND_IMAGE_PATH = "graphics/screenshot.png";

    private final StackPane root = new StackPane();
    private final PacManXXL_Common_StartPageMenu menu;
    private final MediaPlayer voicePlayer = new MediaPlayer(VOICE);

    public PacManXXL_Common_StartPage(GameUI ui) {
        Flyer flyer = new Flyer(LOCAL_RESOURCES.loadImage(BACKGROUND_IMAGE_PATH));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        menu = new PacManXXL_Common_StartPageMenu(ui);
        menu.scalingProperty().bind(ui.stage().heightProperty()
            .map(height -> {
                double h = height.doubleValue();
                h *= 0.8; // take 80% of stage height
                h /= TS(menu.numTilesY()); // scale according to menu height
                return Math.round(h * 100.0) / 100.0; // round to 2 decimal digits
            }));

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
        root.focusedProperty().addListener((_, _, _) -> {
            if (root.isFocused()) {
                Logger.info("Focus now on {}, passing to {}", root, menu);
                menu.requestFocus();
                menu.draw();
                onEnter(ui);
            } else {
                onExit(ui);
            }
        });

        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            Logger.info("Key '{}' pressed!", e.getCode());
            ui.startPagesView().pauseTimer();
            switch (e.getCode()) {
                case E -> {
                    voicePlayer.stop();
                    ui.showEditorView();
                }
                case ENTER -> {
                    voicePlayer.stop();
                    menu.startGame();
                }
                case ESCAPE -> voicePlayer.stop();
            }
        });
    }

    @Override
    public void onEnter(GameUI ui) {
        ui.selectGameVariant(menu.state().gameVariant);

        menu.soundEnabledProperty().bind(ui.soundManager().mutedProperty().not());
        menu.syncMenuState();
        menu.startAnimation();

        voicePlayer.play();
    }

    @Override
    public void onExit(GameUI ui) {
        voicePlayer.stop();
        menu.stopAnimation();
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public String title() {
        String gameVariant = menu.state().gameVariant;
        if (StandardGameVariant.PACMAN_XXL.name().equals(gameVariant)) {
            return "Pac-Man XXL"; //TODO localize
        }
        if (StandardGameVariant.MS_PACMAN_XXL.name().equals(gameVariant)) {
            return "Ms. Pac-Man XXL"; //TODO localize
        }
        return "Unknown game variant";
    }

}