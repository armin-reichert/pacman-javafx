/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.ArcadePalette;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_StartPage;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage extends StackPane implements GameUI_StartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_StartPage.class;
    private static final Image WALLPAPER = LOCAL_RESOURCES.loadImage("graphics/screenshot.png");
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/game-description.mp3");

    private static final int MENU_MIN_HEIGHT = 400;
    private static final int MENU_MAX_HEIGHT = 800;
    private static final double RELATIVE_MENU_HEIGHT = 0.66;

    private final StringProperty title = new SimpleStringProperty("Pac-Man XXL games");
    private final PacManXXL_OptionMenu menu;

    private GameUI ui;

    private ChangeListener<StandardGameVariant> gameVariantNameListener;
    private ChangeListener<Boolean> cutScenesEnabledListener;
    private ChangeListener<Boolean> play3DListener;

    public PacManXXL_StartPage() {
        final OptionMenuStyle style = OptionMenuStyle.builder()
            .titleFont(Ufx.deriveFont(GameUI.FONT_PAC_FONT_GOOD, 4*TS))
            .titleTextFill(ArcadePalette.ARCADE_RED)
            .textFont(Ufx.deriveFont(GameUI.FONT_ARCADE_8, TS))
            .entryTextFill(ArcadePalette.ARCADE_YELLOW)
            .entryValueFill(ArcadePalette.ARCADE_WHITE)
            .usageTextFill(ArcadePalette.ARCADE_YELLOW)
            .build();

        menu = new PacManXXL_OptionMenu();
        menu.setStyle(style);

        setBackground(Ufx.createWallpaper(WALLPAPER));
        getChildren().addAll(menu.root());

        focusedProperty().addListener((_, _, hasFocus) -> {
            if (hasFocus) {
                updateMenuEntryValueListeners();
                Logger.info("Input focus on {}, passing to {}...", this, menu);
                menu.init(ui);
            }
        });

        addKeyEventHandler();
    }

    @Override
    public void init(GameUI ui) {
        this.ui = requireNonNull(ui);
        updateMenuEntryValueListeners();
    }

    private void addKeyEventHandler() {
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            ui.startPagesView().pauseTimer();
            switch (e.getCode()) {
                case E -> {
                    e.consume();
                    ui.voicePlayer().stop();
                    ui.showEditorView();
                }
                case ENTER -> {
                    e.consume();
                    ui.voicePlayer().stop();
                    menu.startSelectedGame();
                }
                default -> Logger.info("Key '{}': No action assigned.", e.getCode());
            }
        });
    }

    private void updateMenuEntryValueListeners() {
        removeMenuEntryValueListeners();

        gameVariantNameListener = (_, _, newVariant) -> ui.context().selectGameByName(newVariant.name());
        menu.entryGameVariant().valueProperty().addListener(gameVariantNameListener);

        play3DListener = (_, _, play3D) -> {
            GameUI.PROPERTY_3D_ENABLED.set(play3D);
            Logger.info("PROPERTY_3D_ENABLED is set to {}.", play3D);
        };
        menu.entryPlay3D().valueProperty().addListener(play3DListener);

        cutScenesEnabledListener = (_,_,enabled) -> ui.context().currentGame().setCutScenesEnabled(enabled);
        menu.entryCutScenesEnabled().valueProperty().addListener(cutScenesEnabledListener);

        menu.scalingProperty().bind(ui.stage().heightProperty().map(stageHeight -> {
            double menuHeight = stageHeight.doubleValue() * RELATIVE_MENU_HEIGHT;
            menuHeight = Math.clamp(menuHeight, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
            final double scaling = menuHeight / TS(menu.numTilesY());
            return Math.round(scaling * 100.0) / 100.0; // rounded to 2 decimal digits
        }));
    }

    private void removeMenuEntryValueListeners() {
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

    @Override
    public void onEnterStartPage(GameUI ui) {
        final StandardGameVariant selectedGameVariant = menu.entryGameVariant().value();
        switch (selectedGameVariant) {
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> ui.context().selectGameByName(selectedGameVariant.name());
            default -> throw new IllegalStateException("Unexpected game variant in XXL menu: " + selectedGameVariant);
        }
        menu.init(ui);
        ui.voicePlayer().play(VOICE);
    }

    @Override
    public void onExitStartPage(GameUI ui) {
        ui.voicePlayer().stop();
        menu.stopDrawLoop();
        removeMenuEntryValueListeners();
    }

    @Override
    public Region layoutRoot() {
        return this;
    }

    @Override
    public String title() {
        return title.get();
    }
}