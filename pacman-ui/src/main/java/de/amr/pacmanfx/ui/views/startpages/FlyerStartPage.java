/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.basics.json.JsonLoader;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneController;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.controls.GameStartButton;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;

import java.net.URL;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class FlyerStartPage implements StartPage {

    public static final float VOICE_DELAY_SEC = 1.0f;

    public record Config(
        String gameVariant,
        String title,
        String voice,
        String[] images
    ) {}

    protected final Config config;
    protected final StackPane rootPane = new StackPane();
    protected final Flyer flyer = new Flyer();
    protected String title;
    protected String gameVariantName;
    protected GameStartButton startButton;
    protected GameAppContext app;

    protected GameSceneController gameScene;

    private final Media voiceMedia;

    public FlyerStartPage(URL configURL) {
        requireNonNull(configURL);
        config = JsonLoader.load(configURL, Config.class);
        init(config.gameVariant());

        setTitle(config.title());

        final ResourceManager resourceManager = this::getClass; // load relative to subclass!
        flyer.setImages(Stream.of(config.images()).map(resourceManager::loadImage).toArray(Image[]::new));

        voiceMedia = resourceManager.loadMedia(config.voice());
    }

    private void init(String gameVariantName) {
        this.gameVariantName = requireNonNull(gameVariantName);

        title = "Start " + gameVariantName;

        rootPane.getStyleClass().add("flyer-start-page");
        rootPane.getChildren().add(flyer);

        // Let scroll wheel scroll through flyer pages
        rootPane.addEventHandler(ScrollEvent.SCROLL, e-> {
            if (e.getDeltaY() < 0) {
                flyer.nextFlyerPage();
            } else if (e.getDeltaY() > 0) {
                flyer.prevFlyerPage();
            }
        });

        startButton = createStartButton();
    }

    @Override
    public void onInput() {
        final Keyboard keyboard = app().input().keyboard();
        if (keyboard.isKeyPressed(KeyCode.DOWN)) {
            flyer.nextFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.UP)) {
            flyer.prevFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            if (app != null) {
                app.ui().sounds().voice().stop();
                app.ui().shortMessage(app.ui().translations().translate("flash.shut_up"));
            }
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER) && startButton != null) {
            startButton.fire();
        }
    }

    @Override
    public GameAppContext app() {
        return app;
    }

    @Override
    public void setGameApp(GameAppContext app) {
        this.app = requireNonNull(app);
    }

    @Override
    public void onEnter() {
        app.gameVariants().selectVariant(gameVariantName);
        flyer.selectPage(0);
        app.ui().sounds().voice().playAfterSec(VOICE_DELAY_SEC, voiceMedia);
        Platform.runLater(startButton::requestFocus);
    }

    @Override
    public void onExit() {
        app.ui().sounds().voice().stop();
    }

    @Override
    public Pane rootPane() {
        return rootPane;
    }

    @Override
    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected GameStartButton createStartButton() {
        final var button = new GameStartButton("START!");
        button.setOnAction(_ -> app.runAction(app.commonActions().gameFlowActions().actionStartGame()));
        rootPane.getChildren().add(button);

        StackPane.setAlignment(button, Pos.BOTTOM_CENTER);
        button.translateYProperty().bind(rootPane.heightProperty().divide(10).negate());

        return button;
    }
}