package com.hajesha.shopify_word_search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class WordSearchViewModel extends ViewModel {

    //If the list was longer or there were multiple levels, consider fetching list of words with OKHttp or using local database
    //Cons: local database will increase app size
    //      adding a service will increase delay to play game and no offline version without caching (increase complexity)
    private ArrayList<String> words = new ArrayList<String>() {{
        add("Swift");
        add("Kotlin");
        add("ObjectiveC");
        add("Variable");
        add("Java");
        add("Mobile");
    }};

    private Character[][] listOfLetters;

    private Random random = new Random();

    private final int col = 20; // can make these number variables for variety of difficulties
    private final int row = 20;

    private int firstPositionSelected = -1;
    private MutableLiveData<Integer> foundWords;



    private Character[] listOfWords;

    // 1 means forward
    // 0 means no shift
    // -1 means backwards
    // first element is x horizontal, y is vertical
    final static int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 0}, {0, -1}, {-1, -1}, {-1, 1}};

    public WordSearchViewModel() {
        instantiateListOfLetters();
    }

    Character[] getListOfWords() {
        return listOfWords;
    }

    LiveData<Integer> getNumberOfFoundWords() {
        if (foundWords == null) {
            foundWords = new MutableLiveData<>();
            foundWords.setValue(0);
        }
        return foundWords;
    }

    boolean isFirstSelection() {
        return firstPositionSelected == -1;
    }

    int getCol() {
        return col;
    }

    int getFirstPositionSelectedCol(){
        return firstPositionSelected % col;
    }

    int getFirstPositionSelectedRow(){
        return firstPositionSelected / col;
    }

    void setFirstPositionSelected(int position) {
        firstPositionSelected = position;
    }

    /**
     * Gets the direction vector appended with the length of the word.
     * @param position
     * @return {horizontal, vertical, lengthOfWord}
     */
    int[] getDirectionAndLength(int position){
        int endColumn = position % col;
        int endRow = position / col;
        int startColumn = firstPositionSelected % col;
        int startRow = firstPositionSelected / col;

        int horizontalDirection = 0;
        int verticalDirection = 0;
        if (startColumn > endColumn) {
            horizontalDirection = -1;
        } else if (startColumn < endColumn) {
            horizontalDirection = 1;
        }

        if (startRow > endRow) {
            verticalDirection = -1;
        } else if (startRow < endRow) {
            verticalDirection = 1;
        }

        //Figure out the length of the word from one of the directions (horizontal was chosen randomly)
        int length = horizontalDirection != 0 ? Math.abs(endColumn - startColumn) : Math.abs(endRow - startRow);

        int[] direction = {horizontalDirection, verticalDirection, length};
        return direction;
    }

    /**
     * Check if the letter in the position given combined with the first selected point creates a word.
     * @param position
     * @return if it creates a word.
     */
    boolean checkWord(int position) {
        int startColumn = firstPositionSelected % col;
        int startRow = firstPositionSelected / col;
        int[] direction = getDirectionAndLength(position);
        String guessedWord = "";
        int cc = startColumn, rr = startRow;
        for (int i = 0; i <= direction[2]; i++) {
            guessedWord += listOfLetters[rr][cc];
            rr += direction[1];
            cc += direction[0];
        }

        for (String keyWord : words) {
            if (keyWord.toUpperCase().equals(guessedWord)) {
                if (foundWords.getValue() != null){
                    foundWords.setValue(foundWords.getValue() + 1);
                } else {
                    foundWords.setValue(1);
                }
                words.remove(keyWord);
                return true;
            }
        }
        return false;
    }

    /**
     * Clear the first point of the selction.
     */
    void clearSelection() {
        firstPositionSelected = -1;
    }

    /**
     * Instantiates the list of words that will be used to fill out the grid.
     */
    void instantiateListOfLetters() {
        listOfLetters = new Character[row][col];

        boolean allWordsPlaced = false;

        while (!allWordsPlaced) {
            Collections.shuffle(words);
            for (String word : words) {
                placeWord(word.toUpperCase());
            }
            allWordsPlaced = true;
        }

        List wordlist = new ArrayList<Character>();
        Random r = new Random();

        //Fill with random letters
        for (int i = 0; i < listOfLetters.length; i++) {
            for (int j = 0; j < listOfLetters[i].length; j++) {
                Character letter = listOfLetters[i][j];
                if (letter == null) {
                    letter = (char) (r.nextInt(26) + 'A');
                }
                wordlist.add(letter);
            }
        }

        listOfWords = (Character[]) wordlist.toArray(new Character[wordlist.size()]);
    }

    /**
     * Try to place the word somewhere
     * @param word
     */
    private void placeWord(String word) {
        int randPos = 0;
        int isPossibleDirection = -1;

        //Pick a random position on the board and make sure its positive
        do {
            randPos = random.nextInt(row * col);
            isPossibleDirection = isPossibleLocation(randPos, word);
        } while (randPos < 0 || isPossibleDirection == -1);

        putWordOnBoard(isPossibleDirection, word, randPos);
    }

    /**
     * Place the word onto the board
     * @param direction the  direction vector
     * @param word the word to be placed
     * @param startingPosition the position where the word will start being placed
     */
    private void putWordOnBoard(int direction, String word, int startingPosition) {
        int currentColumn = startingPosition % col;
        int currentRow = startingPosition / col;

        int rr, cc, i = 0;

        int[] directionSet = directions[direction];
        for (i = 0, rr = currentRow, cc = currentColumn; i < word.length(); i++) {
            listOfLetters[rr][cc] = word.charAt(i);

            if (i < word.length() - 1) {
                rr += directionSet[1];
                cc += directionSet[0];
            }
        }
    }

    /**
     * Checks if the randomized position will work for the word.
     * @param initialPosition the randomized initial position
     * @param word the word we are trying to place
     * @return the direction vector {horizontal, vertical} that will allow the word to fit
     * May return -1 if there is no direction that will work
     */
    private int isPossibleLocation(int initialPosition, String word) {
        int currentColumn = initialPosition % col;
        int currentRow = initialPosition / col;
        List<int[]> triedDirections = new LinkedList<>(Arrays.asList(directions));

        int randDirection = -1;
        boolean foundWorkingDirection = false;

        while (!foundWorkingDirection) {
            if (triedDirections.size() <= 1) {
                break;
            }
            randDirection = random.nextInt(triedDirections.size() - 1);
            triedDirections.remove(randDirection);
            if (checkBounds(directions[randDirection], word, currentColumn, currentRow)) {
                foundWorkingDirection = checkAvailability(directions[randDirection], word, currentColumn, currentRow);
            }
        }

        if (!foundWorkingDirection) {
            randDirection = -1;
        }
        return randDirection;
    }

    /**
     * Checks if the word can fit within the bound
     * @param randDirection the set of directions (horizontal, vertical)
     * @param word the word that we are trying to place
     * @param currentColumn the column that the word will start in
     * @param currentRow the row that the word will start at
     * @return if the word would fit within those bounds.
     */
    private boolean checkBounds(int[] randDirection, String word, int currentColumn, int currentRow) {
        int wordLength = word.length();
        if (randDirection[0] == 1 && wordLength + currentColumn >= col) { //horizontally forward
            return false;
        } else if (randDirection[0] == -1 && currentColumn - wordLength < 0) { // horizontally backwards
            return false;
        }

        if (randDirection[1] == 1 && currentRow + wordLength >= row) { // vertical forward
            return false;
        } else if (randDirection[1] == 0 && currentRow - wordLength < 0) { // vertically backwards
            return false;
        }

        return true;
    }

    /**
     * This method will check if the cells that will be used are blank or the same character as the one needed
     * @param randDirection the set of directions (horizontal, vertical)
     * @param word the word that we are trying to place
     * @param currentColumn the column that the word will start in
     * @param currentRow the row that the word will start at
     * @return if this space is allocated and workable
     */
    private boolean checkAvailability(int[] randDirection, String word, int currentColumn, int currentRow) {
        int rr, cc, i = 0;

        // check availability of the cell
        for (i = 0, rr = currentRow, cc = currentColumn; i < word.length(); i++) {
            if (rr < 0 || cc < 0 || cc >= col || rr >= row || listOfLetters[rr][cc] != null && listOfLetters[rr][cc] != word.charAt(i)) {
                return false;
            }
            rr += randDirection[1];
            cc += randDirection[0];
        }
        return true;
    }
}
