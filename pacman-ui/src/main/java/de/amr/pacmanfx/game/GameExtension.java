package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.ui.action.core.GameApp;

import java.util.function.Function;

public record GameExtension(Named id, Function<GameApp, Object> creator) {}
