/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuSettings;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariantID.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.model.world.WorldMapSelectionMode.*;
import static java.util.Objects.requireNonNull;

public class PacManXXL_OptionMenu extends OptionMenu {

    private final OptionMenuEntry<GameVariantID> meGameVariantID;
    private final OptionMenuEntry<Boolean> meView3DEnabled;
    private final OptionMenuEntry<Boolean> meCutScenesEnabled;
    private final OptionMenuEntry<WorldMapSelectionMode> meMapOrder;

    private final ChaseAnimation chaseAnimation;

    private Game game;

    private ObservableValue<Double> scaling;

    public PacManXXL_OptionMenu(OptionMenuSettings settings) {
        super(settings);

        setTitle("Pac-Man XXL");

        // Default key code RIGHT is already used to navigate through start pages carousel
        setKeyNextValue(KeyCode.SPACE);

        defineAction(1, KeyCode.E, "OPEN EDITOR");
        defineAction(2, KeyCode.ENTER, "START");

        meGameVariantID = createGameVariantIDEntry();
        meView3DEnabled = createView3DEnabledEntry();
        meCutScenesEnabled = createCutScenesEnabledEntry();
        meMapOrder = createMapOrderEntry();

        addEntry(meGameVariantID);
        addEntry(meView3DEnabled);
        addEntry(meCutScenesEnabled);
        addEntry(meMapOrder);

        chaseAnimation = new ChaseAnimation(settings.numTilesX());
        chaseAnimation.setY((settings.numTilesY() - 12) * WorldMap.TS);
        chaseAnimation.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw();
    }

    @Override
    public void logMenuState() {
        Logger.info("Option Menu: ({}, {}, {}, {})",
            meGameVariantID.value(),
            meView3DEnabled.value() ? "3D" : "2D",
            "Cut Scenes " + (meCutScenesEnabled.value() ? "ON" : "OFF"),
            meMapOrder.value());
    }

    public void init(Game game) {
        this.game = requireNonNull(game);

        scaling = createScaling(game.ui().window().stage());

        final GameVariant currentConfig = game.currentGameVariant();
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
        meGameVariantID.setValue(gameVariant);
        meView3DEnabled.setValue(game.ui().viewModel().common3D.view3DEnabledProperty.get());
        meCutScenesEnabled.setValue(gameContext.flow().cutScenesEnabled());
        meMapOrder.setValue(mapSelector.selectionMode());
        meMapOrder.setEnabled(!mapSelector.customMaps().isEmpty());

        logMenuState();

        soundEnabledProperty().bind(game.ui().sounds().muteProperty().not());
        chaseAnimation.init(currentConfig, canvas, game.ui().sprites().animations());
    }

    public void bind() {
        unbind();
        meGameVariantID.valueProperty().addListener(this::onGameVariantNameChanged);
        meView3DEnabled.valueProperty().addListener(this::onPlay3DSettingsChange);
        meCutScenesEnabled.valueProperty().addListener(this::onCutScenesEnabledSettingsChange);
        scalingProperty().bind(scaling);
    }

    public void unbind() {
        meGameVariantID.valueProperty().removeListener(this::onGameVariantNameChanged);
        meView3DEnabled.valueProperty().removeListener(this::onPlay3DSettingsChange);
        meCutScenesEnabled.valueProperty().removeListener(this::onCutScenesEnabledSettingsChange);
        scalingProperty().unbind();
    }

    public void startAnimation() {
        chaseAnimation.start();
    }

    public void stopAnimation() {
        chaseAnimation.stop();
    }

    public OptionMenuEntry<GameVariantID> meGameVariantID() {
        return meGameVariantID;
    }

    // Private

    private ObservableValue<Double> createScaling(Stage stage) {
        // rounded to 2 decimal digits to avoid too much resizing
        return stage.heightProperty().map(stageHeight -> {
            final double menuHeight = Math.clamp(
                stageHeight.doubleValue() * settings.relHeight(),
                settings.minHeight(), settings.maxHeight());
            final double relHeight = menuHeight / (TS * settings.numTilesY());
            // Round scaling to 2 decimal digits to avoid too much resizing
            return Math.round(relHeight * 100.0) / 100.0;
        });
    }

    private void onGameVariantNameChanged(ObservableValue<? extends GameVariantID> observable, GameVariantID oldVariantID, GameVariantID newVariantID) {
        game.selectGameVariant(newVariantID.name());
    }

    private void onPlay3DSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        game.ui().viewModel().common3D.view3DEnabledProperty.set(newValue);
    }

    private void onCutScenesEnabledSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        game.currentGameContext().flow().setCutScenesEnabled(newValue);
    }

    private OptionMenuEntry<GameVariantID> createGameVariantIDEntry() {
        final var entry = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL)
        {
            @Override
            public void onValueChanged(GameVariantID oldVariant, GameVariantID newVariant) {
                if (game != null) {
                    final GameVariant uiConfig = game.gameVariantRuntime(newVariant.name()).gameVariant();
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

    private OptionMenuEntry<Boolean> createView3DEnabledEntry() {
        final var entry = new OptionMenuEntry<>("SCENE DISPLAY", List.of(true, false), false);
        entry.setValueFormatter(play3D -> play3D ? "3D" : "2D");
        return entry;
    }

    private OptionMenuEntry<Boolean> createCutScenesEnabledEntry() {
        final var entry = new OptionMenuEntry<>("CUTSCENES", List.of(true, false), true);
        entry.setValueFormatter(enabled -> enabled ? "ON" : "OFF");
        return entry;
    }

    private OptionMenuEntry<WorldMapSelectionMode> createMapOrderEntry() {
        final var entry = new OptionMenuEntry<>("MAP ORDER", List.of(CUSTOM_MAPS_FIRST, ALL_RANDOM, NO_CUSTOM_MAPS), CUSTOM_MAPS_FIRST);
        entry.setValueFormatter(order -> {
            if (!entry.isEnabled()) {
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

}