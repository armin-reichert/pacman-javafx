package de.amr.basics.ui.entities.hud.score;

import java.io.File;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

public record ScorePersistencyComp(File file) {

    public static final String GITHUB_PACMAN_JAVAFX = "https://github.com/armin-reichert/pacman-javafx";

    public static final String ATTR_DATE = "date";
    public static final String ATTR_LEVEL = "level";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_URL = "url";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ScorePersistencyComp(File file) {
        this.file = requireNonNull(file);
    }
}
