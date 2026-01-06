/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.api.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;
import static java.util.Objects.requireNonNull;

public class PacManXXL_StartPageMenu extends OptionMenu {

    // State

    private final ObjectProperty<StandardGameVariant> gameVariant = new SimpleObjectProperty<>();
    private final BooleanProperty play3D = new SimpleBooleanProperty();
    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<WorldMapSelectionMode> mapOrder = new SimpleObjectProperty<>();

    public ObjectProperty<StandardGameVariant> gameVariantProperty() {
        return gameVariant;
    }

    public BooleanProperty play3DProperty() {
        return play3D;
    }

    public BooleanProperty cutScenesEnabledProperty() {
        return cutScenesEnabled;
    }

    public ObjectProperty<WorldMapSelectionMode> mapOrderProperty() {
        return mapOrder;
    }

    // Entries

    private final OptionMenuEntry<StandardGameVariant> entryGameVariant = new OptionMenuEntry<>(
        "GAME VARIANT", ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL)
    {
        @Override
        protected void onValueChanged(int index) {
            gameVariantProperty().set(getSelectedValue());
        }

        @Override
        public String getSelectedValueText() {
            final StandardGameVariant gameVariant = gameVariantProperty().get();
            return switch (gameVariant) {
                case null -> "";
                case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
                case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
                default -> "???";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            play3DProperty().set(getSelectedValue());
        }

        @Override
        public String getSelectedValueText() {
            return play3DProperty().get() ? "3D" : "2D";
        }
    };

    private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            cutScenesEnabledProperty().set(getSelectedValue());
        }

        @Override
        public String getSelectedValueText() {
            return cutScenesEnabledProperty().get() ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<WorldMapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
        WorldMapSelectionMode.CUSTOM_MAPS_FIRST, WorldMapSelectionMode.ALL_RANDOM, WorldMapSelectionMode.NO_CUSTOM_MAPS)
    {
        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                mapOrderProperty().set(getSelectedValue());
            }
        }

        @Override
        public String getSelectedValueText() {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (mapOrderProperty().get()) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    private final GameUI ui;
    private final ChaseAnimation chaseAnimation = new ChaseAnimation();
    private final Timeline animationTimer;

    public PacManXXL_StartPageMenu(GameUI ui) {
        super(42, 36, 6, 20);

        this.ui = requireNonNull(ui);

        final var style = new OptionMenuStyle(
            Font.font(GameUI.FONT_PAC_FONT_GOOD.getFamily(), 32),
            GameUI.FONT_ARCADE_8,
            Ufx.colorWithOpacity(DEFAULT_OPTION_MENU_STYLE.backgroundFill(), 0.5),
            DEFAULT_OPTION_MENU_STYLE.borderStroke(),
            ArcadePalette.ARCADE_RED,
            ArcadePalette.ARCADE_YELLOW,
            ArcadePalette.ARCADE_WHITE,
            DEFAULT_OPTION_MENU_STYLE.entryValueDisabledFill(),
            ArcadePalette.ARCADE_YELLOW,
            DEFAULT_OPTION_MENU_STYLE.entrySelectedSound(),
            DEFAULT_OPTION_MENU_STYLE.valueSelectedSound()
        );
        setStyle(style);

        setTitle("Pac-Man XXL");
        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapOrder);

        chaseAnimation.setOrigin(0, TS(23.5f));

        final int freq = 60;
        animationTimer = new Timeline(freq,
            new KeyFrame(Duration.seconds(1.0 / freq), _ -> {
                draw();
                chaseAnimation.update();
                chaseAnimation.draw();
            }));
        animationTimer.setCycleCount(Animation.INDEFINITE);

        gameVariantProperty().addListener((_, _, newVariant) -> {
            final GameUI_Config uiConfig = ui.config(newVariant.name());
            chaseAnimation.init(uiConfig);
            final ActorRenderer actorRenderer = uiConfig.createActorRenderer(canvas);
            actorRenderer.scalingProperty().bind(scalingProperty());
            chaseAnimation.setActorRenderer(actorRenderer);
        });
    }

    public void init(GameUI ui) {
        final Game game = ui.context().currentGame();
        final StandardGameVariant gameVariant = StandardGameVariant.valueOf(ui.context().gameVariantName());
        final var mapSelector = (PacManXXL_MapSelector) game.mapSelector();

        mapSelector.loadMapPrototypes();

        // init state
        gameVariantProperty().set(gameVariant);
        play3DProperty().set(PROPERTY_3D_ENABLED.get());
        cutScenesEnabledProperty().set(game.cutScenesEnabled());
        mapOrderProperty().set(mapSelector.selectionMode());

        soundEnabledProperty().bind(ui.currentConfig().soundManager().muteProperty().not());

        // init entries
        entryCutScenesEnabled.selectValue(cutScenesEnabledProperty().get());
        entryGameVariant.selectValue(gameVariantProperty().get());
        entryPlay3D.selectValue(play3DProperty().get());
        entryMapOrder.selectValue(mapOrderProperty().get());
        entryMapOrder.setEnabled(!mapSelector.customMapPrototypes().isEmpty());


        chaseAnimation.init(ui.currentConfig());
        animationTimer.playFromStart();
    }

    public void stopAnimation() {
        animationTimer.stop();
    }

    @Override
    protected void drawUsageInfo() {
        final GraphicsContext ctx = renderer.ctx();
        final Color normal = style.hintTextFill(), bright = style.entryValueFill();

        ctx.setFont(style.textFont());

        double y = TS(numTilesY() - 8);
        ctx.setFill(normal);
        ctx.fillText("SELECT OPTIONS WITH", TS(6), y);
        ctx.setFill(bright);
        ctx.fillText("UP", TS(26), y);
        ctx.setFill(normal);
        ctx.fillText("AND", TS(29), y);
        ctx.setFill(bright);
        ctx.fillText("DOWN", TS(33), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(8), y);
        ctx.setFill(bright);
        ctx.fillText("SPACE", TS(14), y);
        ctx.setFill(normal);
        ctx.fillText("TO CHANGE VALUE", TS(20), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(10), y);
        ctx.setFill(bright);
        ctx.fillText("E", TS(16), y);
        ctx.setFill(normal);
        ctx.fillText("TO OPEN EDITOR", TS(18), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(11), y);
        ctx.setFill(bright);
        ctx.fillText("ENTER", TS(17), y);
        ctx.setFill(normal);
        ctx.fillText("TO START", TS(23), y);
    }

    public void logState() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={} animation running={}",
            gameVariantProperty().get(),
            play3DProperty().get(),
            cutScenesEnabledProperty().get(),
            mapOrderProperty().get(),
            animationTimer.getStatus() == Animation.Status.RUNNING);
    }

    public void startGame(Game game) {
        final var mapSelector = (PacManXXL_MapSelector) game.mapSelector();
        mapSelector.setSelectionMode(mapOrderProperty().get());
        mapSelector.loadMapPrototypes();
        ui.selectGameVariant(gameVariantProperty().get().name());
    }
}