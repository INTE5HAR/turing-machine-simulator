package com.inte5har.tm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TuringMachine {
    public enum Direction {
        LEFT,
        RIGHT,
        STAY
    }

    private static final class TransitionKey {
        private final String state;
        private final char symbol;

        private TransitionKey(String state, char symbol) {
            this.state = state;
            this.symbol = symbol;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TransitionKey)) {
                return false;
            }
            TransitionKey that = (TransitionKey) other;
            return symbol == that.symbol && state.equals(that.state);
        }

        @Override
        public int hashCode() {
            return 31 * state.hashCode() + symbol;
        }
    }

    private static final class TransitionRule {
        private final String nextState;
        private final char writeSymbol;
        private final Direction direction;

        private TransitionRule(String nextState, char writeSymbol, Direction direction) {
            this.nextState = nextState;
            this.writeSymbol = writeSymbol;
            this.direction = direction;
        }
    }

    private final Map<Integer, Character> tape = new HashMap<>();
    private final Map<TransitionKey, TransitionRule> transitions = new HashMap<>();
    private final Set<String> haltStates = new HashSet<>();
    private final char blankSymbol;

    private String currentState;
    private int headPosition;

    public TuringMachine(String initialState, char blankSymbol) {
        this.currentState = initialState;
        this.blankSymbol = blankSymbol;
    }

    public void loadTape(String input) {
        tape.clear();
        headPosition = 0;
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            if (symbol != blankSymbol) {
                tape.put(i, symbol);
            }
        }
    }

    public void addHaltState(String state) {
        haltStates.add(state);
    }

    public void addTransition(String state, char readSymbol, String nextState, char writeSymbol, Direction direction) {
        transitions.put(new TransitionKey(state, readSymbol), new TransitionRule(nextState, writeSymbol, direction));
    }

    public char readSymbol() {
        return tape.getOrDefault(headPosition, blankSymbol);
    }

    public void writeSymbol(char symbol) {
        if (symbol == blankSymbol) {
            tape.remove(headPosition);
        } else {
            tape.put(headPosition, symbol);
        }
    }

    public void moveLeft() {
        headPosition--;
    }

    public void moveRight() {
        headPosition++;
    }

    public void stay() {
        // intentionally empty; head remains at current position
    }

    public boolean step() {
        if (haltStates.contains(currentState)) {
            return false;
        }

        TransitionRule rule = transitions.get(new TransitionKey(currentState, readSymbol()));
        if (rule == null) {
            return false;
        }

        writeSymbol(rule.writeSymbol);
        if (rule.direction == Direction.LEFT) {
            moveLeft();
        } else if (rule.direction == Direction.RIGHT) {
            moveRight();
        } else {
            stay();
        }
        currentState = rule.nextState;

        return true;
    }

    public int run(int maxSteps) {
        int steps = 0;
        while (steps < maxSteps && step()) {
            steps++;
        }
        return steps;
    }

    public int getHeadPosition() {
        return headPosition;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTapeSnapshot() {
        if (tape.isEmpty()) {
            return String.valueOf(blankSymbol);
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer position : tape.keySet()) {
            min = Math.min(min, position);
            max = Math.max(max, position);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = min; i <= max; i++) {
            builder.append(tape.getOrDefault(i, blankSymbol));
        }
        return builder.toString();
    }
}
