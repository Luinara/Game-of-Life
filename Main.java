package life;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static GameOfLife G;
    static AlgWorker worker;
    private static boolean paused;
    private static boolean ANewUniverse = true;
    private static boolean stopped = false;
    private static boolean finished = false;

    public static void main(String[] args) {
       G = new GameOfLife();

    }

    public static void btnPlay_Click (int times, Universe universe) {
        stopped = false;
        if (ANewUniverse) {
            worker = new AlgWorker(times, universe, G);
            worker.start();
            paused = false;
            ANewUniverse = false;
        } else if (paused) {
            paused = false;
        } else if (finished) {
            finished = false;
            worker = new AlgWorker(times, universe, G);
            worker.start();
            paused = false;
            ANewUniverse = false;
        }
    }

    public static void btnBreak_Click () {
        paused = true;
    }

    public static void btnStop_Click () {
        stopped = true;
        ANewUniverse = true;
        try {
            worker.join(3000);
        } catch (InterruptedException ignored) {
        }
    }

    public static boolean isPaused() {
        return paused;
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static void setFinished(boolean finished ) {
        Main.finished = finished;
    }

    @Deprecated
    public static String handleInput() {
        Scanner sc = new Scanner(System.in);
        while (true){
            String str = sc.nextLine();
            if (str.matches("\\d+")) {
                return str;
            }
            System.out.println("Please enter the size of the universe in numbers.");
        }
    }

}

class Universe {
    public static int n;
    private boolean[][] universe;
    private final Random rnd = new Random();

    public Universe(int n) {
        Universe.n = n;
        universe = init();
    }

    private boolean[][] init(){
        boolean[][] universe = new boolean[n][n];
        for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < n;j ++) {
                universe[i][j] = rnd.nextBoolean();
            }
        }
        return universe;
    }

    public boolean[][] getUniverse() {
        return universe;
    }

    public void setUniverse(boolean[][] universe) {
        this.universe = universe;
    }

    @Deprecated
    public void displayUniverse(int generation, int alive){
        System.out.println("Generation #" + generation);
        System.out.println("Alive: " + alive);
        System.out.println();
        for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < n; j++ ) {
                System.out.print(universe[i][j]? 'O':' ');
            }
            System.out.println();
        }
        System.out.println();
    }

    public int getAliveCells() {
        int c = 0;
        for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < n; j++ ) {
                if (universe[i][j]) {
                    c++;
                }
            }
        }
        return c;
    }


}

class GenerationAlgorithm {
    boolean[][] oldGeneration;
    boolean[][] newGeneration = new boolean[Universe.n][Universe.n];
    private static int sleep;

    public GenerationAlgorithm(boolean[][] state) {
        this.oldGeneration = state;
    }

    public void callNextGeneration(int times, Universe universe, GameOfLife game) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        for ( int i = 0; i < times; i++ ) {


            game.adaptGeneration(i + 1,universe.getAliveCells(),universe.getUniverse());
            changeGeneration();
            oldGeneration = newGeneration.clone();
            universe.setUniverse(oldGeneration);


            if (i < times - 1) {
                newGeneration = new boolean[Universe.n][Universe.n];
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }

            while (Main.isPaused()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }

            if (Main.isStopped()) {
                break;
            }
        }

        Main.setFinished(true);
        game.finished();
    }

    private void changeGeneration() {
        for ( int i = 0; i < Universe.n; i++ ) {
            for ( int j = 0; j < Universe.n; j++ ) {
                if (!oldGeneration[i][j]) {
                    newGeneration[i][j] = hasNeighbours(3, i, j);
                } else {
                    newGeneration[i][j] = hasNeighbours(2, i, j) || hasNeighbours(3, i, j);
                }
            }
        }
    }

    private  boolean hasNeighbours(int neighbours, int x, int y) {
        int c = 0;
        for ( int i = Math.max(x - 1, 0); i < Math.min(x + 2, Universe.n); i++ ) {
            for ( int j = Math.max(y - 1, 0); j < Math.min(y + 2, Universe.n); j++ ) {
                if (oldGeneration[i][j] && !(i == x && j == y)) {
                    c++;
                }
            }
        }
        // making universe periodic
        // covering the edges

        // upper edge
        if (x - 1 < 0 && !(y - 1 < 0) && !(y + 1 > Universe.n - 1)) {
            for ( int i = y - 1; i <= y + 1; i++ ) {
                if (oldGeneration[Universe.n - 1][i]) {c++;}
            }
        }

        // right edge edge
        if (y - 1 < 0 && !(x - 1 < 0) && !(x + 1 > Universe.n - 1)) {
            for ( int i = x - 1; i <= x + 1; i++ ) {
                if (oldGeneration[i][Universe.n - 1]) { c++;}
            }
        }

        // lower edge
        if (x + 1 > Universe.n - 1 && !(y - 1 < 0) && !(y + 1 > Universe.n - 1)) {
            for ( int i = y - 1; i <= y + 1; i++ ) {
                if (oldGeneration[0][i]) {c++;}
            }
        }

        // left edge
        if (y + 1 > Universe.n - 1 && !(x - 1 < 0) && !(x + 1 > Universe.n - 1)) {
            for ( int i = x - 1; i <= x + 1; i++ ) {
                if (oldGeneration[i][0]) {c++;}
            }
        }

        // covering the corners
        // upper right
        if (x - 1 < 0 && y - 1 < 0) {
            if (oldGeneration[0][Universe.n - 1]) {c++;}
            if (oldGeneration[1][Universe.n - 1]) {c++;}
            if (oldGeneration[Universe.n - 1][0]) {c++;}
            if (oldGeneration[Universe.n - 1][1]) {c++;}
            if (oldGeneration[Universe.n - 1][Universe.n - 1]) {c++;}
        }

        // upper left
        if (x - 1 < 0 && y + 1 > Universe.n - 1) {
            if (oldGeneration[0][0]) {c++;}
            if (oldGeneration[1][0]) {c++;}
            if (oldGeneration[Universe.n - 1][0]) {c++;}
            if (oldGeneration[Universe.n - 1][Universe.n - 1]) {c++;}
            if (oldGeneration[Universe.n - 1][Universe.n - 2]) {c++;}
        }

        // lower right
        if (x + 1 > Universe.n - 1 && y - 1 < 0) {
            if (oldGeneration[0][0]) {c++;}
            if (oldGeneration[0][1]) {c++;}
            if (oldGeneration[0][Universe.n - 1]) {c++;}
            if (oldGeneration[Universe.n - 1][Universe.n - 1]) {c++;}
            if (oldGeneration[Universe.n - 2][Universe.n - 1]) {c++;}
        }

        // lower left
        if (x + 1 > Universe.n - 1 && y + 1 > Universe.n - 1) {
            if (oldGeneration[0][0]) {c++;}
            if (oldGeneration[0][Universe.n - 2]) {c++;}
            if (oldGeneration[0][Universe.n - 1]) {c++;}
            if (oldGeneration[Universe.n - 2][0]) {c++;}
            if (oldGeneration[Universe.n - 1][0]) {c++;}
        }

        return c == neighbours;
    }

    public static void setSleep(int sleep) {
        GenerationAlgorithm.sleep = sleep;
    }
}

class AlgWorker extends Thread {
    int times;
    Universe universe;
    GameOfLife G;

    public AlgWorker(int times,Universe universe, GameOfLife G) {
        super();
        this.universe = universe;
        this.times = times;
        this.G = G;
    }

    @Override
    public void run() {
        GenerationAlgorithm alg = new GenerationAlgorithm(universe.getUniverse());
        alg.callNextGeneration(times,universe,G);
    }
}

class GameOfLife extends JFrame {
    JLabel a;
    JLabel g;
    rightPanel right;
    JButton btnPlay;
    JButton btnBreak;
    JButton btnStop;
    JButton btnRepeat;

    Universe universe;
    int c1 = 0;

    int n;
    int times = 200;
    int sleep;
    Color MainColor = Color.BLACK;

    public GameOfLife() {
        super("Game of Live");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLayout();

        setMinimumSize(new Dimension(780,550));
        setSize(780, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JPanel jpLeft = new JPanel();
        jpLeft.setBounds(0,0,100,550);
        jpLeft.setLayout(new BorderLayout());

            JToolBar tbMenu = new JToolBar();

                btnPlay = new JButton("Play");
                tbMenu.add(btnPlay);

                btnBreak = new JButton("Break");
                btnBreak.setEnabled(false);
                tbMenu.add(btnBreak);

                btnStop = new JButton("Stop");
                btnStop.setEnabled(false);
                tbMenu.add(btnStop);

                btnRepeat = new JButton("Restart");
                btnRepeat.setEnabled(false);
                tbMenu.add(btnRepeat);
            jpLeft.add(tbMenu, BorderLayout.NORTH);


            JPanel jpStatus = new JPanel();
            jpStatus.setBounds(0,470,100,30);
            jpStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));
            jpStatus.setLayout(new BoxLayout(jpStatus, BoxLayout.Y_AXIS));

                JSeparator sStatus = new JSeparator(JSeparator.HORIZONTAL);
                sStatus.setForeground(Color.BLACK);
                jpStatus.add(sStatus);

                JLabel lbGeneration = new JLabel("Generation #0");
                lbGeneration.setName("GenerationLabel");
                lbGeneration.setFont(new Font("Dialog",Font.BOLD,16));
                lbGeneration.setBounds(10,5,300,10);
                lbGeneration.setHorizontalAlignment(SwingConstants.LEFT);
                jpStatus.add(lbGeneration);
                g = lbGeneration;

                JLabel lbAlive = new JLabel("Alive: 0");
                lbAlive.setName("AliveLabel");
                lbAlive.setFont(new Font("Dialog",Font.BOLD,16));
                lbAlive.setBounds(10,20,300,10);
                lbAlive.setHorizontalAlignment(SwingConstants.LEFT);
                jpStatus.add(lbAlive);
                a = lbAlive;

            jpLeft.add(jpStatus,BorderLayout.SOUTH);

            
            JPanel jpCustom = new JPanel();

                JPanel jpColor = new JPanel();
                jpColor.setBounds(0,100,100,100);
                jpColor.setLayout(new BorderLayout());
                jpColor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));

                    JPanel jpOColor = new JPanel(new BorderLayout());

                        JLabel lbColor = new JLabel("Change Color");
                        lbColor.setFont(new Font("Dialog",Font.BOLD,16));
                        lbColor.setHorizontalAlignment(SwingConstants.LEFT);
                        jpOColor.add(lbColor,BorderLayout.WEST);

                        JPanel jpComBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        jpComBox.setBounds(10,10,100,50);
                        jpComBox.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                            String[] colors = {"Black","Blue","Cyan","Dark Grey","Grey","Green","Light Grey","Magenta","Orange","Pink","Red","Yellow"};
                            JComboBox<String> jcColor = new JComboBox<>(colors);
                            jcColor.setSize(100,10);
                            jpComBox.add(jcColor);
                        jpOColor.add(jpComBox,BorderLayout.SOUTH);
                    jpColor.add(jpOColor,BorderLayout.NORTH);

                    JSeparator sNorth = new JSeparator(JSeparator.HORIZONTAL);
                    sNorth.setForeground(Color.BLACK);
                    jpColor.add(sNorth,BorderLayout.SOUTH);
                jpCustom.add(jpColor);


                JPanel jpSpeed = new JPanel();
                jpSpeed.setBounds(0,200,100,100);
                jpSpeed.setLayout(new BorderLayout());
                jpSpeed.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));

                    JLabel lbSpeed = new JLabel("Evolution Speed:");
                    lbSpeed.setFont(new Font("Dialog",Font.BOLD,16));
                    jpSpeed.add(lbSpeed,BorderLayout.NORTH);

                    JSlider slSpeed = new JSlider(10,100,50);
                    slSpeed.setPreferredSize(new Dimension(80,60));
                    slSpeed.setMajorTickSpacing(10);
                    slSpeed.setMinorTickSpacing(5);
                    slSpeed.setPaintTicks(true);
                    slSpeed.setPaintLabels(true);
                    sleep = slSpeed.getValue() * 10;
                    GenerationAlgorithm.setSleep(sleep);
                    jpSpeed.add(slSpeed,BorderLayout.CENTER);

                    JSeparator sCenter = new JSeparator(JSeparator.HORIZONTAL);
                    sCenter.setForeground(Color.BLACK);
                    jpSpeed.add(sCenter,BorderLayout.SOUTH);
                jpCustom.add(jpSpeed);


                JPanel jpSize = new JPanel();
                jpSize.setBounds(0,300,100,100);
                jpSize.setLayout(new BorderLayout());
                jpSize.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));
                jpCustom.add(jpSize);

                    JLabel lbSize = new JLabel("Universe Size NxN:");
                    lbSize.setFont(new Font("Dialog",Font.BOLD,16));
                    jpSize.add(lbSize,BorderLayout.NORTH);

                    JSlider slSize = new JSlider(10,100,50);
                    slSize.setPreferredSize(new Dimension(100,60));
                    slSize.setMajorTickSpacing(10);
                    slSize.setMinorTickSpacing(5);
                    slSize.setPaintTicks(true);
                    slSize.setPaintLabels(true);
                    n = slSize.getValue();
                    universe = new Universe(n);
                    jpSize.add(slSize,BorderLayout.CENTER);

                    JSeparator sSouth = new JSeparator(JSeparator.HORIZONTAL);
                    sSouth.setForeground(Color.BLACK);
                    jpSize.add(sSouth,BorderLayout.SOUTH);

                JPanel jpNOGen = new JPanel();
                jpNOGen.setBounds(0,400,120,100);
                jpNOGen.setLayout(new BorderLayout());
                jpNOGen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));
                jpCustom.add(jpNOGen);

                    JLabel lbNOGen = new JLabel("Number of Generations:");
                    lbNOGen.setFont(new Font("Dialog",Font.BOLD,16));
                    jpNOGen.add(lbNOGen,BorderLayout.NORTH);

                    JPanel jpText = new JPanel(new BorderLayout());
                    jpText.setSize(100,40);
                    jpText.setBorder(BorderFactory.createEmptyBorder(10, 0, 10,30));
                        JTextField txNOGen = new JTextField();
                        txNOGen.setText("Enter the number");
                        txNOGen.setBounds(10,20,200,25);
                        txNOGen.setFont(new Font("Dialog",Font.PLAIN,14));
                        jpText.add(txNOGen,BorderLayout.NORTH);

                        JLabel lbEx = new JLabel("Press Enter after writing the number");
                        lbEx.setFont(new Font("Dialog",Font.PLAIN,12));
                        jpText.add(lbEx,BorderLayout.SOUTH);
                    jpNOGen.add(jpText,BorderLayout.SOUTH);

            jpCustom.setLayout(new BoxLayout(jpCustom, BoxLayout.Y_AXIS));
            jpLeft.add(jpCustom,BorderLayout.CENTER);

        add(jpLeft,BorderLayout.WEST);

        JPanel jpLay = new JPanel(new BorderLayout());
            JSeparator middle = new JSeparator(JSeparator.VERTICAL);
            middle.setForeground(Color.BLACK);
            jpLay.add(middle,BorderLayout.WEST);

            right = new rightPanel();
            //right.setLayout(new GridLayout(Universe.n, Universe.n, 0, 0));
            jpLay.add(right,BorderLayout.CENTER);
        add(jpLay,BorderLayout.CENTER);

        btnPlay.addActionListener(actionEvent -> {
            if (c1 == 0) {
                n = slSize.getValue();
                universe = new Universe(n);
            }
            c1++;
            Main.btnPlay_Click(times,universe);
            btnPlay.setEnabled(false);
            btnBreak.setEnabled(true);
            btnStop.setEnabled(true);
            btnRepeat.setEnabled(true);
        });

        btnBreak.addActionListener(actionEvent -> {
            Main.btnBreak_Click();
            btnPlay.setEnabled(true);
            btnBreak.setEnabled(false);
            btnStop.setEnabled(false);
            btnRepeat.setEnabled(false);
        });

        btnStop.addActionListener(actionEvent -> {
            int t = slSize.getValue();
            MainColor = Color.WHITE;
            Main.btnStop_Click();
            universe = new Universe(t);
            adaptGeneration(0,0, new boolean[t][t]);
            setMainColor(jcColor);
            btnStop.setEnabled(false);
            btnRepeat.setEnabled(false);
            btnPlay.setEnabled(true);
            btnBreak.setEnabled(false);
        });

        btnRepeat.addActionListener(actionEvent -> {
            int t = slSize.getValue();
            MainColor = Color.WHITE;
            Main.btnStop_Click();
            universe = new Universe(t);
            adaptGeneration(0,0, new boolean[t][t]);
            setMainColor(jcColor);
            Main.btnPlay_Click(times,universe);
            btnPlay.setEnabled(false);
            btnBreak.setEnabled(true);
            btnStop.setEnabled(true);
            btnRepeat.setEnabled(true);
        });

        jcColor.addActionListener(actionEvent -> setMainColor(jcColor));

        slSpeed.addChangeListener(changeEvent -> {
            sleep = slSpeed.getValue() * 10;
            GenerationAlgorithm.setSleep(sleep);
        });

        txNOGen.addActionListener(actionEvent -> {
            String str = txNOGen.getText();
            if (str.matches("\\d+")) {
                times = Integer.parseInt(str);
            } else {
                lbEx.setText("Wrong enter");
            }
        });

    }

    private void setMainColor(JComboBox<String> jcColor) {
        String color = (String) jcColor.getSelectedItem();
        switch (Objects.requireNonNull(color)){
            case "Black":
                MainColor = Color.BLACK;
                break;
            case "Blue":
                MainColor = Color.BLUE;
                break;
            case "Cyan":
                MainColor = Color.CYAN;
                break;
            case "Dark Grey":
                MainColor = Color.DARK_GRAY;
                break;
            case "Grey":
                MainColor = Color.GRAY;
                break;
            case "Green":
                MainColor = Color.GREEN;
                break;
            case "Light Grey":
                MainColor = Color.LIGHT_GRAY;
                break;
            case "Magenta":
                MainColor = Color.MAGENTA;
                break;
            case "Orange":
                MainColor = Color.ORANGE;
                break;
            case "Pink":
                MainColor = Color.PINK;
                break;
            case "Red":
                MainColor = Color.RED;
                break;
            case "Yellow":
                MainColor = Color.YELLOW;
                break;
        }
    }

    public void adaptGeneration(int generation, int alive, boolean[][] arr) {
        a.setText(String.format("Alive: %d", alive));
        g.setText(String.format("Generation #%d",generation));
        right.adapt(arr, MainColor);
        right.repaint();
    }

    public void finished() {
        btnPlay.setEnabled(true);
        btnBreak.setEnabled(false);
        btnStop.setEnabled(true);
        btnRepeat.setEnabled(true);
    }

}

class rightPanel extends JPanel {
    Square[][] squares = new Square[Universe.n][Universe.n];
    boolean[][] current = new boolean[Universe.n][Universe.n];
    Color c;
    int oldN;

    public rightPanel() {
        oldN = Universe.n;
        int cellSize = Math.min(this.getWidth() - 12, this.getHeight() - 10) / Universe.n;
        setPreferredSize(new Dimension(512,510));
        int k = (this.getWidth() - cellSize * Universe.n) / 2;
        int l = (this.getHeight() - cellSize * Universe.n) / 2;
        for ( int i = 0; i < Universe.n; i++ ) {
            for ( int j = 0; j < Universe.n; j++ ) {
                squares[i][j] = new Square(j * cellSize + k, i * cellSize + l, cellSize, Color.WHITE);
            }
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        reSize();

        for ( int i = 0; i < current.length; i++ ) {
            for ( int j = 0; j < current.length; j++ ) {
                if (current[i][j]) {
                    squares[i][j].setColor(c);
                    squares[i][j].reColor(g);
                } else {
                    squares[i][j].whiten(g);
                }
            }
        }
    }

    private void reSize() {
        squares = new Square[current.length][current.length];
        int cellSize = Math.min(this.getWidth() - 12, this.getHeight() - 10) / current.length;
        int k = (this.getWidth() - cellSize * Universe.n) / 2;
        int l = (this.getHeight() - cellSize * Universe.n) / 2;
        for ( int i = 0; i < current.length; i++ ) {
            for ( int j = 0; j < current.length; j++ ) {
                squares[i][j] = new Square(j * cellSize + k, i * cellSize + l, cellSize, Color.WHITE);
            }
        }
    }

    public void adapt(boolean[][] arr, Color color) {
        this.current = arr;
        this.c = color;
    }

}

class Square {
    int x, y, side;
    Color color;

    public Square(int x, int y, int side, Color color) {
        this.x = x;
        this.y = y;
        this.side = side;
        this.color = color;
    }

    @Deprecated
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle square = new Rectangle(x,y,side,side);

        g2d.setColor(color);
        g2d.fill(square);
        g2d.setColor(Color.BLACK);
        g2d.draw(square);
    }

    public void reColor(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle square = new Rectangle(x,y,side,side);

        g2d.setColor(color);
        g2d.fill(square);
        g2d.setColor(Color.BLACK);
        g2d.draw(square);
    }

    public void whiten(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle square = new Rectangle(x,y,side,side);

        g2d.setColor(Color.WHITE);
        g2d.fill(square);
        g2d.setColor(Color.BLACK);
        g2d.draw(square);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}