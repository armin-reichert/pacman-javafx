/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import de.amr.pacmanfx.engine.EngineContext;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.config.Cartridge;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final Map<String, GameVariantRuntime> runtimesByID = new HashMap<>();

    private final StringProperty currentVariantID = new SimpleStringProperty();

    public DefaultGameVariantManager() {}

    @Override
    public void registerVariant(EngineContext engine, String variantName) {
        requireNonNull(engine);
        requireNonNull(variantName);
        try {
            final GameVariantRuntime runtime = createGameVariantRuntime(engine, variantName);
            runtimesByID.put(variantName, runtime);
        } catch (Exception x) {
            throw new RuntimeException("Game variant '%s' could not be registered".formatted(variantName), x);
        }
    }

    @Override
    public StringProperty currentVariantIDProperty() {
        return currentVariantID;
    }

    @Override
    public void addVariantChangeListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        currentVariantID.addListener(listener);
    }

    @Override
    public String currentVariantID() {
        return currentVariantID.get();
    }

    @Override
    public GameVariantRuntime currentRuntime() {
        return variantRuntime(currentVariantID());
    }

    @Override
    public GameVariantRuntime variantRuntime(String variantName) {
        requireNonNull(variantName);
        return runtimesByID.get(variantName);
    }

    @Override
    public boolean variantExists(String variantName) {
        requireNonNull(variantName);
        return runtimesByID.containsKey(variantName);
    }

    @Override
    public void selectVariant(EngineContext engine, String variantName) {
        requireNonNull(engine);
        requireNonNull(variantName);
        if (!variantExists(variantName)) {
            registerVariant(engine, variantName);
        }
        variantRuntime(variantName).worldMapManager().loadCustomMaps();
        currentVariantID.set(variantName);
    }

    private GameVariantRuntime createGameVariantRuntime(EngineContext engine, String variantName) {
        final Cartridge cartridge = CartridgeRegistry.instance().getCartridgeByName(variantName);
        final var runtime = new DefaultGameVariantRuntime(cartridge, engine);
        runtime.gameFlow().addState(new Test_ShortTestState());
        runtime.gameFlow().addState(new Test_MediumTestState());
        runtime.gameFlow().addState(new Test_CutScenesTestState());
        runtime.worldMapManager().loadMapPrototypes();
        return runtime;
    }
}
