/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.json.JsonLoader;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GameVariantUIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuSettings;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class XXL_StartPage implements StartPage {

    static final ResourceManager RM = () -> XXL_PacMan_GameVariantUIConfig.class;

    static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    static final String OPTION_MENU_SETTINGS_PATH = ROOT_PATH + "option-menu.json";

    static final Image WALLPAPER_IMAGE   = RM.loadImage(ROOT_PATH + "graphics/screenshot.png");
    static final Media VARIANT_NARRATION = RM.loadMedia(ROOT_PATH + "sound/game-description.mp3");

    private final StackPane rootPane = new StackPane();
    private final XXL_OptionMenu menu;
    private final String title;

    private GameAppContext app;

    public XXL_StartPage() {
        title = "Pac-Man XXL games"; // TODO localize

        final OptionMenuSettings menuSettings = JsonLoader.load(
            getClass().getResource(OPTION_MENU_SETTINGS_PATH),
            OptionMenuSettings.class
        );
        menu = new XXL_OptionMenu(menuSettings);

        rootPane.getChildren().add(menu.rootPane());

        rootPane.setBackground(Ufx.createWallpaper(WALLPAPER_IMAGE));

        // Requesting focus is important e.g. when quitting the game scene and returning to the menu!
        rootPane.focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus) {
                Platform.runLater(menu::requestFocus);
            }
        });
    }

    @Override
    public GameAppContext app() {
        return app;
    }

    @Override
    public void setGameApp(GameAppContext app) {
        this.app = requireNonNull(app);
        // Ensure both game variants are available
        app.gameVariants().registerGameVariant(GameVariantID.ARCADE_PACMAN_XXL.name());
        app.gameVariants().registerGameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name());
    }

    @Override
    public void onInput() {
        final Keyboard keyboard = app.input().keyboard();
        if (keyboard.isKeyPressed(KeyCode.E)) {
            pauseProgressTimer();
            app.runAction(app.commonActions().editorActions().actionOpenEditor());
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
            pauseProgressTimer();
            final WorldMapSelectionMode mode = menu.selectedMapSelectionMode();
            XXL_WorldMapManager.instance().setSelectionMode(mode);
            app.startGame();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            app.ui().shortMessage("OK, I shut my mouth");
            stopTalking();
        }
    }

    @Override
    public void onEnter() {
        final GameVariantID selectedGameVariantID = menu.meGameVariantID().value();
        switch (selectedGameVariantID) {
            case ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL -> app.gameVariants().selectVariant(selectedGameVariantID.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariantID);
        }

        menu.init(app);
        menu.bind();
        menu.startAnimation();

        Platform.runLater(() -> {
            menu.requestFocus();
            startTalking();
        });
    }

    @Override
    public void onExit() {
        stopTalking();
        menu.unbind();
        menu.stopDrawLoop();
        menu.stopAnimation();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title;
    }

    // Private area

    private void pauseProgressTimer() {
        app.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().pauseProgress();
    }

    private void startTalking() {
        app.ui().sounds().voice().playAfterSec(0.5, VARIANT_NARRATION);
    }

    private void stopTalking() {
        app.ui().sounds().voice().stop();
    }
}