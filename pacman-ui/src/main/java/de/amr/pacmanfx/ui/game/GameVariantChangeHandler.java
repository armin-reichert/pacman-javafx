/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.tinylog.Logger;

public class GameVariantChangeHandler implements ChangeListener<String> {

    private final PacManGamesCollection game;

    public GameVariantChangeHandler(PacManGamesCollection game) {
        this.game = game;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldVariantName, String newVariantName) {
        Logger.info("Game variant name change: {} -> {}", oldVariantName, newVariantName);

        if (oldVariantName != null) {
            exitGameVariant(game.gameVariant(oldVariantName));
        }
        if (newVariantName != null) {
            enterGameVariant(game.gameVariant(newVariantName));
        }
    }

    private void enterGameVariant(GameVariant gameVariant) {
        gameVariant.config().init(game);
        //TODO rethink
        game.ui().viewModel().maze3D.init(gameVariant.config().worldSettings().maze());

        final var gameVariantContext = new GameVariantContext(game, gameVariant);
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
}
