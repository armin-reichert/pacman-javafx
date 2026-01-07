/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_StartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
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
    private static final Media VOICE = LOCAL_RESOURCES.loadMedia("sound/game-description.mp3");

    private static final String BACKGROUND_IMAGE_PATH = "graphics/screenshot.png";

    private final StackPane root = new StackPane();
    private final MediaPlayer voicePlayer = new MediaPlayer(VOICE);

    private PacManXXL_StartPageMenu menu;

    private final StringProperty title = new SimpleStringProperty("TITLE");

    public PacManXXL_StartPage() {
        final var flyer = new Flyer(LOCAL_RESOURCES.loadImage(BACKGROUND_IMAGE_PATH));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer);
    }

    @Override
    public void init(GameUI ui) {
        requireNonNull(ui);

        menu = new PacManXXL_StartPageMenu(ui);

        ui.context().gameVariantNameProperty().addListener((_, _, gameVariantName) -> {
            if (ARCADE_PACMAN_XXL.name().equals(gameVariantName) || ARCADE_MS_PACMAN_XXL.name().equals(gameVariantName)) {
                menu.init(ui);
            }
        });

        menu.cutScenesEnabledProperty().addListener((_,_,enabled) -> ui.context().currentGame().setCutScenesEnabled(enabled));

        menu.play3DProperty().addListener((_, _, play3D) -> GameUI.PROPERTY_3D_ENABLED.set(play3D));

        menu.scalingProperty().bind(ui.stage().heightProperty()
                .map(height -> {
                    double h = height.doubleValue();
                    h *= 0.8; // take 80% of stage height
                    h /= TS(menu.numTilesY()); // scale according to menu height
                    return Math.round(h * 100.0) / 100.0; // round to 2 decimal digits
                }));

        title.bind(Bindings.createStringBinding(
            () -> {
                final StandardGameVariant gameVariant = menu.gameVariant();
                final String playSceneMode = menu.play3D() ? "3D" : "2D";
                return gameVariant == null
                    ? ""
                    : ui.config(gameVariant.name()).assets().translated("app.title", playSceneMode);
            },
            menu.gameVariantProperty(), menu.play3DProperty()
        ));

        root.getChildren().add(menu.root());

        root.focusedProperty().addListener((_, _, _) -> {
            if (root.isFocused()) {
                Logger.info("Focus now on {}, passing to {}", root, menu);
                onEnter(ui);
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
                    menu.startGame(ui.context().currentGame());
                }
                case ESCAPE -> voicePlayer.stop();
            }
        });
    }

    @Override
    public void onEnter(GameUI ui) {
        pauseSec(1, voicePlayer::play).play();
        menu.requestFocus();
        final StandardGameVariant gameVariant = menu.gameVariant();
        switch (gameVariant) {
            case null -> ui.selectGameVariant(ARCADE_PACMAN_XXL.name());
            case ARCADE_PACMAN_XXL,ARCADE_MS_PACMAN_XXL -> {
                ui.selectGameVariant(gameVariant.name());
                menu.init(ui);
            }
            default -> Logger.error("Unexpected game variant in XXL menu: {}", gameVariant);
        }
    }

    @Override
    public void onExit(GameUI ui) {
        voicePlayer.stop();
        menu.stopAnimation();
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