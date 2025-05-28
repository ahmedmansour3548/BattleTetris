# 🕹 BattleTetris

[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)  
[![License: CC0](https://img.shields.io/badge/License-CC0-lightgrey)](LICENSE)

<p align="center">
  <img src="src/img/logo.png" alt="BattleTetris Logo" width="400"/>
</p>


A competitive two‑player twist on the classic Tetris game: one player drops blocks from the top, the other from the bottom—meeting in the middle. It offers two modes; Normal and Wacky. Normal more closely resembles standard Tetris, while Wacky mode introduces the ability for falling pieces to merge together, creating a whole new dimension of strategy.

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
| Pause/Resume  | `P` **or** Pause button  |
| Restart Game  | Restart button    |
| Back to Menu  | Menu button         |

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
