/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;


import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.GameSession;
import org.tinylog.Logger;

public final class GameState_DemoLevelPlaying extends GameState {

    private final int huntingStartTick;

    public GameState_DemoLevelPlaying(int huntingStartTick) {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
        this.huntingStartTick = huntingStartTick;
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        session.setLevel(game.variantConfig().gamePlay().buildDemoLevel(game));
        session.hud().showCredit().hideLivesCounter();
        game.eventManager().publishGameEvent(new LevelCreatedEvent(session.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variantConfig().systems();
        final GamePlay gamePlay = game.variantConfig().gamePlay();
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (tick == 1) {
            session.score().data().setEnabled(false);
            session.highScore().data().setEnabled(false);
            gamePlay.prepareLevelForPlaying(game);
            gamePlay.showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);
            LevelCounterSystem.update(session.levelCounter(), level.number(), level.bonusSymbolCode(0));
            Logger.info("Demo level {} started", level.number());
            // Note: This event is very important because it triggers the creation of the actor animations!
            game.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == 2) {
            // Now, actor animations are available, show them
            pac.show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == huntingStartTick) {
            // Clear "READY!" message. "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
            session.hud().optMessage()
                .filter(message -> message.type() == GameLevelMessageType.READY)
                .ifPresent(_ -> session.hud().clearMessage());

            level.heartbeat().setStartState(Pulse.State.ON);
            level.heartbeat().restart();

            systems.spriteAnim().playSelected(pac);
            level.entities().ghosts().forEach(systems.spriteAnim()::playSelected);

            // This call fires a game event!
            level.huntingTimerStrategy().startFirstPhase(game, level.number());
        }
        else if (tick > huntingStartTick) {
            gamePlay.hunt(game, level);
            session.gameFlow().enterState(game, computeNextState(game, level));
        }
    }

    private CommonGameStateID computeNextState(GameContext game, GameLevel level) {
        if (game.variantConfig().rules().isLevelCompleted(level)) {
            return CommonGameStateID.GAME_INTRO;
        }
        else if (game.session().thisFrame().huntingStep().pacKilled()) {
            return CommonGameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (game.session().thisFrame().huntingStep().hasGhostBeenKilled()) {
            return CommonGameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
