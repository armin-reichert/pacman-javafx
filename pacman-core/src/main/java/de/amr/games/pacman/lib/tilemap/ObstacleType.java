package de.amr.games.pacman.lib.tilemap;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The encoding depends on the byte values in interface {@link TileEncoding}!
 */
public enum ObstacleType {
    CROSS(
        "dfdgegfdfege",
        "dcfdgecgfcdfegce",
        "dfbdgbegfdbfebge",
        "dcfbdgbecgfcdbfebgce"
    ),

    F(
        "dcgfcdbfebgdbfeb",
        "dgbefbdgbecgfceb",
        "dcgfcdbfebgcdbfeb",
        "dgbecfbdgbecgfceb",
        "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce",
        "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce"
    ),

    GAMMA_SPIKED("dcfbdgbecgfcdbfeb"),
    GAMMA_SPIKED_MIRRORED("dgbecgfcdbfebgceb"),

    H(
        "dgefdgbfegdfeb",
        "dcgfdegfcedfge",
        "dgbecfbdgbfebgcdbfeb",
        "dcgfcdbecgfcedcfbgce"
    ),

    L(
        "dgegfceb", "dcgfdfeb", "dcgbfege", "dfdgbfce", // 8 segments
        "dcgbfegce", "dcfdgbfce", "dcgfcdfeb", "dgecgfceb", // 9 segments
        "dgbegfceb", "dcgfdbfeb", "dcgbfebge", "dfbdgbfce", // 9 segments
        "dcgbfebgce", "dcfbdgbfce", "dgbecgfceb", "dcgfcdbfeb" // 10 segment
    ),

    L_SPIKED("dcfbdgbecgbfebgce"),

    L_SPIKED_MIRRORED("dcfbdgbfcdbfebgce"),

    LL("dcgbecgbfebgcebgce"),

    LL_MIRRORED("dcfbdcfbdgbfcdbfce"),

    LLL("dcgbecgbecgbfebgcebgcebgce"),

    LLL_MIRRORED("dcfbdcfbdcfbdgbfcdbfcdbfce"),

    O("dgfe", "dcgfce", "dgbfeb", "dcgbfceb"),

    OPEN_SQUARE_NW("dgbecfbgcedcgbfceb"),
    OPEN_SQUARE_NE("dcgbfcedcfbgcdbfeb"),
    OPEN_SQUARE_SE("dcgbfebgcdbecgfceb"),
    OPEN_SQUARE_SW("dcgfcdbecfbdgbfceb"),

    S(
        "dcfdgbfcdfeb",
        "dgecgbfegceb",
        "dcgbegfcebge",
        "dfbdcgfdbfce",
        "dcfbdgbfcdbfeb",
        "dgbecgbfebgceb",
        "dcgbecgfcebgce",
        "dcfbdcgfcdbfce"
    ),

    SPACESHIP_UP("dcfbdcgfcdbecgfcebgce"),
    SPACESHIP_DOWN("dcgbecgfcdbfcedcfbgce"),
    SPACESHIP_LEFT("dcfbdgbecgbfebgcdbfeb"),
    SPACESHIP_RIGHT("dgbecfbdgbfcdbfebgceb"),

    T(
        "dgbecgfcdbfeb",
        "dcfbdgbfebgce",
        "dcgfcdbfebgce",
        "dcfbdgbecgfce"
    ),

    T_TWO_ROWS("dcgbecgfcdbfceb"),

    U(
        "dcgbfcedfge", "dcgfdegfceb", "dcgbfegdfeb", "dgefdgbfceb",
        "dcgbfcedcfgce", "dcgfcdecgfceb", "dcgbfebgdbfeb", "dgbefbdgbfceb",
        "dcgbfcedcfbgce", "dcgfcdbecgfceb", "dcgbfebgcdbfeb", "dgbecfbdgbfceb"
    ),

    //TODO these obstacle types belong elsewhere

    // Junior Pac-Man Map #4 left of house
    JUNIOR_4_LEFT_OF_HOUSE("dcgbecgfcebgcedcfbgce"),

    // Junior Pac-Man Map #4 right of house
    JUNIOR_4_RIGHT_OF_HOUSE("dcfbgcedcfbdcgfcdbfce"),

    ANY;

    public static Optional<ObstacleType> identify(String encoding) {
        return Stream.of(values()).filter(type -> type.matches(encoding)).findFirst();
    }

    ObstacleType(String... encodingStrings) {
        encodings = Set.of(encodingStrings);
    }

    private final Set<String> encodings;

    public Stream<String> encodings() {
        return encodings.stream();
    }

    public boolean matches(String encoding) {
        return encodings.contains(encoding);
    }
}
