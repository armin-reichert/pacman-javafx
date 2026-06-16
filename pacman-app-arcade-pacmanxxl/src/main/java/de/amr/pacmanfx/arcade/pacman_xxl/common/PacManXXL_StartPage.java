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
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage {

    private static final String ROOT_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";

    private static final ResourceManager RM = () -> PacManXXL_PacMan_UIConfig.class;

    private static final Image WALLPAPER_IMAGE = RM.loadImage(ROOT_PATH + "graphics/screenshot.png");

    private static final Media VOICE = RM.loadMedia(ROOT_PATH + "sound/game-description.mp3");

    private static final int   MENU_MIN_HEIGHT = 400;
    private static final int   MENU_MAX_HEIGHT = 800;
    private static final float MENU_REL_HEIGHT = 0.66f;

    private static final OptionMenuStyle DEFAULT_MENU_STYLE = OptionMenuStyle.builder()
        .titleFont(Ufx.deriveFont(GameUI_Constants.FONT_PAC_FONT_GOOD, 4 * TS))
        .titleTextFill(ArcadePalette.ARCADE_RED)
        .textFont(Ufx.deriveFont(GameUI_Constants.FONT_ARCADE_8, TS))
        .entryTextFill(ArcadePalette.ARCADE_YELLOW)
        .entryValueFill(ArcadePalette.ARCADE_WHITE)
        .usageTextFill(ArcadePalette.ARCADE_YELLOW)
        .build();

    // Menu must adapt to selected game variant and global property change and scales with scene resize
    private class MenuUpdater {

        private ChangeListener<GameVariantID> gameVariantNameListener;
        private ChangeListener<Boolean> cutScenesEnabledListener;
        private ChangeListener<Boolean> play3DListener;
        private ObservableValue<Double> scaling;

        public void connect(Game game) {
            gameVariantNameListener = (_, _, variant) -> game.selectGameVariant(variant.name());
            play3DListener = (_, _, enable3D) -> game.ui().settings3D().view3DEnabledProperty().set(enable3D);
            cutScenesEnabledListener = (_, _, enableCutScenes) -> game.currentGameContext().flow().setCutScenesEnabled(enableCutScenes);

            scaling = game.ui().window().stage().heightProperty().map(stageHeight -> {
                final double menuHeight = Math.clamp(stageHeight.doubleValue() * MENU_REL_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
                final double scaling = menuHeight / TS(menu.numTilesY());
                return Math.round(scaling * 100.0) / 100.0; // rounded to 2 decimal digits to avoid too much resizing
            });
        }

        public void update() {
            clear();
            menu.entryGameVariant().valueProperty().addListener(gameVariantNameListener);
            menu.entryPlay3D().valueProperty().addListener(play3DListener);
            menu.entryCutScenesEnabled().valueProperty().addListener(cutScenesEnabledListener);
            menu.scalingProperty().bind(scaling);
        }

        public void clear() {
            menu.entryGameVariant().valueProperty().removeListener(gameVariantNameListener);
            menu.entryPlay3D().valueProperty().removeListener(play3DListener);
            menu.entryCutScenesEnabled().valueProperty().removeListener(cutScenesEnabledListener);
            menu.scalingProperty().unbind();
        }
    }

    private class KeyboardInputHandler implements Keyboard.StateListener{

        @Override
        public void onKeyboardStateChange(Keyboard keyboard) {
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
        }
    }

    private final StackPane rootPane = new StackPane();
    private final PacManXXL_OptionMenu menu = new PacManXXL_OptionMenu();

    private Game game;
    private final String title;

    private final MenuUpdater menuUpdater = new MenuUpdater();
    private final KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler();


    public PacManXXL_StartPage() {
        title = "Pac-Man XXL games"; // TODO localize
        menu.setStyle(DEFAULT_MENU_STYLE);
        rootPane.getChildren().add(menu.rootPane());
        rootPane.setBackground(UfxBackgrounds.createWallpaper(WALLPAPER_IMAGE));

        rootPane.focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus) {
                menu.init(game);
                menuUpdater.update();
                game.input().keyboard().removeStateListener(keyboardInputHandler);
                game.input().keyboard().addStateListener(keyboardInputHandler);
            }
        });
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
        menuUpdater.connect(game);
        menuUpdater.update();
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
        game.input().keyboard().addStateListener(keyboardInputHandler);
    }

    @Override
    public void onExit() {
        stopTalking(game);
        menu.stopDrawLoop();
        menuUpdater.clear();
        game.input().keyboard().removeStateListener(keyboardInputHandler);
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


    private void pauseProgressTimer(Game game) {
        game.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class).pauseProgressTimer();
    }

    private void stopTalking(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
    }
}