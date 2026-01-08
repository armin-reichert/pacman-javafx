/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_StartPage;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.uilib.animation.AnimationSupport.pauseSec;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements GameUI_StartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> PacManXXL_StartPage.class;
    private static final String BACKGROUND_IMAGE_PATH = "graphics/screenshot.png";

    private final MediaPlayer voicePlayer = new MediaPlayer(LOCAL_RESOURCES.loadMedia("sound/game-description.mp3"));
    private final StringProperty title = new SimpleStringProperty("Pac-Man XXL games");
    private final StackPane root = new StackPane();
    private final PacManXXL_OptionMenu menu;

    public PacManXXL_StartPage() {
        final var flyer = new Flyer(LOCAL_RESOURCES.loadImage(BACKGROUND_IMAGE_PATH));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer);

        final OptionMenuStyle style = OptionMenuStyle.builder()
            .titleFont(Ufx.deriveFont(GameUI.FONT_PAC_FONT_GOOD, 4*TS))
            .textFont(Ufx.deriveFont(GameUI.FONT_ARCADE_8, TS))
            .titleTextFill(ArcadePalette.ARCADE_RED)
            .entryTextFill(ArcadePalette.ARCADE_YELLOW)
            .entryValueFill(ArcadePalette.ARCADE_WHITE)
            .hintTextFill(ArcadePalette.ARCADE_YELLOW)
            .build();

        menu = new PacManXXL_OptionMenu();
        menu.setStyle(style);
        menu.setRenderer(new PacManXXL_OptionMenuRenderer(menu.canvas()));

        root.getChildren().add(menu.root());
    }

    @Override
    public void init(GameUI ui) {
        requireNonNull(ui);
        addEventHandlers(ui);
        bindMenu(ui);
    }

    private void addEventHandlers(GameUI ui) {
        root.focusedProperty().addListener((_, _, _) -> {
            if (root.isFocused()) {
                Logger.info("Focus now on {}, passing to {}", root, menu);
                onEnterStartPage(ui);
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
                    menu.startSelectedGame();
                }
                case ESCAPE -> voicePlayer.stop();
            }
        });
    }

    private void bindMenu(GameUI ui) {
        ui.context().gameVariantNameProperty().addListener(
            (_, _, gameVariantName) -> {
                if (ARCADE_PACMAN_XXL.name().equals(gameVariantName) || ARCADE_MS_PACMAN_XXL.name().equals(gameVariantName)) {
                    menu.init(ui);
                }
            });

        menu.entryCutScenesEnabled.valueProperty().addListener(
            (_,_,enabled) -> ui.context().currentGame().setCutScenesEnabled(enabled));

        menu.entryPlay3D.valueProperty().addListener(
            (_, _, play3D) -> GameUI.PROPERTY_3D_ENABLED.set(play3D));

        menu.scalingProperty().bind(ui.stage().heightProperty().map(
            height -> {
                double h = height.doubleValue();
                h *= 0.8; // take 80% of stage height
                h /= TS(menu.numTilesY()); // scale according to menu height
                return Math.round(h * 100.0) / 100.0; // round to 2 decimal digits
            }));
    }

    @Override
    public void onEnterStartPage(GameUI ui) {
        pauseSec(1, voicePlayer::play).play();

        menu.requestFocus();
        menu.startDrawLoop();

        menu.entryPlay3D.valueProperty().set(GameUI.PROPERTY_3D_ENABLED.get());
        final StandardGameVariant selectedGameVariant = menu.entryGameVariant.value();
        switch (selectedGameVariant) {
            case null -> ui.selectGameVariant(ARCADE_PACMAN_XXL.name());
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> {
                ui.selectGameVariant(selectedGameVariant.name());
                menu.init(ui);
            }
            default -> Logger.error("Unexpected game variant in XXL menu: {}", selectedGameVariant);
        }
    }

    @Override
    public void onExitStartPage(GameUI ui) {
        voicePlayer.stop();
        menu.stopDrawLoop();
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public String title() {
        return title.get();
    }
}