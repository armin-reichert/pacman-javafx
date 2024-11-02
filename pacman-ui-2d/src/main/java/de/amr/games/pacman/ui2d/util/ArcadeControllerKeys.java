package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCodeCombination;

public record ArcadeControllerKeys(
    KeyCodeCombination coin,
    KeyCodeCombination start,
    KeyCodeCombination up,
    KeyCodeCombination down,
    KeyCodeCombination left,
    KeyCodeCombination right
) implements ArcadeController {}
