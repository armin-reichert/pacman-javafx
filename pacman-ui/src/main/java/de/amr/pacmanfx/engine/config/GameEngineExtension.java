package de.amr.pacmanfx.engine.config;

import de.amr.basics.Named;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;

import java.util.function.Function;

public record GameEngineExtension(Named id, Function<PacManGamesEngine, Object> creator) {}
