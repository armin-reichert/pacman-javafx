/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.subviews.playview.GameEventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameVariantChangeHandler implements ChangeListener<String> {

    private final AppContext context;
    private final GameEventHandler gameEventHandler;

    public GameVariantChangeHandler(AppContext context) {
        this.context = requireNonNull(context);
        gameEventHandler = new GameEventHandler(context);
    }

    @Override
    public void changed(ObservableValue<? extends String> py, String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            exitGameVariant(oldGameVariantName);
        }
        if (newGameVariantName != null) {
            enterGameVariant(newGameVariantName);
        }
        context.ui().view().statusIconBox().bind(context.gameForVariant(newGameVariantName).gameModel());
    }

    private void exitGameVariant(String variantName) {
        final GameModel oldGameModel = context.gameForVariant(variantName).gameModel();
        context.ui().view().stage().getIcons().removeAll();
        context.ui().configurations().dispose(variantName);
        context.ui().sounds().dispose();
        oldGameModel.flow().removeGameEventListener(gameEventHandler);
    }

    public void enterGameVariant(String variantName) {
        final GameModel newGameModel = context.gameForVariant(variantName).gameModel();
        final UIConfig config = context.ui().configurations().getOrCreateUIConfig(variantName);
        config.init(context);
        final Image icon = config.assets().image("app_icon");
        if (icon != null) {
            context.ui().view().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not find application icon for game variant {}", variantName);
        }
        newGameModel.flow().addGameEventListener(gameEventHandler);
    }
}
