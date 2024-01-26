package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PERSPECTIVE;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_PIP_ON;

/**
 * @author Armin Reichert
 */
public class GamePageContextMenu extends ContextMenu {
	private CheckMenuItem autopilotItem;
	private CheckMenuItem immunityItem;
	private CheckMenuItem pipItem;
	private ToggleGroup perspectivesToggleGroup;
	private Font titleItemFont = Font.font("Sans", FontWeight.BOLD, 14);

	public void rebuild(Theme theme, ActionHandler3D actionHandler, GameScene gameScene, List<ResourceBundle> messageBundles) {
		titleItemFont = theme.font("font.handwriting", 20);
		getItems().clear();

		getItems().add(createTitleItem(message(messageBundles,"scene_display")));
		if (gameScene instanceof PlayScene2D) {
			var item = new MenuItem(message(messageBundles,"use_3D_scene"));
			item.setOnAction(e -> actionHandler.toggle2D3D());
			getItems().add(item);
		} else if (gameScene instanceof PlayScene3D) {
			var item = new MenuItem(message(messageBundles,"use_2D_scene"));
			item.setOnAction(e -> actionHandler.toggle2D3D());
			getItems().add(item);
			pipItem = new CheckMenuItem(message(messageBundles,"pip"));
			pipItem.setOnAction(e -> actionHandler.togglePipVisible());
			getItems().add(pipItem);

			getItems().add(createTitleItem(message(messageBundles,"select_perspective")));
			perspectivesToggleGroup = new ToggleGroup();
			for (var p : Perspective.values()) {
				var rmi = new RadioMenuItem(message(messageBundles,p.name()));
				rmi.setUserData(p);
				rmi.setToggleGroup(perspectivesToggleGroup);
				getItems().add(rmi);
			}
			perspectivesToggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
				if (nv != null) {
					// Note: These are the user data of the radio menu item!
					PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
				}
			});
		}

		getItems().add(createTitleItem(message(messageBundles,"pacman")));
		autopilotItem = new CheckMenuItem(message(messageBundles,"autopilot"));
		autopilotItem.setOnAction(e -> actionHandler.toggleAutopilot());
		getItems().add(autopilotItem);
		immunityItem = new CheckMenuItem(message(messageBundles,"immunity"));
		immunityItem.setOnAction(e -> actionHandler.toggleImmunity());
		getItems().add(immunityItem);
	}

	private MenuItem createTitleItem(String title) {
		var text = new Text(title);
		text.setFont(titleItemFont);
		return new CustomMenuItem(text);
	}

	public void updateState() {
		if (perspectivesToggleGroup != null) {
			for (var toggle : perspectivesToggleGroup.getToggles()) {
				toggle.setSelected(PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
			}
		}
		if (pipItem != null) {
			pipItem.setSelected(PY_PIP_ON.get());
		}
		if (autopilotItem != null) {
			autopilotItem.setSelected(GameController.it().isAutoControlled());
		}
		if (immunityItem != null) {
			immunityItem.setSelected(GameController.it().isImmune());
		}
	}
}