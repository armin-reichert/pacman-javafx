/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.views.playview.GameEventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import static java.util.Objects.requireNonNull;

public class GameVariantChangeHandler implements ChangeListener<String> {

    private final Game game;
    private final GameEventHandler gameEventHandler;

    public GameVariantChangeHandler(Game game) {
        this.game = requireNonNull(game);
        gameEventHandler = new GameEventHandler(game);
    }

    @Override
    public void changed(ObservableValue<? extends String> py, String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            exitGameVariant(oldGameVariantName);
        }
        if (newGameVariantName != null) {
            enterGameVariant(newGameVariantName);
        }
    }

    private void exitGameVariant(String variantName) {
        game.ui().window().stage().getIcons().removeAll();
        game.ui().sounds().dispose();
        game.gameVariant(variantName).uiConfig().dispose();
        game.currentGameContext().flow().removeGameEventListener(gameEventHandler);
    }

    public void enterGameVariant(String variantName) {
        final UIConfig uiConfig = game.gameVariant(variantName).uiConfig();
        uiConfig.init(game);
        game.currentGameContext().flow().addGameEventListener(gameEventHandler);
    }
}
