package com.inte5har.tm;

import java.util.Collections;
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

    public enum TapeMode {
        DOUBLY_INFINITE,
        SEMI_INFINITE
    }

    public enum TerminationReason {
        READY,
        HALTED,
        MISSING_TRANSITION,
        STEP_LIMIT_REACHED
    }

    public static final class TransitionKey {
        private final String state;
        private final char symbol;

        public TransitionKey(String state, char symbol) {
            this.state = state;
            this.symbol = symbol;
        }

        public String getState() {
            return state;
        }

        public char getSymbol() {
            return symbol;
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

        @Override
        public String toString() {
            return "(" + state + ", '" + symbol + "')";
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
    private final Set<String> knownStates = new HashSet<>();
    private final Set<Character> knownAlphabet = new HashSet<>();
    private final String initialState;
    private final char blankSymbol;

    private String currentState;
    private int headPosition;
    private TapeMode tapeMode = TapeMode.DOUBLY_INFINITE;
    private TerminationReason terminationReason = TerminationReason.READY;
    private String fallbackHaltState = null;
    private boolean strictTransitions = false;

    public TuringMachine(String initialState, char blankSymbol) {
        this.initialState = initialState;
        this.currentState = initialState;
        this.blankSymbol = blankSymbol;
        this.knownStates.add(initialState);
        this.knownAlphabet.add(blankSymbol);
    }

    public void loadTape(String input) {
        tape.clear();
        currentState = initialState;
        headPosition = 0;
        terminationReason = isHalted() ? TerminationReason.HALTED : TerminationReason.READY;
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            knownAlphabet.add(symbol);
            if (symbol != blankSymbol) {
                tape.put(i, symbol);
            }
        }
    }

    public void addHaltState(String state) {
        haltStates.add(state);
        knownStates.add(state);
    }

    public void addTransition(String state, char readSymbol, String nextState, char writeSymbol, Direction direction) {
        transitions.put(new TransitionKey(state, readSymbol), new TransitionRule(nextState, writeSymbol, direction));
        knownStates.add(state);
        knownStates.add(nextState);
        knownAlphabet.add(readSymbol);
        knownAlphabet.add(writeSymbol);
    }

    public boolean hasTransition(String state, char readSymbol) {
        return transitions.containsKey(new TransitionKey(state, readSymbol));
    }

    public void registerState(String state) {
        knownStates.add(state);
    }

    public void registerAlphabetSymbol(char symbol) {
        knownAlphabet.add(symbol);
    }

    public Set<String> getKnownStates() {
        return Collections.unmodifiableSet(knownStates);
    }

    public Set<Character> getKnownAlphabet() {
        return Collections.unmodifiableSet(knownAlphabet);
    }

    public Set<String> getHaltStates() {
        return Collections.unmodifiableSet(haltStates);
    }

    public char getBlankSymbol() {
        return blankSymbol;
    }

    public String getInitialState() {
        return initialState;
    }

    public TapeMode getTapeMode() {
        return tapeMode;
    }

    public void setTapeMode(TapeMode tapeMode) {
        if (tapeMode == null) {
            throw new IllegalArgumentException("TapeMode cannot be null");
        }
        this.tapeMode = tapeMode;
    }

    public boolean allowsNegativePositions() {
        return tapeMode == TapeMode.DOUBLY_INFINITE;
    }

    public void setFallbackHaltState(String fallbackHaltState) {
        this.fallbackHaltState = fallbackHaltState;
        if (fallbackHaltState != null) {
            addHaltState(fallbackHaltState);
        }
    }

    public String getFallbackHaltState() {
        return fallbackHaltState;
    }

    public void setStrictTransitions(boolean strictTransitions) {
        this.strictTransitions = strictTransitions;
    }

    public boolean isStrictTransitions() {
        return strictTransitions;
    }

    public TerminationReason getTerminationReason() {
        return terminationReason;
    }

    public boolean isHalted() {
        return haltStates.contains(currentState);
    }

    public boolean isStuck() {
        return terminationReason == TerminationReason.MISSING_TRANSITION;
    }

    public Set<TransitionKey> getMissingTransitions(Set<String> states, Set<Character> alphabet) {
        Set<TransitionKey> missing = new HashSet<>();
        for (String state : states) {
            if (haltStates.contains(state)) {
                continue;
            }
            for (char symbol : alphabet) {
                TransitionKey key = new TransitionKey(state, symbol);
                if (!transitions.containsKey(key)) {
                    missing.add(key);
                }
            }
        }
        return missing;
    }

    public Set<TransitionKey> getMissingTransitions() {
        return getMissingTransitions(knownStates, knownAlphabet);
    }

    public boolean isComplete(Set<String> states, Set<Character> alphabet) {
        return getMissingTransitions(states, alphabet).isEmpty();
    }

    public boolean isComplete() {
        return isComplete(knownStates, knownAlphabet);
    }

    public void validateCompleteness(Set<String> states, Set<Character> alphabet) {
        Set<TransitionKey> missing = getMissingTransitions(states, alphabet);
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Transition rules are missing for " + missing.size() + " pair(s): " + missing);
        }
    }

    public void validateCompleteness() {
        validateCompleteness(knownStates, knownAlphabet);
    }

    public char readSymbol() {
        return tape.getOrDefault(headPosition, blankSymbol);
    }

    public char readSymbolAt(int position) {
        return tape.getOrDefault(position, blankSymbol);
    }

    public void writeSymbol(char symbol) {
        if (symbol == blankSymbol) {
            tape.remove(headPosition);
        } else {
            tape.put(headPosition, symbol);
        }
    }

    public void moveLeft() {
        if (tapeMode == TapeMode.SEMI_INFINITE && headPosition <= 0) {
            headPosition = 0;
            return;
        }
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
            terminationReason = TerminationReason.HALTED;
            return false;
        }

        TransitionRule rule = transitions.get(new TransitionKey(currentState, readSymbol()));
        if (rule == null) {
            if (fallbackHaltState != null) {
                currentState = fallbackHaltState;
                terminationReason = TerminationReason.HALTED;
                return false;
            }
            if (strictTransitions) {
                throw new IllegalStateException("No transition rule defined for state '" + currentState
                        + "' and symbol '" + readSymbol() + "' at position " + headPosition);
            }
            terminationReason = TerminationReason.MISSING_TRANSITION;
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

        if (haltStates.contains(currentState)) {
            terminationReason = TerminationReason.HALTED;
        }

        return true;
    }

    public int run(int maxSteps) {
        int steps = 0;
        while (steps < maxSteps && step()) {
            steps++;
        }
        if (steps >= maxSteps && !isHalted() && terminationReason != TerminationReason.MISSING_TRANSITION) {
            terminationReason = TerminationReason.STEP_LIMIT_REACHED;
        }
        return steps;
    }

    public int getHeadPosition() {
        return headPosition;
    }

    public String getCurrentState() {
        return currentState;
    }

    public boolean hasWrittenCells() {
        return !tape.isEmpty();
    }

    public Integer getMinPosition() {
        if (tape.isEmpty()) {
            return null;
        }
        int min = Integer.MAX_VALUE;
        for (Integer position : tape.keySet()) {
            min = Math.min(min, position);
        }
        return min;
    }

    public Integer getMaxPosition() {
        if (tape.isEmpty()) {
            return null;
        }
        int max = Integer.MIN_VALUE;
        for (Integer position : tape.keySet()) {
            max = Math.max(max, position);
        }
        return max;
    }

    public int getTapeOffset() {
        return getTapeOffset(false);
    }

    public int getTapeOffset(boolean includeHead) {
        if (tape.isEmpty()) {
            return includeHead ? headPosition : 0;
        }
        int min = Integer.MAX_VALUE;
        for (Integer position : tape.keySet()) {
            min = Math.min(min, position);
        }
        return includeHead ? Math.min(min, headPosition) : min;
    }

    public String getTapeSnapshot() {
        return getTapeSnapshot(false);
    }

    public String getTapeSnapshot(boolean includeHead) {
        if (tape.isEmpty()) {
            return String.valueOf(blankSymbol);
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer position : tape.keySet()) {
            min = Math.min(min, position);
            max = Math.max(max, position);
        }

        if (includeHead) {
            min = Math.min(min, headPosition);
            max = Math.max(max, headPosition);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = min; i <= max; i++) {
            builder.append(tape.getOrDefault(i, blankSymbol));
        }
        return builder.toString();
    }

    public String getTapeSnapshot(int fromPosition, int toPosition) {
        if (fromPosition > toPosition) {
            throw new IllegalArgumentException("fromPosition (" + fromPosition + ") cannot be greater than toPosition (" + toPosition + ")");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = fromPosition; i <= toPosition; i++) {
            builder.append(tape.getOrDefault(i, blankSymbol));
        }
        return builder.toString();
    }

    public String getTapeSnapshotWithHead() {
        return getTapeSnapshotWithHead(true);
    }

    public String getTapeSnapshotWithHead(boolean includeHead) {
        int min;
        int max;
        if (tape.isEmpty()) {
            if (includeHead) {
                min = headPosition;
                max = headPosition;
            } else {
                return String.valueOf(blankSymbol);
            }
        } else {
            int minWritten = Integer.MAX_VALUE;
            int maxWritten = Integer.MIN_VALUE;
            for (Integer position : tape.keySet()) {
                minWritten = Math.min(minWritten, position);
                maxWritten = Math.max(maxWritten, position);
            }
            if (includeHead) {
                min = Math.min(minWritten, headPosition);
                max = Math.max(maxWritten, headPosition);
            } else {
                min = minWritten;
                max = maxWritten;
            }
        }
        return getTapeSnapshotWithHead(min, max);
    }

    public String getTapeSnapshotWithHead(int fromPosition, int toPosition) {
        if (fromPosition > toPosition) {
            throw new IllegalArgumentException("fromPosition (" + fromPosition + ") cannot be greater than toPosition (" + toPosition + ")");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = fromPosition; i <= toPosition; i++) {
            char symbol = tape.getOrDefault(i, blankSymbol);
            if (i == headPosition) {
                builder.append('[').append(symbol).append(']');
            } else {
                builder.append(symbol);
            }
        }
        return builder.toString();
    }
}
