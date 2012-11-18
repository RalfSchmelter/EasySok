package org.easysok;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Point;

/**
 * 
 * This class represents a sokoban map.
 *
 * Note that there exists two version of most functions.
 * One taking a x and y values and one an index (x + y * getWidth()).
 *
 * The height and width of the map must be smaller than 128.
 */
public class Map {

    /**
     * A keeper on an empty field.
     */
    public static final int KEEPER = 0;

    /**
     * A keeper on a goal field.
     */
    public static final int KEEPER_ON_GOAL = 1;

    /**
     * A gem on an empty field.
     */
    public static final int GEM = 2;

    /**
     * A gem on an empty field.
     */
    public static final int GEM_ON_GOAL = 3;

    /**
     * An empty field.
     */
    public static final int EMPTY = 4;

    /**
     * A goal field.
     */
    public static final int GOAL = 5;

    /**
     * A wall field.
     */
    public static final int WALL = 6;

    /**
     * An outside field.
     */
    public static final int OUTSIDE = 7;

    /**
     * The map is valid.
     */
    public static final int IS_VALID = 0;

    /**
     * The map contains no keeper.
     */
    public static final int NO_KEEPER = 1;

    /**
     * The map contains more than one keeper.
     */
    public static final int TOO_MANY_KEEPERS = 2;

    /**
     * The map contains no gems.
     */
    public static final int NO_GEMS = 3;

    /**
     * The map contains more gems than goals.
     */
    public static final int MORE_GEMS_THAN_GOALS = 4;

    /**
     * The map contains more goals than gems.
     */
    public static final int MORE_GOALS_THAN_GEMS = 5;

    /**
     * The map is not closed (an outside field next to a non wall field).
     */
    public static final int MAP_LEAKS = 6;

    /**
     * The map is already solved.
     */
    public static final int MAP_SOLVED = 7;

    /**
     * Any other reason why the map is invalid.
     */
    public static final int MAP_INVALID = 8;

    /**
     * The field is crossed.
     */
    private static final int CROSSED = 8;

    /**
     * The field is reachable.
     */
    private static final int REACHABLE = 16;

    /**
     * The field is a deadlock field.
     */
    private static final int DEADLOCK = 32;

    /**
     * Mask out the piece.
     */
    private static final int PIECE = 7;

    /**
     * All attributes.
     */
    private static final int ALL = PIECE + CROSSED + REACHABLE + DEADLOCK;

    /**
     * Clear all pieces.
     */
    private static final int CLEAR_PIECE = ALL - PIECE;

    /**
     * Clear crossed.
     */
    private static final int CLEAR_CROSSED = ALL - CROSSED;

    /**
     * Clear reachable.
     */
    private static final int CLEAR_REACHABLE = ALL - REACHABLE;

    /**
     * Clear deadlocks.
     */
    private static final int CLEAR_DEADLOCK = ALL - DEADLOCK;

    /**
     * Mapping from piece->contains gem
     */
    private static boolean[] piece_contains_gem = new boolean[] {false, false, true, true, false, false, false, false};

    /**
     * Mapping from piece->contains goal
     */
    private static boolean[] piece_contains_goal = new boolean[] {false, true, false, true, false, true, false, false};

    /**
     * Mapping from piece->text
     */
    private static char[] piece_to_text = new char[] {'@', '+', '$', '*', ' ', '.', '#', ' '};

    /**
     * The matcher for checking if a line is a valid map line.
     */
    private static Matcher map_regexp = Pattern.compile("^ *#[# .$*@+]* *$").matcher("");

    /**
     * The width of the map.
     */
    private int width;

    /**
     * The height of the map.
     */
    private int height;

    /**
     * This is m_width * m_height.
     */
    private int size;

    /**
     * Here we store the position of the keeper.
     */
    private Point keeper;

    /**
     * The validity of the map.
     */
    private int validity;

    /**
     * The number of empty goals.
     */
    private int empty_goals;

    /**
     * If true, we have already calculated the deadlocks.
     */
    private boolean deadlocks_valid;

    /**
     * If true, the reachable fields are up to date.
     */
    private boolean reachable_valid;

    /**
     * If true, the number of empty goals is valid.
     */
    private boolean empty_goals_valid;

    /**
     * If true, the validity is valid :)
     */
    private boolean validity_valid;

    /**
     * Here we store the pieces and additional information of the fields.
     */
    private int[] pieces;

    /**
     * Here we store the offset to go one in x- or y-direction.
     */
    private int[] xy_offsets;

    /**
     * Creates a new map.
     *
     * @param width The width of the map.
     * @param height The height of the map.
     * @param pieces An array containing the pieces.
     */
    public Map(int width, int height, int[] pieces) {
        assert width > 0;
        assert height > 0;
        assert width < 128;
        assert height < 128;

        this.width = width;
        this.height = height;
        this.size = width * height;
        this.keeper = new Point(0, 0);
        this.empty_goals_valid = true;
        this.pieces = pieces.clone();
        this.xy_offsets = new int[] {-1, 1, -width, width};

        createOutsidePieces();
        setupKeeperAndEmptyGoals();
    }

    /**
     * Constructs the map from a list of lines in xsb format.
     *
     * The constructor removes all lines in the string list, which were before the
     * actual map and the lines of the actual map itself.
     * Be sure to call isValid() afterwards.
     *
     * @param lines The list with the lines.
     */
    public Map(List<String> lines) {
        this.empty_goals_valid = true;

        while (!lines.isEmpty() && !isMapLine(lines.get(0))) {
            lines.remove(0);
        }

        if (!lines.isEmpty()) {
            int max_width = 0;
            ArrayList<String> map_lines = new ArrayList<String>();

            while (!lines.isEmpty() && isMapLine(lines.get(0))) {
                String line = lines.get(0);
                lines.remove(0);

                while (line.charAt(line.length() - 1) == ' ') {
                    line = line.substring(0, line.length() - 1);
                }

                max_width = Math.max(max_width, line.length());
                map_lines.add(line);
            }

            this.width = max_width;
            this.height = map_lines.size();
            this.xy_offsets = new int[] {-1, 1, -width, width};
            this.size = width * height;
            this.pieces = new int[size];
            Arrays.fill(this.pieces, EMPTY);

            for (int y = 0; y < height; ++y) {
                String act_line = map_lines.get(y);
                int act_width = act_line.length();

                for (int x = 0; x < act_width; ++x) {
                    char act_char = act_line.charAt(x);

                    for (int i = 0; i < 8; ++i) {
                        if (piece_to_text[i] == act_char) {
                            pieces[x + y * width] = i;

                            break;
                        }
                    }
                }
            }
        }

        createOutsidePieces();
        setupKeeperAndEmptyGoals();  
    }

    /**
     * Returns the width of the map.
     * 
     * @return the width of the map.
     */
    public int getWith() {
        return width;
    }
    
    /**
     * Returns the height of the map.
     * 
     * @return the height of the map.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Returns <code>true</code>, if the map is valid.
     */
    public boolean isValid() {
        return validity() == IS_VALID;
    }

    /**
     * Returns the validity of the map.
     *
     * You should call this after creation of the map, when you can not be sure if the map
     * is valid.
     */
    public int validity() {
        if (validity_valid) {
            return validity;
        }

        validity = IS_VALID;
        int keepers = 0;
        int goals = 0;
        int gems = 0;

        for (int i = 0; i < size; ++i) {
            int piece = getPiece(i);

            if (pieceContainsKeeper(piece)) {
                ++keepers;
            }

            if (pieceContainsGem(piece)) {
                ++gems;
            }

            if (pieceContainsGoal(piece)) {
                ++goals;
            }
        }

        if (keepers < 1) {
            validity =  NO_KEEPER;
        }
        else if (keepers > 1) {
            validity = TOO_MANY_KEEPERS;
        }

        if (gems < 1) {
            validity = NO_GEMS;
        }

        if (goals < gems) {
            validity = MORE_GEMS_THAN_GOALS;
        }
        else if (goals > gems) {
            validity = MORE_GOALS_THAN_GEMS;
        }

        if (validity != IS_VALID) {
            return validity;
        }

        for (int i = 0; i < size; ++i) {
            if (getPiece(i) == OUTSIDE) {
                for (int j = 0; j < 4; ++j) {
                    int nb_index = i + xy_offsets[j];

                    // It doesn't matter if we cross the sides of the map!
                    if (isValidIndex(nb_index)) {
                        int piece = getPiece(nb_index);

                        if ((piece != OUTSIDE) && (piece != WALL)) {
                            validity = MAP_LEAKS;

                            return validity;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            int piece1 = getPiece(x, 0);
            int piece2 = getPiece(x, height - 1);

            if ((piece1 != OUTSIDE) && (piece1 != WALL)) {
                validity = MAP_LEAKS;

                return validity;
            }

            if ((piece2 != OUTSIDE) && (piece2 != WALL)) {
                validity = MAP_LEAKS;

                return validity;
            }
        }

        for (int y = 0; y < height; ++y) {
            int piece1 = getPiece(0, y);
            int piece2 = getPiece(width - 1, y);

            if ((piece1 != OUTSIDE) && (piece1 != WALL)) {
                validity = MAP_LEAKS;

                return validity;
            }

            if ((piece2 != OUTSIDE) && (piece2 != WALL)) {
                validity = MAP_LEAKS;

                return validity;
            }
        }

        if (isSolved()) {
            validity = MAP_SOLVED;

            return validity;
        }

        validity = IS_VALID;

        return validity;
    }
    
    /**
     * Returns the piece at the given position.
     * 
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The piece.
     */
    public int getPiece(int x, int y) {
        return pieces[x + y * width] & PIECE;
    }
    
    /**
     * Returns the piece at the given index.
     * 
     * @param index The index.
     * @return The piece.
     */
    public int getPiece(int index) {
        return pieces[index] & PIECE;
    }

    /**
     * Creates the outside pieces.
     */
    private void createOutsidePieces() {
        for (int x = 0; x < width; ++x) {
            createOutsidePiecesHelper(x, 0);
            createOutsidePiecesHelper(x, height - 1);
        }

        for (int y = 0; y < height; ++y) {
            createOutsidePiecesHelper(0, y);
            createOutsidePiecesHelper(width - 1, y);
        }
    }

    /**
     * Helper function needed by createOutsidePieces().
     */
    private void createOutsidePiecesHelper(int x, int y) {
        int index = x + width * y;

        if (getPiece(index) != EMPTY) {
            return;
        }

        pieces[index] = OUTSIDE;

        if (x > 0) {
            createOutsidePiecesHelper(x - 1, y);
        }

        if (y > 0) {
            createOutsidePiecesHelper(x, y - 1);
        }

        if (x + 1 < width) {
            createOutsidePiecesHelper(x + 1, y);
        }

        if (y + 1 < height) {
            createOutsidePiecesHelper(x, y + 1);
        }
    }

    /**
     * Finds and set the keeper and the number of empty goals.
     */
    private void setupKeeperAndEmptyGoals() {
        empty_goals = 0;

        for (int i = 0; i < size; ++i) {
            int piece = getPiece(i);

            if (pieceContainsGoal(piece) && !pieceContainsGem(piece)) {
                ++empty_goals;
            }

            if (pieceContainsKeeper(piece)) {
                keeper = getPoint(i);
            }
        }
    }

    /**
     * Returns true, if the piece contains a keeper.
     *
     * @param piece The piece to test.
     */
    private static boolean pieceContainsKeeper(int piece) {
        assert piece >= KEEPER;
        assert piece <= OUTSIDE;

        return piece <= KEEPER_ON_GOAL;
    }

    /**
     * Returns true, if the piece contains a gem.
     *
     * @param piece The piece to test.
     */
    private static boolean pieceContainsGem(int piece) {
        assert piece >= KEEPER;
        assert piece <= OUTSIDE;

        return piece_contains_gem[piece];
    }

    /**
     * Returns true, of the piece contains a goal.
     *
     * @param piece The piece to test.
     */
    private static boolean pieceContainsGoal(int piece) {
        assert piece >= KEEPER;
        assert piece <= OUTSIDE;

        return piece_contains_goal[piece];
    }

    /**
     * Returns the point for the given index.
     *
     * @param index In index.
     */
    private Point getPoint(int index) {    	
        int y = index / width;
        int x = index - y * width;

        return new Point(x, y);
    }

    /**
     * Returns <code>true</code> if the line is a valid line of a map.
     * 
     * @param line The line.
     * @return <code>true</code> if it as map line.
     */
    private static boolean isMapLine(String line) {
        return map_regexp.reset(line).find();
    }

    /**
     * Returns <code>true</code> if the index is valid.
     * 
     * @param index The index.
     * @return <code>true</code> if the index is valid.
     */
    public boolean isValidIndex(int index) {
        return (index >= 0) && (index < size);
    }
    
    /**
     * Returns <code>true</code> if the map is solved.
     * 
     * @return <code>true</code> if the map is solved.
     */
    public boolean isSolved() {
        return numberOfEmptyGoals() == 0;
    }
    
    /**
     * Returns the number of empty goals.
     * 
     * @return the number of empty goals.
     */
    public int numberOfEmptyGoals() {
        if (!empty_goals_valid) {
            setupNumberOfEmptyGoals();
        }

        return empty_goals;
    }
    
    /**
     * Sets up the number of empty goals.
     */
    private void setupNumberOfEmptyGoals() {
        empty_goals = 0;

        for (int i = 0; i < size; ++i) {
            int piece = getPiece(i);

            if (pieceContainsGoal(piece) && !pieceContainsGem(piece)) {
                ++empty_goals;
            }
        }

        empty_goals_valid = true;
    }
}
