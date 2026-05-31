/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_ServiceFacade;
import de.amr.pacmanfx.ui.subviews.editor.Editor_SubView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlay_SubView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPages_SubView;
import de.amr.pacmanfx.ui.view.GameUI_View;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.io.File;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public class SubViewManager {

    private final ObjectProperty<GameUI_SubView> currentSubView = new SimpleObjectProperty<>();

    private Supplier<Editor_SubView> editorViewFactory;
    private BooleanSupplier editorCanOpen = () -> false;

    private StartPages_SubView startView;
    private GamePlay_SubView gamePlayView;
    private Editor_SubView editorView;

    public SubViewManager() {}

    public void attachUI(GameUI_View view, GameUI_ServiceFacade services) {
        requireNonNull(view);
        requireNonNull(services);

        currentSubViewProperty().addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindings().dispose();
            }

            view.replaceSubView(newView);

            newView.onEnter();

            services.flashMessages().clearMessage();
        });
    }

    public void setGamePlayView(GamePlay_SubView newGamePlayView) {
        requireNonNull(newGamePlayView);
        gamePlayView = newGamePlayView;
    }

    public void setStartView(StartPages_SubView newStartView) {
        requireNonNull(newStartView);
        startView = newStartView;
    }

    public void setEditorViewFactory(Supplier<Editor_SubView> factory) {
        this.editorViewFactory = requireNonNull(factory);
    }

    public void setEditorCanOpen(BooleanSupplier editorCanOpen) {
        this.editorCanOpen = requireNonNull(editorCanOpen);
    }

    public StartPages_SubView startView() {
        return startView;
    }

    public GamePlay_SubView gamePlayView() {
        return gamePlayView;
    }

    public Optional<Editor_SubView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    public void selectStartView() {
        currentSubViewProperty().set(startView);
    }

    public void selectGamePlayView() {
        currentSubViewProperty().set(gamePlayView);
    }

    public void createEditorIfNotExisting(File workDir) {
        if (editorView == null) {
            editorView = editorViewFactory.get();
            editorView.editor().init(workDir);
        }
    }

    public void selectEditorView(GameUI ui) {
        if (editorView == null) {
            Logger.warn("Editor view has not been created yet");
            return;
        }
        if (editorCanOpen.getAsBoolean()) {
            ui.stopGame();
            editorView.editor().start();
            currentSubViewProperty().set(editorView);
        } else {
            Logger.warn("Editor cannot open!");
        }
    }

    public boolean isSelected(GameUI_SubView view) {
        return currentView() == view;
    }

    public ObjectProperty<GameUI_SubView> currentSubViewProperty() {
        return currentSubView;
    }

    public GameUI_SubView currentView() {
        return currentSubView.get();
    }
}
