package src;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainMenu extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField widthField;
    private final JTextField heightField;
    private final JPanel cards;
    private final CardLayout cardLayout;

    public MainMenu() {
        super("Battle Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // CardLayout container
        cardLayout = new CardLayout();
        cards     = new JPanel(cardLayout);
        add(cards);

        // Build menu panel
        MenuPanel menu = new MenuPanel();
        cards.add(menu, "MENU");

        // Grab references to the text fields
        this.widthField  = menu.getWidthField();
        this.heightField = menu.getHeightField();

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startTetris(boolean wacky) {
        int cols, rows;
        try {
            cols = Integer.parseInt(widthField.getText().trim());
            rows = Integer.parseInt(heightField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid integers for width and height.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // final clamp
        cols = Math.max(5, Math.min(50, cols));
        rows = Math.max(10, Math.min(50, rows));
        widthField.setText("" + cols);
        heightField.setText("" + rows);

        Tetris game = new Tetris(cols, rows, wacky);
        if (cards.getComponentCount() > 1) {
            cards.remove(1);
        }
        cards.add(game, "GAME");
        cardLayout.show(cards, "GAME");

        pack();
        game.requestFocusInWindow();
        game.startGameLoop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }

    private class MenuPanel extends JPanel {
        private final JTextField widthField  = new JTextField("10", 3);
        private final JTextField heightField = new JTextField("23", 3);

        MenuPanel() {
            super(new BorderLayout());
            setPreferredSize(new Dimension(500, 650));
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setOpaque(false);

            // Top: Logo
            JLabel logoLabel = new JLabel();
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            try {
                BufferedImage logo = ImageIO.read(getClass().getResource("img/logo.png"));
                int maxW = 400, maxH = 200;
                double scale = Math.min((double)maxW / logo.getWidth(),
                                        (double)maxH / logo.getHeight());
                Image scaled = logo.getScaledInstance(
                    (int)(logo.getWidth()*scale),
                    (int)(logo.getHeight()*scale),
                    Image.SCALE_SMOOTH
                );
                logoLabel.setIcon(new ImageIcon(scaled));
            } catch (IOException|IllegalArgumentException e) {
                logoLabel.setText("BATTLE TETRIS");
                logoLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
                logoLabel.setForeground(Color.WHITE);
            }
            add(logoLabel, BorderLayout.NORTH);

            // Center: form
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(12, 12, 12, 12);
            gbc.fill  = GridBagConstraints.HORIZONTAL;

            // Columns row
            gbc.gridx = 0; gbc.gridy = 0;
            JLabel wLabel = new JLabel("\u25B6 Columns:");
            styleLabel(wLabel);
            form.add(wLabel, gbc);

            gbc.gridx = 1;
            form.add(makeAdjustPanel(widthField, 5, 50), gbc);

            // Rows row
            gbc.gridx = 0; gbc.gridy = 1;
            JLabel hLabel = new JLabel("\u25B6 Rows:");
            styleLabel(hLabel);
            form.add(hLabel, gbc);

            gbc.gridx = 1;
            form.add(makeAdjustPanel(heightField, 10, 50), gbc);

            // Buttons row
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JPanel btnRow = new JPanel(new GridLayout(1,2, 20, 0));
            btnRow.setOpaque(false);
            btnRow.add(makeMenuButton("Normal", e -> startTetris(false)));
            btnRow.add(makeMenuButton("Wacky",  e -> startTetris(true)));
            form.add(btnRow, gbc);

            add(form, BorderLayout.CENTER);

            // Footer
            JLabel footer = new JLabel("© Ahmed Mansour | 2024 VARLAB Game Jam", SwingConstants.CENTER);
            footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
            footer.setForeground(Color.LIGHT_GRAY);
            add(footer, BorderLayout.SOUTH);
        }

        JTextField getWidthField()  { return widthField; }
        JTextField getHeightField() { return heightField; }

        @Override
        protected void paintComponent(Graphics g) {
            // gradient background
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(20,20,20),
                0, getHeight(), new Color(60,60,60)
            );
            g2.setPaint(gp);
            g2.fillRect(0,0,getWidth(),getHeight());
            super.paintComponent(g);
        }

        private void styleLabel(JLabel lbl) {
            lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
            lbl.setForeground(Color.WHITE);
        }

        private JPanel makeAdjustPanel(JTextField field, int min, int max) {
            // style the text field
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setFont(new Font("SansSerif", Font.PLAIN, 16));
            field.setBackground(new Color(40,40,40));
            field.setForeground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            field.setCaretColor(Color.WHITE);

            // enforce on focus lost
            field.setInputVerifier(new InputVerifier() {
                @Override public boolean verify(JComponent c) {
                    try {
                        int v = Integer.parseInt(field.getText().trim());
                        if (v < min) v = min;
                        if (v > max) v = max;
                        field.setText(""+v);
                    } catch (NumberFormatException ex) {
                        field.setText(""+min);
                    }
                    return true;
                }
            });

            // buttons
            JButton minus = new JButton("-");
            JButton plus  = new JButton("+");
            for (JButton b : new JButton[]{minus, plus}) {
                b.setFont(new Font("SansSerif", Font.BOLD, 16));
                b.setForeground(Color.WHITE);
                b.setBackground(new Color(80,80,160));
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder(4,12,4,12));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){ b.setBackground(new Color(100,100,200)); }
                    public void mouseExited(MouseEvent e){  b.setBackground(new Color(80,80,160)); }
                });
            }

            minus.addActionListener(e -> {
                int v = Integer.parseInt(field.getText().trim());
                if (v > min) field.setText(""+(v-1));
            });
            plus.addActionListener(e -> {
                int v = Integer.parseInt(field.getText().trim());
                if (v < max) field.setText(""+(v+1));
            });

            JPanel panel = new JPanel(new BorderLayout(4,0));
            panel.setOpaque(false);
            panel.add(minus, BorderLayout.WEST);
            panel.add(field, BorderLayout.CENTER);
            panel.add(plus,  BorderLayout.EAST);
            return panel;
        }

        private JButton makeMenuButton(String text, ActionListener al) {
            JButton b = new JButton(text);
            b.setFont(new Font("SansSerif", Font.BOLD, 20));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(80, 80, 160));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(al);
            b.addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ b.setBackground(new Color(100,100,200)); }
                public void mouseExited(MouseEvent e){  b.setBackground(new Color(80,80,160)); }
            });
            return b;
        }
    }
}
