/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.GlobalsUI;
import de.amr.pacmanfx.ui.d3.Globals3D;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameViewImplementation implements GameView {

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>(this, "stage");

    private Game game;

    private final GameViewMainScene mainScene;

    private final StatusIconBox statusIconBox;

    private StringBinding stageTitleBinding;

    public GameViewImplementation(GameViewMainScene mainScene, StatusIconBox statusIconBox) {
        this.mainScene = mainScene;
        this.statusIconBox = statusIconBox;
    }

    @Override
    public void setGame(Game game) {
        this.game = requireNonNull(game);

        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(game),
            game.clock().updatesDisabledProperty(),
            game.variantNameProperty(),
            game.ui().subViews().selectedSubViewProperty(),
            game.ui().gameScenes().gameSceneProperty(),
            GlobalsUI.PROPERTY_DEBUG_INFO_VISIBLE,
            Globals3D.PROPERTY_3D_ENABLED
        );
    }

    @Override
    public void show() {
        final Stage theStage = stage();
        if (theStage == null) {
            throw new IllegalStateException("No stage assigned to game view");
        }
        theStage.setScene(mainScene);
        theStage.titleProperty().bind(stageTitleBindingProperty());

        updateStageIcon(game);
        registerIconUpdater(game);

        theStage.setMinWidth(GlobalsUI.MIN_STAGE_WIDTH);
        theStage.setMinHeight(GlobalsUI.MIN_STAGE_HEIGHT);
        theStage.centerOnScreen();
        theStage.show();
    }

    @Override
    public void replaceSubView(SubView subView) {
        mainScene.replaceSubView(subView);
    }

    @Override
    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    @Override
    public GameViewMainScene mainScene() {
        return mainScene;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StringBinding stageTitleBindingProperty() {
        return stageTitleBinding;
    }

    private String computeStageTitle(Game game) {
        final SubView view = game.ui().subViews().currentView();
        return view == null
            ? game.ui().translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(game));
    }

    // Private area

    private void updateStageIcon(Game game) {
        final Image icon = game.currentUIConfig().assets().image("app_icon");
        if (icon != null) {
            game.ui().view().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private final ChangeListener<String> iconUpdateListener = (_, _, _) -> updateStageIcon(game);

    private void registerIconUpdater(Game game) {
        game.variantNameProperty().removeListener(iconUpdateListener);
        game.variantNameProperty().addListener(iconUpdateListener);
    }

    private String titleForCurrentGameScene(Game game) {
        final GameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = GlobalsUI.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = Globals3D.PROPERTY_3D_ENABLED.get();
        final boolean paused = game.clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(game, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(Game game, boolean paused, boolean is3D) {
        final String gameVariantName = game.currentGameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = game.ui().translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = game.currentUIConfig();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.textBundle() != null
            && appSpecificTranslator.textBundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}
