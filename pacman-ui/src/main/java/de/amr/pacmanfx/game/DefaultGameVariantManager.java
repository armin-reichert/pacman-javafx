/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.core.model.test.LevelMediumTestState;
import de.amr.pacmanfx.core.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final CartridgeRepository cartridgeRepository;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(CartridgeRepository cartridgeRepository, GameViewModel viewModel) {
        this.cartridgeRepository = requireNonNull(cartridgeRepository);
        this.viewModel = requireNonNull(viewModel);
    }

    @Override
    public void registerGameVariant(String variantName) {
        final boolean includeInteractiveTests = viewModel.testStatesIncludedProperty.get();
        final GameVariant gameVariant = createGameVariant(variantName, includeInteractiveTests);
        variantsByName.put(variantName, gameVariant);
    }

    @Override
    public StringProperty selectedVariantNameProperty() {
        return selectedVariantName;
    }

    @Override
    public void addVariantNameListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        selectedVariantName.addListener(listener);
    }

    @Override
    public String currentVariantName() {
        return selectedVariantName.get();
    }

    @Override
    public GameVariant currentGameVariant() {
        return gameVariantByName(currentVariantName());
    }

    @Override
    public GameVariant gameVariantByName(String variantName) {
        requireNonNull(variantName);
        return variantsByName.get(variantName);
    }

    @Override
    public boolean isVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return variantsByName.containsKey(variantName);
    }

    @Override
    public void selectVariant(String variantName) {
        if (!isVariantRegistered(variantName)) {
            registerGameVariant(variantName);
        }
        final GameVariant variant = variantsByName.get(variantName);
        selectedVariantName.set(variantName);
    }

    private GameVariant createGameVariant(String variantName, boolean includeInteractiveTests) {
        final Cartridge cartridge = cartridgeRepository.cartridgeByName(variantName);
        final var variant = new GameVariant(cartridge);
        if (includeInteractiveTests) {
            final GameFlowController gameFlow = variant.config().gameFlow();
            gameFlow.addState(new LevelShortTestState());
            gameFlow.addState(new LevelMediumTestState());
            gameFlow.addState(new CutScenesTestState());
        }
        variant.config().worldMapManager().loadMapPrototypes();
        Logger.info("Loaded world maps for game variant {}", variantName);
        return variant;
    }
}
