package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class PlayViewGameVariantChangeHandler implements ChangeListener<String> {

    private final PlayView playView;

    public PlayViewGameVariantChangeHandler(PlayView playView) {
        this.playView = requireNonNull(playView);
    }

    @Override
    public void changed(ObservableValue<? extends String> py, String oldGameVariantName, String newGameVariantName) {
        if (oldGameVariantName != null) {
            cleanupOldGameVariant(oldGameVariantName);
        }
        if (newGameVariantName != null) {
            initNewGameVariant(newGameVariantName);
        }
    }

    private void cleanupOldGameVariant(String variantName) {
        final GameUI ui = playView.ui();
        final Game oldGame = ui.gameContext().gameByVariantName(variantName);

        oldGame.flow().removeGameEventListener(playView.gameEventHandler());

        Logger.info("Cleanup old game variant {}...", variantName);

        ui.stage().getIcons().removeAll();
        ui.uiConfigManager().dispose(variantName);
        ui.soundManager().dispose();

        Logger.info("Cleanup of old game variant {} complete.", variantName);
    }

    private void initNewGameVariant(String variantName) {
        final GameUI ui = playView.ui();
        final Game newGame = ui.gameContext().gameByVariantName(variantName);

        newGame.flow().addGameEventListener(playView.gameEventHandler());

        Logger.info("Initialize new game variant {}...", variantName);

        final UIConfig newUiConfig = ui.config(variantName);
        newUiConfig.init(ui);

        final Image icon = newUiConfig.assets().image("app_icon");
        if (icon != null) {
            ui.stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not find application icon for game variant {}", variantName);
        }

        Logger.info("Initialization of game variant {} complete.", variantName);
    }

}
