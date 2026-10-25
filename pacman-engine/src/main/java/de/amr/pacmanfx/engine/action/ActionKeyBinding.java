/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.action;

import de.amr.pacmanfx.engine.EngineCoreContext;
import javafx.scene.input.KeyCodeCombination;

public record ActionKeyBinding<C extends EngineCoreContext>(GameAction<C> action, KeyCodeCombination... keyCombinations) {}
