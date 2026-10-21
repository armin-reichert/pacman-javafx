/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import de.amr.pacmanfx.engine.EngineContext;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.config.Cartridge;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private EngineContext engine;

    private final Map<String, GameVariantRuntime> runtimesByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    public DefaultGameVariantManager() {}

    public void init(EngineContext engine) {
        this.engine = requireNonNull(engine);
    }

    @Override
    public void registerVariantConfig(String variantName) {
        requireNonNull(variantName);

        final GameVariantRuntime gameVariantRuntime;
        try {
            gameVariantRuntime = createGameVariantRuntime(engine, variantName);
            runtimesByName.put(variantName, gameVariantRuntime);
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
        return runtimeByName(currentVariantName());
    }

    @Override
    public GameVariantRuntime runtimeByName(String variantName) {
        requireNonNull(variantName);
        return runtimesByName.get(variantName);
    }

    @Override
    public boolean isVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return runtimesByName.containsKey(variantName);
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

    private GameVariantRuntime createGameVariantRuntime(EngineContext engine, String variantName) {
        final Cartridge cartridge = CartridgeRegistry.instance().getCartridgeByName(variantName);
        final var variantRuntime = new DefaultGameVariantRuntime(cartridge, engine);
        final GameFlow gameFlow = variantRuntime.playConfig().gameFlow();
        gameFlow.addState(new Test_ShortTestState());
        gameFlow.addState(new Test_MediumTestState());
        gameFlow.addState(new Test_CutScenesTestState());
        variantRuntime.playConfig().worldMapManager().loadMapPrototypes();
        Logger.info("Loaded world maps for game variant {}", variantName);
        return variantRuntime;
    }
}
