package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Bonus;

// Specific events as records (immutable, concise)
    public record BonusActivatedEvent(Bonus bonus) implements GameEvent {}
