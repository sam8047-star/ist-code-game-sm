import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.net.URL;

public class SpaceGame extends JFrame implements KeyListener {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;

    private static final int OBSTACLE_WIDTH = 50;
    private static final int OBSTACLE_HEIGHT = 50;

    private static final int PROJECTILE_WIDTH = 8;
    private static final int PROJECTILE_HEIGHT = 18;

    private static final int HEALTH_POWERUP_SIZE = 30;

    private static final int PLAYER_SPEED = 7;
    private int obstacleSpeed = 3;
    private static final int PROJECTILE_SPEED = 12;

    private int score = 0;
    private int health = 100;
    private int level = 1;
    private int timeLeft = 60;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;
    private JLabel levelLabel;

    private Timer gameTimer;
    private Timer countdownTimer;

    private boolean isGameOver;
    private boolean shieldActive;
    private boolean isProjectileVisible;
    private boolean isFiring;

    private boolean movingLeft = false;
    private boolean movingRight = false;

    private String gameOverReason = "";

    private int playerX, playerY;
    private int projectileX, projectileY;

    private ArrayList<int[]> obstacles;
    private ArrayList<Point> stars;
    private ArrayList<Point> healthPowerUps;

    private Image playerImage;
    private Image healthPowerUpImage;
    private Image[] obstacleImages;

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        loadImages();

        obstacles = new ArrayList<>();
        stars = new ArrayList<>();
        healthPowerUps = new ArrayList<>();

        createStars();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        gamePanel.setLayout(null);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setBounds(10, 10, 150, 25);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 100");
        healthLabel.setForeground(Color.WHITE);
        healthLabel.setBounds(10, 35, 150, 25);
        gamePanel.add(healthLabel);

        timerLabel = new JLabel("Time: 60");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBounds(380, 10, 120, 25);
        gamePanel.add(timerLabel);

        levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setBounds(380, 35, 120, 25);
        gamePanel.add(levelLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        resetGame();

        gameTimer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
            }

            gamePanel.repaint();
        });
        gameTimer.start();

        countdownTimer = new Timer(1000, e -> {
            if (!isGameOver) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft);

                if (timeLeft == 40) {
                    level = 2;
                    obstacleSpeed = 5;
                }

                if (timeLeft == 20) {
                    level = 3;
                    obstacleSpeed = 7;
                }

                levelLabel.setText("Level: " + level);

                if (timeLeft <= 0) {
                    timeLeft = 0;
                    timerLabel.setText("Time: 0");
                    gameOverReason = "Time Ran Out!";
                    isGameOver = true;
                }
            }
        });
        countdownTimer.start();
    }

    private void resetGame() {
        score = 0;
        health = 100;
        level = 1;
        timeLeft = 60;
        obstacleSpeed = 3;

        isGameOver = false;
        shieldActive = false;
        isProjectileVisible = false;
        isFiring = false;

        movingLeft = false;
        movingRight = false;

        gameOverReason = "";

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 60;

        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;

        obstacles.clear();
        healthPowerUps.clear();

        scoreLabel.setText("Score: 0");
        healthLabel.setText("Health: 100");
        timerLabel.setText("Time: 60");
        levelLabel.setText("Level: 1");

        gamePanel.requestFocusInWindow();
    }

    private void loadImages() {
        URL playerURL = getClass().getResource("/resources/spaceship.png");

        if (playerURL != null) {
            playerImage = new ImageIcon(playerURL).getImage();
        } else {
            System.out.println("Could not load spaceship.png");
        }

        URL healthURL = getClass().getResource("/resources/healthup.png");

        if (healthURL != null) {
            healthPowerUpImage = new ImageIcon(healthURL).getImage();
            System.out.println("Loaded healthup.png");
        } else {
            System.out.println("Could not load healthup.png");
        }

        obstacleImages = new Image[4];

        for (int i = 0; i < 4; i++) {
            URL asteroidURL = getClass().getResource("/resources/asteroid" + (i + 1) + ".png");

            if (asteroidURL != null) {
                obstacleImages[i] = new ImageIcon(asteroidURL).getImage();
                System.out.println("Loaded asteroid" + (i + 1) + ".png");
            } else {
                System.out.println("Could not load asteroid" + (i + 1) + ".png");
            }
        }
    }

    private void createStars() {
        for (int i = 0; i < 80; i++) {
            int x = (int) (Math.random() * WIDTH);
            int y = (int) (Math.random() * HEIGHT);
            stars.add(new Point(x, y));
        }
    }

    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawStars(g);

        if (playerImage != null) {
            g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        if (shieldActive) {
            g.setColor(Color.CYAN);
            g.drawOval(playerX - 8, playerY - 8, PLAYER_WIDTH + 16, PLAYER_HEIGHT + 16);
        }

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        drawObstacles(g);

        for (Point powerUp : healthPowerUps) {
            if (healthPowerUpImage != null) {
                g.drawImage(healthPowerUpImage, powerUp.x, powerUp.y,
                        HEALTH_POWERUP_SIZE, HEALTH_POWERUP_SIZE, this);
            } else {
                g.setColor(Color.GREEN);
                g.fillOval(powerUp.x, powerUp.y, HEALTH_POWERUP_SIZE, HEALTH_POWERUP_SIZE);
            }
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2 - 30);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(gameOverReason, WIDTH / 2 - 75, HEIGHT / 2);

            g.drawString("Final Score: " + score, WIDTH / 2 - 80, HEIGHT / 2 + 30);
            g.drawString("Press R to Replay", WIDTH / 2 - 85, HEIGHT / 2 + 60);
        }
    }

    private void drawStars(Graphics g) {
        Color[] colors = {Color.WHITE, Color.YELLOW, Color.CYAN, Color.PINK};

        for (Point star : stars) {
            g.setColor(colors[(int) (Math.random() * colors.length)]);
            g.fillOval(star.x, star.y, 3, 3);
        }
    }

    private void drawObstacles(Graphics g) {
        for (int[] obstacle : obstacles) {
            int x = obstacle[0];
            int y = obstacle[1];
            int imageIndex = obstacle[2];

            if (obstacleImages[imageIndex] != null) {
                g.drawImage(obstacleImages[imageIndex], x, y,
                        OBSTACLE_WIDTH, OBSTACLE_HEIGHT, this);
            } else {
                g.setColor(Color.RED);
                g.fillRect(x, y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            }
        }
    }

    private void update() {
        movePlayer();

        moveObstacles();
        createObstacles();

        moveProjectile();

        checkProjectileCollision();
        checkPlayerCollision();

        createHealthPowerUps();
        moveHealthPowerUps();
        checkHealthPowerUpCollision();

        scoreLabel.setText("Score: " + score);
        healthLabel.setText("Health: " + health);
    }

    private void movePlayer() {
        if (movingLeft && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }

        if (movingRight && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }
    }

    private void moveObstacles() {
        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i)[1] += obstacleSpeed;

            if (obstacles.get(i)[1] > HEIGHT) {
                obstacles.remove(i);
                i--;
            }
        }
    }

    private void createObstacles() {
        double chance = 0.02 + (level * 0.005);

        if (Math.random() < chance) {
            int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
            int imageIndex = (int) (Math.random() * 4);

            obstacles.add(new int[]{obstacleX, 0, imageIndex});
        }
    }

    private void moveProjectile() {
        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;

            if (projectileY < 0) {
                isProjectileVisible = false;
            }
        }
    }

    private void createHealthPowerUps() {
        if (Math.random() < 0.004) {
            int x = (int) (Math.random() * (WIDTH - HEALTH_POWERUP_SIZE));
            healthPowerUps.add(new Point(x, 0));
        }
    }

    private void moveHealthPowerUps() {
        for (int i = 0; i < healthPowerUps.size(); i++) {
            healthPowerUps.get(i).y += 2;

            if (healthPowerUps.get(i).y > HEIGHT) {
                healthPowerUps.remove(i);
                i--;
            }
        }
    }

    private void checkPlayerCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < obstacles.size(); i++) {
            int x = obstacles.get(i)[0];
            int y = obstacles.get(i)[1];

            Rectangle obstacleRect = new Rectangle(
                    x + 5,
                    y + 5,
                    OBSTACLE_WIDTH - 10,
                    OBSTACLE_HEIGHT - 10
            );

            if (playerRect.intersects(obstacleRect)) {
                obstacles.remove(i);
                playSound("collision.wav");

                if (!shieldActive) {
                    health -= 20;

                    if (health <= 0) {
                        health = 0;
                        healthLabel.setText("Health: 0");
                        gameOverReason = "Health Reached 0!";
                        isGameOver = true;
                    }
                }

                break;
            }
        }
    }

    private void checkProjectileCollision() {
        if (!isProjectileVisible) {
            return;
        }

        Rectangle projectileRect = new Rectangle(
                projectileX - 8,
                projectileY - 8,
                PROJECTILE_WIDTH + 16,
                PROJECTILE_HEIGHT + 16
        );

        for (int i = 0; i < obstacles.size(); i++) {
            int x = obstacles.get(i)[0];
            int y = obstacles.get(i)[1];

            Rectangle obstacleRect = new Rectangle(x, y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);

            if (projectileRect.intersects(obstacleRect)) {
                obstacles.remove(i);
                score += 10;
                isProjectileVisible = false;
                playSound("collision.wav");
                return;
            }
        }
    }

    private void checkHealthPowerUpCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = 0; i < healthPowerUps.size(); i++) {
            Rectangle powerUpRect = new Rectangle(
                    healthPowerUps.get(i).x,
                    healthPowerUps.get(i).y,
                    HEALTH_POWERUP_SIZE,
                    HEALTH_POWERUP_SIZE
            );

            if (playerRect.intersects(powerUpRect)) {
                healthPowerUps.remove(i);
                health += 20;

                if (health > 100) {
                    health = 100;
                }

                break;
            }
        }
    }

    private void playSound(String fileName) {
        try {
            URL soundURL = getClass().getResource("/resources/" + fileName);

            if (soundURL == null) {
                System.out.println("Sound file not found: " + fileName);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

        } catch (Exception e) {
            System.out.println("Sound could not play: " + fileName);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_R && isGameOver) {
            resetGame();
            return;
        }

        if (isGameOver) {
            return;
        }

        if (keyCode == KeyEvent.VK_LEFT) {
            movingLeft = true;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            movingRight = true;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            fireProjectile();
        } else if (keyCode == KeyEvent.VK_CONTROL) {
            shieldActive = true;
        }
    }

    private void fireProjectile() {
        isFiring = true;

        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = true;

        playSound("fire.wav");

        new Thread(() -> {
            try {
                Thread.sleep(350);
                isFiring = false;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT) {
            movingLeft = false;
        }

        if (keyCode == KeyEvent.VK_RIGHT) {
            movingRight = false;
        }

        if (keyCode == KeyEvent.VK_CONTROL) {
            shieldActive = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}