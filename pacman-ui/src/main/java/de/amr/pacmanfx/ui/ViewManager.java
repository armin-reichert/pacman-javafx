/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.tinylog.Supplier;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ViewManager {

    /** Index in layout pane child list where view is placed */
    private static final int RESERVED_VIEW_INDEX_IN_LAYOUT = 0;

    public enum ViewID { START_VIEW, PLAY_VIEW, EDITOR_VIEW }

    private final Map<ViewID, View> viewMap = new EnumMap<>(ViewID.class);
    private final ObjectProperty<ViewID> selectedID = new SimpleObjectProperty<>();
    private final ObjectProperty<View> currentView = new SimpleObjectProperty<>();
    private final Supplier<EditorView> editorViewFactory;

    public ViewManager(
        GameUI ui,
        Scene scene,
        Supplier<EditorView> editorViewFactory,
        FlashMessageView flashMessageView)
    {
        requireNonNull(ui);
        requireNonNull(scene);
        this.editorViewFactory = requireNonNull(editorViewFactory);
        requireNonNull(flashMessageView);

        viewMap.put(ViewID.START_VIEW, new StartPagesCarousel(ui));
        viewMap.put(ViewID.PLAY_VIEW, new PlayView(ui, scene));
        selectedID.addListener((_, oldID, newID) -> changeView(scene, flashMessageView, oldID, newID));
    }

    private void changeView(Scene scene, FlashMessageView flashMessageView, ViewID oldID, ViewID newID) {
        requireNonNull(newID);
        if (oldID != null) {
            final View oldView = viewMap.get(oldID);
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindingsManager().releaseBindings(GameUI.KEYBOARD);
            }
        }
        final View newView = viewMap.get(newID);
        if (!(scene.getRoot() instanceof Pane layoutPane)) {
            throw new IllegalStateException("Cannot replace view: Scene root must be a pane but is " + scene.getRoot());
        }
        if (layoutPane.getChildren().isEmpty()) {
            throw new IllegalStateException("Layout pane has no placeholder for embedding view");
        }
        layoutPane.getChildren().set(RESERVED_VIEW_INDEX_IN_LAYOUT, newView.root());
        newView.actionBindingsManager().activateBindings(GameUI.KEYBOARD);
        newView.onEnter();
        flashMessageView.clear();
        currentView.set(newView);
    }

    public void selectView(ViewID id) {
        requireNonNull(id);
        if (id == ViewID.EDITOR_VIEW) {
            createEditorViewIfMissing();
            getView(ViewID.EDITOR_VIEW, EditorView.class).editor().start();
        }
        selectedID.set(id);
    }

    public ObjectProperty<ViewID> selectedIDProperty() {
        return selectedID;
    }

    public boolean isSelected(ViewID id) {
        requireNonNull(id);
        return id == selectedID.get();
    }

    public ReadOnlyObjectProperty<View> currentViewProperty() {
        return currentView;
    }

    public View currentView() {
        return currentView.get();
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V getView(ViewID id, Class<V> viewClass) {
        requireNonNull(id);
        requireNonNull(viewClass);
        final View view = viewMap.get(id);
        if (view == null) {
            throw new IllegalArgumentException("No view with ID '%s' is registered".formatted(id));
        }
        if (viewClass.isInstance(view)) {
            return (V) view;
        }
        throw new IllegalArgumentException("View with ID '%s' has type %s (expected: %s)".formatted(id, view.getClass(), viewClass));
    }

    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable((EditorView) viewMap.get(ViewID.EDITOR_VIEW));
    }

    private void createEditorViewIfMissing() {
        var editorView = (EditorView) viewMap.get(ViewID.EDITOR_VIEW);
        if (editorView == null) {
            editorView = editorViewFactory.get();
            editorView.editor().init(GameBox.CUSTOM_MAP_DIR);
            viewMap.put(ViewID.EDITOR_VIEW, editorView);
        }
    }
}
