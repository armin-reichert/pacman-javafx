/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.layout.BorderPane;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class CanvasLayoutPane extends BorderPane {

    private final DecoratedCanvas decoratedCanvas = new DecoratedCanvas();

    public CanvasLayoutPane() {
        setCenter(decoratedCanvas);
    }

    public DecoratedCanvas canvas() {
        return decoratedCanvas;
    }
}