/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Dashboard with different sections showing info and allowing configuration.
 * 
 * @author Armin Reichert
 */
public class Dashboard extends VBox {

	public static final int MIN_COL_WIDTH = 100;
	public static final int MIN_LABEL_WIDTH = 120;
	public static final Color TEXT_COLOR = Color.WHITE;
	public static final Font LABEL_FONT = Font.font("Tahoma", 12);
	public static final Font TEXT_FONT = Font.font("Tahoma", 12);

	private final List<Section> sections = new ArrayList<>();

	public Dashboard(Theme theme) {
		sections.add(new SectionGeneral(theme, "General"));
		sections.add(new SectionAppearance(theme, "Appearance"));
		sections.add(new Section3D(theme, "3D Settings"));
		sections.add(new SectionGameControl(theme, "Game Control"));
		sections.add(new SectionGameInfo(theme, "Game Info"));
		sections.add(new SectionGhostsInfo(theme, "Ghosts Info"));
		sections.add(new SectionKeys(theme, "Keyboard Shortcuts"));
		sections.add(new SectionAbout(theme, "About"));
		sections().map(Section::getRoot).forEach(getChildren()::add);
	}

	public Stream<Section> sections() {
		return sections.stream();
	}

	public void update() {
		sections.forEach(Section::update);
	}
}