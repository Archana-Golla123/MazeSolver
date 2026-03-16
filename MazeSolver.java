import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;

public class MazeSolver extends JPanel {
    private final int rows = 10;
    private final int cols = 16;
    private final int cellSize = 50;

    private int[][] maze = new int[rows][cols]; // 0=path,1=wall,2=start,3=end
    private boolean[][] visited;
    private java.util.List<Point> path = new ArrayList<>();
    private Point start = null, end = null;

    public MazeSolver() {
        setPreferredSize(new java.awt.Dimension(cols * cellSize, rows * cellSize));
        visited = new boolean[rows][cols];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / cellSize;
                int row = e.getY() / cellSize;
                if (row >= rows || col >= cols) return;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Left click → toggle wall
                    if (maze[row][col] == 0) maze[row][col] = 1;
                    else if (maze[row][col] == 1) maze[row][col] = 0;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Right click → set start/end
                    if (start == null) {
                        maze[row][col] = 2;
                        start = new Point(row, col);
                    } else if (end == null && maze[row][col] != 2) {
                        maze[row][col] = 3;
                        end = new Point(row, col);
                    }
                }
                repaint();
            }
        });
    }

    public void solveMaze() {
        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Set start and end points first!");
            return;
        }

        path.clear();
        visited = new boolean[rows][cols];

        new Thread(() -> {
            Queue<java.util.List<Point>> queue = new LinkedList<>();
            queue.add(new ArrayList<>(java.util.List.of(start)));
            visited[start.x][start.y] = true;

            while (!queue.isEmpty()) {
                java.util.List<Point> curPath = queue.poll();
                Point cur = curPath.get(curPath.size() - 1);

                path = curPath;
                repaint();

                if (cur.equals(end)) return;

                try { Thread.sleep(50); } catch (Exception ignored) {}

                for (Point next : neighbors(cur)) {
                    if (!visited[next.x][next.y] && maze[next.x][next.y] != 1) {
                        visited[next.x][next.y] = true;
                        java.util.List<Point> newPath = new ArrayList<>(curPath);
                        newPath.add(next);
                        queue.add(newPath);
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "No solution exists!");
        }).start();
    }

    private java.util.List<Point> neighbors(Point p) {
        java.util.List<Point> result = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = p.x + d[0], ny = p.y + d[1];
            if (nx >= 0 && ny >= 0 && nx < rows && ny < cols)
                result.add(new Point(nx, ny));
        }
        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = cellSize / 3;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Color color;
                if (maze[i][j] == 1) color = new Color(33,33,33);       // Wall
                else if (maze[i][j] == 2) color = new Color(0,200,83);  // Start
                else if (maze[i][j] == 3) color = new Color(229,57,53);// End
                else if (path.contains(new Point(i,j))) color = new Color(255,235,59); // Path
                else if (visited[i][j]) color = new Color(30,136,229); // Visited
                else color = new Color(66,66,66);                       // Empty

                g2.setColor(color);
                g2.fillRoundRect(j*cellSize, i*cellSize, cellSize-2, cellSize-2, arc, arc);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(j*cellSize, i*cellSize, cellSize-2, cellSize-2, arc, arc);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Interactive Maze Solver");
            MazeSolver solver = new MazeSolver();

            JButton solveButton = new JButton("Solve Maze");
            solveButton.addActionListener(e -> solver.solveMaze());

            JButton resetButton = new JButton("Reset Maze");
            resetButton.addActionListener(e -> {
                solver.maze = new int[solver.rows][solver.cols];
                solver.start = null;
                solver.end = null;
                solver.path.clear();
                solver.visited = new boolean[solver.rows][solver.cols];
                solver.repaint();
            });

            JPanel panel = new JPanel();
            panel.add(solveButton);
            panel.add(resetButton);

            frame.setLayout(new BorderLayout());
            frame.add(solver, BorderLayout.CENTER);
            frame.add(panel, BorderLayout.SOUTH);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}