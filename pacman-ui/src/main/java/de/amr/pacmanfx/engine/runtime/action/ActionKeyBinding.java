/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.pacmanfx.engine.action.GameAction;
import javafx.scene.input.KeyCodeCombination;

public record ActionKeyBinding(GameAction<GameEngineContext> action, KeyCodeCombination... keyCombinations) {}
