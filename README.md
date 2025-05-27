# 🟦 BattleTetris

[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)  
[![License: CC0](https://img.shields.io/badge/License-CC0-lightgrey)](LICENSE)

A competitive two‑player twist on the classic Tetris game: one player drops blocks from the top, the other from the bottom—meeting in the middle. Fast, frenetic, and perfect for head‑to‑head showdowns!

---

## 🎯 Features

- **Dual‐direction play**  
  – Player 1 drops pieces from the top ↓  
  – Player 2 drops pieces from the bottom ↑  
- **Passing over**  
  Pieces may overlap the opponent’s falling blocks (they turn white) but only “fix” when they collide with walls or their own stack.  
- **Hard & Soft Drops**  
  – Soft drop for fine placement  
  – Hard drop for instant “slam” with satisfying sound & flash  
- **Ghost Preview**  
  Semi‑transparent outlines show exactly where your piece will land.  
- **Line clears per half‑board**  
  Each player clears lines in their own half—strategize zone control!  
- **Smooth DAS + ARR**  
  Delayed Auto Shift (DAS) and Auto Repeat Rate (ARR) tuned for pro‑level responsiveness.  
- **Pause & Restart**  
  Buttons appear in a sleek dark‑mode control panel. No need to close the window to play again.

---

## 🎮 Controls

| Action       | Player 1 Keys   | Player 2 Keys    |
|--------------|-----------------|------------------|
| Move Left    | `A`             | ◀️ Arrow         |
| Move Right   | `D`             | ▶️ Arrow         |
| Rotate       | `W`             | ▲ Arrow         |
| Soft Drop    | `S`             | ▼ Arrow         |
| Hard Drop    | `V`             | Space           |
| Pause/Resume | `P` or ⬤ Pause  | (Button)        |
| Restart Game | (Button)        | (Button)        |

---

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher  
- A modern IDE (IntelliJ IDEA, Eclipse) or command‑line toolchain

### Build & Run

1. **Clone the repo**  
   ```bash
   git clone https://github.com/<your‑username>/BattleTetris.git
   cd BattleTetris

2. **Compile**  
    ```bash
    javac -d out src/**/*.java

3. **Compile**  
    ```bash
    java -cp out Tetris