package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

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
import de.amr.games.pacman.ui.fx.common.ControllableCamera;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.GameScene2D;
import de.amr.games.pacman.ui.fx.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.SubScene2D;
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
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> gameScenes = new EnumMap<>(GameType.class);

	private GameModel game;
	private GameScene currentGameScene;
	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final Text camInfoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();

	private Stage stage;
	private Scene mainScene;
	private SubScene2D content2D;

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		createGameScenes();
		buildStage(SubScene2D.ASPECT_RATIO * height, height);
		onGameChangedFX(controller.getGame());
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	private void buildStage(double stageContentWidth, double stageContentHeight) {
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			controller.endGameLoop();
			Platform.exit();
		});
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		camInfoView.setFill(Color.WHITE);
		camInfoView.setFont(Font.font("Sans", 12));
		camInfoView.setTextAlignment(TextAlignment.CENTER);
		StackPane.setAlignment(camInfoView, Pos.TOP_LEFT);

		// assuming intial game scene is 2D
		content2D = new SubScene2D(stageContentWidth, stageContentHeight);

		mainScene = new Scene(new StackPane(content2D.getScene(), flashMessageView, camInfoView));
		mainScene.setFill(Color.DARKSLATEBLUE);
		mainScene.widthProperty().addListener((s, o, n) -> {
			double newWidth = n.doubleValue();
			double newHeight = newWidth / SubScene2D.ASPECT_RATIO;
			if (newHeight < mainScene.getHeight()) {
				content2D.resize(newWidth, newHeight);
				log("New main scene height: %f", newHeight);
			}
		});
		mainScene.heightProperty().addListener((s, o, n) -> {
			double newWidth = mainScene.getWidth();
			double newHeight = n.doubleValue();
			content2D.resize(Math.max(newWidth, stageContentWidth), newHeight);
			log("New main scene height: %f", newHeight);
		});

		stage.setScene(mainScene);
	}

	private void createGameScenes() {
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		gameScenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene2D(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));

		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
		gameScenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene2D(controller, renderings.get(PACMAN), sounds.get(PACMAN))//
		));
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

	private void setGameScene(GameScene newGameScene) {
		if (newGameScene.getCamera().isPresent()) {
			ControllableCamera camera = newGameScene.getCamera().get();
			camInfoView.textProperty().bind(camera.infoProperty);
			if (newGameScene.isCameraEnabled()) {
				content2D.cameraOn(camera);
			} else {
				content2D.cameraOff(camera);
			}
		} else if (currentGameScene != null && currentGameScene.getCamera().isPresent()) {
			content2D.cameraOff(currentGameScene.getCamera().get());
		}
		newGameScene.start();
		currentGameScene = newGameScene;
	}

	private void onGameChangedFX(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		setGameScene(currentGameScene());
	}

	private void updateAndRenderFX() {
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
		camInfoView.setVisible(currentGameScene.isCameraEnabled());

		// 2D content is drawn explicitly:
		if (currentGameScene instanceof GameScene2D) {
			try {
				content2D.draw((GameScene2D) currentGameScene);
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentGameScene);
				x.printStackTrace();
			}
		}
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

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private void toggleCamera() {
		if (currentGameScene.isCameraEnabled()) {
			currentGameScene.enableCamera(false);
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, currentGameScene.getCamera().get()::onKeyPressed);
			content2D.cameraOff(currentGameScene.getCamera().get());
			showFlashMessage("Camera OFF", clock.sec(0.5));
		} else if (currentGameScene.getCamera().isPresent()) {
			currentGameScene.enableCamera(true);
			content2D.cameraOn(currentGameScene.getCamera().get());
			stage.addEventHandler(KeyEvent.KEY_PRESSED, currentGameScene.getCamera().get()::onKeyPressed);
			showFlashMessage("Camera ON", clock.sec(0.5));
		}
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
		Platform.runLater(this::updateAndRenderFX);
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
}