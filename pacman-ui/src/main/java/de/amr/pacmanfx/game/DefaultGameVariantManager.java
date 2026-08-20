/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

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
    public GameVariant gameVariantByName(String name) {
        requireNonNull(name);
        return variantsByName.get(name);
    }

    @Override
    public boolean isVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return variantsByName.containsKey(variantName);
    }

    @Override
    public void selectVariant(String name) {
        requireNonNull(name);
        if (!variantsByName.containsKey(name)) {
            final boolean testStatesIncluded = viewModel.testStatesIncludedProperty.get();
            final GameVariant gameVariant = createGameVariant(name, testStatesIncluded);
            variantsByName.put(name, gameVariant);
        }
        variantsByName.get(name).config().worldMapManager().loadMapPrototypes();
        Logger.info("Loaded world maps for game variant {}", name);
        selectedVariantName.set(name);
    }

    private GameVariant createGameVariant(String variantName, boolean testStatesIncluded) {
        final Cartridge cartridge = cartridgeRepository.cartridgeByName(variantName);
        final var gameVariant = new GameVariant(cartridge);
        if (testStatesIncluded) {
            gameVariant.config().gameFlow().addTestStates();
        }
        return gameVariant;
    }
}
