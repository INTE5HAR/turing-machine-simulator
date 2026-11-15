# turing-machine-simulator

A Turing machine simulator in Java. Define states and transition rules, load a tape, and step through the computation with basic operations.

## Implemented basic TM operations

- Load tape input (`loadTape`)
- Read current symbol (`readSymbol`)
- Write current symbol (`writeSymbol`)
- Move head left/right/stay (`moveLeft`, `moveRight`, `stay`)
- Add transition rules (`addTransition`)
- Execute one step (`step`) or run multiple steps (`run`)

## Run the example

From repository root:

```bash
javac -d out src/main/java/com/inte5har/tm/*.java
java -cp out com.inte5har.tm.Main
```

The example performs a unary increment: `111` becomes `1111`.
