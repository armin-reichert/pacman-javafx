/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameClock;
import javafx.application.Platform;

public class GameClockController {

    private final PacManGamesCollection game;
    private final GameClock clock;

    public GameClockController(PacManGamesCollection game, GameClock clock) {
        this.game = game;
        this.clock = clock;
    }

    public void configure() {
        clock.setUpdateAction(this::onUpdate);
        clock.setPermanentAction(this::onRender);
        clock.setErrorHandler(this::onError);
    }

    private void onUpdate() {
        game.context().flow().makeStep();
        Platform.runLater(() -> game.ui().gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock.currentTick())));
    }

    private void onRender() {
        Platform.runLater(() -> game.ui().viewManager().assertCurrentView().render());
    }

    private void onError(Throwable reason) {
        game.ka_tas_tro_phe(reason);
    }
}
