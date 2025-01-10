package de.amr.games.pacman.lib.tilemap;

import java.util.Set;
import java.util.stream.Stream;

/**
 * The encoding depends on the byte values in interface {@link TileEncoding}!
 */
public enum ObstacleType {
    CROSS_SHAPE(
        "dfdgegfdfege",
        "dcfdgecgfcdfegce",
        "dfbdgbegfdbfebge",
        "dcfbdgbecgfcdbfebgce"
    ),

    F_SHAPE(
        "dcgfcdbfebgdbfeb",
        "dgbefbdgbecgfceb",
        "dcgfcdbfebgcdbfeb",
        "dgbecfbdgbecgfceb",
        "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce",
        "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce"
    ),

    GAMMA_SPIKED("dcfbdgbecgfcdbfeb"),
    GAMMA_SPIKED_MIRRORED("dgbecgfcdbfebgceb"),

    H_SHAPE(
        "dgefdgbfegdfeb",
        "dcgfdegfcedfge",
        "dgbecfbdgbfebgcdbfeb",
        "dcgfcdbecgfcedcfbgce"
    ),

    L_SHAPE(
        "dgegfceb", "dcgfdfeb", "dcgbfege", "dfdgbfce", // 8 segments
        "dcgbfegce", "dcfdgbfce", "dcgfcdfeb", "dgecgfceb", // 9 segments
        "dgbegfceb", "dcgfdbfeb", "dcgbfebge", "dfbdgbfce", // 9 segments
        "dcgbfebgce", "dcfbdgbfce", "dgbecgfceb", "dcgfcdbfeb" // 10 segment
    ),

    L_SPIKED("dcfbdgbecgbfebgce"),

    L_SPIKED_MIRRORED("dcfbdgbfcdbfebgce"),

    LL_SHAPE("dcgbecgbfebgcebgce"),

    LL_SHAPE_MIRRORED("dcfbdcfbdgbfcdbfce"),

    LLL_SHAPE("dcgbecgbecgbfebgcebgcebgce"),

    LLL_SHAPE_MIRRORED("dcfbdcfbdcfbdgbfcdbfcdbfce"),

    O_SHAPE("dgfe", "dcgfce", "dgbfeb", "dcgbfceb"),

    OPEN_SQUARE_NW("dgbecfbgcedcgbfceb"),
    OPEN_SQUARE_NE("dcgbfcedcfbgcdbfeb"),
    OPEN_SQUARE_SE("dcgbfebgcdbecgfceb"),
    OPEN_SQUARE_SW("dcgfcdbecfbdgbfceb"),

    S_SHAPE(
        "dcfdgbfcdfeb",
        "dgecgbfegceb",
        "dcgbegfcebge",
        "dfbdcgfdbfce",
        "dcfbdgbfcdbfeb",
        "dgbecgbfebgceb",
        "dcgbecgfcebgce",
        "dcfbdcgfcdbfce"
    ),

    SPACE_SHIP_UP("dcfbdcgfcdbecgfcebgce"),
    SPACE_SHIP_DOWN("dcgbecgfcdbfcedcfbgce"),
    SPACE_SHIP_LEFT("dcfbdgbecgbfebgcdbfeb"),
    SPACE_SHIP_RIGHT("dgbecfbdgbfcdbfebgceb"),

    T_SHAPE(
        "dgbecgfcdbfeb",
        "dcfbdgbfebgce",
        "dcgfcdbfebgce",
        "dcfbdgbecgfce"
    ),

    T_SHAPE_TWO_ROWS("dcgbecgfcdbfceb"),

    U_SHAPE(
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
