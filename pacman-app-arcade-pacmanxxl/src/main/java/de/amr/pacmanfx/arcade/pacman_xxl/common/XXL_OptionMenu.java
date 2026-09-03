/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelectionMode;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantUIConfig;
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
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.Objects.requireNonNull;

public class XXL_OptionMenu extends OptionMenu {

    private final OptionMenuEntry<GameVariantID> meGameVariantID;
    private final OptionMenuEntry<Boolean> meView3DEnabled;
    private final OptionMenuEntry<Boolean> meCutScenesEnabled;
    private final OptionMenuEntry<WorldMapSelectionMode> meMapOrder;

    private final XXL_ChaseAnimation chaseAnimation;

    private GameAppContext app;

    private ObservableValue<Double> scaling;

    public XXL_OptionMenu(OptionMenuSettings settings) {
        super(settings);

        setTitle("Pac-Man XXL");

        // Default key code RIGHT is already used to navigate through start pages carousel
        setKeyNextValue(KeyCode.SPACE);

        defineAction(1, KeyCode.E, "OPEN EDITOR");
        defineAction(2, KeyCode.ENTER, "START");

        meGameVariantID    = createGameVariantIDEntry();
        meView3DEnabled    = createView3DEnabledEntry();
        meCutScenesEnabled = createCutScenesEnabledEntry();
        meMapOrder         = createMapOrderEntry();

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
        chaseAnimation.draw(app.clock().currentTick());
    }

    @Override
    public void logMenuState() {
        Logger.info("Option Menu: {}, {}, Cutscenes {}, {}",
            meGameVariantID.value(),
            meView3DEnabled.value() ? "3D" : "2D",
            meCutScenesEnabled.value() ? "ON" : "OFF",
            meMapOrder.value()
        );
    }

    public void init(GameAppContext app) {
        this.app = requireNonNull(app);

        final String variantName = app.gameVariants().currentVariantName();
        final GameVariant variant = app.gameVariants().currentGameVariant();

        final WorldMapManager mapManager = variant.config().worldMapManager();
        if (!(mapManager instanceof XXL_WorldMapManager xxlMapManager)) {
            final String message = "Expected XXL map manager but found %s".formatted(mapManager.getClass().getSimpleName());
            throw new IllegalStateException(message);
        }
        xxlMapManager.loadMapPrototypes();

        // Init entries
        meGameVariantID.setValue(GameVariantID.valueOf(variantName));
        meView3DEnabled.setValue(app.ui().viewModel().common3DSettings().view3DEnabledProperty().get());
        meCutScenesEnabled.setValue(app.game().session().cutScenesEnabled());
        meMapOrder.setValue(xxlMapManager.selectionMode());
        meMapOrder.setEnabled(!xxlMapManager.customMaps().isEmpty());

        logMenuState();

        soundEnabledProperty().bind(app.ui().soundManager().muteProperty().not());

        scaling = computeScalingValue(app.ui().window().stage().heightProperty());

        chaseAnimation.setGameVariant(app.game(), variant, canvas);
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
        chaseAnimation.startChaseSimulation();
    }

    public void stopAnimation() {
        chaseAnimation.stopChaseSimulation();
    }

    public OptionMenuEntry<GameVariantID> meGameVariantID() {
        return meGameVariantID;
    }

    public WorldMapSelectionMode selectedMapSelectionMode() {
        return meMapOrder.value();
    }

    // Private

    private ObservableValue<Double> computeScalingValue(ReadOnlyDoubleProperty stageHeightProperty) {
        return stageHeightProperty.map(Number::doubleValue).map(h -> {
            final double menuHeightPixels = Math.clamp(h * settings.relHeight(), settings.minHeight(), settings.maxHeight());
            final double scaling = menuHeightPixels / (TS * settings.numTilesY());
            // Round to 2 decimal digits to reduce number of resizing changes
            return Math.round(scaling * 100.0) / 100.0;
        });
    }

    private void onGameVariantNameChanged(ObservableValue<? extends GameVariantID> observable, GameVariantID oldID, GameVariantID newID) {
        app.gameVariants().selectVariant(newID.name());
    }

    private void onPlay3DSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        app.ui().viewModel().common3DSettings().view3DEnabledProperty().set(newValue);
    }

    private void onCutScenesEnabledSettingsChange(ObservableValue<? extends Boolean> obs,  Boolean oldValue, Boolean newValue) {
        app.game().session().setCutScenesEnabled(newValue);
    }

    private void changeGameVariant(GameContext game, GameVariant newVariant) {
        requireNonNull(game);
        requireNonNull(newVariant);

        stopAnimation();

        final GameVariantUIConfig uiConfig = newVariant.uiConfig();
        uiConfig.init();
        uiConfig.loadSounds(app.ui().soundManager());
        uiConfig.connectApp(app);

        chaseAnimation.setGameVariant(game, newVariant, canvas);
        startAnimation();
    }

    private OptionMenuEntry<GameVariantID> createGameVariantIDEntry() {
        final var entry = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL)
        {
            @Override
            public void onValueChanged(GameVariantID oldVariantID, GameVariantID newVariantID) {
                if (app != null) {
                    final GameVariant newGameVariant = app.gameVariants().gameVariantByName(newVariantID.name());
                    changeGameVariant(app.game(), newGameVariant);
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
        final List<WorldMapSelectionMode> options = List.of(
            WorldMapSelectionMode.CUSTOM_MAPS_FIRST,
            WorldMapSelectionMode.ALL_RANDOM,
            WorldMapSelectionMode.NO_CUSTOM_MAPS
        );

        final var entry = new OptionMenuEntry<>("MAP ORDER", options, WorldMapSelectionMode.CUSTOM_MAPS_FIRST);

        entry.setValueFormatter(order -> {
            if (!entry.isEnabled()) {
                return "NO CUSTOM MAPS!";
            }
            return switch (order) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM        -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS    -> "NO CUSTOM MAPS";
            };
        });
        return entry;
    }
}