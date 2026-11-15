package com.inte5har.tm;

public class TuringMachineTest {
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static void main(String[] args) {
        testBasicTapeOperations();
        testUnaryIncrementRun();
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
        assertEquals("1111", machine.getTapeSnapshot(), "unary increment should append one symbol");

        machine.loadTape("1");
        int secondRunSteps = machine.run(100);

        assertEquals(2, secondRunSteps, "run should work after loading another tape");
        assertEquals("halt", machine.getCurrentState(), "machine should halt again on the second run");
        assertEquals("11", machine.getTapeSnapshot(), "second unary increment should append one symbol");
    }
}
