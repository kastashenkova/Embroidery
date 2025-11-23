package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmbroideryPattern extends JPanel {

    // Початковий розмір матриці візерунка
    private static final int INITIAL_PATTERN_DIMENSION = 61;
    private static final int KATERNA_PATTERN_SIZE = 31; // Розмір візерунка Катерина (31x31)
    private static final int DEFAULT_CELL_SIZE = 10; // Стандартний розмір клітинки в пікселях

    // Відступи для номерів рядків та стовпців
    private static final int HEADER_OFFSET_X = 30; // Відступ зліва для номерів стовпців
    private static final int HEADER_OFFSET_Y = 30; // Відступ зверху для номерів рядків

    // Змінні будуть динамічно змінюватися
    private int currentPatternWidth;
    private int currentPatternHeight;

    // Зміщення для малювання візерунка "Катерина"
    private int katernaOffsetX;
    private int katernaOffsetY;

    private static final Color RED = new Color(200, 50, 50);
    private static final Color BLACK = new Color(50, 50, 50);
    private static final Color WHITE = new Color(255, 255, 255); // Колір фону та білих частин орнаменту
    private static final Color GRID_COLOR = new Color(220, 220, 220); // Колір сітки
    private static final Color PANEL_BACKGROUND_COLOR = new Color(230, 230, 230); // Колір фону панелі
    private static final Color NUMBER_COLOR = new Color(100, 100, 100); // Колір номерів

    private Random random = new Random();

    // The pattern matrix: 0 - background (white), 1 - red, 2 - black, 3 - white (ornament part)
    private int[][] pattern;

    private Color currentColor = RED; // Поточний обраний колір для малювання (за замовчуванням червоний)

    private boolean isDrawing = false; // Чи відбувається процес малювання
    private boolean drawMode = true; // true - малювання, false - стирання

    // Режими симетрії
    private SymmetryMode symmetryMode = SymmetryMode.NONE;

    public enum SymmetryMode {
        NONE,            // Без симетрії
        HORIZONTAL,      // Горизонтальна симетрія
        VERTICAL,        // Вертикальна симетрія
        QUADRANT         // Чотирикратна симетрія
    }

    public EmbroideryPattern() {
        // Ініціалізуємо матрицю початковим розміром
        currentPatternWidth = INITIAL_PATTERN_DIMENSION;
        currentPatternHeight = INITIAL_PATTERN_DIMENSION;
        pattern = new int[currentPatternHeight][currentPatternWidth];

        // Збільшуємо preferredSize, щоб врахувати місце для заголовків
        setPreferredSize(new Dimension(800 + HEADER_OFFSET_X, 600 + HEADER_OFFSET_Y));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                handleMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    handleMouseEvent(e);
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // При зміні розміру вікна, коригуємо розмір логічної сітки
                adjustPatternDimensions();
                // Оновлюємо зміщення для центрування Катерини
                calculateKaterynaOffset();
                repaint();
            }
        });
    }

    // Метод для динамічного коригування розмірів матриці pattern
    private void adjustPatternDimensions() {
        // Тепер розраховуємо потрібний розмір, віднімаючи відступи для заголовків
        int neededWidth = (getWidth() - HEADER_OFFSET_X) / DEFAULT_CELL_SIZE;
        int neededHeight = (getHeight() - HEADER_OFFSET_Y) / DEFAULT_CELL_SIZE;

        boolean changed = false;
        if (neededWidth > currentPatternWidth) {
            currentPatternWidth = neededWidth + 10; // Додаємо запас
            changed = true;
        }
        if (neededHeight > currentPatternHeight) {
            currentPatternHeight = neededHeight + 10; // Додаємо запас
            changed = true;
        }

        if (changed) {
            int[][] newPattern = new int[currentPatternHeight][currentPatternWidth];
            // Скопіювати існуючі дані
            for (int r = 0; r < pattern.length; r++) {
                if (r < newPattern.length) { // Перевірка, щоб не вийти за межі newPattern
                    for (int c = 0; c < pattern[0].length; c++) {
                        if (c < newPattern[0].length) { // Перевірка, щоб не вийти за межі newPattern
                            newPattern[r][c] = pattern[r][c];
                        }
                    }
                }
            }
            pattern = newPattern;
            System.out.println("Pattern dynamically resized to: " + currentPatternWidth + "x" + currentPatternHeight);
        }
    }

    // Метод для розрахунку зміщення візерунка "Катерина" для центрування
    public void calculateKaterynaOffset() {
        // Розраховуємо зміщення відносно "доступної" області, після відступу для заголовків
        int usableWidth = currentPatternWidth;
        int usableHeight = currentPatternHeight;

        katernaOffsetX = (usableWidth - KATERNA_PATTERN_SIZE) / 2;
        katernaOffsetY = (usableHeight - KATERNA_PATTERN_SIZE) / 2;

        // Забезпечуємо, щоб зміщення не було від'ємним
        if (katernaOffsetX < 0) katernaOffsetX = 0;
        if (katernaOffsetY < 0) katernaOffsetY = 0;
    }

    // Метод для генерації візерунка "Катерина"
    public void generateKaterynaPattern() {
        // Очищуємо всю поточну сітку
        for (int r = 0; r < currentPatternHeight; r++) {
            for (int c = 0; c < currentPatternWidth; c++) {
                pattern[r][c] = 0; // Все біле
            }
        }

        // --- Black central cross and arms ---
        setPixelKateryna(0, 13, 2);
        setPixelKateryna(0, 14, 2);
        setPixelKateryna(0, 15, 2);

        setPixelKateryna(1, 12, 2);
        setPixelKateryna(1, 13, 2);
        setPixelKateryna(1, 15, 2);
        setPixelKateryna(1, 16, 2);

        setPixelKateryna(2, 11, 2);
        setPixelKateryna(2, 12, 2);
        setPixelKateryna(2, 16, 2);
        setPixelKateryna(2, 17, 2);

        setPixelKateryna(3, 10, 2);
        setPixelKateryna(3, 11, 2);
        setPixelKateryna(3, 17, 2);
        setPixelKateryna(3, 18, 2);

        setPixelKateryna(4, 11, 2);
        setPixelKateryna(4, 12, 2);
        setPixelKateryna(4, 13, 2);
        setPixelKateryna(4, 14, 2);
        setPixelKateryna(4, 15, 2);
        setPixelKateryna(4, 16, 2);
        setPixelKateryna(4, 17, 2);

        setPixelKateryna(5, 12, 2);
        setPixelKateryna(5, 13, 2);
        setPixelKateryna(5, 14, 2);
        setPixelKateryna(5, 15, 2);
        setPixelKateryna(5, 16, 2);

        setPixelKateryna(6, 13, 2);
        setPixelKateryna(6, 14, 2);
        setPixelKateryna(6, 15, 2);

        setPixelKateryna(7, 9, 2);
        setPixelKateryna(7, 10, 2);
        setPixelKateryna(7, 11, 2);
        setPixelKateryna(7, 12, 2);
        setPixelKateryna(7, 13, 2);
        setPixelKateryna(7, 14, 2);
        setPixelKateryna(7, 15, 2);
        setPixelKateryna(7, 16, 2);
        setPixelKateryna(7, 17, 2);

        setPixelKateryna(8, 8, 2);
        setPixelKateryna(8, 9, 2);
        setPixelKateryna(8, 10, 2);
        setPixelKateryna(8, 11, 2);
        setPixelKateryna(8, 12, 2);
        setPixelKateryna(8, 13, 2);
        setPixelKateryna(8, 14, 2);
        setPixelKateryna(8, 15, 2);
        setPixelKateryna(8, 16, 2);
        setPixelKateryna(8, 17, 2);

        setPixelKateryna(9, 7, 2);
        setPixelKateryna(9, 8, 2);
        setPixelKateryna(9, 18, 2);
        setPixelKateryna(9, 19, 2);

        setPixelKateryna(10, 6, 2);
        setPixelKateryna(10, 7, 2);
        setPixelKateryna(10, 19, 2);
        setPixelKateryna(10, 20, 2);

        setPixelKateryna(11, 5, 2);
        setPixelKateryna(11, 6, 2);
        setPixelKateryna(11, 20, 2);
        setPixelKateryna(11, 21, 2);

        setPixelKateryna(12, 4, 2);
        setPixelKateryna(12, 5, 2);
        setPixelKateryna(12, 21, 2);
        setPixelKateryna(12, 22, 2);

        setPixelKateryna(13, 0, 2);
        setPixelKateryna(13, 1, 2);
        setPixelKateryna(13, 2, 2);
        setPixelKateryna(13, 3, 2);
        setPixelKateryna(13, 4, 2);

        setPixelKateryna(14, 0, 2);
        setPixelKateryna(14, 28, 2);
        setPixelKateryna(14, 29, 2);
        setPixelKateryna(14, 30, 2);

        setPixelKateryna(15, 0, 2);
        setPixelKateryna(15, 1, 2);
        setPixelKateryna(15, 2, 2);
        setPixelKateryna(15, 3, 2);
        setPixelKateryna(15, 4, 2);

        // --- White (3) parts that are part of the fixed pattern ---
        setPixelKateryna(1, 14, 3);
        setPixelKateryna(2, 13, 3);
        setPixelKateryna(2, 14, 3);
        setPixelKateryna(2, 15, 3);

        setPixelKateryna(3, 12, 3);
        setPixelKateryna(3, 13, 3);
        setPixelKateryna(3, 14, 3);
        setPixelKateryna(3, 15, 3);
        setPixelKateryna(3, 16, 3);

        setPixelKateryna(6, 2, 3);
        setPixelKateryna(7, 3, 3);
        setPixelKateryna(7, 4, 3);
        setPixelKateryna(8, 2, 3);
        setPixelKateryna(8, 3, 3);

        setPixelKateryna(9, 10, 3);
        setPixelKateryna(9, 12, 3);
        setPixelKateryna(9, 14, 3);
        setPixelKateryna(9, 16, 3);

        setPixelKateryna(10, 9, 3);
        setPixelKateryna(10, 11, 3);
        setPixelKateryna(10, 13, 3);
        setPixelKateryna(10, 15, 3);
        setPixelKateryna(10, 17, 3);

        setPixelKateryna(11, 8, 3);
        setPixelKateryna(11, 10, 3);
        setPixelKateryna(11, 12, 3);
        setPixelKateryna(11, 14, 3);
        setPixelKateryna(11, 16, 3);
        setPixelKateryna(11, 18, 3);

        setPixelKateryna(12, 7, 3);
        setPixelKateryna(12, 9, 3);
        setPixelKateryna(12, 11, 3);
        setPixelKateryna(12, 13, 3);
        setPixelKateryna(12, 15, 3);
        setPixelKateryna(12, 17, 3);
        setPixelKateryna(12, 19, 3);

        setPixelKateryna(13, 6, 3);
        setPixelKateryna(13, 8, 3);
        setPixelKateryna(13, 10, 3);
        setPixelKateryna(13, 12, 3);
        setPixelKateryna(13, 14, 3);
        setPixelKateryna(13, 16, 3);
        setPixelKateryna(13, 18, 3);
        setPixelKateryna(13, 20, 3);

        setPixelKateryna(14, 1, 3);
        setPixelKateryna(14, 2, 3);
        setPixelKateryna(14, 3, 3);
        setPixelKateryna(14, 4, 3);
        setPixelKateryna(14, 5, 3);
        setPixelKateryna(14, 7, 3);
        setPixelKateryna(14, 9, 3);
        setPixelKateryna(14, 11, 3);
        setPixelKateryna(14, 13, 3);
        setPixelKateryna(14, 15, 3);
        setPixelKateryna(14, 17, 3);
        setPixelKateryna(14, 19, 3);
        setPixelKateryna(14, 21, 3);
        setPixelKateryna(14, 22, 3);
        setPixelKateryna(14, 23, 3);
        setPixelKateryna(14, 24, 3);
        setPixelKateryna(14, 25, 3);
        setPixelKateryna(14, 26, 3);

        setPixelKateryna(15, 14, 3);

        generateRandomRedForKaterynaPattern();
        repaint();
    }

    // Метод для випадкової генерації червоних пікселів у візерунку Катерина
    private void generateRandomRedForKaterynaPattern() {
        for (int r = 0; r < KATERNA_PATTERN_SIZE; r++) {
            for (int c = 0; c < KATERNA_PATTERN_SIZE; c++) {
                // Застосовуємо поточне зміщення
                int targetRow = r + katernaOffsetY;
                int targetCol = c + katernaOffsetX;

                if (targetRow >= 0 && targetRow < currentPatternHeight && targetCol >= 0 && targetCol < currentPatternWidth) {
                    if (pattern[targetRow][targetCol] == 0) { // Тільки якщо це фоновий білий
                        boolean isRed = random.nextDouble() < 0.2; //
                        if (isRed) {
                            setPixelKateryna(r, c, 1); // 1 для червоного
                        } else {
                            setPixelKateryna(r, c, 0); // 0 для фону
                        }
                    }
                }
            }
        }
    }

    // Цей метод встановлює піксель для візерунка Катерина, зміщений відносно katernaOffsetX/Y
    // та з урахуванням симетрії *для розміру Катерина*.
    private void setPixelKateryna(int row, int col, int value) {
        // Застосовуємо зміщення, щоб малювати в поточній сітці pattern
        int targetRow = row + katernaOffsetY;
        int targetCol = col + katernaOffsetX;

        // Встановлення оригінального пікселя
        if (targetRow >= 0 && targetRow < currentPatternHeight && targetCol >= 0 && targetCol < currentPatternWidth) {
            pattern[targetRow][targetCol] = value;
        }

        // Симетрія відносно центру візерунка Катерина
        // Горизонтальна симетрія відносно центру візерунка Катерина
        int symColH_Kateryna = KATERNA_PATTERN_SIZE - 1 - col;
        int symColH_Global = symColH_Kateryna + katernaOffsetX;
        if (targetRow >= 0 && targetRow < currentPatternHeight && symColH_Global >= 0 && symColH_Global < currentPatternWidth) {
            pattern[targetRow][symColH_Global] = value;
        }

        // Вертикальна симетрія відносно центру візерунка Катерина
        int symRowV_Kateryna = KATERNA_PATTERN_SIZE - 1 - row;
        int symRowV_Global = symRowV_Kateryna + katernaOffsetY;
        if (symRowV_Global >= 0 && symRowV_Global < currentPatternHeight && targetCol >= 0 && targetCol < currentPatternWidth) {
            pattern[symRowV_Global][targetCol] = value;
        }

        // Діагональна симетрія відносно центру візерунка Катерина
        if (symRowV_Global >= 0 && symRowV_Global < currentPatternHeight && symColH_Global >= 0 && symColH_Global < currentPatternWidth) {
            pattern[symRowV_Global][symColH_Global] = value;
        }
    }

    // Обробка подій миші для малювання
    private void handleMouseEvent(MouseEvent e) {
        int currentCellSize = DEFAULT_CELL_SIZE;

        // Враховуємо зміщення заголовків
        int col = (e.getX() - HEADER_OFFSET_X) / currentCellSize;
        int row = (e.getY() - HEADER_OFFSET_Y) / currentCellSize;

        // Перевіряємо, чи потрібно розширити матрицю, щоб вмістити цю клітинку
        boolean resized = false;
        if (col >= currentPatternWidth) {
            currentPatternWidth = col + 1;
            resized = true;
        }
        if (row >= currentPatternHeight) {
            currentPatternHeight = row + 1;
            resized = true;
        }

        if (resized) {
            int[][] newPattern = new int[currentPatternHeight][currentPatternWidth];
            // Скопіювати існуючі дані
            for (int r = 0; r < pattern.length; r++) {
                if (r < newPattern.length) {
                    for (int c = 0; c < pattern[0].length; c++) {
                        if (c < newPattern[0].length) {
                            newPattern[r][c] = pattern[r][c];
                        }
                    }
                }
            }
            pattern = newPattern;
            System.out.println("Pattern dynamically resized to: " + currentPatternWidth + "x" + currentPatternHeight);
        }

        if (row >= 0 && row < currentPatternHeight && col >= 0 && col < currentPatternWidth) {
            int valueToSet = drawMode ? getColorValue(currentColor) : 0;

            // Застосовуємо симетрію відносно центру поточної розширеної області
            setPixelSymmetrically(row, col, valueToSet);
            repaint();
        }
    }

    // Допоміжний метод для встановлення пікселів з симетрією (для інтерактивного малювання)
    private void setPixelSymmetrically(int row, int col, int value) {
        if (row < 0 || row >= currentPatternHeight || col < 0 || col >= currentPatternWidth) return;

        pattern[row][col] = value;

        // Застосовуємо симетрію залежно від обраного режиму
        switch (symmetryMode) {
            case HORIZONTAL:
                int symColH = currentPatternWidth - 1 - col;
                if (symColH >= 0 && symColH < currentPatternWidth) {
                    pattern[row][symColH] = value;
                }
                break;
            case VERTICAL:
                int symRowV = currentPatternHeight - 1 - row;
                if (symRowV >= 0 && symRowV < currentPatternHeight) {
                    pattern[symRowV][col] = value;
                }
                break;
            case QUADRANT:
                // Малюємо оригінал
                pattern[row][col] = value;

                // Горизонтальне відображення
                int symColH_q = currentPatternWidth - 1 - col;
                if (symColH_q >= 0 && symColH_q < currentPatternWidth) {
                    pattern[row][symColH_q] = value;
                }

                // Вертикальне відображення
                int symRowV_q = currentPatternHeight - 1 - row;
                if (symRowV_q >= 0 && symRowV_q < currentPatternHeight) {
                    pattern[symRowV_q][col] = value;
                }

                // Діагональне відображення (квадрант)
                if (symRowV_q >= 0 && symRowV_q < currentPatternHeight && symColH_q >= 0 && symColH_q < currentPatternWidth) {
                    pattern[symRowV_q][symColH_q] = value;
                }
                break;
            case NONE:
            default:
                // Без симетрії, вже встановлено pattern[row][col] = value;
                break;
        }
    }

    // Допоміжний метод для горизонтального віддзеркалення фрагмента
    private int[][] flipFragmentHorizontal(int[][] originalFragment) {
        if (originalFragment == null || originalFragment.length == 0 || originalFragment[0].length == 0) {
            return originalFragment;
        }
        int rows = originalFragment.length;
        int cols = originalFragment[0].length;
        int[][] flippedFragment = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                flippedFragment[r][c] = originalFragment[r][cols - 1 - c];
            }
        }
        return flippedFragment;
    }

    // Допоміжний метод для вертикального віддзеркалення фрагмента
    private int[][] flipFragmentVertical(int[][] originalFragment) {
        if (originalFragment == null || originalFragment.length == 0 || originalFragment[0].length == 0) {
            return originalFragment;
        }
        int rows = originalFragment.length;
        int cols = originalFragment[0].length;
        int[][] flippedFragment = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                flippedFragment[r][c] = originalFragment[rows - 1 - r][c];
            }
        }
        return flippedFragment;
    }

    // Допоміжний метод для квадрантного (горизонтального + вертикального) віддзеркалення фрагмента
    private int[][] flipFragmentQuadrant(int[][] originalFragment) {
        return flipFragmentVertical(flipFragmentHorizontal(originalFragment));
    }


    private int getColorValue(Color color) {
        if (color.equals(RED)) return 1;
        if (color.equals(BLACK)) return 2;
        if (color.equals(WHITE)) return 3;
        return 0; // Default to background (transparent or non-ornament part)
    }

    private Color getColorForValue(int value) {
        switch (value) {
            case 1: return RED;
            case 2: return BLACK;
            case 3: return WHITE;
            default: return WHITE; // 0 (background) will also be white
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9)); // Reduce font size for numbers

        int currentCellSize = DEFAULT_CELL_SIZE;

        int cellsToDrawWidth = (getWidth() - HEADER_OFFSET_X) / currentCellSize;
        int cellsToDrawHeight = (getHeight() - HEADER_OFFSET_Y) / currentCellSize;

        // Fill the panel background
        g2d.setColor(PANEL_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw column numbers
        g2d.setColor(NUMBER_COLOR);
        for (int c = 0; c < cellsToDrawWidth; c++) {
            String colNum = String.valueOf(c);
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(colNum);
            int x = HEADER_OFFSET_X + c * currentCellSize + (currentCellSize - stringWidth) / 2;
            int y = HEADER_OFFSET_Y / 2 + fm.getAscent() / 2; // Center vertically
            g2d.drawString(colNum, x, y);
        }

        // Draw row numbers
        for (int r = 0; r < cellsToDrawHeight; r++) {
            String rowNum = String.valueOf(r);
            FontMetrics fm = g2d.getFontMetrics();
            int stringHeight = fm.getHeight();
            int x = HEADER_OFFSET_X / 2 - fm.stringWidth(rowNum) / 2; // Center horizontally
            // Center vertically within the cell, considering header offset
            int y = HEADER_OFFSET_Y + r * currentCellSize + (currentCellSize + fm.getAscent()) / 2 - stringHeight/2;
            g2d.drawString(rowNum, x, y);
        }


        // Draw the main grid and pattern
        for (int row = 0; row < cellsToDrawHeight; row++) {
            for (int col = 0; col < cellsToDrawWidth; col++) {
                int x = HEADER_OFFSET_X + col * currentCellSize;
                int y = HEADER_OFFSET_Y + row * currentCellSize;

                if (row < currentPatternHeight && col < currentPatternWidth) {
                    Color color = getColorForValue(pattern[row][col]);
                    g2d.setColor(color);
                    g2d.fillRect(x, y, currentCellSize, currentCellSize);

                    g2d.setColor(color.darker()); // Draw cell border slightly darker
                    g2d.drawRect(x, y, currentCellSize - 1, currentCellSize - 1);
                } else {
                    g2d.setColor(WHITE); // Draw empty cells as white
                    g2d.fillRect(x, y, currentCellSize, currentCellSize);
                }
            }
        }

        // Draw grid lines
        g2d.setColor(GRID_COLOR);
        for (int i = 0; i <= cellsToDrawWidth; i++) {
            g2d.drawLine(HEADER_OFFSET_X + i * currentCellSize, HEADER_OFFSET_Y,
                    HEADER_OFFSET_X + i * currentCellSize, HEADER_OFFSET_Y + cellsToDrawHeight * currentCellSize);
        }
        for (int i = 0; i <= cellsToDrawHeight; i++) {
            g2d.drawLine(HEADER_OFFSET_X, HEADER_OFFSET_Y + i * currentCellSize,
                    HEADER_OFFSET_X + cellsToDrawWidth * currentCellSize, HEADER_OFFSET_Y + i * currentCellSize);
        }
    }


    public void savePatternAsPng() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Embroidery Pattern");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            int saveCellSize = DEFAULT_CELL_SIZE;
            // Dimensions for the image to be saved, excluding headers
            int saveWidth = currentPatternWidth * saveCellSize;
            int saveHeight = currentPatternHeight * saveCellSize;

            BufferedImage image = new BufferedImage(
                    saveWidth,
                    saveHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();

            g2.setColor(WHITE); // Background for the saved image
            g2.fillRect(0, 0, saveWidth, saveHeight);

            // Draw the pattern onto the buffered image
            for (int row = 0; row < currentPatternHeight; row++) {
                for (int col = 0; col < currentPatternWidth; col++) {
                    int x = col * saveCellSize;
                    int y = row * saveCellSize;

                    Color color = getColorForValue(pattern[row][col]);
                    g2.setColor(color);
                    g2.fillRect(x, y, saveCellSize, saveCellSize);

                    g2.setColor(color.darker());
                    g2.drawRect(x, y, saveCellSize - 1, saveCellSize - 1);
                }
            }
            // Draw grid lines on the saved image
            g2.setColor(GRID_COLOR);
            for (int i = 0; i <= currentPatternWidth; i++) {
                g2.drawLine(i * saveCellSize, 0, i * saveCellSize, saveHeight);
            }
            for (int i = 0; i <= currentPatternHeight; i++) {
                g2.drawLine(0, i * saveCellSize, saveWidth, i * saveCellSize);
            }
            g2.dispose();

            try {
                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Pattern successfully saved to " + fileToSave.getName(),
                        "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving pattern: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public void openPatternFromPng() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Embroidery Pattern");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(fileToOpen);
                if (image == null) {
                    JOptionPane.showMessageDialog(this, "Could not read image. Maybe it's not a PNG.",
                            "Open Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Dynamically set the pattern size based on the image size
                int newWidth = image.getWidth() / DEFAULT_CELL_SIZE;
                int newHeight = image.getHeight() / DEFAULT_CELL_SIZE;

                // Expand pattern if needed, but don't shrink it immediately
                if (newWidth > currentPatternWidth || newHeight > currentPatternHeight) {
                    currentPatternWidth = Math.max(newWidth, currentPatternWidth);
                    currentPatternHeight = Math.max(newHeight, currentPatternHeight);
                    pattern = new int[currentPatternHeight][currentPatternWidth];
                }

                // Clear the current pattern (the entire matrix)
                for (int r = 0; r < currentPatternHeight; r++) {
                    for (int c = 0; c < currentPatternWidth; c++) {
                        pattern[r][c] = 0;
                    }
                }

                // Iterate through image pixels and convert them to our pattern format
                for (int r = 0; r < newHeight; r++) {
                    for (int c = 0; c < newWidth; c++) {
                        // Sample the center of each cell to determine its color
                        int x = c * DEFAULT_CELL_SIZE + DEFAULT_CELL_SIZE / 2;
                        int y = r * DEFAULT_CELL_SIZE + DEFAULT_CELL_SIZE / 2;

                        if (x < image.getWidth() && y < image.getHeight()) {
                            int rgb = image.getRGB(x, y);
                            Color pixelColor = new Color(rgb);

                            int value = 0; // Default to background
                            if (isClose(pixelColor, RED)) {
                                value = 1;
                            } else if (isClose(pixelColor, BLACK)) {
                                value = 2;
                            } else if (isClose(pixelColor, WHITE)) {
                                value = 3;
                            }
                            pattern[r][c] = value;
                        }
                    }
                }
                repaint();
                JOptionPane.showMessageDialog(this, "Pattern successfully opened from " + fileToOpen.getName(),
                        "Open", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening pattern: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private boolean isClose(Color c1, Color c2) {
        int threshold = 30; // Define a tolerance for color comparison
        return Math.abs(c1.getRed() - c2.getRed()) < threshold &&
                Math.abs(c1.getGreen() - c2.getGreen()) < threshold &&
                Math.abs(c1.getBlue() - c2.getBlue()) < threshold;
    }

    // Змінений метод applyFragmentDuplication для врахування дзеркального відображення
    public void applyFragmentDuplication(int fragRowStart, int fragColStart, int fragRows, int fragCols, SymmetryMode duplicationSymmetry) {
        // Store the original fragment
        int[][] originalFragment = new int[fragRows][fragCols];
        for (int r = 0; r < fragRows; r++) {
            for (int c = 0; c < fragCols; c++) {
                if (fragRowStart + r < currentPatternHeight && fragColStart + c < currentPatternWidth) {
                    originalFragment[r][c] = pattern[fragRowStart + r][fragColStart + c];
                } else {
                    originalFragment[r][c] = 0; // Out of bounds of source pattern - treat as background
                }
            }
        }

        // Clear the entire pattern before applying fragments
        for (int r = 0; r < currentPatternHeight; r++) {
            for (int c = 0; c < currentPatternWidth; c++) {
                pattern[r][c] = 0;
            }
        }

        // Apply the original fragment (no flip for the initial placement)
        overlayFragment(originalFragment, fragRowStart, fragColStart);

        // Apply duplication with the chosen symmetry and flip
        switch (duplicationSymmetry) {
            case HORIZONTAL:
                int[][] flippedH = flipFragmentHorizontal(originalFragment);
                int targetColH = currentPatternWidth - fragCols; // Place at the far right
                overlayFragment(flippedH, fragRowStart, targetColH);
                break;
            case VERTICAL:
                int[][] flippedV = flipFragmentVertical(originalFragment);
                int targetRowV = currentPatternHeight - fragRows; // Place at the far bottom
                overlayFragment(flippedV, targetRowV, fragColStart);
                break;
            case QUADRANT:
                // Original (already done)
                // Top-right (horizontal flip)
                int[][] flippedTR = flipFragmentHorizontal(originalFragment);
                int targetColTR = currentPatternWidth - fragCols;
                overlayFragment(flippedTR, fragRowStart, targetColTR);

                // Bottom-left (vertical flip)
                int[][] flippedBL = flipFragmentVertical(originalFragment);
                int targetRowBL = currentPatternHeight - fragRows;
                overlayFragment(flippedBL, targetRowBL, fragColStart);

                // Bottom-right (horizontal + vertical flip)
                int[][] flippedBR = flipFragmentQuadrant(originalFragment);
                int targetRowBR = currentPatternHeight - fragRows;
                int targetColBR = currentPatternWidth - fragCols;
                overlayFragment(flippedBR, targetRowBR, targetColBR);
                break;
            case NONE:
                // No duplication, just the original fragment (already done)
                break;
        }
        repaint();
    }


    private void overlayFragment(int[][] fragment, int startRow, int startCol) {
        if (fragment == null) return;
        for (int r = 0; r < fragment.length; r++) {
            for (int c = 0; c < fragment[0].length; c++) {
                int targetRow = startRow + r;
                int targetCol = startCol + c;

                // Check if the target cell is within the bounds of the pattern
                if (targetRow >= 0 && targetCol >= 0 &&
                        targetRow < currentPatternHeight && targetCol < currentPatternWidth) {
                    if (fragment[r][c] != 0) { // Only insert non-zero pixels (non-background)
                        pattern[targetRow][targetCol] = fragment[r][c];
                    }
                }
            }
        }
    }

    private static class BoundingBox {
        int minRow = Integer.MAX_VALUE;
        int maxRow = -1;
        int minCol = Integer.MAX_VALUE;
        int maxCol = -1;

        boolean isEmpty() {
            return minRow > maxRow || minCol > maxCol;
        }
    }

    private BoundingBox findActivePatternBounds() {
        BoundingBox bounds = new BoundingBox();
        for (int r = 0; r < currentPatternHeight; r++) {
            for (int c = 0; c < currentPatternWidth; c++) {
                if (pattern[r][c] != 0) {
                    bounds.minRow = Math.min(bounds.minRow, r);
                    bounds.maxRow = Math.max(bounds.maxRow, r);
                    bounds.minCol = Math.min(bounds.minCol, c);
                    bounds.maxCol = Math.max(bounds.maxCol, c);
                }
            }
        }
        return bounds;
    }

    private int[][] extractFullPatternFragment(BoundingBox bounds) {
        if (bounds.isEmpty()) {
            return null; // No active pattern
        }

        int fragRows = bounds.maxRow - bounds.minRow + 1;
        int fragCols = bounds.maxCol - bounds.minCol + 1;
        int[][] fragment = new int[fragRows][fragCols];

        for (int r = 0; r < fragRows; r++) {
            for (int c = 0; c < fragCols; c++) {
                fragment[r][c] = pattern[bounds.minRow + r][bounds.minCol + c];
            }
        }
        return fragment;
    }

    // Змінений метод duplicateFullPatternRight для дзеркального відображення
    public void duplicateFullPatternRight() {
        BoundingBox bounds = findActivePatternBounds();
        if (bounds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pattern on the canvas to duplicate.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[][] originalFragment = extractFullPatternFragment(bounds);
        if (originalFragment == null) return;

        int[][] flippedFragment = flipFragmentHorizontal(originalFragment); // Дзеркальне відображення по горизонталі

        // Start duplication immediately after the right edge of the original pattern
        int targetCol = bounds.maxCol + 1;

        overlayFragment(flippedFragment, bounds.minRow, targetCol);
        repaint();
    }

    // Змінений метод duplicateFullPatternLeft для дзеркального відображення
    public void duplicateFullPatternLeft() {
        BoundingBox bounds = findActivePatternBounds();
        if (bounds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pattern on the canvas to duplicate.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[][] originalFragment = extractFullPatternFragment(bounds);
        if (originalFragment == null) return;

        int[][] flippedFragment = flipFragmentHorizontal(originalFragment); // Дзеркальне відображення по горизонталі

        int fragCols = flippedFragment[0].length; // Використовуємо розмір віддзеркаленого фрагмента

        // Start duplication before the left edge of the original pattern
        int targetCol = bounds.minCol - fragCols;

        overlayFragment(flippedFragment, bounds.minRow, targetCol);
        repaint();
    }

    // Змінений метод duplicateFullPatternUp для дзеркального відображення
    public void duplicateFullPatternUp() {
        BoundingBox bounds = findActivePatternBounds();
        if (bounds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pattern on the canvas to duplicate.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[][] originalFragment = extractFullPatternFragment(bounds);
        if (originalFragment == null) return;

        int[][] flippedFragment = flipFragmentVertical(originalFragment); // Дзеркальне відображення по вертикалі

        int fragRows = flippedFragment.length; // Використовуємо розмір віддзеркаленого фрагмента

        // Start duplication above the top edge of the original pattern
        int targetRow = bounds.minRow - fragRows;

        overlayFragment(flippedFragment, targetRow, bounds.minCol);
        repaint();
    }

    // Змінений метод duplicateFullPatternDown для дзеркального відображення
    public void duplicateFullPatternDown() {
        BoundingBox bounds = findActivePatternBounds();
        if (bounds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pattern on the canvas to duplicate.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[][] originalFragment = extractFullPatternFragment(bounds);
        if (originalFragment == null) return;

        int[][] flippedFragment = flipFragmentVertical(originalFragment); // Дзеркальне відображення по вертикалі

        int fragRows = flippedFragment.length; // Використовуємо розмір віддзеркаленого фрагмента

        // Start duplication immediately after the bottom edge of the original pattern
        int targetRow = bounds.maxRow + 1;

        overlayFragment(flippedFragment, targetRow, bounds.minCol);
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ukrainian Folk Ornament - Kateryna Astashenkova");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            EmbroideryPattern patternPanel = new EmbroideryPattern();
            frame.add(patternPanel, BorderLayout.CENTER);

            // Control panel for all buttons and options
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical arrangement

            // Panel for action buttons (New Pattern, Clear, Save, Open)
            JPanel actionButtonsPanel = new JPanel();
            actionButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            JButton newPatternBtn = new JButton("Kateryna Pattern");
            newPatternBtn.addActionListener(e -> {
                patternPanel.calculateKaterynaOffset(); // Recalculate offset before generation
                patternPanel.generateKaterynaPattern();
            });
            actionButtonsPanel.add(newPatternBtn);

            JButton clearBtn = new JButton("Clear");
            clearBtn.addActionListener(e -> {
                for (int r = 0; r < patternPanel.currentPatternHeight; r++) {
                    for (int c = 0; c < patternPanel.currentPatternWidth; c++) {
                        patternPanel.pattern[r][c] = 0; // Set all to background
                    }
                }
                patternPanel.repaint();
            });
            actionButtonsPanel.add(clearBtn);

            JButton saveBtn = new JButton("Save PNG");
            saveBtn.addActionListener(e -> patternPanel.savePatternAsPng());
            actionButtonsPanel.add(saveBtn);

            JButton openBtn = new JButton("Open PNG");
            openBtn.addActionListener(e -> patternPanel.openPatternFromPng());
            actionButtonsPanel.add(openBtn);


            // Panel for color selection
            JPanel colorPanel = new JPanel();
            colorPanel.setBorder(BorderFactory.createTitledBorder("Color"));
            JButton redBtn = new JButton("Red");
            redBtn.setBackground(RED);
            redBtn.addActionListener(e -> patternPanel.currentColor = RED);
            colorPanel.add(redBtn);

            JButton blackBtn = new JButton("Black");
            blackBtn.setBackground(BLACK);
            blackBtn.setForeground(WHITE); // Make text visible
            blackBtn.addActionListener(e -> patternPanel.currentColor = BLACK);
            colorPanel.add(blackBtn);

            JButton whiteBtn = new JButton("White (Ornament)");
            whiteBtn.setBackground(WHITE);
            whiteBtn.addActionListener(e -> patternPanel.currentColor = WHITE);
            colorPanel.add(whiteBtn);

            JButton eraserBtn = new JButton("Eraser");
            eraserBtn.addActionListener(e -> patternPanel.drawMode = false);
            colorPanel.add(eraserBtn);

            JButton drawModeBtn = new JButton("Draw Mode");
            drawModeBtn.addActionListener(e -> patternPanel.drawMode = true);
            colorPanel.add(drawModeBtn);


            // Panel for drawing symmetry modes
            JPanel symmetryPanel = new JPanel();
            symmetryPanel.setBorder(BorderFactory.createTitledBorder("Drawing Symmetry"));
            JComboBox<SymmetryMode> symmetryComboBox = new JComboBox<>(SymmetryMode.values());
            symmetryComboBox.setSelectedItem(SymmetryMode.NONE); // Default to no symmetry for drawing
            symmetryComboBox.addActionListener(e -> patternPanel.symmetryMode = (SymmetryMode) symmetryComboBox.getSelectedItem());
            symmetryPanel.add(new JLabel("Mode:"));
            symmetryPanel.add(symmetryComboBox);


            // Panel for fragment duplication (with specified coordinates)
            JPanel duplicationPanel = new JPanel();
            duplicationPanel.setBorder(BorderFactory.createTitledBorder("Fragment Duplication (Specified Coordinates)"));
            JTextField rowStartField = new JTextField("0", 3);
            JTextField colStartField = new JTextField("0", 3);
            JTextField rowsField = new JTextField(String.valueOf(KATERNA_PATTERN_SIZE), 3); // Default to Kateryna size
            JTextField colsField = new JTextField(String.valueOf(KATERNA_PATTERN_SIZE), 3); // Default to Kateryna size

            // Changed combo box to reflect that duplication now applies flips
            JComboBox<SymmetryMode> duplicationSymmetryComboBox = new JComboBox<>(SymmetryMode.values());
            duplicationSymmetryComboBox.setSelectedItem(SymmetryMode.NONE);

            JButton applyDuplicationBtn = new JButton("Apply Duplication (with flip)"); // Змінена назва кнопки
            applyDuplicationBtn.addActionListener(e -> {
                try {
                    int rStart = Integer.parseInt(rowStartField.getText());
                    int cStart = Integer.parseInt(colStartField.getText());
                    int rNum = Integer.parseInt(rowsField.getText());
                    int cNum = Integer.parseInt(colsField.getText());
                    SymmetryMode mode = (SymmetryMode) duplicationSymmetryComboBox.getSelectedItem();

                    // Updated bounds check, considering that `pattern` now has `currentPatternWidth/Height` size
                    if (rStart < 0 || cStart < 0 || rNum <= 0 || cNum <= 0 ||
                            rStart + rNum > patternPanel.currentPatternHeight || cStart + cNum > patternPanel.currentPatternWidth) {
                        JOptionPane.showMessageDialog(frame, "Invalid fragment dimensions or coordinates! They must be within 0-" + (patternPanel.currentPatternHeight - 1) + " for height and 0-" + (patternPanel.currentPatternWidth - 1) + " for width.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    patternPanel.applyFragmentDuplication(rStart, cStart, rNum, cNum, mode);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers for fragment dimensions.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            duplicationPanel.add(new JLabel("Row Start:"));
            duplicationPanel.add(rowStartField);
            duplicationPanel.add(new JLabel("Col Start:"));
            duplicationPanel.add(colStartField);
            duplicationPanel.add(new JLabel("Rows:"));
            duplicationPanel.add(rowsField);
            duplicationPanel.add(new JLabel("Cols:"));
            duplicationPanel.add(colsField);
            duplicationPanel.add(new JLabel("Duplication Symmetry:"));
            duplicationPanel.add(duplicationSymmetryComboBox);
            duplicationPanel.add(applyDuplicationBtn);

            // Panel for duplicating the entire active pattern
            JPanel fullPatternDuplicationPanel = new JPanel();
            fullPatternDuplicationPanel.setBorder(BorderFactory.createTitledBorder("Duplicate Entire Pattern (with flip)")); // Змінена назва заголовка

            JButton dupRightBtn = new JButton("Duplicate Right");
            dupRightBtn.addActionListener(e -> patternPanel.duplicateFullPatternRight());
            fullPatternDuplicationPanel.add(dupRightBtn);

            JButton dupLeftBtn = new JButton("Duplicate Left");
            dupLeftBtn.addActionListener(e -> patternPanel.duplicateFullPatternLeft());
            fullPatternDuplicationPanel.add(dupLeftBtn);

            JButton dupUpBtn = new JButton("Duplicate Up");
            dupUpBtn.addActionListener(e -> patternPanel.duplicateFullPatternUp());
            fullPatternDuplicationPanel.add(dupUpBtn);

            JButton dupDownBtn = new JButton("Duplicate Down");
            dupDownBtn.addActionListener(e -> patternPanel.duplicateFullPatternDown());
            fullPatternDuplicationPanel.add(dupDownBtn);


            // Add all sub-panels to the main control panel
            controlPanel.add(actionButtonsPanel);
            controlPanel.add(colorPanel);
            controlPanel.add(symmetryPanel);
            controlPanel.add(duplicationPanel);
            controlPanel.add(fullPatternDuplicationPanel);


            frame.add(controlPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null); // Center the frame on the screen
            frame.setVisible(true);

            // Call these methods after the frame is fully set up and visible
            patternPanel.adjustPatternDimensions();
            patternPanel.calculateKaterynaOffset();
            patternPanel.generateKaterynaPattern();
        });
    }
}