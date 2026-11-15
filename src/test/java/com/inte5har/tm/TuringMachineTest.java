package com.inte5har.tm;

import java.util.Set;

public class TuringMachineTest {
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " Expected: true, Actual: false");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " Expected: false, Actual: true");
        }
    }

    public static void main(String[] args) {
        testBasicTapeOperations();
        testUnaryIncrementRun();
        testCompletenessValidation();
        testMissingRuleHandlingAndFallback();
        testStrictTransitions();
        testTapeModesAndNegativePositions();
        testTapeSnapshotsAndCoordinates();
        testStepLimitTerminationReason();
        System.out.println("All tests passed");
    }

    private static void testBasicTapeOperations() {
        TuringMachine machine = new TuringMachine("start", '_');
        machine.loadTape("10");

        assertEquals('1', machine.readSymbol(), "readSymbol should return first tape symbol");
        machine.moveRight();
        assertEquals('0', machine.readSymbol(), "moveRight should update head position");

        machine.writeSymbol('1');
        assertEquals("11", machine.getTapeSnapshot(), "writeSymbol should update tape");

        machine.moveLeft();
        machine.writeSymbol('_');
        assertEquals("1", machine.getTapeSnapshot(), "writing blank should clear current tape cell");
    }

    private static void testUnaryIncrementRun() {
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.addHaltState("halt");
        machine.addTransition("q0", '1', "q0", '1', TuringMachine.Direction.RIGHT);
        machine.addTransition("q0", '_', "halt", '1', TuringMachine.Direction.STAY);
        machine.loadTape("111");

        int steps = machine.run(100);

        assertEquals(4, steps, "run should process all transitions until halt");
        assertEquals("halt", machine.getCurrentState(), "machine should enter halt state");
        assertTrue(machine.isHalted(), "isHalted should be true");
        assertEquals(TuringMachine.TerminationReason.HALTED, machine.getTerminationReason(), "terminationReason should be HALTED");
        assertEquals("1111", machine.getTapeSnapshot(), "unary increment should append one symbol");

        machine.loadTape("1");
        int secondRunSteps = machine.run(100);

        assertEquals(2, secondRunSteps, "run should work after loading another tape");
        assertEquals("halt", machine.getCurrentState(), "machine should halt again on the second run");
        assertEquals("11", machine.getTapeSnapshot(), "second unary increment should append one symbol");
    }

    private static void testCompletenessValidation() {
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.addHaltState("halt");
        machine.addTransition("q0", '1', "q0", '1', TuringMachine.Direction.RIGHT);

        assertFalse(machine.isComplete(), "machine should be incomplete because rule for '_' is missing");

        Set<TuringMachine.TransitionKey> missing = machine.getMissingTransitions();
        assertEquals(1, missing.size(), "should have exactly 1 missing transition");
        TuringMachine.TransitionKey missingKey = missing.iterator().next();
        assertEquals("q0", missingKey.getState(), "missing state should be q0");
        assertEquals('_', missingKey.getSymbol(), "missing symbol should be '_'");

        boolean threw = false;
        try {
            machine.validateCompleteness();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw, "validateCompleteness should throw IllegalStateException when rules are missing");

        machine.addTransition("q0", '_', "halt", '1', TuringMachine.Direction.STAY);
        assertTrue(machine.isComplete(), "machine should be complete once all rules are supplied");
        machine.validateCompleteness(); // should not throw
    }

    private static void testMissingRuleHandlingAndFallback() {
        // Test default behavior: stops without entering halt state, marks MISSING_TRANSITION
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.addHaltState("halt");
        machine.loadTape("0"); // no rule for ('q0', '0')

        int steps = machine.run(10);
        assertEquals(0, steps, "should stop immediately on missing transition");
        assertEquals("q0", machine.getCurrentState(), "currentState should remain q0");
        assertFalse(machine.isHalted(), "isHalted should be false when stopping due to missing rule");
        assertTrue(machine.isStuck(), "isStuck should be true on missing transition");
        assertEquals(TuringMachine.TerminationReason.MISSING_TRANSITION, machine.getTerminationReason(),
                "terminationReason should be MISSING_TRANSITION");

        // Test fallback halt state: enters halt state when no rule matches
        TuringMachine fallbackMachine = new TuringMachine("q0", '_');
        fallbackMachine.setFallbackHaltState("trap");
        fallbackMachine.loadTape("0");

        int fbSteps = fallbackMachine.run(10);
        assertEquals(0, fbSteps, "should stop on missing rule");
        assertEquals("trap", fallbackMachine.getCurrentState(), "machine should enter fallback halt state");
        assertTrue(fallbackMachine.isHalted(), "machine should be halted in fallback state");
        assertFalse(fallbackMachine.isStuck(), "machine should not be stuck when entering fallback halt state");
        assertEquals(TuringMachine.TerminationReason.HALTED, fallbackMachine.getTerminationReason(),
                "terminationReason should be HALTED when entering fallback halt state");
    }

    private static void testStrictTransitions() {
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.setStrictTransitions(true);
        machine.loadTape("0");

        boolean threw = false;
        try {
            machine.step();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw, "step should throw IllegalStateException in strict mode on missing transition");
    }

    private static void testTapeModesAndNegativePositions() {
        // Doubly infinite tape (default): negative positions permitted
        TuringMachine doubly = new TuringMachine("q0", '_');
        assertEquals(TuringMachine.TapeMode.DOUBLY_INFINITE, doubly.getTapeMode(), "default mode should be DOUBLY_INFINITE");
        assertTrue(doubly.allowsNegativePositions(), "should allow negative positions in doubly infinite mode");

        doubly.moveLeft();
        assertEquals(-1, doubly.getHeadPosition(), "head should move to -1");
        doubly.writeSymbol('A');
        assertEquals('A', doubly.readSymbol(), "should read symbol written at -1");
        doubly.moveLeft();
        assertEquals(-2, doubly.getHeadPosition(), "head should move to -2");
        doubly.writeSymbol('B');

        assertEquals("BA", doubly.getTapeSnapshot(), "snapshot should cover written negative range");
        assertEquals(-2, doubly.getTapeOffset(), "snapshot offset should be -2");
        assertEquals(Integer.valueOf(-2), doubly.getMinPosition(), "min position should be -2");
        assertEquals(Integer.valueOf(-1), doubly.getMaxPosition(), "max position should be -1");

        // Semi infinite tape: head bounded at position 0
        TuringMachine semi = new TuringMachine("q0", '_');
        semi.setTapeMode(TuringMachine.TapeMode.SEMI_INFINITE);
        assertEquals(TuringMachine.TapeMode.SEMI_INFINITE, semi.getTapeMode(), "mode should be SEMI_INFINITE");
        assertFalse(semi.allowsNegativePositions(), "should not allow negative positions in semi infinite mode");

        assertEquals(0, semi.getHeadPosition(), "initial head position should be 0");
        semi.moveLeft();
        assertEquals(0, semi.getHeadPosition(), "moveLeft at 0 should stay at 0 in SEMI_INFINITE mode");

        semi.moveRight();
        semi.moveRight();
        assertEquals(2, semi.getHeadPosition(), "moveRight should advance head to 2");
        semi.moveLeft();
        assertEquals(1, semi.getHeadPosition(), "moveLeft should move head to 1");
        semi.moveLeft();
        assertEquals(0, semi.getHeadPosition(), "moveLeft should move head to 0");
        semi.moveLeft();
        assertEquals(0, semi.getHeadPosition(), "moveLeft should remain at 0");
    }

    private static void testTapeSnapshotsAndCoordinates() {
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.loadTape("11"); // positions 0 and 1

        // Default getTapeSnapshot shows range of written cells
        assertEquals("11", machine.getTapeSnapshot(), "default snapshot should show written cells");
        assertEquals(0, machine.getTapeOffset(), "offset should be 0");

        // Move head beyond written cells to position 3
        machine.moveRight(); // 1
        machine.moveRight(); // 2
        machine.moveRight(); // 3
        assertEquals(3, machine.getHeadPosition(), "head should be at position 3");

        // Without includeHead: shows written range "11"
        assertEquals("11", machine.getTapeSnapshot(false), "snapshot without head should show '11'");

        // With includeHead: shows range from 0 to 3 ("11__")
        assertEquals("11__", machine.getTapeSnapshot(true), "snapshot with head should include unwritten head position");
        assertEquals(0, machine.getTapeOffset(true), "offset should be 0");

        // Custom range snapshot
        assertEquals("1__", machine.getTapeSnapshot(1, 3), "custom range snapshot should show slice");
        assertEquals("____", machine.getTapeSnapshot(2, 5), "custom range outside written area should show blanks");

        // Tape snapshot with head marker
        assertEquals("11_[_]", machine.getTapeSnapshotWithHead(), "snapshot with head should enclose head in brackets");

        // Reading symbols at specific positions
        assertEquals('1', machine.readSymbolAt(0), "readSymbolAt(0) should be '1'");
        assertEquals('1', machine.readSymbolAt(1), "readSymbolAt(1) should be '1'");
        assertEquals('_', machine.readSymbolAt(2), "readSymbolAt(2) should be blank");
        assertEquals('_', machine.readSymbolAt(3), "readSymbolAt(3) should be blank");
    }

    private static void testStepLimitTerminationReason() {
        TuringMachine machine = new TuringMachine("q0", '_');
        // Infinite loop: stays in q0 moving right writing 1
        machine.addTransition("q0", '1', "q0", '1', TuringMachine.Direction.RIGHT);
        machine.addTransition("q0", '_', "q0", '1', TuringMachine.Direction.RIGHT);
        machine.loadTape("1");

        int steps = machine.run(20);
        assertEquals(20, steps, "should execute exactly 20 steps");
        assertEquals(TuringMachine.TerminationReason.STEP_LIMIT_REACHED, machine.getTerminationReason(),
                "terminationReason should be STEP_LIMIT_REACHED");
        assertFalse(machine.isHalted(), "machine did not halt");
        assertFalse(machine.isStuck(), "machine is not stuck on missing rule");
    }
}
