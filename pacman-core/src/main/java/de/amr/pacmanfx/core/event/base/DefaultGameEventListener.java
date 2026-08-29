package de.amr.pacmanfx.core.event.base;

import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.ghost.GhostEntersHouseEvent;
import de.amr.pacmanfx.core.event.ghost.GhostStartsReturningHomeEvent;
import de.amr.pacmanfx.core.event.pac.*;

import java.io.IOException;

/**
 * Implementors can override {@link #onGameEvent(GameEvent)} or specific methods for targeted reactions.
 */
public interface DefaultGameEventListener extends GameEventListener {

    @Override
    default void onGameEvent(GameEvent event) {
        switch (event) {
            case BonusActivatedEvent e           -> onBonusActivated(e);
            case BonusEatenEvent e               -> onBonusEaten(e);
            case BonusExpiredEvent e             -> onBonusExpired(e);
            case CreditAddedEvent e              -> onCreditAdded(e);
            case GameContinuedEvent e            -> onGameContinued(e);
            case GameStartedEvent e              -> onGameStarted(e);
            case GameStateChangeEvent e          -> onGameStateChange(e);
            case GenericChangeEvent e            -> onGenericChange(e);
            case GhostEatenEvent e               -> onGhostEaten(e);
            case GhostEntersHouseEvent e         -> onGhostEntersHouse(e);
            case GhostStartsReturningHomeEvent e -> onGhostStartsReturningHome(e);
            case HighScoreAccessErrorEvent e     -> onHighScoreAccessError(e);
            case HuntingPhaseStartedEvent e      -> onHuntingPhaseStarted(e);
            case IntermissionStartedEvent e      -> onIntermissionStarted(e);
            case LevelCreatedEvent e             -> onLevelCreated(e);
            case LevelStartedEvent e             -> onLevelStarted(e);
            case PacDeadEvent e                  -> onPacDead(e);
            case PacDyingEvent e                 -> onPacDying(e);
            case PacEatsFoodEvent e              -> onPacEatsFood(e);
            case PacPowerStartsEvent e           -> onPacPowerStarts(e);
            case PacPowerEndsEvent e             -> onPacPowerEnds(e);
            case PacPowerStartsFadingEvent e     -> onPacPowerStartsFading(e);
            case SpecialScoreEvent e             -> onSpecialScore(e);
            case StopAllSoundsEvent e            -> onStopAllSounds(e);
            case TestStartedEvent e              -> onTestStarted(e);
        }
    }

    // public implementations remain the same
    default void onGameStateChange(GameStateChangeEvent e) {}

    default void onCreditAdded(CreditAddedEvent e) {}
    default void onSpecialScore(SpecialScoreEvent e) {}
    default void onBonusActivated(BonusActivatedEvent e) {}
    default void onBonusEaten(BonusEatenEvent e) {}
    default void onBonusExpired(BonusExpiredEvent e) {}
    default void onGameContinued(GameContinuedEvent e) {}
    default void onGameStarted(GameStartedEvent e) {}
    default void onGhostEaten(GhostEatenEvent e) {}
    default void onGhostEntersHouse(GhostEntersHouseEvent e) {}
    default void onGhostStartsReturningHome(GhostStartsReturningHomeEvent e) {}
    default void onHighScoreAccessError(HighScoreAccessErrorEvent e) {}
    default void onHuntingPhaseStarted(HuntingPhaseStartedEvent e) {}
    default void onIntermissionStarted(IntermissionStartedEvent e) {}
    default void onLevelCreated(LevelCreatedEvent e) {}
    default void onLevelStarted(LevelStartedEvent e) {}
    default void onPacDead(PacDeadEvent e) {}
    default void onPacDying(PacDyingEvent e) {}
    default void onPacEatsFood(PacEatsFoodEvent e) {}
    default void onPacPowerStarts(PacPowerStartsEvent e) {}
    default void onPacPowerEnds(PacPowerEndsEvent e) {}
    default void onPacPowerStartsFading(PacPowerStartsFadingEvent e) {}
    default void onStopAllSounds(StopAllSoundsEvent e) {}
    default void onTestStarted(TestStartedEvent e) {}

    default void onGenericChange(GenericChangeEvent e) {}
}
