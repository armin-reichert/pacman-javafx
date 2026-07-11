/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.startpages;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.JsonConfigLoader;
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

    public record Config(
        String gameVariant,
        String title,
        String voice,
        String[] images
    ) {}

    protected final StackPane rootPane = new StackPane();
    protected final Flyer flyer = new Flyer();
    protected String title;
    protected String gameVariantName;
    protected GameStartButton startButton;
    protected Game game;
    protected Media voice;

    public FlyerStartPage(URL configURL) {
        requireNonNull(configURL);
        final ResourceManager resourceManager = this::getClass;

        var config = JsonConfigLoader.load(configURL, Config.class);
        init(config.gameVariant());

        setTitle(config.title());
        setVoice(resourceManager.loadMedia(config.voice()));
        flyer.setImages(
            Stream.of(config.images()).map(resourceManager::loadImage).toArray(Image[]::new)
        );
    }

    public FlyerStartPage(String variantName, String title, Media voiceMedia, Image... images) {
        init(variantName);

        setTitle(title);
        setVoice(voiceMedia);
        flyer.setImages(images);
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
    public void onInput(Input input) {
        final Keyboard keyboard = input.keyboard();
        if (keyboard.isKeyPressed(KeyCode.DOWN)) {
            flyer.nextFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.UP)) {
            flyer.prevFlyerPage();
        }
        else if (keyboard.isKeyPressed(KeyCode.S)) {
            if (game != null) {
                game.ui().sounds().stopAndDisposeVoice();
                game.ui().shortMessage(game.ui().translations().translate("flash.shut_up"));
            }
        }
        else if (keyboard.isKeyPressed(KeyCode.ENTER) && startButton != null) {
            startButton.fire();
        }
    }

    @Override
    public void connect(Game game) {
        this.game = requireNonNull(game);
    }

    @Override
    public void onEnter() {
        game.variants().selectVariant(gameVariantName);
        flyer.selectPage(0);
        talk();
        Platform.runLater(startButton::requestFocus);
    }

    @Override
    public void onExit() {
        stopTalking();
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

    public void setVoice(Media voice) {
        this.voice = requireNonNull(voice);
    }

    public void talk() {
        if (voice != null) {
            game.ui().sounds().playVoice(voice);
        }
    }


    public void stopTalking() {
        game.ui().sounds().stopAndDisposeVoice();
    }

    protected GameStartButton createStartButton() {
        final var button = new GameStartButton("START!");
        button.setOnAction(_ -> game.actions().gameFlowActions().actionStartGame().execute());
        rootPane.getChildren().add(button);

        StackPane.setAlignment(button, Pos.BOTTOM_CENTER);
        button.translateYProperty().bind(rootPane.heightProperty().divide(10).negate());

        return button;
    }
}