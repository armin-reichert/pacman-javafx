package de.amr.pacmanfx.event;

import org.tinylog.Logger;

public class DefaultGameEventListener implements GameEventListener{

    /**
     * Central event dispatcher. Implementors can override this for common handling
     * or override specific methods for targeted reactions.
     */
    @Override
    public void onGameEvent(GameEvent event) {
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
            case TestStartedEvent e              -> onTestStarted(e);
        }
    }

    // public implementations remain the same
    public void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter new game state '{}'", e.newState().name());
    }

    public void onCreditAdded(CreditAddedEvent e) {}
    public void onSpecialScore(SpecialScoreEvent e) {}
    public void onBonusActivated(BonusActivatedEvent e) {}
    public void onBonusEaten(BonusEatenEvent e) {}
    public void onBonusExpired(BonusExpiredEvent e) {}
    public void onGameContinued(GameContinuedEvent e) {}
    public void onGameStarted(GameStartedEvent e) {}
    public void onGhostEaten(GhostEatenEvent e) {}
    public void onGhostEntersHouse(GhostEntersHouseEvent e) {}
    public void onGhostStartsReturningHome(GhostStartsReturningHomeEvent e) {}
    public void onHuntingPhaseStarted(HuntingPhaseStartedEvent e) {}
    public void onIntermissionStarted(IntermissionStartedEvent e) {}
    public void onLevelCreated(LevelCreatedEvent e) {}
    public void onLevelStarted(LevelStartedEvent e) {}
    public void onPacDead(PacDeadEvent e) {}
    public void onPacDying(PacDyingEvent e) {}
    public void onPacEatsFood(PacEatsFoodEvent e) {}
    public void onPacGetsPower(PacGetsPowerEvent e) {}
    public void onPacLostPower(PacLostPowerEvent e) {}
    public void onPacPowerFades(PacPowerFadesEvent e) {}
    public void onStopAllSounds(StopAllSoundsEvent e) {}
    public void onTestStarted(TestStartedEvent e) {}

    public void onGenericChange(GenericChangeEvent e) {}
}
