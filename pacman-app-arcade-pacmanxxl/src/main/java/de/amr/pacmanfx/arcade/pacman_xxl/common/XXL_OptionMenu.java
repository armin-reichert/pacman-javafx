/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.core.model.world.WorldMapSelector;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.optionmenu.OptionMenuSettings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariantID.ARCADE_PACMAN_XXL;
import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static java.util.Objects.requireNonNull;

public class XXL_OptionMenu extends OptionMenu {

    private final OptionMenuEntry<GameVariantID> meGameVariantID;
    private final OptionMenuEntry<Boolean> meView3DEnabled;
    private final OptionMenuEntry<Boolean> meCutScenesEnabled;
    private final OptionMenuEntry<WorldMapSelectionMode> meMapOrder;

    private final XXL_ChaseAnimation chaseAnimation;

    private GameAppContext appContext;

    private ObservableValue<Double> scaling;

    public XXL_OptionMenu(OptionMenuSettings settings) {
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

        chaseAnimation = new XXL_ChaseAnimation(settings.numTilesX());
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

    public void init(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);

        final GameUI ui = appContext.ui();
        final GameVariantRenderConfig renderConfig = appContext.variants().currentVariant().config().renderConfig();
        final GameContext gameContext = appContext.currentGameContext();
        final GameVariantID gameVariantID = GameVariantID.valueOf(appContext.variants().currentVariantName());
        final WorldMapSelector worldMapSelector = gameContext.model().mapSelector();

        if (!(worldMapSelector instanceof XXL_MapSelector mapSelector)) {
            final String message = "Expected XXL map selector but found %s"
                .formatted(worldMapSelector.getClass().getSimpleName());
            Logger.error(message);
            throw new IllegalStateException(message);
        }
        mapSelector.loadMapPrototypes();

        scaling = createScalingValue(ui.window().stage().heightProperty());

        // Init entries
        meGameVariantID.setValue(gameVariantID);
        meView3DEnabled.setValue(ui.viewModel().common3D.view3DEnabledProperty.get());
        meCutScenesEnabled.setValue(gameContext.flow().cutScenesEnabled());
        meMapOrder.setValue(mapSelector.selectionMode());
        meMapOrder.setEnabled(!mapSelector.customMaps().isEmpty());

        logMenuState();

        soundEnabledProperty().bind(ui.sounds().muteProperty().not());
        chaseAnimation.init(renderConfig, canvas, ui.sprites().animations());
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

    public WorldMapSelectionMode selectedMapSelectionMode() {
        return meMapOrder.value();
    }

    // Private

    private ObservableValue<Double> createScalingValue(ReadOnlyDoubleProperty stageHeightProperty) {
        return stageHeightProperty.map(Number::doubleValue).map(h -> {
            final double menuHeightPixels = Math.clamp(h * settings.relHeight(), settings.minHeight(), settings.maxHeight());
            final double scaling = menuHeightPixels / (TS * settings.numTilesY());
            // Round to 2 decimal digits to reduce number of resizing changes
            return Math.round(scaling * 100.0) / 100.0;
        });
    }

    private void onGameVariantNameChanged(ObservableValue<? extends GameVariantID> observable, GameVariantID oldVariantID, GameVariantID newVariantID) {
        appContext.variants().selectVariant(newVariantID.name());
    }

    private void onPlay3DSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        appContext.ui().viewModel().common3D.view3DEnabledProperty.set(newValue);
    }

    private void onCutScenesEnabledSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        appContext.currentGameContext().flow().setCutScenesEnabled(newValue);
    }

    private OptionMenuEntry<GameVariantID> createGameVariantIDEntry() {
        final var entry = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL)
        {
            @Override
            public void onValueChanged(GameVariantID oldVariant, GameVariantID newVariant) {
                if (appContext != null) {
                    final GameVariantRenderConfig renderConfig = appContext.variants().gameVariantByName(newVariant.name()).config().renderConfig();
                    chaseAnimation.init(renderConfig, canvas, appContext.ui().sprites().animations());
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
        final var entry = new OptionMenuEntry<>("MAP ORDER", List.of(
            WorldMapSelectionMode.CUSTOM_MAPS_FIRST,
            WorldMapSelectionMode.ALL_RANDOM,
            WorldMapSelectionMode.NO_CUSTOM_MAPS),
            WorldMapSelectionMode.CUSTOM_MAPS_FIRST);

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