package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;

import java.util.function.Function;

public record GameExtension(Identifier id, Function<PacManGamesCollection, Object> creator) {}
