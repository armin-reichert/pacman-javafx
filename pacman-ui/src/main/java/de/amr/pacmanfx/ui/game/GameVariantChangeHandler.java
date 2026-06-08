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

    private final Game appContext;
    private final GameEventHandler gameEventHandler;

    public GameVariantChangeHandler(Game appContext) {
        this.appContext = requireNonNull(appContext);
        gameEventHandler = new GameEventHandler(appContext);
    }

    @Override
    public void changed(ObservableValue<? extends String> py, String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            exitGameVariant(oldGameVariantName);
        }
        if (newGameVariantName != null) {
            enterGameVariant(newGameVariantName);
        }
        appContext.ui().view().statusIconBox().bind(appContext.gameForVariant(newGameVariantName).gameModel());
    }

    private void exitGameVariant(String variantName) {
        appContext.ui().view().stage().getIcons().removeAll();
        appContext.ui().configurations().dispose(variantName);
        appContext.ui().sounds().dispose();
        appContext.currentGameContext().flow().removeGameEventListener(gameEventHandler);
    }

    public void enterGameVariant(String variantName) {
        final UIConfig config = appContext.ui().configurations().getOrCreateUIConfig(variantName);
        config.init(appContext);
        final Image icon = config.assets().image("app_icon");
        if (icon != null) {
            appContext.ui().view().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not find application icon for game variant {}", variantName);
        }
        appContext.currentGameContext().flow().addGameEventListener(gameEventHandler);
    }
}
