package de.amr.pacmanfx.app.config;

import de.amr.basics.Named;
import de.amr.pacmanfx.app.runtime.GameEngine;

import java.util.function.Function;

public record GameEngineExtension(Named id, Function<GameEngine, Object> creator) {}
