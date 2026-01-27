package de.amr.pacmanfx.event;

public record CreditAddedEvent(int credits) implements GameEvent {}
