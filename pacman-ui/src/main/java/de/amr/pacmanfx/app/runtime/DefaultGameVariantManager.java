/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.config.Cartridge;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private EngineUIContext engineContext;

    private GameViewModel viewModel;

    private final Map<String, DefaultGameVariantRuntime> configsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    public DefaultGameVariantManager() {}

    public void init(EngineUIContext engineContext) {
        this.engineContext = requireNonNull(engineContext);
    }

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = requireNonNull(viewModel);
    }

    @Override
    public void registerVariantConfig(String variantName) {
        requireNonNull(variantName);
        final boolean includeInteractiveTests = viewModel.testStatesIncludedProperty().get();
        final DefaultGameVariantRuntime gameVariantRuntime;
        try {
            gameVariantRuntime = createGameVariantRuntime(engineContext, variantName, includeInteractiveTests);
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
    public DefaultGameVariantRuntime currentRuntime() {
        return runtimeByName(currentVariantName());
    }

    @Override
    public DefaultGameVariantRuntime runtimeByName(String variantName) {
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
        runtimeByName(variantName).playConfig().worldMapManager().loadCustomMaps();
        Logger.info("Loaded custom maps for game variant {}", variantName);
        selectedVariantName.set(variantName);
    }

    private DefaultGameVariantRuntime createGameVariantRuntime(EngineUIContext engineContext, String variantName, boolean includeInteractiveTests) {
        final Cartridge cartridge = PlayStation.instance().cartridgeByName(variantName);
        final var variantRuntime = new DefaultGameVariantRuntime(cartridge, engineContext);
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
