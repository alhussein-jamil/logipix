# Logipix

Java solver for Logipix path puzzles, written in 2022 by Alhussein Jamil and Jia Jean Law.

A cell holding a positive number *n* must be joined to exactly one other *n* by a path of *n* cells, counting both ends. Paths do not cross or share a cell. Zero is an empty cell a path may cross. A one occupies only its own cell.

Equal clues are paired by a path of that exact length. The solver lists the legal routes for each pair, then fills the board from the clue with the fewest routes still open. The 40×24 board has no solution.

## Puzzle file

Puzzles are concatenated. Each one is a width, a height, then `width * height` integers in row-major order. Any whitespace between integers is fine.

```text
3 1
3 0 3
```

[`puzzles/logix.txt`](puzzles/logix.txt) is the original set of ten boards, from 3×3 through 30×35.

## Build and run

JDK 17 or newer. The Maven Wrapper is included, so a separate Maven install is optional.

```bash
./mvnw -B package
java -jar target/logipix-1.0.0.jar puzzles/logix.txt
```

On Windows, use `mvnw.cmd` instead of `./mvnw`. With no argument, the program reads `puzzles/logix.txt` from the current directory.

Each puzzle prints a board and the runtime in milliseconds, or `NO SOLUTION`.

| Glyph | Meaning |
| --- | --- |
| space | empty |
| `@` | a 1 |
| `►` `▼` `◄` `▲` | start of a path |
| `→` `↓` `←` `↑` | continuation |
| `●` | end of a path |

## Tests

```bash
./mvnw -B verify
```

## License

MIT. Copyright (c) 2022 Alhussein Jamil and Jia Jean Law. See [LICENSE](LICENSE).
