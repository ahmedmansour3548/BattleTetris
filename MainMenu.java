// MainMenu.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField widthField;
    private final JTextField heightField;

    public MainMenu() {
        super("Battle Tetris Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.DARK_GRAY);

        // Title
        JLabel title = new JLabel("BATTLE TETRIS", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 72));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Center panel for inputs & buttons
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Input labels & fields
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel wLabel = new JLabel("Width:");
        wLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        wLabel.setForeground(Color.WHITE);
        center.add(wLabel, gbc);

        widthField = new JTextField("12", 4);
        widthField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        center.add(widthField, gbc.gridx = 1);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel hLabel = new JLabel("Height:");
        hLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        hLabel.setForeground(Color.WHITE);
        center.add(hLabel, gbc);

        heightField = new JTextField("23", 4);
        heightField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        center.add(heightField, gbc.gridx = 1);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton normalBtn = new JButton("Start Normal");
        normalBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        normalBtn.addActionListener(e -> startTetris(false));
        center.add(normalBtn, gbc);

        gbc.gridy = 3;
        JButton wackyBtn = new JButton("Start Wacky");
        wackyBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        wackyBtn.addActionListener(e -> startTetris(true));
        center.add(wackyBtn, gbc);

        add(center, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("Made by Ahmed Mansour — VARLAB 2024 Game Jam", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 14));
        footer.setForeground(Color.LIGHT_GRAY);
        add(footer, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Launches either the normal or wacky Tetris variant in a new window.
     * Hides this menu.
     */
    private void startTetris(boolean wacky) {
        int cols, rows;
        try {
            cols = Integer.parseInt(widthField.getText());
            rows = Integer.parseInt(heightField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid integers for width and height.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hide menu
        setVisible(false);

        // Create the game panel
        Tetris game = new Tetris(cols, rows);

        JFrame gameFrame = new JFrame(wacky ? "Wacky Tetris" : "Normal Tetris");
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.add(game);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        game.startGameLoop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}
