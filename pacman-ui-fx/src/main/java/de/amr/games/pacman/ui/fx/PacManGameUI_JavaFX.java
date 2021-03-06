package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.GameScene2D;
import de.amr.games.pacman.ui.fx.common.GameScene3D;
import de.amr.games.pacman.ui.fx.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.PlayScene3D;
import de.amr.games.pacman.ui.fx.common.SceneContainer2D;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
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

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);

	private GameModel game;
	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();
	private CameraController camControl;

	private final Text infoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final StackPane mainSceneRoot = new StackPane();
	private final Scene mainScene;
	private final SceneContainer2D subScene2DContainer;

	private BooleanProperty scenes3DProperty = new SimpleBooleanProperty();
	private BooleanProperty infoVisibleProperty = new SimpleBooleanProperty();
	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		game = controller.getGame();
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
		subScene2DContainer = new SceneContainer2D(width, height);
		mainSceneRoot.getChildren().addAll(flashMessageView, infoView);
		mainScene = new Scene(mainSceneRoot, width, height, Color.BLACK);
		stage.setScene(mainScene);
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> Platform.exit());

		// TODO is separate keyboard still needed?
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			switch (e.getCode()) {
			case F11:
				stage.setFullScreen(true);
				break;
			case DIGIT3:
				if (e.isControlDown()) {
					scenes3DProperty.set(!scenes3DProperty.get());
					String message = String.format("3D scenes %s", scenes3DProperty.get() ? "ON" : "OFF");
					showFlashMessage(message, clock.sec(1));
				}
				break;
			case C:
				if (e.isControlDown()) {
					toggleCameraControlFor2DScene();
				}
				break;
			case I:
				if (e.isControlDown()) {
					infoVisibleProperty.set(!infoVisibleProperty.get());
				}
				break;
			default:
				break;
			}
		});

		infoView.setFill(Color.WHITE);
		infoView.setFont(Font.font("Sans", 12));
		infoView.setText("");
		infoView.visibleProperty().bind(infoVisibleProperty);
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		stage.centerOnScreen();
		stage.show();

	}

	@Override
	public void show() {
		// done in start()
	}

	private void addResizeHandler(GameScene scene) {
		if (scene instanceof GameScene2D) {
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / GameScene.ASPECT_RATIO;
				if (newHeight < mainScene.getHeight()) {
					subScene2DContainer.resize(newWidth, newHeight);
					log("New scene height: %f", newHeight);
				}
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = (int) mainScene.getWidth();
				double newHeight = n.doubleValue();
				subScene2DContainer.resize(Math.max(newWidth, mainScene.getWidth()), newHeight);
				log("New scene height: %f", newHeight);
			});
		} else if (scene instanceof GameScene3D) {
			GameScene3D scene3D = (GameScene3D) scene;
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / GameScene.ASPECT_RATIO;
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
		SubScene newSubScene = null;
		if (newGameScene instanceof GameScene3D) {
			GameScene3D gameScene3D = (GameScene3D) newGameScene;
			newSubScene = gameScene3D.getSubScene();
			gameScene3D.resize(mainScene.getWidth(), mainScene.getHeight());
			gameScene3D.getCamera().setTranslateY(gameScene3D.getSubScene().getHeight() / 2);
			gameScene3D.getCamera().setTranslateZ(-gameScene3D.getSubScene().getHeight());
			gameScene3D.getCamera().setRotate(30);
			camControlOn(gameScene3D.getCamera());
		} else if (newGameScene instanceof GameScene2D) {
			newSubScene = subScene2DContainer.getSubScene();
			subScene2DContainer.setGameScene((GameScene2D) newGameScene);
			subScene2DContainer.resize(mainScene.getWidth(), mainScene.getHeight());
			subScene2DContainer.perspectiveViewOff();
			camControlOff();
		} else {
			throw new IllegalStateException();
		}
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(newSubScene, flashMessageView, infoView);
		addResizeHandler(newGameScene);
		newGameScene.start();
		currentGameScene = newGameScene;
		log("New game scene %s started", newGameScene);
	}

	private GameScene createGameScene(Camera camera, double height) {
		GameType currentGame = currentGame();
		if (currentGame == PACMAN) {
			FXRendering pacManRendering = renderings.get(PACMAN);
			SoundManager pacManSounds = sounds.get(PACMAN);
			switch (game.state) {
			case INTRO:
				return new PacMan_IntroScene(camera, controller, pacManRendering, pacManSounds);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new PacMan_IntermissionScene1(camera, controller, pacManRendering, pacManSounds);
				}
				if (game.intermissionNumber == 2) {
					return new PacMan_IntermissionScene2(camera, controller, pacManRendering, pacManSounds);
				}
				if (game.intermissionNumber == 3) {
					return new PacMan_IntermissionScene3(camera, controller, pacManRendering, pacManSounds);
				}
				throw new IllegalStateException();
			default:
				return scenes3DProperty.get() ? new PlayScene3D(controller, height)
						: new PlayScene2D(camera, controller, pacManRendering, pacManSounds);
			}
		}

		else if (currentGame == MS_PACMAN) {
			FXRendering msPacManRendering = renderings.get(MS_PACMAN);
			SoundManager msPacManSounds = sounds.get(MS_PACMAN);
			switch (game.state) {
			case INTRO:
				return new MsPacMan_IntroScene(camera, controller, msPacManRendering, msPacManSounds);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new MsPacMan_IntermissionScene1(camera, controller, msPacManRendering, msPacManSounds);
				}
				if (game.intermissionNumber == 2) {
					return new MsPacMan_IntermissionScene2(camera, controller, msPacManRendering, msPacManSounds);
				}
				if (game.intermissionNumber == 3) {
					return new MsPacMan_IntermissionScene3(camera, controller, msPacManRendering, msPacManSounds);
				}
				throw new IllegalStateException();
			default:
				return scenes3DProperty.get() ? new PlayScene3D(controller, height)
						: new PlayScene2D(camera, controller, msPacManRendering, msPacManSounds);
			}
		}
		throw new IllegalStateException();
	}

	private Class<? extends GameScene> getSceneClassForCurrentGame() {
		if (currentGame() == PACMAN) {
			switch (game.state) {
			case INTRO:
				return PacMan_IntroScene.class;
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return PacMan_IntermissionScene1.class;
				}
				if (game.intermissionNumber == 2) {
					return PacMan_IntermissionScene2.class;
				}
				if (game.intermissionNumber == 3) {
					return PacMan_IntermissionScene3.class;
				}
				break;
			default:
				return scenes3DProperty.get() ? PlayScene3D.class : PlayScene2D.class;
			}
		} else {
			switch (game.state) {
			case INTRO:
				return MsPacMan_IntroScene.class;
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return MsPacMan_IntermissionScene1.class;
				}
				if (game.intermissionNumber == 2) {
					return MsPacMan_IntermissionScene2.class;
				}
				if (game.intermissionNumber == 3) {
					return MsPacMan_IntermissionScene3.class;
				}
				break;
			default:
				return scenes3DProperty.get() ? PlayScene3D.class : PlayScene2D.class;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
	}

	@Override
	public void onGameStateChanged(PacManGameState from, PacManGameState to) {
		if (from == PacManGameState.CHANGING_LEVEL) {
			currentGameScene.start();
			showFlashMessage("Enter level " + game.levelNumber, clock.sec(1));
		}
	}

	@Override
	public void update() {
		Class<? extends GameScene> sceneToDisplay = getSceneClassForCurrentGame();
		if (currentGameScene == null || currentGameScene.getClass() != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentGameScene, sceneToDisplay);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (GameScene2D.class.isAssignableFrom(sceneToDisplay)) {
				GameScene scene2D = createGameScene(subScene2DContainer.getSubScene().getCamera(), mainScene.getHeight());
				setGameScene(scene2D);
			} else if (GameScene3D.class.isAssignableFrom(sceneToDisplay)) {
				GameScene scene3D = createGameScene(null, mainScene.getHeight());
				setGameScene(scene3D);
			}
		}
		currentGameScene.update();
		flashMessageView.update();
		updateInfoView();

		// 2D content mus be drawn explicitly:
		if (currentGameScene instanceof GameScene2D) {
			subScene2DContainer.draw();
		}
	}

	private void updateInfoView() {
		String text = "";
		if (currentGameScene instanceof GameScene2D) {
			text += String.format("\n2D scene: w=%.2f h=%.2f", subScene2DContainer.getSubScene().getWidth(),
					subScene2DContainer.getSubScene().getHeight());
		} else {
			GameScene3D scene3D = (GameScene3D) currentGameScene;
			text += String.format("\n3D scene: w=%.2f h=%.2f", scene3D.getSubScene().getWidth(),
					scene3D.getSubScene().getHeight());
			if (camControl != null) {
				text += camControl.getCameraInfo();
				text += "\n";
			}
			text += String.format("Main scene: w=%.2f h=%.2f", mainScene.getWidth(), mainScene.getHeight());
		}
		infoView.setText(text);
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private void toggleCameraControlFor2DScene() {
		if (camControl != null) {
			camControlOff();
			subScene2DContainer.perspectiveViewOff();
			showFlashMessage("Perspective View OFF", clock.sec(0.5));
		} else {
			camControlOn(subScene2DContainer.getPerspectiveCamera());
			subScene2DContainer.perspectiveViewOn();
			showFlashMessage("Perspective View ON", clock.sec(0.5));
		}
	}

	private void camControlOff() {
		if (camControl != null) {
			mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);
		}
		camControl = null;
	}

	private void camControlOn(Camera camera) {
		camControl = new CameraController(camera);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);
	}

	@Override
	public void reset() {
		currentGameScene.end();
		onGameChanged(game);
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