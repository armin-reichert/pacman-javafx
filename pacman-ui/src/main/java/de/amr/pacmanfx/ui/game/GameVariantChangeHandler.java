/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.subviews.playview.GameEventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.tinylog.Logger;

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
        game.ui().view().statusIconBox().bind(game.currentGameContext().model());
    }

    private void exitGameVariant(String variantName) {
        game.ui().view().stage().getIcons().removeAll();
        game.ui().configurations().dispose(variantName);
        game.ui().sounds().dispose();
        game.currentGameContext().flow().removeGameEventListener(gameEventHandler);
    }

    public void enterGameVariant(String variantName) {
        final UIConfig config = game.ui().configurations().getOrCreateUIConfig(variantName);
        config.init(game);
        final Image icon = config.assets().image("app_icon");
        if (icon != null) {
            game.ui().view().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not find application icon for game variant {}", variantName);
        }
        game.currentGameContext().flow().addGameEventListener(gameEventHandler);
    }
}
