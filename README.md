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
- Transition completeness validation (`isComplete`, `validateCompleteness`, `getMissingTransitions`)
- Halt states, fallback halt state on missing rules (`setFallbackHaltState`), and execution status tracking (`TerminationReason`, `isHalted`, `isStuck`)
- Configurable tape modes: doubly-infinite or semi-infinite (`TapeMode.SEMI_INFINITE`, left-bounded at 0)
- Enhanced tape inspection and snapshots: arbitrary ranges, snapshots with head position (`getTapeSnapshot(includeHead)`), annotated snapshots with head marker (`getTapeSnapshotWithHead`), and coordinate offsets
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

## Handling the Limitations

The simulator provides explicit options and utilities to address common Turing machine constraints:

### 1. Missing Transition Rules and Halting
- **Completeness check:** Use `machine.isComplete()` or `machine.validateCompleteness()` to verify that every state and symbol pair has a defined rule before running. Call `machine.getMissingTransitions()` to inspect which pairs are missing.
- **Fallback halt state:** If no transition matches at runtime, the machine stops. To enter an explicit halt state on missing rules, configure `machine.setFallbackHaltState("reject")` (or `"halt"`). When a missing rule occurs, the machine enters that state, `isHalted()` returns `true`, and `getTerminationReason()` is `TerminationReason.HALTED`.
- **Termination diagnostics:** Query `machine.getTerminationReason()` (`HALTED`, `MISSING_TRANSITION`, `STEP_LIMIT_REACHED`) or `machine.isStuck()` to distinguish normal completion from undefined transitions or step timeouts.
- **Strict mode:** Enable `machine.setStrictTransitions(true)` to fail fast with an `IllegalStateException` whenever an undefined rule is encountered during execution.

### 2. Tape Boundaries and Snapshots
- **Tape modes (Semi-infinite vs. Doubly-infinite):** By default, the tape is doubly-infinite (`TapeMode.DOUBLY_INFINITE`), permitting negative head positions. For standard textbook semi-infinite tapes, set `machine.setTapeMode(TapeMode.SEMI_INFINITE)` so the head cannot move left beyond cell 0.
- **Snapshot including head:** `machine.getTapeSnapshot(true)` expands the snapshot range to include the current head position even when the head is over unwritten blank cells.
- **Visual snapshot with head marker:** `machine.getTapeSnapshotWithHead()` encloses the symbol under the head in brackets, e.g. `111[1]`.
- **Custom slice snapshots:** `machine.getTapeSnapshot(fromPosition, toPosition)` returns the exact slice of the tape between any two coordinates.
- **Coordinate offsets:** Inspect written tape boundaries with `machine.getMinPosition()`, `machine.getMaxPosition()`, and `machine.getTapeOffset()`.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
