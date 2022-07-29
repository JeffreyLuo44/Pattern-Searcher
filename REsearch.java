/*
 * Jedd Lupoy (1536884)
 * Jeffrey Luo (1535901)
 */

import java.io.*;
import java.util.*;

public class REsearch{
    private static ArrayList<State> states = new ArrayList<>();
    private static ArrayList<Integer> visitedStateNumbers;
    private static char[] chars;
    private static int mark = 0;
    private static int point = 0;

    public static class State implements Comparable<State> {
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
        public int getNext1() { return _n1; }
        public int getNext2() { return _n2; }
        //Sorts the states in ascending order.
        public int compareTo(State compareState){
            int compareAmount = compareState.getIndex();
            return this._index - compareAmount;
        }
    }

    public static void main(String[] args){
        BufferedReader br = null;
        try {
            if (args.length == 1){
                br = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while((line = br.readLine()) != null) {
                    //Split and collect each corresponding input as state properties.
                    String[] splitString = line.split(", ");     
                    int state = Integer.parseInt(splitString[0]);
                    char c = splitString[1].charAt(0);
                    int n1 = Integer.parseInt(splitString[2]);
                    int n2 = Integer.parseInt(splitString[3]);
                    //Create a new state with the corresponding properties.
                    State s = new State(state, c, n1 , n2);
                    //Add the new state to the array of states.
                    states.add(s);     
                }
                //Sort states in order by state number.
                Collections.sort(states);
                br = new BufferedReader(new FileReader(args[0])); 
                //Read every line.
                while ((line = br.readLine()) != null) {
                    //If there is a match, print out the matching String.
                    if (StringSearch(line))
                        System.out.println(line);
                }
            }else{
                System.err.println("USAGE: java REsearch <file>");
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    /*
    * Find whether the String matches the pattern given by the array of states (FSM).
    */
    private static Boolean StringSearch(String line){
        Deque d;
        int currentHeadState;
        mark = 0;
        //Separate the String into chars.
        chars = line.toCharArray();
        visitedStateNumbers = new ArrayList<>();
        //Initialise a new deque.
        d = new Deque();
        //Add -1 which is the SCAN to deque.
        d.push(-1);
        //Push starting state onto deque.
        d.push(states.get(0).getIndex());
        //Ensure mark is pointing to a char from the String.
        while (mark < chars.length){
            //Set the point where the marker points to.
            point = mark;
            //Ensure point is pointing to a char from the String.
            while (point < chars.length){
                currentHeadState = d.peek();
                // System.out.print("\nInitial =>");
                // d.length();
                //Pop the current head state to consume it.
                d.pop();
                //Check if not already visited state.
                if (!visitedStateNumbers.contains(Integer.valueOf(states.get(currentHeadState).getIndex()))){
                    //Mark the current head state as visited.
                    Integer visitedStateNumber = Integer.valueOf(states.get(currentHeadState).getIndex());
                    visitedStateNumbers.add(visitedStateNumber);
                    //Check if the next state is the final state (match found).
                    if (states.get(currentHeadState).getNext1() == 0 && states.get(currentHeadState).getNext2() == 0)
                        return true;
                    //If head state of deque is equal to char at point.
                    if (states.get(currentHeadState).getChar() == chars[point]){
                        //Put one of the two next possible states to the end of the deque.
                        d.put(states.get(currentHeadState).getNext1());
                    }
                    //If branching state.
                    else if (states.get(currentHeadState).getChar() == '~'){
                        if (states.get(currentHeadState).getNext1() != states.get(currentHeadState).getNext2()){
                            //Push the two next possible states to the start of the deque.
                            d.push(states.get(currentHeadState).getNext1());
                            d.push(states.get(currentHeadState).getNext2());
                        }else if (states.get(currentHeadState).getIndex() == 0){
                            //Push the next possible state on if the current state is the starting state.
                            d.push(states.get(currentHeadState).getNext1());
                        }else{
                            //Put the next possible state on if the state's char is '.'.
                            d.put(states.get(currentHeadState).getNext1());
                        }
                    }
                    // System.out.print("\nAfter =>");
                    // d.length();
                }

                //Check if SCAN becomes head and if all possible current states were consumed.
                if (d.peek() == -1){
                    visitedStateNumbers = new ArrayList<>();
                    if (d.length() == 1){
                        //Only the SCAN is left in the deque (no match found).
                        //Push starting state onto deque.
                        d.push(states.get(0).getIndex());
                        break;
                    }else{
                        //SCAN is head and there are next possible state(s).
                        //Pop SCAN.
                        d.pop();
                        //Put SCAN below next possible states to make them possible current states.
                        d.put(-1);
                        //Move pointer to next char in text.
                        point++;
                        // System.out.print("\nSCAN is head and there are next possible state(s). Advance point");
                        // d.length();
                    }
                }
            }
            //Move marker to next char in text.
            mark++;
            // System.out.print("\nAdvance mark...");
        }
        //Marked all chars (no matches found).
        // System.out.print("\nNo matches found");
        return false;
    }
}

class Deque{
    private DequeNode head;

    private class DequeNode{
        //Each node points to the next node in deque.
        DequeNode next;
        private int data;

        //Constructor that takes in the state number as data.
        public DequeNode(int data){
            this.data = data;
        }

        //Returns the state number.
        private int getData(){
            return data;
        }

        //Recursive method to count all nodes in deque.
        private int countNodes(int n){
            n++;
            //Can be uncommented to see what values are in the deque when calling length()
            // if (this == head){
            //     System.out.print("Deque: " + String.valueOf(this.data) + ", ");
            // }else{
            //     System.out.print(String.valueOf(this.data) + ", ");
            // }
            if (next != null){
                return next.countNodes(n);
            }
            return n;
        }
    }

    //Returns whether there is no head node or not any node in the deque.
    public boolean isEmpty(){
        return ( head == null );
    }

    //Returns the length of the deque by iterating through nodes (if any).
    public int length(){
        if (isEmpty()){
            return 0;
        }
        return head.countNodes(0);
    }

    //Creates a new node and makes it the new head node, adding it to the front/top of deque.
    public void push(int data){
        DequeNode d = new DequeNode(data);
        if(!isEmpty()){
            //Make the current head node the next node.
            d.next = head;
            head = d;
        }
        else{
            head = d;
        }
    }
    
    //Removes the head node from the deque and makes it's next node the head node.
    public int pop(){
        DequeNode d = head;
        head = head.next;
        return d.getData();
    }

    //Gets the head node's state number
    public int peek(){
        return head.getData();
    }

    //Creates a new node, and adds the node to the back/bottom of deque.
    public void put(int data){
        DequeNode d = new DequeNode(data);
        if (isEmpty()){head = d;}
        DequeNode iter = head;
        //Iterates through deque nodes to the last node which is not pointing to a next node.
        while (iter.next != null)
            iter = iter.next;
        //Makes the null next node point to the new node.
        iter.next = d;
    }
}