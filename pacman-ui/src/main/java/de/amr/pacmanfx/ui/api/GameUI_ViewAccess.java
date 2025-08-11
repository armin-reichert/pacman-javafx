/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.GameUI_View;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;

import java.util.Optional;

public interface GameUI_ViewAccess {
    GameUI_View currentView();

    Optional<EditorView> optEditorView();

    PlayView playView();

    StartPagesView startPagesView();

    void showEditorView();

    void showPlayView();

    void showStartView();
}