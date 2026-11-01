/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.controls.Carousel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class DefaultGameViewManager implements GameViewManager {

    private final ObjectProperty<GameView> currentView = new SimpleObjectProperty<>();

    private StartPagesView startPagesView;

    private GamePlayView gamePlayView;

    private EditorView editorView;

    public DefaultGameViewManager() {}

    @Override
    public void init(EngineUIContext engine) {
        startPagesView = new StartPagesView(engine.translationManager());
        gamePlayView = new GamePlayView(engine);
        editorView = new EditorView();

        //TODO Check if necessary
        selectStartPagesView();
    }

    @Override
    public void onVariantSelected(EngineUIContext engine, String variantID) {
        gamePlayView.dashboard().init(engine);
        selectStartPagesView();
        final Carousel carousel = startPagesView.rootPane();
        if (!carousel.getItems().isEmpty()) {
            carousel.setSelectedIndex(0);
        }
    }

    @Override
    public ObjectProperty<GameView> currentViewProperty() {
        return currentView;
    }

    @Override
    public StartPagesView startPagesView() {
        return startPagesView;
    }

    @Override
    public void selectStartPagesView() {
        currentView.set(startPagesView);
    }

    @Override
    public boolean isStartPagesViewSelected() {
        return currentView() == startPagesView;
    }

    @Override
    public GamePlayView gamePlayView() {
        return gamePlayView;
    }

    @Override
    public void selectGamePlayView() {
        currentView.set(gamePlayView);
    }

    @Override
    public boolean isGamePlayViewSelected() {
        return currentView() == gamePlayView;
    }

    @Override
    public EditorView editorView() {
        return editorView;
    }

    @Override
    public void selectEditorView() {
        currentView.set(editorView);
    }

    @Override
    public boolean isEditorViewSelected() {
        return currentView() == editorView;
    }
}
