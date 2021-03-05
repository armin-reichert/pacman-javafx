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
import de.amr.games.pacman.ui.fx.common.ContainerScene2D;
import de.amr.games.pacman.ui.fx.common.ControllableCamera;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.GameScene2D;
import de.amr.games.pacman.ui.fx.common.GameScene3D;
import de.amr.games.pacman.ui.fx.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.PlayScene3D;
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
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	private boolean use3D = false;

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> gameScenes = new EnumMap<>(GameType.class);

	private GameModel game;
	private GameScene currentGameScene;
	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final Text infoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();

	private Stage stage;
	private Scene mainScene;
	private ContainerScene2D container2D;

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		createGameScenes(height);
		initStage(ContainerScene2D.ASPECT_RATIO * height, height);
		onGameChangedFX(controller.getGame());
		addResizeHandler(currentGameScene);
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	@Override
	public void show() {
//		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	private void initStage(double sceneWidth, double sceneHeight) {
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			controller.endGameLoop();
			Platform.exit();
		});
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		infoView.setFill(Color.WHITE);
		infoView.setFont(Font.font("Sans", 12));
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		// assuming intial game scene is 2D
		container2D = new ContainerScene2D(sceneWidth, sceneHeight);
	}

	private void addResizeHandler(GameScene scene) {
		if (scene instanceof GameScene2D) {
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / ContainerScene2D.ASPECT_RATIO;
				if (newHeight < mainScene.getHeight()) {
					container2D.resize(newWidth, newHeight);
					log("New scene height: %f", newHeight);
				}
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = (int) mainScene.getWidth();
				double newHeight = n.doubleValue();
				container2D.resize(Math.max(newWidth, mainScene.getWidth()), newHeight);
				log("New scene height: %f", newHeight);
			});
		} else if (scene instanceof GameScene3D) {
			GameScene3D scene3D = (GameScene3D) scene;
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / ContainerScene2D.ASPECT_RATIO;
				scene3D.resize(newWidth, newHeight);
				log("New scene height: %f", newHeight);
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = (int) mainScene.getWidth();
				double newHeight = n.doubleValue();
				scene3D.resize(newWidth, newHeight);
				log("New scene height: %f", newHeight);
			});
		}
	}

	private void setGameScene(GameScene newGameScene) {
		if (newGameScene instanceof GameScene3D) {
			GameScene3D scene3D = (GameScene3D) newGameScene;
			mainScene = new Scene(new StackPane(scene3D.getSubScene(), flashMessageView, infoView), mainScene.getWidth(),
					mainScene.getHeight(), Color.BLACK);
			stage.setScene(mainScene);
			scene3D.resize(mainScene.getWidth(), mainScene.getHeight());
			ControllableCamera camera = scene3D.getCamera().get();
			camera.setTranslateY(scene3D.getSubScene().getHeight());
			camera.setTranslateZ(-scene3D.getSubScene().getHeight());
			camera.setRotate(30);
			scene3D.enableCamera(true);
		} else {
			mainScene = new Scene(new StackPane(container2D.getSubScene(), flashMessageView, infoView), Color.BLACK);
			stage.setScene(mainScene);
			if (newGameScene.getCamera().isPresent()) {
				ControllableCamera camera = newGameScene.getCamera().get();
				if (newGameScene.isCameraEnabled()) {
					container2D.cameraOn(camera);
				} else {
					container2D.cameraOff(camera);
				}
			} else if (currentGameScene != null && currentGameScene.getCamera().isPresent()) {
				container2D.cameraOff(currentGameScene.getCamera().get());
			}
		}
		addResizeHandler(newGameScene);

		if (currentGameScene != null) {
			currentGameScene.getCamera()
					.ifPresent(camera -> stage.removeEventHandler(KeyEvent.KEY_PRESSED, camera::handleKeyEvent));
		}
		newGameScene.getCamera().ifPresent(camera -> stage.addEventHandler(KeyEvent.KEY_PRESSED, camera::handleKeyEvent));

		newGameScene.start();
		currentGameScene = newGameScene;
	}

	private void createGameScenes(double height) {
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		gameScenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene2D(controller, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene3D(controller, height)//
		));

		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
		gameScenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene2D(controller, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene3D(controller, height)//
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
			return gameScenes.get(currentGame).get(use3D ? 5 : 4);
		}
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
		updateInfoView();

		// 2D content is drawn explicitly:
		if (currentGameScene instanceof GameScene2D) {
			try {
				container2D.draw((GameScene2D) currentGameScene);
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentGameScene);
				x.printStackTrace();
			}
		}
	}

	private void updateInfoView() {
		String text = "";
		if (currentGameScene.getCamera().isPresent() && currentGameScene.isCameraEnabled()) {
			text += currentGameScene.getCamera().get().getInfo();
			text += "\n";
		}
		text += String.format("Main scene: w=%.2f h=%.2f", mainScene.getWidth(), mainScene.getHeight());
		if (currentGameScene instanceof GameScene2D) {
			text += String.format("\n2D scene: w=%.2f h=%.2f", container2D.getSubScene().getWidth(),
					container2D.getSubScene().getHeight());
		} else {
			GameScene3D scene3D = (GameScene3D) currentGameScene;
			text += String.format("\n3D scene: w=%.2f h=%.2f", scene3D.getSubScene().getWidth(),
					scene3D.getSubScene().getHeight());
		}
		infoView.setText(text);
	}

	private void handleGlobalKeys() {
		if (keyboard.keyPressed("F11")) {
			stage.setFullScreen(true);
		}
		if (keyboard.keyPressed("F3")) {
			use3D = !use3D;
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
		if (currentGameScene instanceof GameScene2D) {
			if (currentGameScene.isCameraEnabled()) {
				currentGameScene.enableCamera(false);
				container2D.cameraOff(currentGameScene.getCamera().get());
				showFlashMessage("Camera OFF", clock.sec(0.5));
			} else if (currentGameScene.getCamera().isPresent()) {
				currentGameScene.enableCamera(true);
				container2D.cameraOn(currentGameScene.getCamera().get());
				showFlashMessage("Camera ON", clock.sec(0.5));
			}
		}
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
		if (currentGameScene instanceof GameScene2D) {
			return Optional.of(renderings.get(currentGame()));
		}
		if (currentGameScene instanceof PlayScene3D) {
			return Optional.of((PlayScene3D) currentGameScene);
		}
		return Optional.empty();
	}
}