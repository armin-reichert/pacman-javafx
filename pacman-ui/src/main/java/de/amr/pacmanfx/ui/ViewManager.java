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

/**
 * Manages switching between different {@link View} instances in the application.
 * <p>
 * A {@code ViewManager} maintains a mapping from {@link ViewID} to concrete {@link View}
 * implementations and handles the full lifecycle when switching:
 * <ul>
 *   <li>calling {@code onExit()} on the old view</li>
 *   <li>releasing and activating keyboard bindings</li>
 *   <li>replacing the view node inside a designated placeholder of the layout pane</li>
 *   <li>calling {@code onEnter()} on the new view</li>
 *   <li>clearing flash messages</li>
 * </ul>
 *
 * The layout pane is expected to contain a placeholder node at index
 * {@link #RESERVED_VIEW_INDEX_IN_LAYOUT}, which will be replaced whenever the
 * selected view changes.
 */
public class ViewManager {

    /** Index in the layout pane's child list where the active view is embedded. */
    private static final int RESERVED_VIEW_INDEX_IN_LAYOUT = 0;

    /**
     * Identifiers for the views managed by this class.
     */
    public enum ViewID { START_VIEW, PLAY_VIEW, EDITOR_VIEW }

    private final Map<ViewID, View> viewMap = new EnumMap<>(ViewID.class);
    private final ObjectProperty<ViewID> selectedID = new SimpleObjectProperty<>();
    private final ObjectProperty<View> currentView = new SimpleObjectProperty<>();
    private final Supplier<EditorView> editorViewFactory;

    /**
     * Creates a new {@code ViewManager}.
     *
     * @param ui                the game UI context
     * @param scene             the JavaFX scene whose root must be a {@link Pane}
     * @param editorViewFactory factory for lazily creating the editor view
     * @param flashMessageView  view used for displaying transient messages
     */
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

        selectedID.addListener((_, oldID, newID) ->
            changeView(scene, flashMessageView, oldID, newID));
    }

    /**
     * Switches from the old view to the new view, performing all required lifecycle steps.
     */
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
            throw new IllegalStateException(
                "Cannot replace view: Scene root must be a Pane but is " + scene.getRoot());
        }
        if (layoutPane.getChildren().isEmpty()) {
            throw new IllegalStateException(
                "Layout pane has no placeholder for embedding view");
        }

        layoutPane.getChildren().set(RESERVED_VIEW_INDEX_IN_LAYOUT, newView.root());
        newView.actionBindingsManager().activateBindings(GameUI.KEYBOARD);
        newView.onEnter();
        flashMessageView.clear();
        currentView.set(newView);
    }

    /**
     * Selects the view with the given ID. If the editor view is selected for the first time,
     * it is created lazily and initialized.
     *
     * @param id the identifier of the view to select
     */
    public void selectView(ViewID id) {
        requireNonNull(id);
        if (id == ViewID.EDITOR_VIEW) {
            createEditorViewIfMissing();
            getView(ViewID.EDITOR_VIEW, EditorView.class).editor().start();
        }
        selectedID.set(id);
    }

    /**
     * @return the property representing the currently selected view ID
     */
    public ObjectProperty<ViewID> selectedIDProperty() {
        return selectedID;
    }

    /**
     * Checks whether the given view ID is currently selected.
     *
     * @param id the view ID to check
     * @return {@code true} if the view is selected
     */
    public boolean isSelected(ViewID id) {
        requireNonNull(id);
        return id == selectedID.get();
    }

    /**
     * @return a read-only property representing the currently active view
     */
    public ReadOnlyObjectProperty<View> currentViewProperty() {
        return currentView;
    }

    /**
     * @return the currently active view, or {@code null} if none is active yet
     */
    public View currentView() {
        return currentView.get();
    }

    /**
     * Retrieves a view by ID and type.
     *
     * @param id        the view ID
     * @param viewClass the expected view type
     * @param <V>       the view type
     * @return the view instance
     * @throws IllegalArgumentException if no view is registered or the type does not match
     */
    @SuppressWarnings("unchecked")
    public <V extends View> V getView(ViewID id, Class<V> viewClass) {
        requireNonNull(id);
        requireNonNull(viewClass);

        final View view = viewMap.get(id);
        if (view == null) {
            throw new IllegalArgumentException(
                "No view with ID '%s' is registered".formatted(id));
        }
        if (viewClass.isInstance(view)) {
            return (V) view;
        }

        throw new IllegalArgumentException(
            "View with ID '%s' has type %s (expected: %s)"
                .formatted(id, view.getClass(), viewClass));
    }

    /**
     * @return an {@link Optional} containing the editor view if it has been created
     */
    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable((EditorView) viewMap.get(ViewID.EDITOR_VIEW));
    }

    /**
     * Lazily creates the editor view if it does not yet exist.
     */
    private void createEditorViewIfMissing() {
        var editorView = (EditorView) viewMap.get(ViewID.EDITOR_VIEW);
        if (editorView == null) {
            editorView = editorViewFactory.get();
            editorView.editor().init(GameBox.CUSTOM_MAP_DIR);
            viewMap.put(ViewID.EDITOR_VIEW, editorView);
        }
    }
}
