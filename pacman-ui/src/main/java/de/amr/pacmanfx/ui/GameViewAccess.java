/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameUI_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;

import java.util.Optional;

public interface GameViewAccess {
    GameUI_View currentView();
    PlayView playView();
    StartPagesView startPagesView();
    Optional<EditorView> optEditorView();

    void showEditorView();
    void showPlayView();
    void showStartView();
}