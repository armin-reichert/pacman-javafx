package de.amr.pacmanfx.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.function.Function;

public record GameExtension(Identifier id, Function<GameAppContext, Object> creator) {}
