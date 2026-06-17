/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.uilib.Ufx.deriveFont;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.createWallpaper;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage, ChangeListener<GameVariantID> {

    static final int   MENU_MIN_HEIGHT = 400;
    static final int   MENU_MAX_HEIGHT = 800;
    static final float MENU_REL_HEIGHT = 0.66f;

    static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    static final ResourceManager RM = () -> PacManXXL_PacMan_UIConfig.class;
    static final Image WALLPAPER_IMAGE = RM.loadImage(ROOT_PATH + "graphics/screenshot.png");
    static final Media VOICE = RM.loadMedia(ROOT_PATH + "sound/game-description.mp3");

    // Menu must adapt to selected game variant and global property change and scales with scene resize

    private ChangeListener<Boolean> cutScenesEnabledListener;
    private ChangeListener<Boolean> play3DListener;

    private ObservableValue<Double> scaling;

    private final StackPane rootPane;
    private final PacManXXL_OptionMenu menu;

    private Game game;
    private final String title;

    public PacManXXL_StartPage() {
        title = "Pac-Man XXL games"; // TODO localize

        menu = new PacManXXL_OptionMenu();
        menu.setStyle(OptionMenuStyle.builder()
            .titleFont        (deriveFont(GameUI_Constants.FONT_PAC_FONT_GOOD, 4 * TS))
            .titleTextFill    (ArcadePalette.ARCADE_RED)
            .textFont         (deriveFont(GameUI_Constants.FONT_ARCADE_8, TS))
            .entryTextFill    (ArcadePalette.ARCADE_YELLOW)
            .entryValueFill   (ArcadePalette.ARCADE_WHITE)
            .usageTextFill    (ArcadePalette.ARCADE_YELLOW)
            .build()
        );

        rootPane = new StackPane(menu.rootPane());
        rootPane.setBackground(createWallpaper(WALLPAPER_IMAGE));

        rootPane.focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus) {
                // This is important such that quitting the game scene and returning to the menu will work!
                Platform.runLater(menu::requestFocus);
            }
        });
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);

        play3DListener = (_, _, enable3D) -> game.ui().settings().d3().view3DEnabledProperty().set(enable3D);

        cutScenesEnabledListener = (_, _, enableCutScenes) -> game.currentGameContext().flow().setCutScenesEnabled(enableCutScenes);

        scaling = game.ui().window().stage().heightProperty().map(stageHeight -> {
            final double menuHeight = Math.clamp(stageHeight.doubleValue() * MENU_REL_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
            final double scaling = menuHeight / TS(menu.numTilesY());
            return Math.round(scaling * 100.0) / 100.0; // rounded to 2 decimal digits to avoid too much resizing
        });

        bindMenu();

        game.input().keyboard().addStateListener(keyboard -> {
            if (keyboard.isKeyPressed(KeyCode.E)) {
                pauseProgressTimer(game);
                game.actions().editorActions().actionOpenEditor().execute();
            }
            else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
                pauseProgressTimer(game);
                game.start();
            }
            else if (keyboard.isKeyPressed(KeyCode.S)) {
                game.ui().shortMessage("OK, I shut my mouth");
                stopTalking(game);
            }
        });
    }

    @Override
    public void onEnter() {
        final GameVariantID selectedGameVariant = menu.entryGameVariant().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL -> game.selectGameVariant(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        menu.init(game);
        game.ui().sounds().playVoice(VOICE);
    }

    @Override
    public void onExit() {
        stopTalking(game);
        menu.stopDrawLoop();
        unbindMenu();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title;
    }

    // ChangeListener<GameVariantID>

    @Override
    public void changed(ObservableValue<? extends GameVariantID> observable, GameVariantID oldVariantID, GameVariantID newVariantID) {
        game.selectGameVariant(newVariantID.name());
    }

    // Private area

    private void bindMenu() {
        unbindMenu();
        menu.entryGameVariant().valueProperty().addListener(this);
        menu.entryPlay3D().valueProperty().addListener(play3DListener);
        menu.entryCutScenesEnabled().valueProperty().addListener(cutScenesEnabledListener);
        menu.scalingProperty().bind(scaling);
    }

    private void unbindMenu() {
        menu.entryGameVariant().valueProperty().removeListener(this);
        menu.entryPlay3D().valueProperty().removeListener(play3DListener);
        menu.entryCutScenesEnabled().valueProperty().removeListener(cutScenesEnabledListener);
        menu.scalingProperty().unbind();
    }

    private void pauseProgressTimer(Game game) {
        game.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class).pauseProgressTimer();
    }

    private void stopTalking(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
    }
}