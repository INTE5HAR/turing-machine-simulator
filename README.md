<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=0:0d1117,100:f0b429&height=160&section=header&text=Turing%20Machine%20Simulator&fontSize=40&fontColor=ffffff" alt="Turing Machine Simulator banner" />
</p>

<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=20&duration=3000&pause=1000&color=F0B429&center=true&vCenter=true&width=700&lines=Define+states+and+transition+rules;Load+a+tape+and+step+through+the+computation;Unary+increment:+111+%E2%86%92+1111" alt="Typing animation" />
</p>

A [Turing machine](https://en.wikipedia.org/wiki/Turing_machine) is an abstract model of computation. It uses a tape, a read/write head, a set of states, and transition rules. This project implements one in Java.

## Features

- Sparse tape with a configurable blank symbol
- Transition rules keyed by state and read symbol
- Halt states
- Single-step execution (`step`)
- Bounded execution (`run(maxSteps)`)
- Basic operations for direct control: `loadTape`, `readSymbol`, `writeSymbol`, `moveLeft`, `moveRight`, `stay`, `addTransition`, `addHaltState`

## Requirements

- Java 17 or later
- `javac` and `java` on your `PATH`

## Project structure

```text
.
├── src/
│   ├── main/java/com/inte5har/tm/
│   │   ├── Main.java            # Example program
│   │   └── TuringMachine.java   # Core simulator
│   └── test/java/com/inte5har/tm/
│       └── TuringMachineTest.java
├── LICENSE
└── README.md
```

## Quick start

Run these commands from the repository root.

Compile:

```bash
javac -d out src/main/java/com/inte5har/tm/*.java
```

Run the example:

```bash
java -cp out com.inte5har.tm.Main
```

Expected output:

```text
Initial intent: unary increment
Steps executed: 4
Final state: halt
Head position: 3
Tape: 1111
```

> **Note:** On Windows Command Prompt, the `*.java` wildcard does not expand. Use Git Bash, PowerShell, or an IDE such as VS Code.

## Run the tests

```bash
javac -d out src/main/java/com/inte5har/tm/*.java src/test/java/com/inte5har/tm/*.java
java -cp out com.inte5har.tm.TuringMachineTest
```

Expected output ends with `All tests passed`.

## Example: unary increment

The machine adds one `1` to a unary number.

| State | Read | Next state | Write | Move  |
|-------|------|------------|-------|-------|
| `q0`  | `1`  | `q0`       | `1`   | Right |
| `q0`  | `_`  | `halt`     | `1`   | Stay  |

1. The head reads `1` and moves right. This repeats until the head reads a blank.
2. The head reads a blank, writes `1`, and enters `halt`.

Input `111` becomes `1111`.

## Usage

```java
TuringMachine machine = new TuringMachine("q0", '_');
machine.addHaltState("halt");
machine.addTransition("q0", '1', "q0", '1', TuringMachine.Direction.RIGHT);
machine.addTransition("q0", '_', "halt", '1', TuringMachine.Direction.STAY);

machine.loadTape("111");
int steps = machine.run(100);

System.out.println(machine.getTapeSnapshot()); // 1111
```

## Limitations

- The simulator does not check that a rule exists for every state and symbol pair. If no rule matches, the run stops, and the machine does not enter a halt state.
- The tape is stored sparsely. The head can move to negative positions, and the snapshot shows only the range between the leftmost and rightmost written cells.

## Further reading

- [Turing machine, Wikipedia](https://en.wikipedia.org/wiki/Turing_machine)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
