/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.json.JsonLoader;
import de.amr.basics.ui.assets.ResourceManager;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuSettings;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import static de.amr.pacmanfx.engine.action.ActionExecutor.runAction;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class XXL_StartPage implements StartPage {

    static final ResourceManager RM = () -> XXL_PacMan_UIConfig.class;

    static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    static final String OPTION_MENU_SETTINGS_PATH = ROOT_PATH + "option-menu.json";

    static final Image WALLPAPER_IMAGE   = RM.loadImage(ROOT_PATH + "graphics/screenshot.png");
    static final Media VARIANT_NARRATION = RM.loadMedia(ROOT_PATH + "sound/game-description.mp3");

    private final StackPane rootPane = new StackPane();
    private final XXL_OptionMenu menu;
    private final String title;

    private EngineContext engine;

    public XXL_StartPage(String title) {
        this.title = title;

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
    public void setEngine(EngineContext engine) {
        this.engine = requireNonNull(engine);
        // Ensure both game variants are available
        engine.gameVariantManager().registerVariant(engine, GameVariantID.ARCADE_PACMAN_XXL.name());
        engine.gameVariantManager().registerVariant(engine, GameVariantID.ARCADE_MS_PACMAN_XXL.name());
    }

    @Override
    public void onInput(Input input) {
        final Keyboard keyboard = input.keyboard();
        if (keyboard.isKeyPressed(KeyCode.E)) {
            pauseProgressTimer();
            runAction(CommonGameActions.instance().editorActions().actionOpenEditor(), engine);
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
            pauseProgressTimer();
            final WorldMapSelectionMode mode = menu.selectedMapSelectionMode();
            XXL_WorldMapManager.instance().setSelectionMode(mode);
            engine.lifeCycle().startSession();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            engine.ui().showMessage("OK, I shut my mouth");
            stopTalking();
        }
    }

    @Override
    public void onEnter() {
        final GameVariantID selectedVariantID = menu.selectedGameVariantID();
        switch (selectedVariantID) {
            case ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL -> engine.gameVariantManager().selectVariant(engine, selectedVariantID.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedVariantID);
        }

        final GameVariantRuntime runtime = engine.gameVariantManager().currentRuntime();
        engine.uiConfig(runtime).load(engine);

        menu.init(engine);
        menu.bindEntries();
        menu.restartChaseAnimation(runtime);

        Platform.runLater(() -> {
            menu.requestFocus();
            startTalking();
        });
    }

    @Override
    public void onExit() {
        stopTalking();
        menu.unbindEntries();
        menu.stopDrawLoop();
        menu.stopChaseAnimation();
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
        final StartPagesView startPagesView = engine.viewManager().startPagesView();
        startPagesView.rootPane().pauseProgress();
    }

    private void startTalking() {
        engine.soundManager().voice().playAfterSec(0.5, VARIANT_NARRATION);
    }

    private void stopTalking() {
        engine.soundManager().voice().stop();
    }
}