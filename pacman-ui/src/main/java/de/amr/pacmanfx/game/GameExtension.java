package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;

import java.util.function.Function;

public record GameExtension(Named id, Function<PacManGamesEngine, Object> creator) {}
