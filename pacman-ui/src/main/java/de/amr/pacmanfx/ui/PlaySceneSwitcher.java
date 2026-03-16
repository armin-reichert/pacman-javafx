/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import org.tinylog.Logger;

import static de.amr.pacmanfx.model.GameControl.CommonGameState.EATING_GHOST;
import static de.amr.pacmanfx.model.GameControl.CommonGameState.HUNTING;

public interface PlaySceneSwitcher {

    static void switchTo3D(GameUI ui, GameScene2D playScene2D, PlayScene3D playScene3D) {
        ui.gameContext().clock().stop();

        final UIConfig uiConfig = ui.currentConfig();
        final Game game = ui.gameContext().game();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final State<Game> state = game.control().state();

        if (playScene3D.optGameLevel3D().isEmpty()) {
            playScene3D.replaceGameLevel3D(level);
        }

        final GameLevel3D gameLevel3D = playScene3D.optGameLevel3D().orElseThrow();
        gameLevel3D.livesCounter3D().startTracking(gameLevel3D.pac3D());
        gameLevel3D.rebuildLevelCounter3D(uiConfig.entityConfig().levelCounter());

        playScene3D.initFood3D(level.worldMap().foodLayer(), state.nameMatches(HUNTING.name(), EATING_GHOST.name()));
        playScene3D.initPac3D(gameLevel3D.pac3D(), level);
        playScene3D.updateHUD3D(level);
        playScene3D.replaceActionBindings(level);
        playScene3D.fadeIn();

        if (state.nameMatches(HUNTING.name()) && level.pac().powerTimer().isRunning()) {
            playScene3D.soundEffects().playPacPowerSound();
        }

        ui.gameContext().clock().start();
        Logger.info("3D scene {} entered from 3D scene {}", playScene3D.getClass().getSimpleName(), playScene2D.getClass().getSimpleName());
    }

    static void switchTo2D(GameUI ui, PlayScene3D playScene3D, GameScene2D playScene2D) {
        ui.gameContext().clock().stop();

        final Game game = ui.gameContext().game();
        game.optGameLevel().ifPresent(playScene2D::acceptGameLevel);

        ui.gameContext().clock().start();
        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), playScene3D.getClass().getSimpleName());
    }
}
