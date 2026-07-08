/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

import java.util.Optional;

public interface Game extends GameLifecycle {

    Input input();

    GameUI createUI(GameUISettings settings, DashboardFactory dashboardFactory, Stage stage, int width, int height);

    void setUI(GameUI ui);

    GameUI ui();

    GameClock clock();

    CoinMechanism coinMechanism();

    StringProperty variantNameProperty();

    void selectVariant(String variantName);

    String variantName();

    GameVariant gameVariant();

    GameVariant gameVariant(String variantName);

    CommonActions actions();

    /**
     * @return the game context of the currently selected game
     */
    GameContext context();

    default Optional<GameSoundEffects> soundEffects() {
        return gameVariant().config().optSoundEffects();
    }

    GameExtensions extensions();

    DirectoryWatchdog watchdog();
}
