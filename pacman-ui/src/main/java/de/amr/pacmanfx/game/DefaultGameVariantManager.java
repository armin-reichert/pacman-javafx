/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final CartridgeRepository cartridges;

    private final Map<String, GameVariantConfig> configsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(CartridgeRepository cartridges, GameViewModel viewModel) {
        this.cartridges = requireNonNull(cartridges);
        this.viewModel = requireNonNull(viewModel);
    }

    @Override
    public void registerVariantConfig(String variantName) {
        requireNonNull(variantName);
        final boolean includeInteractiveTests = viewModel.testStatesIncludedProperty().get();
        final GameVariantConfig gameVariantConfig = createGameVariant(variantName, includeInteractiveTests);
        configsByName.put(variantName, gameVariantConfig);
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
    public GameVariantConfig currentVariantConfig() {
        return variantConfigByName(currentVariantName());
    }

    @Override
    public GameVariantConfig variantConfigByName(String variantName) {
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
        if (!isVariantRegistered(variantName)) {
            registerVariantConfig(variantName);
        }
        variantConfigByName(variantName).playConfig().worldMapManager().loadCustomMaps();
        Logger.info("Loaded custom maps for game variant {}", variantName);
        selectedVariantName.set(variantName);
    }

    private GameVariantConfig createGameVariant(String variantName, boolean includeInteractiveTests) {
        final Cartridge cartridge = cartridges.cartridgeByName(variantName);
        final var variant = new GameVariantConfig(cartridge);
        if (includeInteractiveTests) {
            final GameFlowController gameFlow = variant.playConfig().gameFlow();
            gameFlow.addState(new Test_ShortTestState());
            gameFlow.addState(new Test_MediumTestState());
            gameFlow.addState(new Test_CutScenesTestState());
        }
        variant.playConfig().worldMapManager().loadMapPrototypes();
        Logger.info("Loaded world maps for game variant {}", variantName);
        return variant;
    }
}
