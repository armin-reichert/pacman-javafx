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
import de.amr.pacmanfx.engine.runtime.DefaultGameVariantRuntime;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
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

    private final GameEngineContext engineContext;

    private final Map<String, DefaultGameVariantRuntime> configsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(GameEngineContext engineContext, GameViewModel viewModel) {
        this.engineContext = requireNonNull(engineContext);
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
        return variantRuntimeByName(currentVariantName());
    }

    @Override
    public DefaultGameVariantRuntime variantRuntimeByName(String variantName) {
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

    private DefaultGameVariantRuntime createGameVariantRuntime(GameEngineContext engineContext, String variantName, boolean includeInteractiveTests)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

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
