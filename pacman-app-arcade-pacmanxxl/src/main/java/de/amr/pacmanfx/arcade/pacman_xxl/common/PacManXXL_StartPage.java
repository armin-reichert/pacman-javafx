/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import java.util.Optional;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.uilib.Ufx.deriveFont;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.createWallpaper;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage {

    static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    static final ResourceManager RM = () -> PacManXXL_PacMan_UIConfig.class;
    static final Image WALLPAPER_IMAGE = RM.loadImage(ROOT_PATH + "graphics/screenshot.png");
    static final Media VARIANT_NARRATION = RM.loadMedia(ROOT_PATH + "sound/game-description.mp3");

    private final StackPane rootPane;
    private final PacManXXL_OptionMenu menu;

    private Game game;
    private final String title;

    private final Keyboard.StateListener keyboardHandler = (Keyboard keyboard) -> {
        if (keyboard.isKeyPressed(KeyCode.E)) {
            pauseProgressTimer();
            game.actions().editorActions().actionOpenEditor().execute();
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
            pauseProgressTimer();
            game.start();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            game.ui().shortMessage("OK, I shut my mouth");
            stopTalking();
        }
    };

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
        game.input().keyboard().addStateListener(keyboardHandler);
    }

    @Override
    public void onEnter() {
        final GameVariantID selectedGameVariant = menu.menuEntries().gameVariantID().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL -> game.selectGameVariant(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        game.ui().sounds().playVoice(VARIANT_NARRATION);
        menu.init(game);
        menu.bind();
        Platform.runLater(menu::requestFocus);
    }

    @Override
    public void onExit() {
        stopTalking();
        menu.unbind();
        menu.stopDrawLoop();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public Optional<Node> startButton() {
        return Optional.empty();
    }

    @Override
    public String title() {
        return title;
    }

    // Private area

    private void pauseProgressTimer() {
        game.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().pauseProgressTimer();
    }

    private void stopTalking() {
        game.ui().sounds().stopAndDisposeVoice();
    }
}