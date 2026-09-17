/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.test.Test_CutScenesTestState;
import de.amr.pacmanfx.core.model.test.Test_MediumTestState;
import de.amr.pacmanfx.core.model.test.Test_ShortTestState;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultGameVariantManager implements GameVariantManager {

    private final GameBox gameBox;

    private final GameApp app;

    private final Map<String, GameVariantRuntime> configsByName = new HashMap<>();

    private final StringProperty selectedVariantName = new SimpleStringProperty();

    private final GameViewModel viewModel;

    public DefaultGameVariantManager(GameBox gameBox, GameApp app, GameViewModel viewModel) {
        this.gameBox = requireNonNull(gameBox);
        this.app = requireNonNull(app);
        this.viewModel = requireNonNull(viewModel);
    }

    @Override
    public void registerVariantConfig(String variantName) {
        requireNonNull(variantName);
        final boolean includeInteractiveTests = viewModel.testStatesIncludedProperty().get();
        final GameVariantRuntime gameVariantRuntime = createGameVariantRuntime(gameBox, app, variantName, includeInteractiveTests);
        configsByName.put(variantName, gameVariantRuntime);
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
        return variantConfigByName(currentVariantName());
    }

    @Override
    public GameVariantRuntime variantConfigByName(String variantName) {
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
        variantConfigByName(variantName).playConfig().worldMapManager().loadCustomMaps();
        Logger.info("Loaded custom maps for game variant {}", variantName);
        selectedVariantName.set(variantName);
    }

    private GameVariantRuntime createGameVariantRuntime(GameBox gameBox, GameApp app, String variantName, boolean includeInteractiveTests) {
        final Cartridge cartridge = gameBox.cartridgeByName(variantName);
        final var variantRuntime = new GameVariantRuntime(gameBox, cartridge, app);
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
