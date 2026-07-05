
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classic Snake game built with pure Java Swing (no external libraries).
 * Run it directly from Eclipse, VS Code, or the command line.
 *
 * Controls:
 *   Arrow keys / WASD - move
 *   P                 - pause / resume
 *   R                 - restart after game over
 *   Esc               - quit
 */
public class SnakeGame extends JPanel implements ActionListener {

    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 24;
    private static final int GRID_HEIGHT = 20;
    private static final int BOARD_WIDTH = TILE_SIZE * GRID_WIDTH;
    private static final int BOARD_HEIGHT = TILE_SIZE * GRID_HEIGHT;
    private static final int INITIAL_DELAY_MS = 130;

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private final List<Point> snake = new ArrayList<>();
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private Point food;
    private final Random random = new Random();

    private Timer timer;
    private boolean running = false;
    private boolean paused = false;
    private int score = 0;
    private int highScore = 0;

    public SnakeGame() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(new Color(20, 20, 20));
        setFocusable(true);
        addKeyListener(new KeyHandler());
        startNewGame();
    }

    private void startNewGame() {
        snake.clear();
        int startX = GRID_WIDTH / 4;
        int startY = GRID_HEIGHT / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        running = true;
        paused = false;

        placeFood();

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(INITIAL_DELAY_MS, this);
        timer.start();
    }

    private void placeFood() {
        while (true) {
            int x = random.nextInt(GRID_WIDTH);
            int y = random.nextInt(GRID_HEIGHT);
            Point candidate = new Point(x, y);
            if (!snake.contains(candidate)) {
                food = candidate;
                return;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            moveSnake();
            checkCollisions();
        }
        repaint();
    }

    private void moveSnake() {
        direction = nextDirection;
        Point head = snake.get(0);
        Point newHead;

        switch (direction) {
            case UP:
                newHead = new Point(head.x, head.y - 1);
                break;
            case DOWN:
                newHead = new Point(head.x, head.y + 1);
                break;
            case LEFT:
                newHead = new Point(head.x - 1, head.y);
                break;
            default:
                newHead = new Point(head.x + 1, head.y);
                break;
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            score += 10;
            if (score > highScore) {
                highScore = score;
            }
            placeFood();
            // speed up slightly as the score grows, with a sensible floor
            int newDelay = Math.max(60, INITIAL_DELAY_MS - (score / 5));
            timer.setDelay(newDelay);
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void checkCollisions() {
        Point head = snake.get(0);

        boolean hitWall = head.x < 0 || head.x >= GRID_WIDTH || head.y < 0 || head.y >= GRID_HEIGHT;

        boolean hitSelf = false;
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i).equals(head)) {
                hitSelf = true;
                break;
            }
        }

        if (hitWall || hitSelf) {
            running = false;
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawFood(g2);
        drawSnake(g2);
        drawHud(g2);

        if (!running) {
            drawGameOver(g2);
        } else if (paused) {
            drawPaused(g2);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(35, 35, 35));
        for (int x = 0; x <= GRID_WIDTH; x++) {
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, BOARD_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            g2.drawLine(0, y * TILE_SIZE, BOARD_WIDTH, y * TILE_SIZE);
        }
    }

    private void drawFood(Graphics2D g2) {
        g2.setColor(new Color(220, 60, 60));
        g2.fillOval(food.x * TILE_SIZE + 2, food.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
    }

    private void drawSnake(Graphics2D g2) {
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g2.setColor(new Color(90, 210, 120));
            } else {
                g2.setColor(new Color(50, 160, 90));
            }
            g2.fillRoundRect(p.x * TILE_SIZE + 1, p.y * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2, 8, 8);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Score: " + score, 10, 20);
        String hs = "Best: " + highScore;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hs, BOARD_WIDTH - fm.stringWidth(hs) - 10, 20);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        centerText(g2, "GAME OVER", BOARD_HEIGHT / 2 - 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        centerText(g2, "Score: " + score, BOARD_HEIGHT / 2 + 5);
        centerText(g2, "Press R to restart", BOARD_HEIGHT / 2 + 35);
    }

    private void drawPaused(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        centerText(g2, "PAUSED", BOARD_HEIGHT / 2);
    }

    private void centerText(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (BOARD_WIDTH - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }

            if (!running) {
                if (key == KeyEvent.VK_R) {
                    startNewGame();
                }
                return;
            }

            if (key == KeyEvent.VK_P) {
                paused = !paused;
                return;
            }

            if (paused) {
                return;
            }

            switch (key) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (direction != Direction.DOWN) nextDirection = Direction.UP;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (direction != Direction.UP) nextDirection = Direction.DOWN;
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (direction != Direction.RIGHT) nextDirection = Direction.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (direction != Direction.LEFT) nextDirection = Direction.RIGHT;
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            SnakeGame gamePanel = new SnakeGame();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}
