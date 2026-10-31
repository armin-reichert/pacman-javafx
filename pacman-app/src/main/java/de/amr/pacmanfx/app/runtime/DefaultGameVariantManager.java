/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import de.amr.pacmanfx.engine.EngineCoreContext;
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
    public void registerVariant(EngineCoreContext engine, String variantID) {
        requireNonNull(engine);
        requireNonNull(variantID);
        try {
            final GameVariantRuntime runtime = createGameVariantRuntime(engine, variantID);
            runtimesByID.put(variantID, runtime);
        } catch (Exception x) {
            throw new RuntimeException("Game variant '%s' could not be registered".formatted(variantID), x);
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
    public GameVariantRuntime variantRuntime(String variantID) {
        requireNonNull(variantID);
        return runtimesByID.get(variantID);
    }

    @Override
    public boolean variantExists(String variantID) {
        requireNonNull(variantID);
        return runtimesByID.containsKey(variantID);
    }

    @Override
    public void selectVariant(EngineCoreContext engine, String variantID) {
        requireNonNull(engine);
        requireNonNull(variantID);
        if (!variantExists(variantID)) {
            registerVariant(engine, variantID);
        }
        variantRuntime(variantID).worldMapManager().loadCustomMaps();
        currentVariantID.set(variantID);
    }

    private GameVariantRuntime createGameVariantRuntime(EngineCoreContext engine, String variantID) {
        final Cartridge cartridge = CartridgeRegistry.instance().getCartridgeByName(variantID);
        final var runtime = new DefaultGameVariantRuntime(cartridge, engine);
        runtime.gameFlow().addState(new Test_ShortTestState());
        runtime.gameFlow().addState(new Test_MediumTestState());
        runtime.gameFlow().addState(new Test_CutScenesTestState());
        return runtime;
    }
}
