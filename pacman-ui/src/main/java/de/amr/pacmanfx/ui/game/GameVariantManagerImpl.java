/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.score.PropertyFileScore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameVariantManagerImpl implements GameVariantManager, ChangeListener<String> {

    private final PacManGamesCollection game;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty variantName = new SimpleStringProperty();

    public GameVariantManagerImpl(PacManGamesCollection game) {
        this.game = requireNonNull(game);
        variantName.addListener(this);
    }

    @Override
    public StringProperty variantNameProperty() {
        return variantName;
    }

    @Override
    public void addVariantNameListener(ChangeListener<String> listener) {
        requireNonNull(listener);
        variantName.addListener(listener);
    }

    @Override
    public void selectVariant(String gameVariantName) {
        requireNonNull(gameVariantName);
        if (machine().containsCartridgeWithName(gameVariantName)) {
            this.variantName.set(gameVariantName);
        } else throw new IllegalArgumentException("Game with name '" + gameVariantName + "' not found");
    }

    @Override
    public GameVariant currentVariant() {
        return variant(currentVariantName());
    }

    @Override
    public String currentVariantName() {
        return variantName.get();
    }

    @Override
    public GameVariant variant(String gameVariantName) {
        return variantsByName.computeIfAbsent(gameVariantName, this::createGameVariant);
    }

    @Override
    public boolean isVariantRegistered(String gameVariantName) {
        requireNonNull(gameVariantName);
        return variantsByName.containsKey(gameVariantName);
    }

    private GameVariant createGameVariant(String variantName) {
        final Cartridge cartridge = machine().cartridgeByName(variantName);
        final var gameVariant = new GameVariant(cartridge);

        //TODO make configurable again if tests should be available
        final GameFlow flow = gameVariant.gameFlow();
        flow.addState(new LevelShortTestState());
        flow.addState(new LevelMediumTestState());
        flow.addState(new CutScenesTestState());

        gameVariant.gameModel().setHighScore(
            new PropertyFileScore(PacManGamesMachine.highScoreFile(variantName)));

        return gameVariant;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldVariantName, String newVariantName) {
        Logger.info("Game variant name change: {} -> {}", oldVariantName, newVariantName);

        if (oldVariantName != null) {
            exitGameVariant(variant(oldVariantName));
        }
        if (newVariantName != null) {
            enterGameVariant(variant(newVariantName));
        }
    }

    private void enterGameVariant(GameVariant gameVariant) {
        gameVariant.config().init(game);
        //TODO rethink
        game.ui().viewModel().maze3D.init(gameVariant.config().worldSettings().maze());

        final var gameVariantContext = new GameVariantContext(game.machine().coinMechanism(), gameVariant);
        gameVariantContext.flow().setContext(gameVariantContext);
        gameVariantContext.eventManager().addGameEventSubscriber(game.ui());

        game.setGameVariantContext(gameVariantContext);
    }

    private void exitGameVariant(GameVariant gameVariant) {
        game.ui().sounds().dispose();
        gameVariant.config().dispose();

        game.context().eventManager().removeGameEventSubscriber(game.ui());
        game.setGameVariantContext(null);
    }

    private PacManGamesMachine machine() {
        return PacManGamesMachine.instance();
    }
}
