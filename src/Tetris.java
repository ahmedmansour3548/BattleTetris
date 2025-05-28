package src;

// Based on code written in 2013 by Johannes Holzfuß <johannes@holzfuss.name>
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.*;
import javax.swing.*;

public class Tetris extends JPanel {
    private static final long serialVersionUID = 1L;
    private final boolean wackyMode;
    private final Board board;
    private final Player p1, p2;
    private boolean isPaused = false;
    private boolean flash = false;
    private PendingDetonation pendingDetonation;
    private final List<Explosion> explosions = new ArrayList<>();
    private final Timer repaintTimer;
    private Clip p1MergeClip, p2MergeClip;
    private final int tileSize = 26;
    private final int previewSize = tileSize * 4;
    private final int numCols, numRows;
    private final int boardWidth, boardHeight;
    private volatile boolean runningGameLoop = false;
    private Thread gameLoopThread;
    private boolean isFullscreen = false;
    private Rectangle windowedBounds;
    public static final int MAX_MERGES = 4;
    private static final int DETONATION_PENALTY = 500;
    private static final int SIDE_PADDING = 10;
    // DAS handlers for smooth horizontal movement
    private final DASHandler p1Left, p1Right, p2Left, p2Right;

    // Controls
    private final JButton pauseButton;
    private final JButton restartButton;
    private final JPanel controlPanel;
    private final JButton menuButton;

    public Tetris(int cols, int rows, boolean wackyMode) {
        this.numCols = cols;
        this.numRows = rows;
        this.boardWidth = cols * tileSize;
        this.boardHeight = rows * tileSize;
        this.wackyMode = wackyMode;
        board = new Board(cols, rows);
        p1 = new Player(1, Direction.DOWN, new Point(cols / 2, 4), board, this);
        p2 = new Player(2, Direction.UP, new Point(cols / 2, rows - 4), board, this);
        initGame();

        // Initialize DAS handlers
        p1Left = new DASHandler(-1, p1);
        p1Right = new DASHandler(+1, p1);
        p2Left = new DASHandler(-1, p2);
        p2Right = new DASHandler(+1, p2);

        // layout & controls
        setLayout(new BorderLayout());
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Resume" : "Pause");
            Tetris.this.requestFocusInWindow();

        });

        restartButton = new JButton("Restart");
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> {
            Tetris.this.requestFocusInWindow();
            restartGame();
        });

        menuButton = new JButton("Menu");
        menuButton.setBackground(new Color(50, 50, 50));
        menuButton.setForeground(Color.WHITE);
        menuButton.setFocusPainted(false);
        menuButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        menuButton.setFont(menuButton.getFont().deriveFont(Font.BOLD, 16f));
        menuButton.addActionListener(e -> {
            // first tear down this game completely
            stopGame();

            // now switch back to the menu
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame) {
                Container content = ((JFrame) w).getContentPane();
                if (content.getComponentCount() > 0 && content.getComponent(0) instanceof JPanel) {
                    JPanel cards = (JPanel) content.getComponent(0);
                    CardLayout cl = (CardLayout) cards.getLayout();
                    cl.show(cards, "MENU");
                }
            }
        });

        JButton fullscreenButton = new JButton("Fullscreen");
        fullscreenButton.setBackground(new Color(50, 50, 50));
        fullscreenButton.setForeground(Color.WHITE);
        fullscreenButton.setFocusPainted(false);
        fullscreenButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        fullscreenButton.setFont(fullscreenButton.getFont().deriveFont(Font.BOLD, 16f));
        fullscreenButton.addActionListener(e -> {
            Tetris.this.requestFocusInWindow();
            toggleFullscreen();
        });

        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(true);
        controlPanel.setBackground(new Color(30, 30, 30, 200));

        // style both buttons
        for (JButton b : new JButton[] { pauseButton, restartButton }) {
            b.setFocusable(false);
            b.setBackground(new Color(50, 50, 50));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
            b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        }

        controlPanel.add(pauseButton);
        controlPanel.add(restartButton);
        controlPanel.add(fullscreenButton);
        controlPanel.add(menuButton);
        add(controlPanel, BorderLayout.SOUTH);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_P) {
                    isPaused = !isPaused;
                    pauseButton.setText(isPaused ? "Resume" : "Pause");
                    return;
                }

                // If we're paused, ignore everything else
                if (isPaused) {
                    return;
                }
                // DAS movement
                switch (code) {
                    case KeyEvent.VK_A:
                        p1Left.keyDown();
                        return;
                    case KeyEvent.VK_D:
                        p1Right.keyDown();
                        return;
                    case KeyEvent.VK_LEFT:
                        p2Left.keyDown();
                        return;
                    case KeyEvent.VK_RIGHT:
                        p2Right.keyDown();
                        return;
                }
                // other game controls
                if (!p1.hasLost() && !p2.hasLost()) {
                    switch (code) {
                        // Player 1
                        case KeyEvent.VK_W:
                            p1.rotate(+1);
                            break;
                        case KeyEvent.VK_S:
                            p1.drop();
                            break;
                        case KeyEvent.VK_V:
                            p1.hardDrop();
                            break;
                        // Player 2
                        case KeyEvent.VK_UP:
                            p2.rotate(+1);
                            break;
                        case KeyEvent.VK_DOWN:
                            p2.drop();
                            break;
                        case KeyEvent.VK_PERIOD:
                            p2.hardDrop();
                            break;
                        case KeyEvent.VK_C:
                            p1.hold();
                            break;
                        case KeyEvent.VK_COMMA:
                            p2.hold();
                            break;
                        case KeyEvent.VK_P:
                            isPaused = !isPaused;
                            break;
                    }
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        p1Left.keyUp();
                        break;
                    case KeyEvent.VK_D:
                        p1Right.keyUp();
                        break;
                    case KeyEvent.VK_LEFT:
                        p2Left.keyUp();
                        break;
                    case KeyEvent.VK_RIGHT:
                        p2Right.keyUp();
                        break;

                }
            }
        });

        repaintTimer = new Timer(16, e -> repaint());
        repaintTimer.start();
    }

    private void toggleFullscreen() {
        // Find our top level window
        Window win = SwingUtilities.getWindowAncestor(this);
        if (!(win instanceof JFrame))
            return;
        JFrame frame = (JFrame) win;

        if (!isFullscreen) {
            // remember normal window bounds
            windowedBounds = frame.getBounds();
            // switch to undecorated/maximized
            frame.dispose();
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } else {
            // restore
            frame.dispose();
            frame.setUndecorated(false);
            frame.setBounds(windowedBounds);
            frame.setExtendedState(JFrame.NORMAL);
            frame.setVisible(true);
        }
        isFullscreen = !isFullscreen;
        revalidate();
        repaint();
    }

    public void onSlam(List<Point> landing) {
        // Play slam sound
        playSound("sfx/slam.wav", false);
        // Flash effect
        flash = true;
        repaint();
        new Timer(100, ev -> {
            flash = false;
            repaint();
            ((Timer) ev.getSource()).stop();
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
        runningGameLoop = true;
        gameLoopThread = new Thread(() -> {
            while (runningGameLoop && !p1.hasLost() && !p2.hasLost()) {
                try {
                    Thread.sleep(1000);
                    if (!isPaused) {
                        if (pendingDetonation != null) {
                            // explode the merged piece player's block...
                            Player explodedWinner = pendingDetonation.winner;
                            pendingDetonation.explode();
                            pendingDetonation = null;
                            // ...but still let the other player drop normally
                            if (explodedWinner.getPlayerId() == 1) {
                                p2.drop();
                            } else {
                                p1.drop();
                            }
                        } else {
                            // normal tick: both players drop
                            p1.drop();
                            p2.drop();
                            if (wackyMode)
                                checkMerge();
                        }
                        repaint();
                    }
                } catch (InterruptedException ignored) {
                    // if we ever interrupt this thread, bail out
                    break;
                }
            }
            // Only play win�?music if we actually ran to completion
            if (runningGameLoop && (p1.hasLost() || p2.hasLost())) {
                stopAllMergeLoops();
                playSound("sfx/win.wav", false);
                SwingUtilities.invokeLater(() -> restartButton.setVisible(true));
            }
        });
        gameLoopThread.start();
    }

    // Completely stops the game
    public void stopGame() {
        // drop out of the loop
        runningGameLoop = false;
        // interrupt the thread in case it’s sleeping
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
            gameLoopThread = null;
        }
        // stop the repaint timer and any merge�?sound loops
        repaintTimer.stop();
        stopAllMergeLoops();
    }

    /**
     * If any tile of p1 and p2 overlap, merge them.
     * The other player takes control,
     * and the loser immediately gets a fresh piece.
     */
    private void checkMerge() {
        List<Point> t1 = p1.getCurrentTiles();
        List<Point> t2 = p2.getCurrentTiles();

        // exact overlap?
        Set<Point> overlap = new HashSet<>(t1);
        overlap.retainAll(t2);
        if (!overlap.isEmpty()) {
            doMerge(t1, t2, overlap.iterator().next());
            return;
        }

        // 2) adjacency (manhattan)
        for (Point a : t1) {
            for (Point b : t2) {
                if (Math.abs(a.x - b.x) + Math.abs(a.y - b.y) == 1) {
                    doMerge(t1, t2, a);
                    return;
                }
            }
        }
    }

    /**
     * Perform the actual merge of two tile lists t1 and t2,
     * union them, reassign control to the opposite side player,
     * and give the loser a fresh piece.
     */
    private void doMerge(List<Point> t1, List<Point> t2, Point contact) {
        // union
        Set<Point> merged = new HashSet<>(t1);
        merged.addAll(t2);

        // who wins
        int mid = numRows / 2;
        boolean inBottom = contact.y >= mid;
        Player winner = inBottom ? p2 : p1;
        Player loser = (winner == p1) ? p2 : p1;

        // build the new piece (tiles + pivot)
        Tetramino m = new Tetramino();
        m.clearTiles();
        merged.forEach(pt -> m.addTile(new Point(pt)));
        m.setPivot(computePivot(merged));

        int old1 = p1.getCurrentPiece().getMergeCount();
        int old2 = p2.getCurrentPiece().getMergeCount();
        int newCount = Math.max(old1, old2) + 1;

        if (newCount >= MAX_MERGES) {
            // hand off the final merged piece
            m.setMergeCount(newCount);
            winner.setCurrentPiece(m);
            loser.newPiece();

            // schedule a detonation for the next tick
            pendingDetonation = new PendingDetonation(contact, winner);
            return;
        } else {
            // normal hand‑off
            m.setMergeCount(newCount); // set PROPAGATED count
            winner.setCurrentPiece(m);
            loser.newPiece();
            startMergeLoop(winner.getPlayerId(), newCount);
        }
    }

    private void triggerExplosion(Point contact) {
        // record explosion in tilespace
        explosions.add(new Explosion(contact.x, contact.y));
        // flash board
        flash = true;
        repaint();
        new Timer(100, ev -> {
            flash = false;
            repaint();
            ((Timer) ev.getSource()).stop();
        }).start();
    }

    private static class Explosion {
        final int tileX, tileY;
        final long start = System.currentTimeMillis();

        Explosion(int tileX, int tileY) {
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }

    private class PendingDetonation {
        final Point contact;
        final Player winner;

        PendingDetonation(Point contact, Player winner) {
            this.contact = contact;
            this.winner = winner;
        }

        void explode() {
            // prevent the looped merge sound from continuing
            stopAllMergeLoops();

            // play one‑shot explosion
            playSound("sfx/merge_explode.wav", false);

            // remove the tiles
            board.getCell(contact.x, contact.y).setState(Cell.State.EMPTY);
            triggerExplosion(contact);

            // penalty & fresh piece
            winner.addScore(-DETONATION_PENALTY);
            winner.newPiece();
        }

    }

    public void playSound(String resourcePath, boolean loop) {
        new Thread(() -> {
            try {
                URL url = getClass().getResource(resourcePath);
                if (url == null) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                AudioInputStream ais = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                if (loop)
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                else
                    clip.start();
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

    /** Stop any existing loop for player, then loop mergeN.wav */
    private void startMergeLoop(int playerId, int mergeCount) {
        stopAllMergeLoops();
        String fname = "sfx/merge" + mergeCount + ".wav";
        try {
            URL url = getClass().getResource(fname);
            if (url == null) {
                System.err.println("!! " + fname + " not found");
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            if (playerId == 1)
                p1MergeClip = clip;
            else
                p2MergeClip = clip;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Stop & close the merge loop for that player */
    private void stopMergeLoop(int playerId) {
        Clip clip = (playerId == 1 ? p1MergeClip : p2MergeClip);
        if (clip != null) {
            clip.stop();
            clip.close();
        }
        if (playerId == 1)
            p1MergeClip = null;
        else
            p2MergeClip = null;
    }

    public void stopAllMergeLoops() {
        stopMergeLoop(1);
        stopMergeLoop(2);
    }

    /**
     * Draw both HOLD box and the next‑queue for player p,
     * at screen coords (x,y). If flipVert is true, draw the
     * next list downward (for the upside‑down player).
     */
    private void drawHoldAndNext(Graphics2D g, Player p, int x, int y, boolean flipVert) {
        int holdPs = previewSize; // size of the HOLD box
        int inset = holdPs / 10; // 10% inset for a slightly shrunken preview

        // draw hold box
        g.setColor(Color.BLACK);
        g.fillRect(x, y, holdPs, holdPs);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, holdPs, holdPs);

        // draw hold label
        String label = "HOLD";
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(label);
        int textH = fm.getAscent();
        if (flipVert) {
            // left side: above box, left‑justified
            g.drawString(label, x + 4, y - 4);
        } else {
            // right side: below box, right‑justified
            int pad = 4;
            int tx = x + holdPs - pad - textW;
            int ty = y + holdPs + textH + 2;
            g.drawString(label, tx, ty);
        }

        // draw held piece
        Tetramino held = p.getHoldPiece();
        if (held != null) {
            Tetramino ph = new Tetramino();
            ph.generateNewPiece(held.getShapeID(), new Point(0, 0));
            ph.setMergeCount(held.getMergeCount());

            List<Color> base = p.getTetraminoColors();
            List<Color> pal = p.hasHoldUsed() ? toGrayscale(base) : base;

            int ss = holdPs - 2 * inset;
            int cell = ss / 4;
            int fudge = cell / 2;
            int extraX = (ph.getShapeID() == 1 ? 0 : fudge);

            drawTetraminoPreview(
                    g, ph, pal,
                    x + inset + extraX,
                    y + inset,
                    ss);
        }

        // set up queue
        int nextPs = holdPs / 2;
        int gap = 4;
        int padV = 8; // extra vertical padding
        List<Integer> next = p.peekNext(5);
        int count = next.size();

        int boxW = nextPs;
        int boxH = count * nextPs + (count - 1) * gap + padV * 2;

        int boxX = flipVert ? x + (holdPs - boxW) : x;
        int boxY = flipVert
                ? y + holdPs + gap - padV
                : y - gap - boxH + padV;

        int nudge = 12;
        if (flipVert) {
            boxY += nudge; // left: push down
        } else {
            boxY -= nudge; // right: push up
        }

        // draw background & border
        g.setColor(Color.BLACK);
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(Color.WHITE);
        g.drawRect(boxX, boxY, boxW, boxH);

        int cell = nextPs / 4;
        int padH = (nextPs - cell * 4) / 2;
        int fudge = cell / 2;

        for (int i = 0; i < count; i++) {
            Tetramino t = new Tetramino();
            t.generateNewPiece(next.get(i), new Point(0, 0));

            int sliceY = flipVert
                    ? boxY + padV + i * (nextPs + gap)
                    : boxY + padV + (count - 1 - i) * (nextPs + gap);

            int extraX = (t.getShapeID() == 1 ? 0 : fudge);

            drawTetraminoPreview(
                    g, t, p.getTetraminoColors(),
                    boxX + padH + extraX,
                    sliceY + padH,
                    nextPs - 2 * padH);
        }
    }

    private List<Color> toGrayscale(List<Color> src) {
        List<Color> gray = new ArrayList<>(src.size());
        for (Color c : src) {
            int l = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
            gray.add(new Color(l, l, l));
        }
        return gray;
    }

    /**
     * Draw a little 4×4 preview of Tetramino t in a box of size ps,
     * perfectly centering its bounding box.
     */
    private void drawTetraminoPreview(Graphics2D g, Tetramino t, List<Color> palette,
            int px, int py, int ps) {
        int cell = ps / 4;

        // find the shape's bounding box in cell coordinates
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Point p : t.getTiles()) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        int shapeW = maxX - minX + 1; // how many cols the shape actually occupies
        int shapeH = maxY - minY + 1; // how many rows

        // compute how much to shift the shape so it's centered
        int offsetX = (4 - shapeW) / 2 - minX;
        int offsetY = (4 - shapeH) / 2 - minY;

        // pad so the 4×4 grid itself is centered inside the ps×ps box
        int pad = (ps - cell * 4) / 2;

        // draw each tile with both padding and centering offsets
        g.setColor(palette.get(t.getShapeID()));
        for (Point p : t.getTiles()) {
            int cx = px + pad + (p.x + offsetX) * cell;
            int cy = py + pad + (p.y + offsetY) * cell;
            g.fillRect(cx, cy, cell - 1, cell - 1);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        // give equal margin on left & right for both players’ UIs,
        // plus SIDE_PADDING so the hold boxes never touch the edge
        int side = previewSize + 20 + SIDE_PADDING;
        // bump bottom margin a bit to make room for controls
        return new Dimension(
                boardWidth + side * 2,
                boardHeight + 150);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint full panel gradient background
        Graphics2D g0 = (Graphics2D) g;
        g0.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint fullGrad = new GradientPaint(
                0, 0, Color.DARK_GRAY,
                0, getHeight(), Color.BLACK);
        g0.setPaint(fullGrad);
        g0.fillRect(0, 0, getWidth(), getHeight());

        // Figure out our natural unscaled size
        Dimension natural = getPreferredSize();
        int natW = natural.width, natH = natural.height;

        // Get actual component size
        int compW = getWidth(), compH = getHeight();

        // Compute uniform scale for fullscreen
        double scale = 1.0;
        if (isFullscreen) {
            scale = Math.min(compW / (double) natW,
                    compH / (double) natH);
        }

        // Compute translation so scaled content is centered
        double tx = (compW / scale - natW) / 2.0;
        double ty = (compH / scale - natH) / 2.0;

        // Apply scale + translate on a fresh Graphics2D
        Graphics2D g2 = (Graphics2D) g0.create();
        g2.scale(scale, scale);
        g2.translate(tx, ty);

        // Draw the board & grid in natural coords
        int offsetX = (natW - boardWidth) / 2;
        int offsetY = (natH - boardHeight) / 2;

        // Board backdrop
        g2.setColor(Color.BLACK);
        g2.fillRect(offsetX, offsetY, boardWidth, boardHeight);
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(offsetX, offsetY, boardWidth, boardHeight);

        // Grid lines
        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(1));
        for (int cx = 1; cx < numCols; cx++) {
            int x = offsetX + cx * tileSize;
            g2.drawLine(x, offsetY, x, offsetY + boardHeight);
        }
        for (int cy = 1; cy < numRows; cy++) {
            int y = offsetY + cy * tileSize;
            g2.drawLine(offsetX, y, offsetX + boardWidth, y);
        }

        // Flash effect
        if (flash) {
            g2.setColor(new Color(15, 15, 15));
            g2.fillRect(offsetX, offsetY, boardWidth, boardHeight);
        }

        // Fixed cells
        for (int x = 0; x < numCols; x++) {
            for (int y = 0; y < numRows; y++) {
                Cell c = board.getCell(x, y);
                if (c.getState() != Cell.State.FIXED)
                    continue;

                Color col;
                int m = c.getMergeCount();
                if (m == 0) {
                    col = (c.getOwnerId() == 1)
                            ? p1.getTetraminoColors().get(c.getShapeId())
                            : p2.getTetraminoColors().get(c.getShapeId());
                } else if (m <= 3) {
                    switch (m) {
                        case 1:
                            col = new Color(0xAA, 0x00, 0xAA);
                            break;
                        case 2:
                            col = Color.YELLOW;
                            break;
                        default:
                            col = Color.RED;
                            break;
                    }
                } else {
                    col = Color.RED;
                }
                g2.setColor(col);
                g2.fillRect(
                        offsetX + x * tileSize,
                        offsetY + y * tileSize,
                        tileSize - 1,
                        tileSize - 1);
            }
        }

        // Center dashed line
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(
                3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10, new float[] { 8, 8 }, 0));
        g2.setColor(Color.LIGHT_GRAY);
        int lineY = offsetY + boardHeight / 2;
        g2.drawLine(offsetX, lineY, offsetX + boardWidth, lineY);
        g2.setStroke(old);

        // Scores
        int boxW = 100;
        int boxH = 36;
        int pad = 12;
        int minGap = tileSize; // require at least one tile width between boxes

        int boxY = offsetY - boxH - pad;

        // initial positions: flush to the sides of the board
        int p1BoxX = offsetX;
        int p2BoxX = offsetX + boardWidth - boxW;

        // if they would come too close, re‑center them with the required gap
        if (p2BoxX - (p1BoxX + boxW) < minGap) {
            int center = offsetX + boardWidth / 2;
            // split the gap evenly around center
            p1BoxX = center - boxW - minGap / 2;
            p2BoxX = center + minGap / 2;
        }

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(p1BoxX, boxY, boxW, boxH, 8, 8);
        g2.fillRoundRect(p2BoxX, boxY, boxW, boxH, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("P1: " + p1.getScore(), p1BoxX + 10, boxY + 24);
        g2.drawString("P2: " + p2.getScore(), p2BoxX + 10, boxY + 24);
        // Explosions
        long now = System.currentTimeMillis();
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion ex = it.next();
            float t = (now - ex.start) / 500f;
            if (t >= 1f) {
                it.remove();
                continue;
            }
            int r = (int) (t * boardWidth);
            int alpha = (int) ((1 - t) * 255);
            int cx = offsetX + ex.tileX * tileSize + tileSize / 2;
            int cy = offsetY + ex.tileY * tileSize + tileSize / 2;
            g2.setColor(new Color(255, 200, 0, alpha));
            g2.setStroke(new BasicStroke((1 - t) * 8));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Hold & Next previews
        int previewSize = 4 * tileSize;
        {
            int p1X = offsetX - previewSize - 20;
            int p1Y = offsetY;
            drawHoldAndNext(g2, p1, p1X, p1Y, true);
        }
        {
            int p2X = offsetX + boardWidth + 20;
            int p2Y = offsetY + boardHeight - previewSize;
            drawHoldAndNext(g2, p2, p2X, p2Y, false);
        }

        // Falling pieces & ghosts — compute each player’s color once, then draw both
        // ghost+piece in that color
        AffineTransform save = g2.getTransform();
        g2.translate(offsetX, offsetY);

        // Player 1
        Color c1 = p1.getCurrentColor();
        p1.drawGhost(g2, c1);
        p1.drawPiece(g2, c1);

        // Player 2
        Color c2 = p2.getCurrentColor();
        p2.drawGhost(g2, c2);
        p2.drawPiece(g2, c2);

        // draw each falling piece with conditional alpha
        float farAlpha = 0.5f, nearAlpha = 1.0f;
        int midRow = numRows / 2;

        List<Point> tiles1 = p1.getCurrentTiles();
        Color col1 = p1.getTetraminoColors()
                .get(p1.getCurrentPiece().getShapeID());
        for (Point pt : tiles1) {
            float a = (pt.y < midRow) ? farAlpha : nearAlpha;
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, a));
            g2.setColor(col1);
            g2.fillRect(
                    pt.x * tileSize,
                    pt.y * tileSize,
                    tileSize - 1, tileSize - 1);
        }

        List<Point> tiles2 = p2.getCurrentTiles();
        Color col2 = p2.getTetraminoColors()
                .get(p2.getCurrentPiece().getShapeID());
        for (Point pt : tiles2) {
            float a = (pt.y > midRow) ? farAlpha : nearAlpha;
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, a));
            g2.setColor(col2);
            g2.fillRect(
                    pt.x * tileSize,
                    pt.y * tileSize,
                    tileSize - 1, tileSize - 1);
        }

        // restore full opacity & original transform
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));
        g2.setTransform(save);

        // Game over message
        if (p1.hasLost() || p2.hasLost()) {
            String msg = p1.hasLost() ? "Player 2 Wins!" : "Player 1 Wins!";
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2.getFontMetrics();
            int mw = fm.stringWidth(msg), mh = fm.getHeight();
            int mx = (natW - mw) / 2, my = (natH - mh) / 2 + fm.getAscent();
            g2.setColor(new Color(255, 0, 0, 200));
            g2.drawString(msg, mx, my);
        }

        if (isPaused && !p1.hasLost() && !p2.hasLost()) {
            String pauseMsg = "Paused";
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics pfm = g2.getFontMetrics();
            int pmw = pfm.stringWidth(pauseMsg);
            int pmh = pfm.getHeight();
            int pmx = (natW - pmw) / 2;
            int pmy = (natH - pmh) / 2 + pfm.getAscent();
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(pauseMsg, pmx, pmy);
        }

        g2.dispose();
    }

    /** Compute center of mass pivot for a merged shape. */
    private Point computePivot(Set<Point> pts) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Point p : pts) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        // center
        return new Point((minX + maxX) / 2, (minY + maxY) / 2);
    }

    // DAS Handler
    private class DASHandler {
        private final int dx;
        private final Player player;
        private Timer dasTimer, arrTimer;
        private boolean held = false; // <- track actual press state

        DASHandler(int dx, Player player) {
            this.dx = dx;
            this.player = player;
        }

        void keyDown() {
            if (p1.hasLost() || p2.hasLost())
                return;
            // ignore repeats until a keyUp()
            if (held)
                return;
            held = true;

            // one immediate move
            player.move(dx);
            Tetris.this.repaint();

            // start DAS delay
            dasTimer = new Timer(200, ev -> {
                // after DAS delay, start ARR if still held
                if (held)
                    startARR();
                dasTimer = null;
            });
            dasTimer.setRepeats(false);
            dasTimer.start();
        }

        void keyUp() {
            // clear state & timers
            held = false;
            if (dasTimer != null) {
                dasTimer.stop();
                dasTimer = null;
            }
            if (arrTimer != null) {
                arrTimer.stop();
                arrTimer = null;
            }
        }

        private void startARR() {
            // auto repeat at fixed rate while held
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
}