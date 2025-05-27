 
// Tetris.java
//
// Based off of code by Johannes Holzfuß <johannes@holzfuss.name>
//

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class WackyTetris extends JPanel {

    private static final long serialVersionUID = -8715353373678321308L;

    

    static WackyPlayer1 p1;
    static WackyPlayer2 p2;
    private static boolean isPaused = false;
    private static boolean p1Lose;
    private static boolean p2Lose;
    private boolean collidedWithOpponent = false;
    private WackyTile[][] board;
    private int tileSize = 26;
    private int numHorizontalTiles = 20;
    private int numVerticalTiles = 35;
    private int boardWidth = numHorizontalTiles * tileSize;
    private int boardHeight = numVerticalTiles * tileSize;

    public WackyTetris(int numTilesWidth, int numTilesHeight) {
        this.numHorizontalTiles = numTilesWidth;
        this.numVerticalTiles = numTilesHeight;
        this.boardWidth = numHorizontalTiles * tileSize;
        this.boardHeight = numVerticalTiles * tileSize;
    }
    
    // Creates a border around the board and initializes the dropping piece
    private void init() {
        this.board = new WackyTile[numHorizontalTiles][numVerticalTiles];
        for (int i = 0; i < numHorizontalTiles; i++) {
            for (int j = 0; j < numVerticalTiles; j++) {
                board[i][j] = new WackyTile(i, j, Color.BLACK, 0);  // Initialize each tile with no owner
                if (i == 0 || i == numHorizontalTiles - 1 || j == 0 || j == numVerticalTiles - 1) {
                    board[i][j].setColor(Color.GRAY);
                    board[i][j].setOwner(-1);
                }
            }
        }
        p1.newPiece();
        p2.newPiece();
    }
    
    
    // Collision test for dropping a piece
    public boolean collidesAt(int x, int y, ArrayList<Point> pieceOffsets, int playerID) {
        try {
                if (playerID == 1) {
                    for (Point p : pieceOffsets) {
                        int collisionX = p.x + x;
                        int collisionY = p.y + y;
                        if (p2.getCurrentBoardPosition().contains(new Point(collisionX, collisionY))) {
                            collidedWithOpponent = true;
                            mergePieces(p.x, p.y);
                            System.out.println("intersected wihle falling!");
                            break;
                        }
                        if (board[collisionX][collisionY].isOwner(p2.getPlayerID())) {
                            p1.setPassingOver(true);
                        }
                        if (board[collisionX][collisionY].isBorder() || board[collisionX][collisionY].isOwner(playerID)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    for (Point p : pieceOffsets) {
                        int collisionX = p.x + x;
                        int collisionY = p.y + y;
                        if (board[collisionX][collisionY].isOwner(p1.getPlayerID())) {
                            p2.setPassingOver(true);
                        }
                        if (board[collisionX][collisionY].isBorder() || board[collisionX][collisionY].isOwner(playerID)) {
                            return true;
                        }
                    }
                    return false;
                
            }
        }
        catch (ConcurrentModificationException cme) {
            System.err.println("OOPS, got a CME!");
            return false;
        }
    }
    
    // Merge pieces when they collide
    private void mergePieces(int collisionX, int collisionY) {
        if (p1.getCurrentBoardPosition().get(0).y >= numVerticalTiles / 2) {
            p2.getCurrentPiece().addTiles(p1.getPieceOffsets(), collisionX, collisionY);
            //p2.addPieceOffsets(p1.getPieceOffsets(), p1.getPieceOrigin());
            p1.newPiece();
            p2.toggleDirection();
        }
        else {
            p1.getCurrentPiece().addTiles(p2.getPieceOffsets(), collisionX, collisionY);
            //p1.addPieceOffsets(p2.getPieceOffsets(), p2.getPieceOrigin());
            p2.newPiece();
            p1.toggleDirection();
        }

        
        // for (Point p : sourcePlayer.getPieceOffsets()) {
        //     //setColor(sourcePlayer.getTetraminoColor());
        //     board[sourcePlayer.getPieceOrigin().x + p.x][sourcePlayer.getPieceOrigin().y + p.y].setOwner(targetPlayer.getPlayerID());
        // }
        // //targetPlayer.addPieceOffsets(sourcePlayer.getPieceOffsets(), new Point(collisionX, collisionY));
        // sourcePlayer.newPiece();
        // targetPlayer.toggleDirection();
        repaint();
    }
    
    // Make the dropping piece part of the board, so it is available for collision detection.
    public void fixToBoard(int playerID) {
        if (playerID == 1) {
            for (Point p : p1.getCurrentPiece().orientations().get(p1.getOrientation())) {
                board[p1.getPieceOrigin().x + p.x][p1.getPieceOrigin().y + p.y].setColor(p1.getTetraminoColor());
                board[p1.getPieceOrigin().x + p.x][p1.getPieceOrigin().y + p.y].setOwner(p1.getPlayerID());
                // Check if Player 1 lost
                if (p1.getPieceOrigin().y + p.y == numVerticalTiles / 2) {
                    p1Lose = true;
                }
            }
            this.collidedWithOpponent = false;
            p1.resetPiece();
            p1.newPiece();
            clearRows(playerID);
        }
        else {
            for (Point p : p2.getCurrentPiece().orientations().get(p2.getOrientation())) {
                board[p2.getPieceOrigin().x + p.x][p2.getPieceOrigin().y + p.y].setColor(p2.getTetraminoColor());
                board[p2.getPieceOrigin().x + p.x][p2.getPieceOrigin().y + p.y].setOwner(p2.getPlayerID());
                // Check if Player 2 lost
                if (p2.getPieceOrigin().y + p.y == numVerticalTiles / 2) {
                    p2Lose = true;
                }
            }
            this.collidedWithOpponent = false;
            p2.resetPiece();
            p2.newPiece();
            clearRows(playerID);
        }
    }
    
    public void deleteRow(Direction direction, int row) {
        if (direction == Direction.DOWN) {
            for (int j = row-1; j > 0; j--) {
                for (int i = 1; i < numHorizontalTiles - 1; i++) {
                    if (board[i][j].getOwner() == p1.getPlayerID() || board[i][j].isEmpty())
                        board[i][j+1].setColor(board[i][j].getColor());
                        board[i][j+1].setOwner(board[i][j].getOwner());
                }
            }
        }
        else {
             for (int j = row + 1; j < numVerticalTiles - 1; j++) {
                for (int i = 1; i < numHorizontalTiles - 1; i++) {
                    if (board[i][j].getOwner() == p1.getPlayerID() || board[i][j].isEmpty())
                        board[i][j-1].setColor(board[i][j].getColor());
                        board[i][j-1].setOwner(board[i][j].getOwner());
                }
            }
        }
    }
    
    // Clear completed rows from the field and award score according to
    // the number of simultaneously cleared rows.
    public void clearRows(int playerID) {
        boolean gap;
        int numClears = 0;
        
        if (playerID == 1) {
            for (int j = (numVerticalTiles - 1) / 2; j < numVerticalTiles - 1; j++) {
                gap = false;
                for (int i = 1; i < numHorizontalTiles - 1; i++) {
                    
                    if (board[i][j].isEmpty() || board[i][j].isOwner(p2.getPlayerID())) {
                        gap = true;
                        break;
                    }
                }
                if (!gap) {
                    deleteRow(Direction.DOWN, j);
                    //j += 1;
                    numClears += 1;
                }
            }
            switch (numClears) {
                case 1:
                    p2.addScore(100);
                    break;
                case 2:
                    p2.addScore(300);
                    break;
                case 3:
                    p2.addScore(500);
                    break;
                case 4:
                    p2.addScore(800);
                    break;
            }
        }
        else {
            for (int j = (numVerticalTiles - 1) / 2; j > 0; j--) {
                gap = false;
                for (int i = 1; i < numHorizontalTiles - 1; i++) {
                    
                    if (board[i][j].isEmpty() || board[i][j].isOwner(p1.getPlayerID())) {
                        gap = true;
                        break;
                    }
                }
                if (!gap) {
                    deleteRow(Direction.UP, j);
                    //j -= 1;
                    numClears += 1;
                }
            }
            switch (numClears) {
                case 1:
                    p1.addScore(100);
                    break;
                case 2:
                    p1.addScore(300);
                    break;
                case 3:
                    p1.addScore(500);
                    break;
                case 4:
                    p1.addScore(800);
                    break;
            }
        }
        
    }
    
    @Override 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Paint the board
        g.fillRect(0, 0, boardWidth, boardHeight);
        for (int i = 0; i < numHorizontalTiles; i++) {
            for (int j = 0; j < numVerticalTiles; j++) {
                g.setColor(board[i][j].getColor());
                g.fillRect(tileSize * i, tileSize * j, tileSize - 1, tileSize - 1);
            }
        }

        // Display the score
        g.setColor(Color.BLACK);
        g.drawString("" + p1.getScore(), 600, 25);

        // Draw the currently falling pieces
        //collidesAt(p1.getPieceOrigin().x, p1.getPieceOrigin().y, p1.getPieceOffsets(), p1.getPlayerID());
        p1.drawPiece(g);
        //collidesAt(p2.getPieceOrigin().x, p2.getPieceOrigin().y, p2.getPieceOffsets(), p2.getPlayerID());
        p2.drawPiece(g);

        // Draw a gray dashed line horizontally across the center of the board
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.GRAY);
        float[] dashPattern = { 11, 10 }; // 11 pixels dash, 10 pixels space
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
        int centerY = boardHeight / 2;
        g2d.drawLine(0, centerY, boardWidth, centerY);

        // Check if either player has lost and display the winning message
        if (p1Lose || p2Lose) {
            String message = p1Lose ? "Player 2 Wins!" : "Player 1 Wins!";
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g.getFontMetrics();
            int msgWidth = fm.stringWidth(message);
            int msgHeight = fm.getHeight();
            int x = (boardWidth + msgWidth) / 2;
            int y = (boardHeight - msgHeight) / 2 + fm.getAscent();
            g.drawString(message, x, y);
        }
    }


    public int getTileSize() {
        return tileSize;
    }

    public ArrayList<Color> getP1TetraminoColors() {
        return p1.getTetraminoColors();
    }

    public ArrayList<Point> getP1PieceOffsets() {
        return p1.getPieceOffsets();
    }

    public ArrayList<Color> getP2TetraminoColors() {
        return p2.getTetraminoColors();
    }

    public ArrayList<Point> getP2PieceOffsets() {
        return p2.getPieceOffsets();
    }

    public WackyTile[][] getBoard() {
        return this.board;
    }

    public int getNumTilesWidth() {
        return this.numHorizontalTiles;
    }
    
    public int getNumTilesHeight() {
        return this.numVerticalTiles;
    }


    public static void mainWacky(int numTilesWidth, int numTilesHeight) {
        JFrame f = new JFrame("Tetris");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(26 * numTilesWidth + 500, 26 * numTilesHeight + 50);
        final WackyTetris game = new WackyTetris(numTilesWidth, numTilesHeight);
        p1 = new WackyPlayer1(game, Direction.DOWN);
        p2 = new WackyPlayer2(game, Direction.UP);

        game.init();
        f.add(game);
        
        // Keyboard controls
        f.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }
            
            public void keyPressed(KeyEvent e) {
                if (!p1Lose && !p2Lose) {
                    switch (e.getKeyCode()) {
                    // Player 1
                    case KeyEvent.VK_W:
                        p1.rotate(-1);
                    break;
                    case KeyEvent.VK_S:
                        p1.rotate(+1);
                        break;
                    case KeyEvent.VK_A:
                        p1.move(-1);
                        break;
                    case KeyEvent.VK_D:
                        p1.move(+1);
                        break;
                    case KeyEvent.VK_V:
                        p1.drop();
                        p1.addScore(1);
                        break;
                    // Player 2
                    case KeyEvent.VK_UP:
                        p2.rotate(-1);
                    break;
                    case KeyEvent.VK_DOWN:
                        p2.rotate(+1);
                        break;
                    case KeyEvent.VK_LEFT:
                        p2.move(-1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        p2.move(+1);
                        break;
                    case KeyEvent.VK_SPACE:
                        p2.drop();
                        p2.addScore(1);
                        break;
                        // General
                    case KeyEvent.VK_P:
                        isPaused = !isPaused;
                    }
                }
            }
            
            public void keyReleased(KeyEvent e) {
            }
            
        });
        f.setVisible(true);
        // Make the falling piece drop every second
        new Thread() {
            @Override public void run() {
                while (!p1Lose && !p2Lose) {
                    try {
                        Thread.sleep(800);
                        if (!isPaused) {
                            p1.drop();
                            p2.drop();
                            //System.out.println("Corner owner: " + game.board[1][34].getOwner());
                        }
                    } catch ( InterruptedException e ) {
                        System.err.println("Something bad happened!");
                    }
                }
            }
        }.start();
    }
}
