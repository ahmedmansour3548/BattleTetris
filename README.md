# 🟦 BattleTetris

[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)  
[![License: CC0](https://img.shields.io/badge/License-CC0-lightgrey)](LICENSE)

<p align="center">
  <img src="src/img/logo.png" alt="BattleTetris Logo" width="400"/>
</p>


A competitive two‑player twist on the classic Tetris game: one player drops blocks from the top, the other from the bottom—meeting in the middle. Fast, frenetic, and perfect for head‑to‑head showdowns!

---

## 🎯 Features

- **Dual‑direction play**  
  – Player 1 drops pieces from the top ↓  
  – Player 2 drops pieces from the bottom ↑  
- **Passing over**  
  Pieces may overlap the opponent’s falling blocks (semi‑transparent) but only “fix” when they collide with walls or their own stack.  
- **Hold & Swap**  
  Store one piece in HOLD; swap back when needed (cannot hold after a merge).  
- **Soft & Hard Drops**  
  – Soft drop for fine placement  
  – Hard drop (“slam”) with satisfying sound & flash  
- **Ghost Preview**  
  Semi‑transparent outline shows exactly where your piece will land.  
- **Half‑board Line Clears**  
  Each player clears lines on their own half—strategize zone control!  
- **Smooth DAS + ARR**  
  Delayed Auto Shift (DAS) and Auto Repeat Rate (ARR), tuned for pro‑level responsiveness.  
- **Pause, Restart & Menu**  
  Sleek dark‑mode control panel with Pause/Resume, Restart, Fullscreen and Menu buttons.

---

## 🎮 Controls

| Action        | Player 1        | Player 2          |
|---------------|-----------------|-------------------|
| Move Left     | `A`             | ◀️ Arrow           |
| Move Right    | `D`             | ▶️ Arrow           |
| Rotate (CW)   | `W`             | ▲ Arrow           |
| Soft Drop     | `S`             | ▼ Arrow           |
| Hard Drop     | `V`             | `.` (Period)      |
| Hold          | `C`             | `,` (Comma)       |
| Pause/Resume  | `P` **or** Pause button | Pause button   |
| Restart Game  | Restart button  | Restart button    |
| Back to Menu  | Menu button     | Menu button       |

---

## 🚀 Getting Started

### Prerequisites

- **Java 11** or higher  
- A modern IDE (IntelliJ IDEA, Eclipse) or a command‑line toolchain  

### Build & Run

1. **Clone the repo**  
   ```bash
   git clone https://github.com/ahmedmansour3548/BattleTetris.git
   cd BattleTetris
   ```
   
2. **Compile**  
   ```bash
	javac -d out src/**/*.java
	```
	
3. **Run**  
   ```bash
	java -cp out MainMenu
	```

### License
This work is released under CC0 1.0 Universal. See LICENSE for details.


Made with ❤️ for VARLAB 2024 Game Jam
