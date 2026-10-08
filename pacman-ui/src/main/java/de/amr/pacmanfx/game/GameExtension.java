package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.ui.action.core.PacManGameEngineContext;

import java.util.function.Function;

public record GameExtension(Named id, Function<PacManGameEngineContext, Object> creator) {}
