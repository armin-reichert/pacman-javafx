/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

/**
 * @param <R> result type
 */
public interface EditorAction<R> {

    R execute();
}
