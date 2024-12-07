package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import javafx.scene.image.Image;

public record ImageAreaWithColorScheme(
        Image source, RectArea area, NES_ColorScheme colorScheme) {
}
