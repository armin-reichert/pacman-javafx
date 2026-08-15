/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final CartridgeRepository cartridgeRepository;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty variantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(CartridgeRepository cartridgeRepository, GameViewModel viewModel) {
        this.cartridgeRepository = requireNonNull(cartridgeRepository);
        this.viewModel = requireNonNull(viewModel);
    }

    public StringProperty variantNameProperty() {
        return variantName;
    }

    @Override
    public void addVariantNameListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        variantName.addListener(listener);
    }

    @Override
    public String currentVariantName() {
        return variantName.get();
    }

    @Override
    public GameVariant currentGameVariant() {
        return gameVariantByName(currentVariantName());
    }

    @Override
    public GameVariant gameVariantByName(String gameVariantName) {
        requireNonNull(gameVariantName);
        final boolean testStatesIncluded = viewModel.testStatesIncludedProperty.get();
        return variantsByName.computeIfAbsent(gameVariantName, name -> createGameVariant(name, testStatesIncluded));
    }

    @Override
    public boolean isVariantRegistered(String variantName) {
        requireNonNull(variantName);
        return variantsByName.containsKey(variantName);
    }

    @Override
    public void selectVariant(String gameVariantName) {
        requireNonNull(gameVariantName);
        if (cartridgeRepository.containsCartridgeWithName(gameVariantName)) {
            this.variantName.set(gameVariantName);
        } else throw new IllegalArgumentException("Game with name '" + gameVariantName + "' not found");
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
