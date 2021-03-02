package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.ControllablePerspectiveCamera;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public static final int PLAYGROUND_WIDTH_UNSCALED = 28 * TS;
	public static final int PLAYGROUND_HEIGHT_UNSCALED = 36 * TS;

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> gameScenes = new EnumMap<>(GameType.class);

	private GameModel game;
	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final double scaling;

	private Stage stage;
	private Text camInfoView;
	private FlashMessageView flashMessageView;
	private ControllablePerspectiveCamera cam;
	private Canvas canvas;

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		scaling = Math.round(height / PLAYGROUND_HEIGHT_UNSCALED);
		buildStage();
		createGameScenes();
		onGameChangedFX(controller.getGame());
		log("JavaFX UI (scaling: %.2f) created at clock tick %d", scaling, clock.ticksTotal);
	}

	private void buildStage() {
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0); // TODO
		});
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		// build scene
		cam = new ControllablePerspectiveCamera();

		camInfoView = new Text();
		camInfoView.setTextAlignment(TextAlignment.CENTER);
		camInfoView.setFill(Color.WHITE);
		camInfoView.setFont(Font.font("Sans", 24));
		Bindings.bindBidirectional(camInfoView.textProperty(), cam.infoProperty);

		flashMessageView = new FlashMessageView();

		canvas = new Canvas(PLAYGROUND_WIDTH_UNSCALED * scaling, PLAYGROUND_HEIGHT_UNSCALED * scaling);
		canvas.getGraphicsContext2D().scale(scaling, scaling);

		Parent sceneContent = updateStage(null);
		stage.setScene(new Scene(sceneContent, Color.BLACK));
	}

	@Override
	public void show() {
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		Platform.runLater(() -> onGameChangedFX(newGame));
	}

	@Override
	public void reset() {
		currentGameScene.end();
		onGameChanged(game);
	}

	@Override
	public void update() {
		Platform.runLater(() -> {
			updateFX();
			renderFX();
		});
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageView.showMessage(message, ticks);
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO
		}
		return Optional.of(sounds.get(currentGame()));
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return Optional.of(renderings.get(currentGame()));
	}

	private void createGameScenes() {
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		gameScenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(controller, scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(controller, scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(controller, scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(controller, scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(controller, scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));
		gameScenes.get(MS_PACMAN).get(4).cameraAllowed = true;

		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
		gameScenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(controller, scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(controller, scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(controller, scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(controller, scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(controller, scaling, renderings.get(PACMAN), sounds.get(PACMAN))//
		));
		gameScenes.get(PACMAN).get(4).cameraAllowed = true;
	}

	private void handleGlobalKeys() {
		if (keyboard.keyPressed("F11")) {
			stage.setFullScreen(true);
		}
		if (keyboard.keyPressed("C")) {
			toggleCamera();
		}
		if (keyboard.keyPressed("S")) {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
		}
		if (keyboard.keyPressed("F")) {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
		}
	}

	private Parent updateStage(GameScene gameScene) {
		StackPane layout = new StackPane();
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);
		layout.getChildren().addAll(canvas, camInfoView, flashMessageView);
		return layout;
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private GameScene currentGameScene() {
		GameType currentGame = currentGame();
		switch (game.state) {
		case INTRO:
			return gameScenes.get(currentGame).get(0);
		case INTERMISSION:
			return gameScenes.get(currentGame).get(game.intermissionNumber);
		default:
			return gameScenes.get(currentGame).get(4);
		}
	}

	private void onGameChangedFX(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		setGameScene(currentGameScene());
	}

	private void setGameScene(GameScene newGameScene) {
		currentGameScene = newGameScene;
		currentGameScene.start();
		stage.getScene().setRoot(updateStage(newGameScene));
		if (!currentGameScene.cameraAllowed) {
			cameraOff();
		}
	}

	private void updateFX() {
		handleGlobalKeys();
		GameScene sceneToDisplay = currentGameScene();
		if (currentGameScene != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentGameScene, sceneToDisplay);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			setGameScene(sceneToDisplay);
		}
		currentGameScene.update();
		flashMessageView.update();
	}

	private void renderFX() {
		try {
			if (cam != null) {
				currentGameScene.updateCamera(cam);
			}
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.setFill(Color.BLACK);
			g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
			currentGameScene.draw(g);
		} catch (Exception x) {
			log("Exception occurred when rendering scene %s", currentGameScene);
			x.printStackTrace();
		}
	}

	private void cameraOn() {
		cam.setRotate(30);
		cam.setTranslateZ(-240);
		stage.getScene().setCamera(cam);
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, cam::onKeyPressed);
		camInfoView.setVisible(true);
	}

	private void cameraOff() {
		cam.setTranslateX(0);
		cam.setTranslateY(0);
		cam.setTranslateZ(0);
		cam.setRotate(0);
		stage.getScene().removeEventHandler(KeyEvent.KEY_PRESSED, cam::onKeyPressed);
		stage.getScene().setCamera(null);
		camInfoView.setVisible(false);
	}

	private void toggleCamera() {
		if (!currentGameScene.cameraAllowed) {
			return;
		}
		if (stage.getScene().getCamera() == null) {
			cameraOn();
			showFlashMessage("Camera ON", clock.sec(1));
		} else {
			cameraOff();
			showFlashMessage("Camera OFF", clock.sec(1));
		}
	}
}