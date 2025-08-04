// ClientUI.java
// UCID: ad273, Date: 2025-08-04
// Swing UI for the client application

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUI extends JFrame {
    private Client client;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Connection Panel
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    
    // Ready Panel
    private JButton readyButton;
    private JTextArea readyMessages;
    
    // Game Panel
    private JTextArea gameMessages;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JTextField wordGuessInput;
    private JPanel letterButtonsPanel;
    private JButton[] letterButtons;
    private JLabel wordBlanksLabel;
    private JLabel strikeLabel;
    private JLabel currentPlayerLabel;
    private JLabel timerLabel;
    private JPanel hangmanPanel;
    private JList<String> playerList;
    private DefaultListModel<String> playerListModel;
    private JButton skipButton;
    private JButton awayButton;
    private JButton spectatorButton;
    
    // Game state
    private GameStatePayload currentGameState;
    private Map<Long, Integer> playerPoints = new HashMap<>();
    private boolean isAway = false;
    private boolean isSpectator = false;
    
    public ClientUI(Client client) {
        this.client = client;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Hangman Game Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        createConnectionPanel();
        createReadyPanel();
        createGamePanel();
        
        add(mainPanel);
        setVisible(true);
    }
    
    private void createConnectionPanel() {
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel titleLabel = new JLabel("Connect to Hangman Server");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        connectionPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0;
        connectionPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        connectionPanel.add(usernameField, gbc);
        
        // Host
        gbc.gridy++;
        gbc.gridx = 0;
        connectionPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField("localhost", 15);
        connectionPanel.add(hostField, gbc);
        
        // Port
        gbc.gridy++;
        gbc.gridx = 0;
        connectionPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("3000", 15);
        connectionPanel.add(portField, gbc);
        
        // Connect button
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> attemptConnection());
        connectionPanel.add(connectButton, gbc);
        
        mainPanel.add(connectionPanel, "CONNECTION");
    }
    
    private void createReadyPanel() {
        JPanel readyPanel = new JPanel(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Waiting for Players", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        readyPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Messages area
        readyMessages = new JTextArea(10, 40);
        readyMessages.setEditable(false);
        readyMessages.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(readyMessages);
        readyPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ready button
        JPanel buttonPanel = new JPanel();
        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            client.sendReady();
            readyButton.setText(readyButton.getText().equals("Ready") ? "Not Ready" : "Ready");
        });
        buttonPanel.add(readyButton);
        readyPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(readyPanel, "READY");
    }
    
    private void createGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        
        // Left panel - Player list and game info
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        
        // Player list
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setCellRenderer(new PlayerListCellRenderer());
        JScrollPane playerScrollPane = new JScrollPane(playerList);
        playerScrollPane.setBorder(BorderFactory.createTitledBorder("Players"));
        leftPanel.add(playerScrollPane, BorderLayout.CENTER);
        
        // Game controls
        JPanel controlsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        skipButton = new JButton("Skip Turn");
        skipButton.addActionListener(e -> client.sendSkip());
        
        awayButton = new JButton("Go Away");
        awayButton.addActionListener(e -> {
            isAway = !isAway;
            client.sendAway(isAway);
            awayButton.setText(isAway ? "Come Back" : "Go Away");
        });
        
        spectatorButton = new JButton("Spectate");
        spectatorButton.addActionListener(e -> {
            isSpectator = !isSpectator;
            client.sendSpectator(isSpectator);
            spectatorButton.setText(isSpectator ? "Join Game" : "Spectate");
        });
        
        controlsPanel.add(skipButton);
        controlsPanel.add(awayButton);
        controlsPanel.add(spectatorButton);
        leftPanel.add(controlsPanel, BorderLayout.SOUTH);
        
        gamePanel.add(leftPanel, BorderLayout.WEST);
        
        // Center panel - Game area
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Game info panel
        JPanel gameInfoPanel = new JPanel(new GridLayout(4, 1));
        currentPlayerLabel = new JLabel("Current Player: None");
        wordBlanksLabel = new JLabel("Word: ");
        strikeLabel = new JLabel("Strikes: 0/6");
        timerLabel = new JLabel("Time: 30");
        
        gameInfoPanel.add(currentPlayerLabel);
        gameInfoPanel.add(wordBlanksLabel);
        gameInfoPanel.add(strikeLabel);
        gameInfoPanel.add(timerLabel);
        centerPanel.add(gameInfoPanel, BorderLayout.NORTH);
        
        // Hangman visualization
        hangmanPanel = new HangmanPanel();
        hangmanPanel.setPreferredSize(new Dimension(200, 200));
        hangmanPanel.setBorder(BorderFactory.createTitledBorder("Hangman"));
        centerPanel.add(hangmanPanel, BorderLayout.CENTER);
        
        // Letter buttons
        letterButtonsPanel = new JPanel(new GridLayout(3, 9, 2, 2));
        letterButtons = new JButton[26];
        for (int i = 0; i < 26; i++) {
            char letter = (char) ('A' + i);
            letterButtons[i] = new JButton(String.valueOf(letter));
            final String letterStr = String.valueOf(letter);
            letterButtons[i].addActionListener(e -> client.sendGuessLetter(letterStr));
            letterButtonsPanel.add(letterButtons[i]);
        }
        centerPanel.add(letterButtonsPanel, BorderLayout.SOUTH);
        
        gamePanel.add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - Messages and input
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));
        
        // Game messages
        gameMessages = new JTextArea(15, 20);
        gameMessages.setEditable(false);
        gameMessages.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane gameScrollPane = new JScrollPane(gameMessages);
        gameScrollPane.setBorder(BorderFactory.createTitledBorder("Game Events"));
        
        // Chat area
        chatArea = new JTextArea(10, 20);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        // Chat input
        chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            if (!chatInput.getText().trim().isEmpty()) {
                client.sendMessage(chatInput.getText().trim());
                chatInput.setText("");
            }
        });
        
        // Word guess input
        wordGuessInput = new JTextField();
        wordGuessInput.addActionListener(e -> {
            if (!wordGuessInput.getText().trim().isEmpty()) {
                client.sendGuessWord(wordGuessInput.getText().trim());
                wordGuessInput.setText("");
            }
        });
        
        JButton sendChatButton = new JButton("Send Chat");
        sendChatButton.addActionListener(e -> {
            if (!chatInput.getText().trim().isEmpty()) {
                client.sendMessage(chatInput.getText().trim());
                chatInput.setText("");
            }
        });
        
        JButton guessWordButton = new JButton("Guess Word");
        guessWordButton.addActionListener(e -> {
            if (!wordGuessInput.getText().trim().isEmpty()) {
                client.sendGuessWord(wordGuessInput.getText().trim());
                wordGuessInput.setText("");
            }
        });
        
        inputPanel.add(chatInput);
        inputPanel.add(sendChatButton);
        inputPanel.add(wordGuessInput);
        inputPanel.add(guessWordButton);
        
        // Arrange right panel
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameScrollPane, chatScrollPane);
        rightSplitPane.setDividerLocation(200);
        rightPanel.add(rightSplitPane, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        
        gamePanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(gamePanel, "GAME");
    }
    
    public void showConnectionPanel() {
        cardLayout.show(mainPanel, "CONNECTION");
    }
    
    public void showReadyPanel() {
        cardLayout.show(mainPanel, "READY");
    }
    
    public void showGamePanel() {
        cardLayout.show(mainPanel, "GAME");
    }
    
    private void attemptConnection() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showMessage("Please enter a username");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            if (client.connect(host, port, username)) {
                showMessage("Connected successfully!");
            } else {
                showMessage("Failed to connect to server");
            }
        } catch (NumberFormatException e) {
            showMessage("Invalid port number");
        }
    }
    
    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (gameMessages != null) {
                gameMessages.append(message + "\n");
                gameMessages.setCaretPosition(gameMessages.getDocument().getLength());
            }
            if (readyMessages != null) {
                readyMessages.append(message + "\n");
                readyMessages.setCaretPosition(readyMessages.getDocument().getLength());
            }
            if (chatArea != null && !message.startsWith("Game:")) {
                chatArea.append(message + "\n");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });
    }
    
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    public void updateGameState(GameStatePayload gameState) {
        this.currentGameState = gameState;
        
        if (gameState.isGameActive()) {
            showGamePanel();
        }
        
        // Update word blanks
        if (wordBlanksLabel != null) {
            wordBlanksLabel.setText("Word: " + gameState.getWordBlanks());
        }
        
        // Update strikes
        if (strikeLabel != null) {
            strikeLabel.setText("Strikes: " + gameState.getStrikes() + "/" + gameState.getMaxStrikes());
        }
        
        // Update timer
        if (timerLabel != null) {
            timerLabel.setText("Time: " + gameState.getTurnTimeRemaining());
        }
        
        // Update hangman visualization
        if (hangmanPanel != null) {
            ((HangmanPanel) hangmanPanel).setStrikes(gameState.getStrikes());
            hangmanPanel.repaint();
        }
        
        // Update letter buttons
        if (letterButtons != null && gameState.getGuessedLetters() != null) {
            for (int i = 0; i < letterButtons.length; i++) {
                char letter = (char) ('A' + i);
                letterButtons[i].setEnabled(!gameState.getGuessedLetters().contains(letter));
            }
        }
        
        // Update current player
        if (currentPlayerLabel != null) {
            // This would need player name resolution, simplified for now
            currentPlayerLabel.setText("Current Player ID: " + gameState.getCurrentPlayerId());
        }
    }
    
    public void updatePoints(PointsPayload pointsPayload) {
        playerPoints.put(pointsPayload.getPlayerId(), pointsPayload.getPoints());
        updatePlayerList();
    }
    
    private void updatePlayerList() {
        // This is simplified - in a full implementation, you'd track player names and states
        playerListModel.clear();
        for (Map.Entry<Long, Integer> entry : playerPoints.entrySet()) {
            playerListModel.addElement("Player " + entry.getKey() + ": " + entry.getValue() + " pts");
        }
    }
    
    public void showScoreboard(String scoreboard) {
        JDialog scoreDialog = new JDialog(this, "Scoreboard", true);
        JTextArea scoreArea = new JTextArea(scoreboard);
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        scoreDialog.add(new JScrollPane(scoreArea));
        scoreDialog.setSize(300, 200);
        scoreDialog.setLocationRelativeTo(this);
        scoreDialog.setVisible(true);
    }
    
    // Custom cell renderer for player list
    private class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            // Color coding for different player states could be added here
            return this;
        }
    }
    
    // Custom panel for hangman visualization
    private class HangmanPanel extends JPanel {
        private int strikes = 0;
        
        public void setStrikes(int strikes) {
            this.strikes = strikes;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.BLACK);
            
            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            
            // Base
            if (strikes >= 1) {
                g2d.drawLine(centerX - 40, centerY + 60, centerX + 40, centerY + 60);
            }
            
            // Pole
            if (strikes >= 2) {
                g2d.drawLine(centerX - 20, centerY + 60, centerX - 20, centerY - 60);
            }
            
            // Top beam
            if (strikes >= 3) {
                g2d.drawLine(centerX - 20, centerY - 60, centerX + 20, centerY - 60);
            }
            
            // Noose
            if (strikes >= 4) {
                g2d.drawLine(centerX + 20, centerY - 60, centerX + 20, centerY - 40);
            }
            
            // Head
            if (strikes >= 5) {
                g2d.drawOval(centerX + 10, centerY - 40, 20, 20);
            }
            
            // Body
            if (strikes >= 6) {
                g2d.drawLine(centerX + 20, centerY - 20, centerX + 20, centerY + 20);
                // Arms
                g2d.drawLine(centerX + 20, centerY - 10, centerX + 10, centerY - 5);
                g2d.drawLine(centerX + 20, centerY - 10, centerX + 30, centerY - 5);
                // Legs
                g2d.drawLine(centerX + 20, centerY + 20, centerX + 10, centerY + 35);
                g2d.drawLine(centerX + 20, centerY + 20, centerX + 30, centerY + 35);
            }
        }
    }
}
