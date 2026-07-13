/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.flow.GameFlow;
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

    private final PacManGamesCollectionImpl game;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty variantName = new SimpleStringProperty();

    public GameVariantManager(PacManGamesCollectionImpl game) {
        this.game = requireNonNull(game);
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
        return variant(currentVariantName());
    }

    public String currentVariantName() {
        return variantName.get();
    }

    public GameVariant variant(String gameVariantName) {
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

        final var gameVariantContext = new GameContextImpl(game.machine().coinMechanism(), gameVariant);
        gameVariantContext.flow().setContext(gameVariantContext);
        gameVariantContext.eventManager().addGameEventSubscriber(game.ui());

        game.setGameContext(gameVariantContext);
    }

    private void exitGameVariant(GameVariant gameVariant) {
        game.ui().sounds().dispose();
        gameVariant.config().dispose();

        game.currentGameContext().eventManager().removeGameEventSubscriber(game.ui());
        game.setGameContext(null);
    }

    private PacManGamesMachine machine() {
        return PacManGamesMachine.instance();
    }
}
