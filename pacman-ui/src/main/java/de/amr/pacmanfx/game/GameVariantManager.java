/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.core.model.test.LevelMediumTestState;
import de.amr.pacmanfx.core.model.test.LevelShortTestState;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameVariantManager implements ChangeListener<String> {

    private final PacManGames gameImpl;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty variantName = new SimpleStringProperty();

    public GameVariantManager(PacManGames gameImpl) {
        this.gameImpl = requireNonNull(gameImpl);
        variantName.addListener(this);
    }

    public StringProperty variantNameProperty() {
        return variantName;
    }

    public void addVariantNameListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        variantName.addListener(listener);
    }

    public void selectVariant(String gameVariantName) {
        requireNonNull(gameVariantName);
        if (machine().containsCartridgeWithName(gameVariantName)) {
            this.variantName.set(gameVariantName);
        } else throw new IllegalArgumentException("Game with name '" + gameVariantName + "' not found");
    }

    public GameVariant currentVariant() {
        return gameVariantByName(currentVariantName());
    }

    public String currentVariantName() {
        return variantName.get();
    }

    public GameVariant gameVariantByName(String gameVariantName) {
        requireNonNull(gameVariantName);
        return variantsByName.computeIfAbsent(gameVariantName, this::createGameVariant);
    }

    public boolean isVariantRegistered(String gameVariantName) {
        requireNonNull(gameVariantName);
        return variantsByName.containsKey(gameVariantName);
    }

    private GameVariant createGameVariant(String variantName) {
        final Cartridge cartridge = machine().cartridgeByName(variantName);
        final var gameVariant = new GameVariant(cartridge);

        //TODO make configurable again if tests should be available
        final GameFlowController flow = gameVariant.gameFlow();
        flow.addState(new LevelShortTestState());
        flow.addState(new LevelMediumTestState());
        flow.addState(new CutScenesTestState());

        gameVariant.gameModel().setHighScore(
            new PropertyFileScore(PacManGames.highScoreFile(variantName)));

        return gameVariant;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldVariantName, String newVariantName) {
        Logger.info("Game variant name change: {} -> {}", oldVariantName, newVariantName);

        if (oldVariantName != null) {
            exitGameVariant(oldVariantName, gameVariantByName(oldVariantName));
        }
        if (newVariantName != null) {
            enterGameVariant(newVariantName, gameVariantByName(newVariantName));
        }
    }

    private void enterGameVariant(String variantName, GameVariant gameVariant) {
        gameVariant.config().init(gameImpl);
        Logger.info("<<<<<<<<<<<<<<<<<<<< Game variant '{}' initialized", variantName);

        //TODO rethink this
        gameImpl.ui().viewModel().maze3D.init(gameVariant.config().worldSettings().maze());

        final var gameContext = new GameContextImpl(gameImpl.coinMechanism(), gameVariant);
        gameContext.eventManager().addGameEventSubscriber(gameImpl.ui());
        gameImpl.setGameContext(gameContext);
    }

    private void exitGameVariant(String variantName, GameVariant gameVariant) {
        gameImpl.ui().sounds().dispose();

        gameVariant.config().dispose();
        Logger.info(">>>>>>>>>>>>>>>>>>>> Game variant '{}' disposed", variantName);

        gameImpl.currentGameContext().eventManager().removeGameEventSubscriber(gameImpl.ui());
        gameImpl.setGameContext(null);
    }

    private GameBox machine() {
        return GameBox.instance();
    }
}
