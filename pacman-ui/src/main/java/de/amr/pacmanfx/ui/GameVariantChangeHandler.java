/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.layout.playview.GameEventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameVariantChangeHandler implements ChangeListener<String> {

    private final GameUI ui;
    private final GameEventHandler gameEventHandler;

    public GameVariantChangeHandler(GameUI ui) {
        this.ui = requireNonNull(ui);
        gameEventHandler = new GameEventHandler(ui);
    }

    @Override
    public void changed(ObservableValue<? extends String> py, String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            exitGameVariant(oldGameVariantName);
        }
        if (newGameVariantName != null) {
            enterGameVariant(newGameVariantName);
        }
        ui.statusIconBox().bind(ui.gameContext().gameForVariant(newGameVariantName));
    }

    private void exitGameVariant(String variantName) {
        final Game oldGame = ui.gameContext().gameForVariant(variantName);
        ui.stage().getIcons().removeAll();
        ui.management().configManager().dispose(variantName);
        ui.management().soundManager().dispose();
        oldGame.flow().removeGameEventListener(gameEventHandler);
    }

    public void enterGameVariant(String variantName) {
        final Game newGame = ui.gameContext().gameForVariant(variantName);
        final UIConfig config = ui.management().configManager().getOrCreateUIConfig(variantName);
        config.init(ui);
        final Image icon = config.assets().image("app_icon");
        if (icon != null) {
            ui.stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not find application icon for game variant {}", variantName);
        }
        newGame.flow().addGameEventListener(gameEventHandler);
    }
}
