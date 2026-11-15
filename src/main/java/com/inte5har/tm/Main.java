package com.inte5har.tm;

public class Main {
    public static void main(String[] args) {
        TuringMachine machine = new TuringMachine("q0", '_');
        machine.addHaltState("halt");

        machine.addTransition("q0", '1', "q0", '1', TuringMachine.Direction.RIGHT);
        machine.addTransition("q0", '_', "halt", '1', TuringMachine.Direction.STAY);

        machine.loadTape("111");
        int steps = machine.run(100);

        System.out.println("Initial intent: unary increment");
        System.out.println("Steps executed: " + steps);
        System.out.println("Final state: " + machine.getCurrentState());
        System.out.println("Head position: " + machine.getHeadPosition());
        System.out.println("Tape: " + machine.getTapeSnapshot());
    }
}
