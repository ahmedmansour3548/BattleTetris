// Based on code written in 2013 by Johannes Holzfuß <johannes@holzfuss.name>
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;

public class Tetris extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Board board;
    private final Player p1, p2;
    private boolean isPaused = false;
    private boolean flash = false;

    private final int tileSize = 26;
    private final int numCols, numRows;
    private final int boardWidth, boardHeight;

    // DAS handlers for smooth horizontal movement
    private final DASHandler p1Left, p1Right, p2Left, p2Right;

    // Controls
    private final JButton pauseButton;
    private final JButton restartButton;
    private final JPanel controlPanel;

    public Tetris(int cols, int rows) {
        this.numCols     = cols;
        this.numRows     = rows;
        this.boardWidth  = cols * tileSize;
        this.boardHeight = rows * tileSize;

        board = new Board(cols, rows);
        p1 = new Player(1, Direction.DOWN, new Point(cols/2, 4), board, this);
        p2 = new Player(2, Direction.UP,   new Point(cols/2, rows-4), board, this);
        initGame();

        // Initialize DAS handlers
        p1Left  = new DASHandler(-1, p1);
        p1Right = new DASHandler(+1, p1);
        p2Left  = new DASHandler(-1, p2);
        p2Right = new DASHandler(+1, p2);

        // layout & controls
        setLayout(new BorderLayout());
            pauseButton = new JButton("Pause");
    pauseButton.addActionListener(e -> {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
    });

    restartButton = new JButton("Restart");
    restartButton.setVisible(false);
    restartButton.addActionListener(e -> restartGame());

    controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    // ─── Dark‑mode styling ────────────────────────────────────────
    controlPanel.setOpaque(true);
    controlPanel.setBackground(new Color(30, 30, 30, 200));

    // style both buttons
    for (JButton b : new JButton[]{ pauseButton, restartButton }) {
        b.setBackground(new Color(50, 50, 50));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
    }

    controlPanel.add(pauseButton);
    controlPanel.add(restartButton);
    add(controlPanel, BorderLayout.SOUTH);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                // DAS movement keys
                switch (code) {
                    case KeyEvent.VK_A: p1Left.keyDown(); return;
                    case KeyEvent.VK_D: p1Right.keyDown(); return;
                    case KeyEvent.VK_LEFT:  p2Left.keyDown(); return;
                    case KeyEvent.VK_RIGHT: p2Right.keyDown(); return;
                }
                // other game controls
                if (!p1.hasLost() && !p2.hasLost()) {
                    switch (code) {
                        // Player 1
                        case KeyEvent.VK_W: p1.rotate(+1); break;
                        case KeyEvent.VK_S: p1.drop();       break;
                        case KeyEvent.VK_V: p1.hardDrop();   break;
                        // Player 2
                        case KeyEvent.VK_UP:    p2.rotate(+1); break;
                        case KeyEvent.VK_DOWN:  p2.drop();     break;
                        case KeyEvent.VK_SPACE: p2.hardDrop(); break;
                        // General
                        case KeyEvent.VK_P: isPaused = !isPaused; break;
                    }
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A: p1Left.keyUp(); break;
                    case KeyEvent.VK_D: p1Right.keyUp(); break;
                    case KeyEvent.VK_LEFT:  p2Left.keyUp(); break;
                    case KeyEvent.VK_RIGHT: p2Right.keyUp(); break;
                }
            }
        });
    }

    /** Called by Player.hardDrop() to trigger slam effects. */
public void onSlam(List<Point> landing) {
    // Play slam sound
    new Thread(() -> {
        try {
            // Look in the classpath under /sfx/slam.wav
            URL url = getClass().getClassLoader().getResource("sfx/slam.wav");
            if (url == null) {
                System.err.println("⚠️  slam.wav not found on classpath");
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();  // now you’ll actually see errors
        }
    }).start();

    // Flash effect
    flash = true;
    repaint();
    new Timer(100, ev -> {
        flash = false;
        repaint();
        ((Timer)ev.getSource()).stop();
    }).start();
}

private void restartGame() {
        // reset flags & UI
        isPaused = false;
        pauseButton.setText("Pause");
        restartButton.setVisible(false);

        // clear board & players
        board.clear();
        p1.reset();
        p2.reset();
        initGame();

        // repaint & restart loop
        repaint();
        startGameLoop();
        requestFocusInWindow();
    }


    private void initGame() {
        board.clear();
        p1.newPiece();
        p2.newPiece();
    }

    public void startGameLoop() {
        new Thread(() -> {
            while (!p1.hasLost() && !p2.hasLost()) {
                try {
                    Thread.sleep(1000);
                    if (!isPaused) {
                        p1.drop();
                        p2.drop();
                        repaint();
                    }
                } catch (InterruptedException ignored) {}
            }
            SwingUtilities.invokeLater(() -> restartButton.setVisible(true));
        }).start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(boardWidth + 200, boardHeight + 120);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        int w = getWidth(), h = getHeight();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        g2.setPaint(new GradientPaint(0,0,Color.DARK_GRAY, 0,h,Color.BLACK));
        g2.fillRect(0,0,w,h);

        int offsetX = (w - boardWidth)/2;
        int offsetY = (h - boardHeight)/2;

        // Board backdrop
        g2.setColor(Color.BLACK);
        g2.fillRect(offsetX, offsetY, boardWidth, boardHeight);
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(offsetX, offsetY, boardWidth, boardHeight);

        if (flash) {
            g2.setColor(new Color(15, 15 , 15));
            g2.fillRect(offsetX, offsetY, boardWidth, boardHeight);
        }

        // Draw fixed cells
        for (int x=0; x<numCols; x++) for (int y=0; y<numRows; y++) {
            Cell c = board.getCell(x,y);
            if (c.getState()==Cell.State.FIXED) {
                Color col = (c.getOwnerId()==1)
                    ? p1.getTetraminoColors().get(c.getShapeId())
                    : p2.getTetraminoColors().get(c.getShapeId());
                g2.setColor(col);
                g2.fillRect(offsetX + x*tileSize,
                            offsetY + y*tileSize,
                            tileSize-1, tileSize-1);
            }
        }

        // Center dashed line
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(3,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10, new float[]{8,8}, 0));
        g2.setColor(Color.LIGHT_GRAY);
        int cy = offsetY + boardHeight/2;
        g2.drawLine(offsetX, cy, offsetX + boardWidth, cy);
        g2.setStroke(old);

        // Scores
        int boxW=100, boxH=36, pad=12;
        int boxY = offsetY - boxH - pad;
        g2.setColor(new Color(0,0,0,160));
        g2.fillRoundRect(offsetX, boxY, boxW, boxH, 8,8);
        g2.fillRoundRect(offsetX+boardWidth-boxW, boxY, boxW, boxH, 8,8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial",Font.BOLD,18));
        g2.drawString("P1: " + p1.getScore(), offsetX+10, boxY+24);
        g2.drawString("P2: " + p2.getScore(),
                      offsetX+boardWidth-boxW+10, boxY+24);

        // Falling pieces and ghosts
        AffineTransform save = g2.getTransform();
        g2.translate(offsetX, offsetY);
        p1.drawGhost(g2);
        p2.drawGhost(g2);
        p1.drawPiece(g2);
        p2.drawPiece(g2);
        g2.setTransform(save);

        // Game-over message
        if (p1.hasLost() || p2.hasLost()) {
            String m = p1.hasLost() ? "Player 2 Wins!" : "Player 1 Wins!";
            g2.setFont(new Font("Arial",Font.BOLD,48));
            FontMetrics fm = g2.getFontMetrics();
            int mw = fm.stringWidth(m), mh = fm.getHeight();
            int mx = (w-mw)/2, my = (h-mh)/2 + fm.getAscent();
            g2.setColor(new Color(255,0,0,200));
            g2.drawString(m, mx, my);
        }

        g2.dispose();
    }

    // ─── DAS Handler ──────────────────────────────────────────
    private class DASHandler {
    private final int dx;
    private final Player player;
    private Timer dasTimer, arrTimer;
    private boolean held = false;   // <- track actual press state

    DASHandler(int dx, Player player) {
        this.dx = dx;
        this.player = player;
    }

    void keyDown() {
        if (p1.hasLost() || p2.hasLost()) return;
        // 1) ignore repeats until a keyUp()
        if (held) return;
        held = true;

        // 2) one immediate move
        player.move(dx);
        Tetris.this.repaint();

        // 3) start DAS delay
        dasTimer = new Timer(200, ev -> {
            // after DAS delay, start ARR if still held
            if (held) startARR();
            dasTimer = null;
        });
        dasTimer.setRepeats(false);
        dasTimer.start();
    }

    void keyUp() {
        // clear state & timers
        held = false;
        if (dasTimer   != null) { dasTimer.stop();   dasTimer   = null; }
        if (arrTimer   != null) { arrTimer.stop();   arrTimer   = null; }
    }

    private void startARR() {
        // auto-repeat at fixed rate while held
        arrTimer = new Timer(50, ev -> {
            if (held) {
                player.move(dx);
                Tetris.this.repaint();
            } else {
                arrTimer.stop();
                arrTimer = null;
            }
        });
        arrTimer.setRepeats(true);
        arrTimer.start();
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tetris game = new Tetris(10,23);
            JFrame frame = new JFrame("BattleTetris");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.startGameLoop();
        });
    }
}
