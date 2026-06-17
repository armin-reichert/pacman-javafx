/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariantID.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.model.world.WorldMapSelectionMode.*;
import static java.util.Objects.requireNonNull;

public class PacManXXL_OptionMenu extends OptionMenu {

    static final int   MENU_MIN_HEIGHT = 400;
    static final int   MENU_MAX_HEIGHT = 800;
    static final float MENU_REL_HEIGHT = 0.66f;

    static final int NUM_TILES_X = 42;
    static final int NUM_TILES_Y = 34;

    static final int TEXT_COLUMN  = 6;
    static final int VALUE_COLUMN = 20;

    static final int CHASE_ANIMATION_Y = (NUM_TILES_Y - 12) * WorldMap.TS;

    public record Options(
        OptionMenuEntry<GameVariantID> gameVariantID,
        OptionMenuEntry<Boolean> enable3D,
        OptionMenuEntry<Boolean> enableCutScenes,
        OptionMenuEntry<WorldMapSelectionMode> mapOrder
    ) {}

    private final Options options;

    private final ChaseAnimation chaseAnimation = new ChaseAnimation(NUM_TILES_X);

    private Game game;

    private ObservableValue<Double> scaling;

    public PacManXXL_OptionMenu() {
        super(NUM_TILES_X, NUM_TILES_Y, TEXT_COLUMN, VALUE_COLUMN);

        setTitle("Pac-Man XXL");

        // Default key code RIGHT is already used to navigate through start pages carousel
        setKeyNextValue(KeyCode.SPACE);

        defineAction(1, KeyCode.E, "OPEN EDITOR");
        defineAction(2, KeyCode.ENTER, "START");

        options = new Options(
            createGameVariantEntry(),
            createSceneDisplayEntry(),
            createCutScenesEntry(),
            createCustomMapsEntry()
        );

        addEntry(options.gameVariantID());
        addEntry(options.enable3D());
        addEntry(options.enableCutScenes());
        addEntry(options.mapOrder());

        chaseAnimation.setY(CHASE_ANIMATION_Y);
        chaseAnimation.scalingProperty().bind(scalingProperty());

        canvas.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                startDrawLoop();
            }
        });
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw();
    }

    public void init(Game game) {
        this.game = requireNonNull(game);

        scaling = createScaling(game);

        final UIConfig currentConfig = game.currentUIConfig();
        final GameContext gameContext = game.currentGameContext();
        final GameVariantID gameVariant = GameVariantID.valueOf(game.currentGameVariantName());
        final GameModel gameModel = gameContext.model();

        if (!(gameModel.mapSelector() instanceof PacManXXL_MapSelector mapSelector)) {
            final String errorMsg = "Expected XXL map selector but found %s".formatted(gameModel.mapSelector().getClass().getSimpleName());
            Logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        mapSelector.loadMapPrototypes();

        // Init entries
        options.gameVariantID().setValue(gameVariant);
        options.enable3D().setValue(game.ui().settings().d3().view3DEnabledProperty().get());
        options.enableCutScenes().setValue(gameContext.flow().cutScenesEnabled());
        options.mapOrder().setValue(mapSelector.selectionMode());
        options.mapOrder().setEnabled(!mapSelector.customMaps().isEmpty());

        logMenuState();

        soundEnabledProperty().bind(game.ui().sounds().muteProperty().not());
        chaseAnimation.init(currentConfig, canvas, game.ui().sprites().animations());
    }

    public void bind() {
        unbind();
        options.gameVariantID().valueProperty().addListener(this::onGameVariantNameChanged);
        options.enable3D().valueProperty().addListener(this::onPlay3DSettingsChange);
        options.enableCutScenes().valueProperty().addListener(this::onCutScenesEnabledSettingsChange);
        scalingProperty().bind(scaling);
    }

    public void unbind() {
        options.gameVariantID().valueProperty().removeListener(this::onGameVariantNameChanged);
        options.enable3D().valueProperty().removeListener(this::onPlay3DSettingsChange);
        options.enable3D().valueProperty().removeListener(this::onCutScenesEnabledSettingsChange);
        scalingProperty().unbind();
    }

    public void startDrawLoop() {
        super.startDrawLoop();
        chaseAnimation.start();
    }

    public void stopDrawLoop() {
        super.stopDrawLoop();
        chaseAnimation.stop();
    }

    public Options options() {
        return options;
    }

    // Private

    private ObservableValue<Double> createScaling(Game game) {
        // rounded to 2 decimal digits to avoid too much resizing
        return game.ui().window().stage().heightProperty().map(stageHeight -> {
            final double menuHeight = Math.clamp(stageHeight.doubleValue() * MENU_REL_HEIGHT, MENU_MIN_HEIGHT, MENU_MAX_HEIGHT);
            final double relHeight = menuHeight / (TS * numTilesY());
            // Round scaling to 2 decimal digits to avoid too much resizing
            return Math.round(relHeight * 100.0) / 100.0;
        });
    }

    private void onGameVariantNameChanged(ObservableValue<? extends GameVariantID> observable, GameVariantID oldVariantID, GameVariantID newVariantID) {
        game.selectGameVariant(newVariantID.name());
    }

    private void onPlay3DSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        game.ui().settings().d3().view3DEnabledProperty().set(newValue);
    }

    private void onCutScenesEnabledSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        game.currentGameContext().flow().setCutScenesEnabled(newValue);
    }

    private OptionMenuEntry<GameVariantID> createGameVariantEntry() {
        final var entry = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL)
        {
            @Override
            public void onValueChanged(GameVariantID oldVariant, GameVariantID newVariant) {
                if (game != null) {
                    final UIConfig uiConfig = game.gameVariant(newVariant.name()).uiConfig();
                    chaseAnimation.init(uiConfig, canvas, game.ui().sprites().animations());
                }
            }
        };

        entry.setValueFormatter(variant -> switch (variant) {
            case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
            case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
            default -> "???";
        });

        return entry;
    }

    private OptionMenuEntry<Boolean> createSceneDisplayEntry() {
        final var entry = new OptionMenuEntry<>("SCENE DISPLAY", List.of(true, false), false);
        entry.setValueFormatter(play3D -> play3D ? "3D" : "2D");
        return entry;
    }

    private OptionMenuEntry<Boolean> createCutScenesEntry() {
        final var entry = new OptionMenuEntry<>("CUTSCENES", List.of(true, false), true);
        entry.setValueFormatter(enabled -> enabled ? "ON" : "OFF");
        return entry;
    }

    private OptionMenuEntry<WorldMapSelectionMode> createCustomMapsEntry() {
        final var entry = new OptionMenuEntry<>("MAP ORDER", List.of(CUSTOM_MAPS_FIRST, ALL_RANDOM, NO_CUSTOM_MAPS), CUSTOM_MAPS_FIRST);
        entry.setValueFormatter(order -> {
            if (!entry.enabled()) {
                return "NO CUSTOM MAPS!";
            }
            return switch (order) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        });
        return entry;
    }

    @Override
    public void logMenuState() {
        Logger.info("Option Menu: ({}, {}, {}, {})",
            options.gameVariantID().value(),
            options.enable3D().value() ? "3D" : "2D",
            "Cut Scenes " + (options.enableCutScenes().value() ? "ON" : "OFF"),
            options.mapOrder().value());
    }
}