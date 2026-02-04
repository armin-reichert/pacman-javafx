/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.ArcadePalette;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.StartPage;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage extends StackPane implements StartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_StartPage.class;
    private static final Image WALLPAPER = LOCAL_RESOURCES.loadImage("graphics/screenshot.png");
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/game-description.mp3");

    private static final int MENU_MIN_HEIGHT = 400;
    private static final int MENU_MAX_HEIGHT = 800;
    private static final double RELATIVE_MENU_HEIGHT = 0.66;

    private static final OptionMenuStyle MENU_STYLE = OptionMenuStyle.builder()
        .titleFont(Ufx.deriveFont(GameUI_Resources.FONT_PAC_FONT_GOOD, 4 * TS))
        .titleTextFill(ArcadePalette.ARCADE_RED)
        .textFont(Ufx.deriveFont(GameUI_Resources.FONT_ARCADE_8, TS))
        .entryTextFill(ArcadePalette.ARCADE_YELLOW)
        .entryValueFill(ArcadePalette.ARCADE_WHITE)
        .usageTextFill(ArcadePalette.ARCADE_YELLOW)
        .build();

    private final StringProperty title = new SimpleStringProperty("Pac-Man XXL games");

    private final PacManXXL_OptionMenu menu;

    private GameUI ui;

    private ChangeListener<GameVariant> gameVariantNameListener;
    private ChangeListener<Boolean> cutScenesEnabledListener;
    private ChangeListener<Boolean> play3DListener;

    public PacManXXL_StartPage() {
        setBackground(Ufx.createWallpaper(WALLPAPER));

        menu = new PacManXXL_OptionMenu();
        menu.setStyle(MENU_STYLE);
        getChildren().addAll(menu.root());

        focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus && ui != null) {
                updateMenuBinding(ui.stage());
                Logger.info("Input focus on {}, passing to {}...", this, menu);
                menu.init(ui);
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case E -> {
                    e.consume();
                    if (ui != null) {
                        ui.voicePlayer().stopVoice();
                        ui.views().getStartPagesView().pauseProgressTimer();
                        ui.showEditorView();
                    }
                }
                case ENTER -> {
                    e.consume();
                    if (ui != null) {
                        ui.voicePlayer().stopVoice();
                        ui.views().getStartPagesView().pauseProgressTimer();
                        menu.startSelectedGame();
                    }
                }
                default -> Logger.info("Key '{}': No start page action assigned.", e.getCode());
            }
        });
    }

    @Override
    public void init(GameUI ui) {
        this.ui = requireNonNull(ui);
        updateMenuBinding(ui.stage());
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        final GameVariant selectedGameVariant = menu.entryGameVariant().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> ui.context().setGameVariantName(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        menu.init(ui);
        ui.voicePlayer().playVoice(VOICE);
    }

    @Override
    public void onExitStartPage(GameUI ui) {
        ui.voicePlayer().stopVoice();
        menu.stopDrawLoop();
        removeMenuBinding();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }

    @Override
    public String title() {
        return title.get();
    }

    private void updateMenuBinding(Stage stage) {
        removeMenuBinding();

        gameVariantNameListener = (_, _, newVariant) -> ui.context().setGameVariantName(newVariant.name());
        menu.entryGameVariant().valueProperty().addListener(gameVariantNameListener);

        play3DListener = (_, _, play3D) -> GameUI.PROPERTY_3D_ENABLED.set(play3D);
        menu.entryPlay3D().valueProperty().addListener(play3DListener);

        cutScenesEnabledListener = (_,_,enabled) -> ui.context().currentGame().setCutScenesEnabled(enabled);
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
                stageHeight.doubleValue() * RELATIVE_MENU_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
            final double scaling = menuHeight / TS(menu.numTilesY());
            return Math.round(scaling * 100.0) / 100.0; // rounded to 2 decimal digits to avoid to much resizing
        });
    }
}