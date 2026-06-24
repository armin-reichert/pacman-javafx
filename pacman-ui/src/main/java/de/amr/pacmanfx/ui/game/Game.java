/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

import java.util.Optional;

public interface Game extends GameLifecycle {

    Input input();

    void createUI(Stage stage, int width, int height);

    GameUI ui();

    GameClock clock();

    CoinMechanism coinMechanism();

    StringProperty gameVariantNameProperty();

    void selectGameVariant(String variantName);

    String currentGameVariantName();

    GameVariant currentGameVariant();

    CommonActions actions();

    GameVariant gameVariant(String variantName);

    GameContext currentGameContext();

    GameVariantConfig currentVariantConfig();

    default Optional<GameSoundEffects> currentSoundEffects() {
        return currentVariantConfig().optSoundEffects();
    }

    GameExtensions extensions();

    DirectoryWatchdog watchdog();
}
