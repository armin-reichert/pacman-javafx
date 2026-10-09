/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.config;

import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.engine.Cartridge;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.runtime.GameVariantRuntime;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngineImpl;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final PlayStation playStation;

    private final PacManGamesEngineImpl engine;

    private final Map<String, GameVariantRuntime> configsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(PlayStation playStation, PacManGamesEngineImpl engine, GameViewModel viewModel) {
        this.playStation = requireNonNull(playStation);
        this.engine = requireNonNull(engine);
        this.viewModel = requireNonNull(viewModel);
    }

    @Override
    public void registerVariantConfig(String variantName) {
        requireNonNull(variantName);
        final boolean includeInteractiveTests = viewModel.testStatesIncludedProperty().get();
        final GameVariantRuntime gameVariantRuntime;
        try {
            gameVariantRuntime = createGameVariantRuntime(playStation, engine, variantName, includeInteractiveTests);
            configsByName.put(variantName, gameVariantRuntime);
        } catch (Exception x) {
            throw new RuntimeException("Game variant could not be registered", x);
        }
    }

    @Override
    public StringProperty selectedVariantNameProperty() {
        return selectedVariantName;
    }

    @Override
    public void addVariantListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        selectedVariantName.addListener(listener);
    }

    @Override
    public String currentVariantName() {
        return selectedVariantName.get();
    }

    @Override
    public GameVariantRuntime currentRuntime() {
        return variantRuntimeByName(currentVariantName());
    }

    @Override
    public GameVariantRuntime variantRuntimeByName(String variantName) {
        requireNonNull(variantName);
        return configsByName.get(variantName);
    }

    @Override
    public boolean isVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return configsByName.containsKey(variantName);
    }

    @Override
    public void selectVariant(String variantName) {
        requireNonNull(variantName);

        if (!isVariantRegistered(variantName)) {
            registerVariantConfig(variantName);
        }
        variantRuntimeByName(variantName).playConfig().worldMapManager().loadCustomMaps();
        Logger.info("Loaded custom maps for game variant {}", variantName);
        selectedVariantName.set(variantName);
    }

    private GameVariantRuntime createGameVariantRuntime(PlayStation playStation, PacManGamesEngineImpl engine, String variantName, boolean includeInteractiveTests)

        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        final Cartridge cartridge = playStation.cartridgeByName(variantName);
        final var variantRuntime = new GameVariantRuntime(playStation, cartridge, engine);
        if (includeInteractiveTests) {
            final GameFlow gameFlow = variantRuntime.playConfig().gameFlow();
            gameFlow.addState(new Test_ShortTestState());
            gameFlow.addState(new Test_MediumTestState());
            gameFlow.addState(new Test_CutScenesTestState());
        }
        variantRuntime.playConfig().worldMapManager().loadMapPrototypes();
        Logger.info("Loaded world maps for game variant {}", variantName);
        return variantRuntime;
    }
}
