package de.amr.pacmanfx.eventng;

public sealed interface GameEventNG permits
    BonusActivatedEvent,
    BonusEatenEvent,
    BonusExpiredEvent,
    CreditAddedEvent,
    GameContinuedEvent,
    GameStartedEvent,
    GameStateChangeEvent,
    GhostEatenEvent,
    GhostEntersHouseEvent,
    GhostStartsReturningHomeEvent,
    HuntingPhaseStartedEvent,
    IntermissionStartedEvent,
    LevelCreatedEvent,
    LevelStartedEvent,
    PacDeadEvent,
    PacDyingEvent,
    PacFoundFoodEvent,
    PacGetsPowerEvent,
    PacLostPowerEvent,
    PacStartsLosingPowerEvent,
    SpecialScoreReachedEvent,
    StopAllSoundsEvent,
    UnspecifiedChangeEvent {

    // Optional: common methods (e.g., source object, timestamp)
    // Object source();
}
