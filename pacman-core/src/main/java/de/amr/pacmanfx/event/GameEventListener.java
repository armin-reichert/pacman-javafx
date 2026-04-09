/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.event;

import org.tinylog.Logger;

public interface GameEventListener {

    /**
     * Central event dispatcher. Implementors can override this for common handling
     * or override specific methods for targeted reactions.
     */
    default void onGameEvent(GameEvent event) {
        switch (event) {
            case BonusActivatedEvent e           -> onBonusActivated(e);
            case BonusEatenEvent e               -> onBonusEaten(e);
            case BonusExpiredEvent e             -> onBonusExpired(e);
            case CreditAddedEvent e              -> onCreditAdded(e);
            case GameContinuedEvent e            -> onGameContinued(e);
            case GameStartedEvent e              -> onGameStarted(e);
            case GameStateChangeEvent e          -> onGameStateChange(e);
            case GenericChangeEvent e            -> ongenericChange(e);
            case GhostEatenEvent e               -> onGhostEaten(e);
            case GhostEntersHouseEvent e         -> onGhostEntersHouse(e);
            case GhostStartsReturningHomeEvent e -> onGhostStartsReturningHome(e);
            case HuntingPhaseStartedEvent e      -> onHuntingPhaseStarted(e);
            case IntermissionStartedEvent e      -> onIntermissionStarted(e);
            case LevelCreatedEvent e             -> onLevelCreated(e);
            case LevelStartedEvent e             -> onLevelStarted(e);
            case PacDeadEvent e                  -> onPacDead(e);
            case PacDyingEvent e                 -> onPacDying(e);
            case PacEatsFoodEvent e              -> onPacEatsFood(e);
            case PacGetsPowerEvent e             -> onPacGetsPower(e);
            case PacLostPowerEvent e             -> onPacLostPower(e);
            case PacPowerFadesEvent e            -> onPacPowerFades(e);
            case SpecialScoreEvent e             -> onSpecialScore(e);
            case StopAllSoundsEvent e            -> onStopAllSounds(e);
        }
    }

    // Default implementations remain the same
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter new game state '{}'", e.newState().name());
    }

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
    default void onHuntingPhaseStarted(HuntingPhaseStartedEvent e) {}
    default void onIntermissionStarted(IntermissionStartedEvent e) {}
    default void onLevelCreated(LevelCreatedEvent e) {}
    default void onLevelStarted(LevelStartedEvent e) {}
    default void onPacDead(PacDeadEvent e) {}
    default void onPacDying(PacDyingEvent e) {}
    default void onPacEatsFood(PacEatsFoodEvent e) {}
    default void onPacGetsPower(PacGetsPowerEvent e) {}
    default void onPacLostPower(PacLostPowerEvent e) {}
    default void onPacPowerFades(PacPowerFadesEvent e) {}
    default void onStopAllSounds(StopAllSoundsEvent e) {}
    default void ongenericChange(GenericChangeEvent e) {}
}
