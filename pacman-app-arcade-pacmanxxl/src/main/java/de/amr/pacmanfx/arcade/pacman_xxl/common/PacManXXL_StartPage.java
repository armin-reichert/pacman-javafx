/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.subviews.startpages.StartPage;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage {

    private static final ResourceManager XXL_RES = () -> PacManXXL_PacMan_UIConfig.class;
    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";

    private static final Image WALLPAPER = XXL_RES.loadImage(XXL_PATH + "graphics/screenshot.png");
    private static final Media VOICE     = XXL_RES.loadMedia(XXL_PATH + "sound/game-description.mp3");

    private static final int   MENU_MIN_HEIGHT = 400;
    private static final int   MENU_MAX_HEIGHT = 800;
    private static final float MENU_REL_HEIGHT = 0.66f;

    private static final OptionMenuStyle MENU_STYLE = OptionMenuStyle.builder()
        .titleFont(Ufx.deriveFont(AppConstants.FONT_PAC_FONT_GOOD, 4 * TS))
        .titleTextFill(ArcadePalette.ARCADE_RED)
        .textFont(Ufx.deriveFont(AppConstants.FONT_ARCADE_8, TS))
        .entryTextFill(ArcadePalette.ARCADE_YELLOW)
        .entryValueFill(ArcadePalette.ARCADE_WHITE)
        .usageTextFill(ArcadePalette.ARCADE_YELLOW)
        .build();

    private final StringProperty title = new SimpleStringProperty("Pac-Man XXL games");

    private final StackPane rootPane = new StackPane();
    private final PacManXXL_OptionMenu menu;

    private AppContext context;

    private ChangeListener<GameVariant> gameVariantNameListener;
    private ChangeListener<Boolean> cutScenesEnabledListener;
    private ChangeListener<Boolean> play3DListener;

    public PacManXXL_StartPage() {
        menu = new PacManXXL_OptionMenu();
        menu.setStyle(MENU_STYLE);
        rootPane.getChildren().add(menu.rootPane());

        rootPane.setBackground(UfxBackgrounds.createWallpaper(WALLPAPER));

        rootPane.focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus && context != null) {
                updateMenuBinding(context.ui().view().stage());
                Logger.info("Input focus on {}, passing to {}...", this, menu);
                menu.init(context);
            }
        });
    }

    @Override
    public void init(AppContext context) {
        this.context = requireNonNull(context);
        updateMenuBinding(context.ui().view().stage());
        installKeyboardHandler(context.input().keyboard);
    }

    private void installKeyboardHandler(Keyboard keyboard) {
        keyboard.addStateListener(_ -> {
            Logger.info("Keyboard state change, keyboard = {}", keyboard);
            if (keyboard.isKeyPressed(KeyCode.E)) {
                context.ui().sounds().stopAndDisposeVoice();
                context.ui().subViews().startView().pauseProgressTimer();
                context.editMap(null);
            }
            else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
                context.ui().sounds().stopAndDisposeVoice();
                context.ui().subViews().startView().pauseProgressTimer();
                menu.startSelectedGame();
            }
        });
    }

    @Override
    public void onEnterStartPage(AppContext context) {
        final GameVariant selectedGameVariant = menu.entryGameVariant().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> context.gameContext().select(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        menu.init(context);
        context.ui().sounds().playVoice(VOICE);
    }

    @Override
    public void onExitStartPage(AppContext context) {
        context.ui().sounds().stopAndDisposeVoice();
        menu.stopDrawLoop();
        removeMenuBinding();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title.get();
    }

    private void updateMenuBinding(Stage stage) {
        removeMenuBinding();

        gameVariantNameListener = (_, _, newVariant) -> context.gameContext().select(newVariant.name());
        menu.entryGameVariant().valueProperty().addListener(gameVariantNameListener);

        play3DListener = (_, _, play3D) -> AppConstants.PROPERTY_3D_ENABLED.set(play3D);
        menu.entryPlay3D().valueProperty().addListener(play3DListener);

        cutScenesEnabledListener = (_,_,enabled) -> context.currentGameFlow().setCutScenesEnabled(enabled);
        menu.entryCutScenesEnabled().valueProperty().addListener(cutScenesEnabledListener);

        menu.scalingProperty().bind(menuScaling(stage));
    }

    private void removeMenuBinding() {
        if (gameVariantNameListener != null) {
            menu.entryGameVariant().valueProperty().removeListener(gameVariantNameListener);
        }

        if (play3DListener != null) {
            menu.entryPlay3D().valueProperty().removeListener(play3DListener);
        }

        if (cutScenesEnabledListener != null) {
            menu.entryCutScenesEnabled().valueProperty().removeListener(cutScenesEnabledListener);
        }

        menu.scalingProperty().unbind();
    }

    /**
     * Computes the scaling of the menu depending on the stage height. The option menu should take a relative amount of
     * the stage height that is clamped to the min/max height interval.
     *
     * @param stage the stage of the UI
     * @return the scaling binding depending on the height of the stage
     */
    private ObservableValue<Double> menuScaling(Stage stage) {
        return stage.heightProperty().map(stageHeight -> {
            final double menuHeight = Math.clamp(
                stageHeight.doubleValue() * MENU_REL_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
            final double scaling = menuHeight / TS(menu.numTilesY());
            return Math.round(scaling * 100.0) / 100.0; // rounded to 2 decimal digits to avoid too much resizing
        });
    }
}