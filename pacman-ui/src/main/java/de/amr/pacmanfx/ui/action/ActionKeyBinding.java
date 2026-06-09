/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import javafx.scene.input.KeyCodeCombination;

public record ActionKeyBinding(GameAction action, KeyCodeCombination... keyCombinations) {}
