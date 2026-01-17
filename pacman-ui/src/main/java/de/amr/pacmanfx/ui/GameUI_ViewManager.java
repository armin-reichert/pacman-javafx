package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.tinylog.Supplier;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameUI_ViewManager {

    private final ObjectProperty<GameUI_View> currentView = new SimpleObjectProperty<>();
    private final StartPagesCarousel startPagesView;
    private final PlayView playView;
    private final Supplier<EditorView> editorViewCreator;
    private EditorView editorView; // created on-demand

    GameUI_ViewManager(
        GameUI ui,
        Scene scene,
        Pane layoutPane,
        Supplier<EditorView> editorViewCreator,
        FlashMessageView flashMessageView)
    {
        this.startPagesView = new StartPagesCarousel();
        startPagesView.setUI(ui);

        this.playView = new PlayView(scene);
        playView.setUI(ui);

        this.editorViewCreator = editorViewCreator;

        currentView.addListener((_, oldView, newView) -> {
            if (oldView != null) {
                oldView.onExit();
            }
            if (newView != null) {
                layoutPane.getChildren().set(0, newView.root());
                newView.onEnter();
            }
            flashMessageView.clear();
        });
    }

    void selectStartView() {
        selectView(startPagesView);
    }

    void selectPlayView() {
        selectView(playView);
    }

    void selectEditorView() {
        if (editorView == null) {
            editorView = editorViewCreator.get();
            editorView.editor().init(GameBox.CUSTOM_MAP_DIR);
        }
        selectView(editorView);
        editorView.editor().start();
    }

    private void selectView(GameUI_View view) {
        requireNonNull(view);
        final GameUI_View oldView = currentView();
        if (oldView == view) {
            return;
        }
        if (oldView != null) {
            oldView.onExit();
            oldView.actionBindingsManager().releaseBindings(GameUI.KEYBOARD);
        }
        view.actionBindingsManager().activateBindings(GameUI.KEYBOARD);
        currentView.set(view);
    }

    ObjectProperty<GameUI_View> currentViewProperty() {
        return currentView;
    }

    public GameUI_View currentView() {
        return currentView.get();
    }

    public StartPagesCarousel startPagesView() {
        return startPagesView;
    }

    public PlayView playView() {
        return playView;
    }

    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    EditorView editorView() {
        return editorView;
    }
}
