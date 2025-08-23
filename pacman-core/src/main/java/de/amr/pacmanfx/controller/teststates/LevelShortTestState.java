/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.teststates;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.Bonus;

public class LevelShortTestState implements GameState {

    private final TickTimer timer = new TickTimer("Timer_" + name());
    private int lastTestedLevelNumber;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    @Override
    public void onEnter(GameContext context) {
        context.coinMechanism().setNumCoins(1);
        lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
        timer.restartIndefinitely();
        context.game().prepareForNewGame();
        context.game().buildNormalLevel(1);
        context.game().startLevel();
        context.gameLevel().showPacAndGhosts();
        GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
        message.setPosition(context.gameLevel().defaultMessagePosition());
        context.gameLevel().setMessage(message);
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameLevel gameLevel = context.gameLevel();
        if (timer.tickCount() > 2 * Globals.NUM_TICKS_PER_SEC) {
            gameLevel.blinking().tick();
            gameLevel.ghosts().forEach(ghost -> ghost.tick(context));
            gameLevel.bonus().ifPresent(bonus -> bonus.tick(context));
        }
        if (timer.atSecond(1.0)) {
            context.game().continueGame();
        }
        else if (timer.atSecond(2)) {
            gameLevel.blinking().setStartPhase(Pulse.ON);
            gameLevel.blinking().restart();
        }
        else if (timer.atSecond(2.5)) {
            gameLevel.clearMessage();
            context.game().activateNextBonus();
        }
        else if (timer.atSecond(4.5)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(6.5)) {
            gameLevel.bonus().ifPresent(Bonus::setInactive); // needed?
            context.game().activateNextBonus();
        }
        else if (timer.atSecond(8.5)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(Globals.NUM_TICKS_PER_SEC));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(10.0)) {
            gameLevel.hidePacAndGhosts();
            context.game().onLevelCompleted();
        }
        else if (timer.atSecond(11.0)) {
            if (gameLevel.number() == lastTestedLevelNumber) {
                context.coinMechanism().setNumCoins(0);
                context.game().resetEverything();
                context.gameController().restart(GamePlayState.BOOT);
            } else {
                timer.restartIndefinitely();
                context.game().startNextLevel();
                GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
                message.setPosition(context.gameLevel().defaultMessagePosition());
                context.gameLevel().setMessage(message);
            }
        }
    }

    @Override
    public void onExit(GameContext context) {
        context.coinMechanism().setNumCoins(0);
        context.game().resetEverything();
        context.game().hudData().theLevelCounter().clear();
    }
}
