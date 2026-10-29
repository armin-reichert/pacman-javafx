/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.beans.property.ObjectProperty;

public interface GameViewManager {

    void init(EngineUIContext engine);

    ObjectProperty<GameView> currentViewProperty();

    default GameView currentView() {
        return currentViewProperty().get();
    }

    StartPagesView startPagesView();

    void selectStartPagesView();

    boolean isStartPagesViewSelected();

    GamePlayView gamePlayView();

    void selectGamePlayView();

    boolean isGamePlayViewSelected();

    EditorView editorView();

    void selectEditorView();

    boolean isEditorViewSelected();

    void onVariantSelected(GameVariantID variantID);
}
