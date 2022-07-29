/*
 * Jedd Lupoy (1536884)
 * Jeffrey Luo (1535901)
 */

import java.io.*;
import java.util.*;

public class REcompile {
    private static ArrayList<State> states = new ArrayList<>();
    private static ArrayList<State> alternationStates = new ArrayList<>();
    private static ArrayList<State> plusStates = new ArrayList<>();
    private static ArrayList<State> statesBeforeParenthesis = new ArrayList<>();
    private static ArrayList<Integer> indexAlternationStates = new ArrayList<>();
    //Special characters except for '.'.
    private static char[] specialChars = new char[]{'\\', '(', ')', '*', '+', '?', '|', '[', ']'};
    private static char[] chars;
    private static int state = 0;
    private static int i = 0;
    private static boolean flag = false;

    public static class State {
        private int _index;
        private char _char;
        private int _n1, _n2;

        public State(int index, char ch, int n1, int n2) {
            _index = index;
            _char = ch;
            _n1 = n1;
            _n2 = n2;
        }

        public int getIndex() { return _index; }
        public char getChar() { return _char; }
        public void setNext1(int n) { _n1 = n; }
        public void setNext2(int n) { _n2 = n; }
        public int getNext1() { return _n1; }
        public int getNext2() { return _n2; }
    }

    /*
     * Insert a character at set position of a string.
     */
    public static String addCharToString(String str, char c, int pos, boolean replace) {
        // Whether character will be replacing another character at position or adding to the string.
        if (replace)
            return str.substring(0, pos) + c + str.substring(pos + 1);
        return str.substring(0, pos) + c + str.substring(pos);
    }

    public static void main(String[] args) {
        try {
            String line = "";

            // There should be at least one argument for the regex.
            if (args.length < 1) {
                System.err.println("USAGE: java REcompile \"<regex>\"");
                return;
            }

            // Assign regex to line string.
            for (int i = 0; i < args.length; i++)
                line += args[i];
            
            // Do not allow "*" as the "all files" representation symbol
            if (line.indexOf("REcompile.java") != -1)
                error();

            // Parenthesis ( ) to left side of | preprocessing
            if (line.length() > 1) {
                int test  = 0;
                for (int i = 1; i < line.length(); i++) {
                    if (line.charAt(i) == '|' && line.charAt(i-1) != '\\') {
                        test = i + 1;
                        line = addCharToString(line, ')', i, false);
                        // indexAlternationStates.add(i);
                        
                        for (int j = i; j >= 0; j--) {
                            if (line.charAt(j) == '(') {
                                line = addCharToString(line, '(', j+1, false);
                                break;
                            }
                            if (line.charAt(j) == '|' && line.charAt(j-1) != '\\') {
                                line = addCharToString(line, '(', j+1, false);
                                line = addCharToString(line, ')', test, false);
                                line = addCharToString(line, '(', 0, false);
                                i = i + 2;
                                break;
                            }
                            if (j == 0) {
                                line = addCharToString(line, '(', 0, false);
                                break;
                            }
                        }
                        //Add 2 to i because of the added ( )
                        i = i + 2;
                    }
                }
                for (int k = 0; k < line.length(); k++) {
                    if (line.charAt(k) == '|')
                        indexAlternationStates.add(k);
                }
            }

            // Square bracket preprocessing [ ].
            if (line.contains("[") && line.contains("]")) {
                boolean actualPair = false;
                int startIndex = 0;
                int endIndex = 0;

                // Go through each character in the regex.
                for (int i = 0; i < line.length(); i++) {
                    // Validating square bracket syntax.
                    if (line.charAt(i) == '['){
                        if (line.charAt(i+1) == ']')
                            error();
                        else if (i == 0){
                            break;
                        } else if (line.charAt(i - 1) != '\\') {
                            // Proceed if a '\' character is found before one of the square brackets.
                            startIndex = i;
                            break;
                        }
                    }
                }

                // Go through each character starting at the end of the regex.
                for (int j = line.length() - 1; j > startIndex; j--) {
                    // Check for a closing square bracket.
                    if (line.charAt(j) == ']'){
                        // Check whether it is a valid bracket pair or not.
                        if (line.charAt(j-1) != '\\'){
                            endIndex = j;
                            actualPair = true;
                            break;
                        }
                    }
                }

                // If valid pair, convert "[ ]" alternation into "|" alternation.
                if (actualPair) {
                    // Add open and closing parenthesis at set regex indexes.
                    line = addCharToString(line, '(', startIndex, true);
                    line = addCharToString(line, ')', endIndex, true);

                    // Begin iterating through all characters in the regex.
                    startIndex++;
                    while (startIndex != endIndex) {
                        // If current index contains special character.
                        if (new String(specialChars).indexOf(line.charAt(startIndex)) != -1 || line.charAt(startIndex) == '.') {
                            line = addCharToString(line, '\\', startIndex, false);
                            startIndex++;
                            endIndex++;
                        }
                        // If character at index is not a closing parenthesis.
                        if (line.charAt(startIndex + 1) != ')') {
                            // Add an OR operator.
                            line = addCharToString(line, '|', startIndex+1, false);
                            startIndex++;
                            endIndex++;
                        }
                        startIndex++;
                    }
                }
            }
            //System.out.println(line);
            // Put all regex characters into a char array.
            chars = line.toCharArray();
            // Cannot have a single * (all file representation symbol)
            if (chars[0] == '*'){
                error();
            }
            // Begin parsing regex.
            parse();

            // Print out the FSM.
            for (int j = 0; j < states.size(); j++) {
                State st = states.get(j);
                String s = st.getIndex() + ", " + st.getChar() + ", " + st.getNext1() + ", " + st.getNext2();
                System.out.println(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // E-> T
    // E-> TE
    public static int expression() {
        int r = 0;
        try {
            // Parse next term and save state number.
            r = term();
            // Check if parsing is not complete.
            if (i < chars.length) {
                // Parse '\' special case.
                if (chars[i] == '\\') term();
                if (i < chars.length) {
                    // Parse next literal or open parenthesis.
                    if (isLiteral(chars[i]) || chars[i] == '(') expression();
                }
                statesBeforeParenthesis.add(states.get(r));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return r;
    }

    //T -> F
    //T -> F*
    //T -> F+
    //T -> F?
    //T -> F|E
    public static int term() {
        int r, t1, e2, f;

        // Get previous state number.
        f = state - 1;
        // Parse next factor and save state number.
        r = t1 = factor();

        // Return state number at the end of regex.
        if (i >= chars.length) { return r; }
        // Set initial f value.
        if (f == -1) { f = 0; }

        // Check for regex symbols.
        switch (chars[i]) {
            case '*':
                // If previous state has the same next states, then set new Next2 state.
                if (states.get(f).getNext1() == states.get(f).getNext2())
                    states.get(f).setNext2(state);
                // Set new Next1 state.
                states.get(f).setNext1(state);
                // Add new branching state.
                states.add(new State(state, '~', state + 1, t1));
                // Iterate to the next index of the regex array.
                i++;
                r = state;
                state++;
                break;
            case '+':
                // Add new branching state.
                State plusState = new State(state, '~', state + 1, t1);
                states.add(plusState);
                plusStates.add(plusState);
                // Iterate to the next index of the regex array.
                i++;
                r = state;
                state++;
                break;
            case '?':
                // If previous state has the same next states, then set new Next2 state.
                if (states.get(f).getNext1() == states.get(f).getNext2())
                    states.get(f).setNext2(state);
                // Set new Next1 state.
                states.get(f).setNext1(state);

                // Iterate to the next index of the regex array.
                i++;
                r = state;
                state++;
                // Add new branching state.
                states.add(new State(r, '~', t1, state));

                // If previous state has the same next states, then set new Next2 state.
                if (states.get(f + 1).getNext1() == states.get(f + 1).getNext2())
                    states.get(f + 1).setNext2(state);
                // Set new Next1 state.
                states.get(f + 1).setNext1(state);
                break;
            case '|':
                // If previous state has the same next states, then set new Next2 state.
                if (states.get(f).getNext1() == states.get(f).getNext2())
                    states.get(f).setNext2(state);
                if (flag) {
                    states.get(f).setNext1(state);
                    flag = false;
                }

                // Go to the previous state number.
                f = state - 1;
                // Iterate to the next index of the regex array.
                i++;
                r = state;
                state++;

                // Add new branching state.
                State altState = new State(r, '~', t1, 0);
                states.add(altState);
                alternationStates.add(altState);
                // Parse next expression and save state number.
                e2 = expression();
                // Set Next2 state of current state to e2.
                states.get(r).setNext2(e2);

                // If previous state has the same next states, then set new Next2 state.
                if (states.get(f).getNext1() == states.get(f).getNext2())
                    states.get(f).setNext2(state);
                // Set new Next1 state.
                states.get(f).setNext1(state);

                // Store the '|' branching state's next states.
                int[] nextStates = { t1, e2 };
                // Iterate through both branching states.
                for (int s = 0; s < nextStates.length; s++) {
                    // Does the next state exist in the '+' array list.
                    if (plusStates.contains(states.get(nextStates[s]))) {
                        // If so, set new next state value.
                        int newNextState = nextStates[s] - 1;
                        switch (s) {
                            case 0:
                                states.get(r).setNext1(newNextState);
                                break;
                            default:
                                states.get(r).setNext2(newNextState);
                                break;
                        }
                    }   
                }
                break;
            default:
                break;
        }
        return r;
    }

    //F -> .
    //F -> v
    //F -> \v
    //F -> (E)
    public static int factor() {
        int r = 0;

        // Check whether next character is a literal or not.
        if (isLiteral(chars[i])) {
            // If literal is a '.', then set state character to '~'
            if (chars[i] != '.'){
                // Add new literal state.
                states.add(new State(state, chars[i], state + 1, state + 1));
            } else {
                // Add new '.' literal state.
                states.add(new State(state, '~', state + 1, state + 1));
            }
            // Iterate to the next index of the regex array.
            i++;
            r = state;
            state++;
        } 
        else {
            // Check for regex symbols.
            switch(chars[i]) {
                case '\\':
                    // Iterate to the next index, effectively skipping the '\' character.
                    i++;
                    // Add new literal state.
                    states.add(new State(state, chars[i], state + 1, state + 1));
                    // Iterate to the next index of the regex array.
                    r = state;
                    state++;
                    i++;
                    break;
                case '(':
                    flag = true;
                    // Iterate to the next index, effectively skipping the '(' character.
                    i++;
                    // Parse next expression and save state number.
                    r = expression();

                    // Invalid regex if no closing parenthesis is found.
                    if (chars[i] != ')'){
                        error();
                    }
                    //statesBeforeParenthesis.add(states.get(r));

                    // Iterate to the next index of the regex array.
                    i++;
                    break;
                default:
                    // Error parsing the regex.
                    error();
                    
            }
        }
        return r;
    }

    /*
     * Begin the regex parsing process and ends when finished parsing.
     */
    public static void parse() {
        int initial = 0;
        // Add new branching state.
        states.add(new State(state, '~', 1, 1));
        // Iterate to next state number.
        state++;
        // Parse next expression and save state number.
        initial = expression();

        if (alternationStates.size() > 1) {
            State lastAltState = alternationStates.get(alternationStates.size() - 1);
            int nextStateValue = states.get(lastAltState.getIndex() - 1).getNext1();
            ArrayList<State> modifiedStates = new ArrayList<>();

            for (int i = 0; i < alternationStates.size() - 1; i++) {
                State altState = alternationStates.get(i);
                State st = states.get(altState.getIndex() - 1);

                for (int j = 0; j < statesBeforeParenthesis.size(); j++) {
                    State sbp = statesBeforeParenthesis.get(j);

                    //System.out.println(sbp.getIndex() + " " + sbp.getChar() + " " + sbp.getNext1() + " " + st.getIndex());

                    if ((sbp.getIndex() - st.getIndex()) > 0) {
                        //System.out.println(sbp.getIndex() + " " + sbp.getChar() + " " + sbp.getNext1());
                        if (st.getChar() != '~')
                            st.setNext2(sbp.getNext1());
                        st.setNext1(sbp.getNext1());
                        modifiedStates.add(st);
                        break;
                    }
                }

                for (int index : indexAlternationStates) {
                    //System.out.println(altState.getIndex() + " " + index + " " + chars[index - 1]);
                    if (!modifiedStates.contains(st)) {
                        if (chars[index - 1] == ')') {
                            if (st.getChar() != '~')
                                st.setNext2(nextStateValue);
                            st.setNext1(nextStateValue);
                        }
                    }
                }
            }
        }

        try {
            // If end of character array does not return 0, error parsing the regex.
            if (chars[i] != 0) error();
        } catch (Exception ex) {

        }
        // Add new end-of-regex branching state.
        states.add(new State(state, '~', 0, 0));
    }

    /*
     * Checks if the character is a literal or not.
     */
    public static boolean isLiteral(char ch) {
        return new String(specialChars).indexOf(ch) == -1 || ch == '.';
    }

    /*
     * Throws error with regex parsing and exits the program.
     */
    public static void error() {
        System.err.println("There was an error when parsing the regex!");
        System.exit(1);
    }
}
