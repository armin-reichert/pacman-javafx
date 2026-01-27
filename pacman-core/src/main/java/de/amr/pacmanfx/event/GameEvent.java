package de.amr.pacmanfx.event;

public sealed interface GameEvent permits
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
    UnspecifiedChangeEvent
{}
