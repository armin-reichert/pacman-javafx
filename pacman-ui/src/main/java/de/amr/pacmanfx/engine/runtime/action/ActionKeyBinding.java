/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime.action;

import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.GameEngineUIContext;
import javafx.scene.input.KeyCodeCombination;

public record ActionKeyBinding(GameAction<GameEngineUIContext> action, KeyCodeCombination... keyCombinations) {}
