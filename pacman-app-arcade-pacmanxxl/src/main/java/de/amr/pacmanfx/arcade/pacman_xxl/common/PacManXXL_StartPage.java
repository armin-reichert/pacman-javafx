/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.d3.Constants3D;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameConstants;
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
        .titleFont(Ufx.deriveFont(GameConstants.FONT_PAC_FONT_GOOD, 4 * TS))
        .titleTextFill(ArcadePalette.ARCADE_RED)
        .textFont(Ufx.deriveFont(GameConstants.FONT_ARCADE_8, TS))
        .entryTextFill(ArcadePalette.ARCADE_YELLOW)
        .entryValueFill(ArcadePalette.ARCADE_WHITE)
        .usageTextFill(ArcadePalette.ARCADE_YELLOW)
        .build();

    private final StringProperty title = new SimpleStringProperty("Pac-Man XXL games");

    // Menu must adapt to selected game variant and global property change and scales with scene resize
    private class MenuBinding {

        private final ChangeListener<GameVariantID> gameVariantNameListener;
        private final ChangeListener<Boolean> cutScenesEnabledListener;
        private final ChangeListener<Boolean> play3DListener;
        private final ObservableValue<Double> scaling;

        public MenuBinding(Game game) {
            gameVariantNameListener = (_, _, variant) -> game.selectGameVariant(variant.name());
            play3DListener = (_, _, enable3D) -> Constants3D.PROPERTY_3D_ENABLED.set(enable3D);
            cutScenesEnabledListener = (_, _, enableCutScenes) -> game.currentGameContext().flow().setCutScenesEnabled(enableCutScenes);

            scaling = game.ui().view().stageProperty().map(stage -> {
                final double menuHeight = Math.clamp(stage.getHeight() * MENU_REL_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
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
        private final Game game;

        public KeyboardInputHandler(Game game) {
            this.game = game;
        }

        @Override
        public void onKeyboardStateChange(Keyboard keyboard) {
            if (keyboard.isKeyPressed(KeyCode.E)) {
                openEditor(game);
            }
            else if (keyboard.isKeyPressed(KeyCode.ENTER)) {
                startSelectedGame(game);
            }

        }
    }

    private final StackPane rootPane = new StackPane();
    private final PacManXXL_OptionMenu menu = new PacManXXL_OptionMenu();

    private MenuBinding menuBinding;
    private KeyboardInputHandler keyboardInputHandler;

    public PacManXXL_StartPage() {
        rootPane.getChildren().add(menu.rootPane());
        rootPane.setBackground(UfxBackgrounds.createWallpaper(WALLPAPER));
        menu.setStyle(MENU_STYLE);
    }

    @Override
    public void init(Game game) {
        requireNonNull(game);

        if (menuBinding == null) {
            menuBinding = new MenuBinding(game);
        }
        menuBinding.update();

        if (keyboardInputHandler == null) {
            keyboardInputHandler = new KeyboardInputHandler(game);
        }

        rootPane.focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus) {
                menu.init(game);
                menuBinding.update();
                game.input().keyboard().removeStateListener(keyboardInputHandler);
                game.input().keyboard().addStateListener(keyboardInputHandler);
            }
        });

    }

    @Override
    public void onEnterStartPage(Game game) {
        final GameVariantID selectedGameVariant = menu.entryGameVariant().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> game.selectGameVariant(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        menu.init(game);
        game.input().keyboard().addStateListener(keyboardInputHandler);
        game.ui().sounds().playVoice(VOICE);
    }

    @Override
    public void onExitStartPage(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
        game.input().keyboard().removeStateListener(keyboardInputHandler);
        menu.stopDrawLoop();
        menuBinding.clear();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title.get();
    }

    // Private

    private void openEditor(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
        game.ui().subViews().startView().pauseProgressTimer();
        CommonActions.ACTION_OPEN_EDITOR.execute(game);
    }

    private void startSelectedGame(Game game) {
        game.ui().sounds().stopAndDisposeVoice();
        game.ui().subViews().startView().pauseProgressTimer();
        menu.startGame();
    }
}