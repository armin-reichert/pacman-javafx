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
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN_XXL;
import static java.util.Objects.requireNonNull;

public class PacManXXL_OptionMenu extends OptionMenu {

    public final OptionMenuEntry<StandardGameVariant> entryGameVariant = new OptionMenuEntry<>(
        "GAME VARIANT", List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL), ARCADE_PACMAN_XXL)
    {
        @Override
        public String formatValue(StandardGameVariant variant) {
            return switch (variant) {
                case null -> ""; //TODO may this happen?
                case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
                case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
                default -> "???";
            };
        }
    };

    public final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>(
        "SCENE DISPLAY", List.of(true, false), false)
    {
        @Override
        public String formatValue(Boolean play3D) {
            return play3D ? "3D" : "2D";
        }
    };

    public final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>(
        "CUTSCENES", List.of(true, false), true)
    {
        @Override
        public String formatValue(Boolean cutScenesEnabled) {
            return cutScenesEnabled ? "YES" : "NO";
        }
    };

    public final OptionMenuEntry<WorldMapSelectionMode> entryMapOrder = new OptionMenuEntry<>(
        "MAP ORDER",
        List.of(
            WorldMapSelectionMode.CUSTOM_MAPS_FIRST,
            WorldMapSelectionMode.ALL_RANDOM,
            WorldMapSelectionMode.NO_CUSTOM_MAPS),
        WorldMapSelectionMode.CUSTOM_MAPS_FIRST)
    {
        @Override
        public String formatValue(WorldMapSelectionMode mode) {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (mode) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    private final AnimationTimer drawLoop;
    private final ChaseAnimation chaseAnimation = new ChaseAnimation();
    private GameUI ui;

    public PacManXXL_OptionMenu() {
        super(42, 36, 6, 20);

        final OptionMenuStyle style = OptionMenuStyle.builder()
            .titleFont(Font.font(GameUI.FONT_PAC_FONT_GOOD.getFamily(), 32))
            .textFont(GameUI.FONT_ARCADE_8)
            .titleTextFill(ArcadePalette.ARCADE_RED)
            .entryTextFill(ArcadePalette.ARCADE_YELLOW)
            .entryValueFill(ArcadePalette.ARCADE_WHITE)
            .hintTextFill(ArcadePalette.ARCADE_YELLOW)
            .build();

        setStyle(style);

        setTitle("Pac-Man XXL");
        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapOrder);

        chaseAnimation.setOffsetY(TS(23.5f));
        chaseAnimation.scalingProperty().bind(scalingProperty());

        drawLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
                chaseAnimation.updateAndDraw();
            }
        };

        entryGameVariant.valueProperty().addListener((_, _, newVariant) -> {
            if (ui != null) {
                final GameUI_Config uiConfig = ui.config(newVariant.name());
                chaseAnimation.init(uiConfig, canvas);
            }
        });
    }

    public void init(GameUI ui) {
        this.ui = requireNonNull(ui);

        final Game game = ui.context().currentGame();
        final StandardGameVariant gameVariant = StandardGameVariant.valueOf(ui.context().gameVariantName());

        final var mapSelector = (PacManXXL_MapSelector) game.mapSelector();
        mapSelector.loadMapPrototypes();

        // init entries
        entryGameVariant.selectValue(gameVariant);
        entryPlay3D.selectValue(GameUI.PROPERTY_3D_ENABLED.get());
        entryCutScenesEnabled.selectValue(game.cutScenesEnabled());
        entryMapOrder.selectValue(mapSelector.selectionMode());
        entryMapOrder.setEnabled(!mapSelector.customMapPrototypes().isEmpty());
        logEntries();

        requestFocus();
        soundEnabledProperty().bind(ui.currentConfig().soundManager().muteProperty().not());

        chaseAnimation.init(ui.currentConfig(), canvas);
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

    public void logEntries() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            entryGameVariant.value(),
            entryPlay3D.value(),
            entryCutScenesEnabled.value(),
            entryMapOrder.value());
    }

    public void startSelectedGame() {
        ui.selectGameVariant(entryGameVariant.value().name());
    }

    public void startDrawLoop() {
        drawLoop.start();
    }

    public void stopDrawLoop() {
        drawLoop.stop();
    }
}