# 8-Puzzle Game Solver

Author: Topinio, Mark Genesis C.  
Course: CMSC170 X-4L

## Description

This app is an implementation of the 8-Puzzle game solver using various search algorithms such as Breadth-First Search (BFS), Depth-First Search (DFS), and A* algorithm. The 8-Puzzle game is a sliding puzzle that consists of a 3x3 grid with eight numbered tiles and one empty tile. The objective is to rearrange the tiles to reach the goal state from the initial state.

The app takes an input file `inputFile.in` that contains the initial configuration of the puzzle, and it finds the optimal solution to reach the goal state.

## Installation

To run the 8-Puzzle Game Solver, follow these steps:

1. Clone this repository to your local machine

2. Install Java SDK and JDK on your computer.

3. Open the project in a text editor like Visual Studio Code.

4. Compile the Java files in the terminal: `javac *.java`

5. Run the program:


## Usage

1. Place your desired initial configuration in the `inputFile.in` file. The initial configuration should be in the format of a 3x3 grid with each tile represented by a number (0 for the empty tile).

2. Run the program following the installation steps.

3. The app will output the steps to reach the goal state, along with the number of moves and the path.

## Algorithms

The app utilizes the following search algorithms:

- Breadth-First Search (BFS): Searches level by level, exploring all possible states before moving on to the next level. Guarantees the optimal solution but may require more memory.
- Depth-First Search (DFS): Explores as far as possible along each branch before backtracking. May not guarantee the optimal solution.
- A* Algorithm: Utilizes a heuristic function to estimate the cost to reach the goal state from the current state. A* combines the cost-so-far and the estimated cost to reach the goal to find the optimal path.

Feel free to explore and compare the results obtained from different algorithms!
