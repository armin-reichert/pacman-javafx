package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * @author Armin Reichert
 */
public class StartPage implements Page {

	private final StackPane root = new StackPane();
	private final BorderPane content = new BorderPane();
	private final Theme theme;
	private final Node playButton;

	public StartPage(Theme theme) {
		checkNotNull(theme);
		this.theme = theme;
		playButton = createPlayButton();
		content.setBottom(playButton);
		BorderPane.setAlignment(playButton, Pos.CENTER);
		playButton.setTranslateY(-10);
		root.setBackground(ResourceManager.coloredBackground(Color.BLACK));
		root.getChildren().add(content);
	}

	@Override
	public void setSize(double width, double height) {
	}

	@Override
	public StackPane root() {
		return root;
	}

	public void setGameVariant(GameVariant gameVariant) {
		checkGameVariant(gameVariant);
		var image = gameVariant == GameVariant.MS_PACMAN
				? theme.image("mspacman.startpage.image")
				: theme.image("pacman.startpage.image");
		var backgroundImage = new BackgroundImage(image,
			BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
			new BackgroundSize(
				AUTO,	AUTO, // width, height
				false, false, // as percentage
				true, // contain
				false // cover
		));
		content.setBackground(new Background(backgroundImage));
	}

	public void setPlayButtonAction(Runnable action) {
		playButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				action.run();
			}
		});
	}

	public void setOnKeyPressed(EventHandler<KeyEvent> handler) {
		root.setOnKeyPressed(handler);
	}

	// TODO This should be a real button but it seems WebFX/GWT has issues with graphic buttons
	private Node createPlayButton() {
		var label = new Text("Play!");
		label.setFill(theme.color("startpage.button.color"));
		label.setFont(theme.font("startpage.button.font"));

		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));
		label.setEffect(ds);

		var button = new BorderPane(label);
		button.setMaxSize(200, 100);
		button.setPadding(new Insets(10));
		button.setCursor(Cursor.HAND);
		button.setBackground(ResourceManager.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));

		return button;
	}
}