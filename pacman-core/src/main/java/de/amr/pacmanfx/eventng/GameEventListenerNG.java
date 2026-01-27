package de.amr.pacmanfx.eventng;

import org.tinylog.Logger;

public interface GameEventListenerNG {

    default void onGameEvent(GameEventNG event) {
        switch (event) {
            case BonusActivatedEvent e          -> onBonusActivated(e);
            case BonusEatenEvent e              -> onBonusEaten(e);
            case BonusExpiredEvent e            -> onBonusExpired(e);
            case CreditAddedEvent e             -> onCreditAdded(e);
            case GameContinuedEvent e           -> onGameContinues(e);
            case GameStartedEvent e             -> onGameStarts(e);
            case GameStateChangeEvent e         -> onGameStateChange(e);
            case GhostEatenEvent e              -> onGhostEaten(e);
            case GhostEntersHouseEvent e        -> onGhostEntersHouse(e);
            case GhostStartsReturningHomeEvent e-> onGhostStartsReturningHome(e);
            case HuntingPhaseStartedEvent e     -> onHuntingPhaseStarted(e);
            case IntermissionStartedEvent e     -> onIntermissionStarted(e);
            case LevelCreatedEvent e            -> onLevelCreated(e);
            case LevelStartedEvent e            -> onLevelStarts(e);
            case PacDeadEvent e                 -> onPacDead(e);
            case PacDyingEvent e                -> onPacDying(e);
            case PacFoundFoodEvent e            -> onPacFindsFood(e);
            case PacGetsPowerEvent e            -> onPacGetsPower(e);
            case PacLostPowerEvent e            -> onPacLostPower(e);
            case PacStartsLosingPowerEvent e    -> onPacStartsLosingPower(e);
            case SpecialScoreReachedEvent e     -> onSpecialScoreReached(e);
            case StopAllSoundsEvent e           -> onStopAllSounds(e);
            case UnspecifiedChangeEvent e       -> onUnspecifiedChange(e);
        }
    }

    // Default implementations remain the same
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter new game state '{}'", e.newState().name());
    }

    default void onCreditAdded(CreditAddedEvent e) {}
    default void onSpecialScoreReached(SpecialScoreReachedEvent e) {}
    default void onBonusActivated(BonusActivatedEvent e) {}
    default void onBonusEaten(BonusEatenEvent e) {}
    default void onBonusExpired(BonusExpiredEvent e) {}
    default void onGameContinues(GameContinuedEvent e) {}
    default void onGameStarts(GameStartedEvent e) {}
    default void onGhostEaten(GhostEatenEvent e) {}
    default void onGhostEntersHouse(GhostEntersHouseEvent e) {}
    default void onGhostStartsReturningHome(GhostStartsReturningHomeEvent e) {}
    default void onHuntingPhaseStarted(HuntingPhaseStartedEvent e) {}
    default void onIntermissionStarted(IntermissionStartedEvent e) {}
    default void onLevelCreated(LevelCreatedEvent e) {}
    default void onLevelStarts(LevelStartedEvent e) {}
    default void onPacDead(PacDeadEvent e) {}
    default void onPacDying(PacDyingEvent e) {}
    default void onPacFindsFood(PacFoundFoodEvent e) {}
    default void onPacGetsPower(PacGetsPowerEvent e) {}
    default void onPacLostPower(PacLostPowerEvent e) {}
    default void onPacStartsLosingPower(PacStartsLosingPowerEvent e) {}
    default void onStopAllSounds(StopAllSoundsEvent e) {}
    default void onUnspecifiedChange(UnspecifiedChangeEvent e) {}
}
