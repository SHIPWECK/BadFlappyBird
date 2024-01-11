package BadFlappyGame.BadFlappyBird;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * This code was written within my first 4 months of coding.
 * All I knew when writing this was Bro Code's full Java course (https://www.youtube.com/watch?v=xk4_1vDrzzo&ab_channel=BroCode).
 * It was horribly unreadable with extra threads and just a mess.
 * I have since made it more readable but left the logic the same.
 * Let this be an artifact of my code from when it was even worse than it is today.
 */

public class Main {
    public static void main(String[] args) {
        new FlappyGame();
    }
}

class FlappyGame implements KeyListener {
    protected Random rand = new Random();
    protected int Score;
    private final static ImageIcon flappyImg = new ImageIcon("FlappyGameBad/flappy.png");

    protected JFrame gameWindow;

    protected JPanel flappy;
    protected JLabel flappyImgLabel; 

    protected final int 
        PIPE_WIDTH = 50,
        PIPE_HEIGHT = 500,
        PIPE_OFFSET = 150,
        FLAPPY_WIDTH = 35,
        FLAPPY_HEIGHT = 9; 

    protected JPanel 
        topPipe1,
        bottemPipe1,
        topPipe2,
        bottemPipe2,
        topPipe3,
        bottemPipe3,
        topPipe4,
        bottemPipe4,
        topPipe5,
        bottemPipe5;

    protected JPanel[] allPipes = {
        topPipe1, 
        bottemPipe1, 
        topPipe2, 
        bottemPipe2, 
        topPipe3, 
        bottemPipe3, 
        topPipe4, 
        bottemPipe4, 
        topPipe5, 
        bottemPipe5
    };

    protected boolean isPaused = false;

    FlappyGame() {
        gameWindow = new JFrame("Flappy Bird");
        gameWindow.setResizable(false);
        gameWindow.setLayout(null);
        gameWindow.addKeyListener(this);
        gameWindow.setBounds(0, 0, 500, 700);
        gameWindow.setIconImage(flappyImg.getImage());
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.getContentPane().setBackground(new Color(135, 206, 235));

        flappy = new JPanel();
        flappyImgLabel = new JLabel(flappyImg);
        
        flappy.setBackground(new Color(0, 0, 0, 0));
        flappy.setBounds(100, 200, FLAPPY_WIDTH, FLAPPY_HEIGHT+20);
        flappy.add(flappyImgLabel);

        // defines all the pipes and adds them to the gamewindow
        for (int i = 0; i < allPipes.length; i++) {
            allPipes[i] = new JPanel();
            allPipes[i].setBackground(Color.GREEN);

            // sees if the pipe is top or bottom and sets its position to be either on top or on the bottom.
            if (i % 2 == 0) 
                // sets the X of the pipe slightly offscreen and the top pipe a sligtly randomized distance
                allPipes[i].setBounds(gameWindow.getWidth() + (PIPE_WIDTH + 30) * i, rand.nextInt(-300, -150), PIPE_WIDTH, PIPE_HEIGHT);
            else 
                // sets the X the same way and sets the y to the top pipe minus an offset constant
                allPipes[i].setBounds(allPipes[i - 1].getX(), allPipes[i - 1].getY() + PIPE_HEIGHT + PIPE_OFFSET, PIPE_WIDTH, PIPE_HEIGHT);
            
            gameWindow.add(allPipes[i]);
        }

        gameWindow.add(flappy);
        gameWindow.setVisible(true);

        // gives a "startup" feel to the game
        try { Thread.sleep(300); }
        catch (InterruptedException e) { e.printStackTrace(); }

        updatePos(allPipes);
        updatePos(flappy);
        
        collisionCheck();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            // jump
            case ' ':
                // resets the flappy bird position to before the peak of its projectile parabola,
                // simulating a jump
                parbolaPositionX = 0;
                break;
            // quit game
            case 'q':
                endGame();
                break;
        }
    }
    
    TimerTask updatePos;
    Timer timer;
    protected void updatePos(JPanel[] pipes) {
        timer = new Timer();
        updatePos = new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < allPipes.length; i++) 
                    if (i % 2 == 0) 
                        if (inBounds(allPipes[i]))
                            allPipes[i].setLocation(allPipes[i].getX() - 2, allPipes[i].getY());
                        else {
                            // sends the pipes off screen to the right after they've left the left side of the window
                            // this allows us to reuse the pipes without deleting and remaking them
                            allPipes[i].setLocation(gameWindow.getWidth() + PIPE_WIDTH * 5, rand.nextInt(-300, -150));
                            Score++;
                        }
                    else
                        allPipes[i].setLocation(allPipes[i - 1].getX(), allPipes[i - 1].getY() + PIPE_HEIGHT + PIPE_OFFSET);
            }
        };

        timer.scheduleAtFixedRate(updatePos, 0, 17);
    }

    // used for calculating position
    protected int parbolaPositionX, parabolaPositionY;
    protected void updatePos(JPanel flappy) {
        var timer = new Timer();
        var updatePos = new TimerTask() {
            @Override
            public void run() {
                // position parabola
                parabolaPositionY = (int)(((Math.pow(parbolaPositionX, 2)) + 120 * parbolaPositionX) / 190) - 10;
                flappy.setLocation(flappy.getX(), flappy.getY() + parabolaPositionY);
                parbolaPositionX++;
            }
        };

        timer.scheduleAtFixedRate(updatePos, 0, 17);
    }

    // Checks if the x position is more than 100 pixels offscreen
    protected boolean inBounds(JPanel object) {
        if (object.getX() < -PIPE_WIDTH || 
            object.getY() > gameWindow.getHeight()
        ) return false;
        else
            return true;
    }

    Timer collisionCheckTimer;
    TimerTask collisionCheckTask;
    protected void collisionCheck() {
        collisionCheckTimer = new Timer();
        collisionCheckTask = new TimerTask() {
            @Override
            public void run() {
                if (flappy.getY() > gameWindow.getHeight() || 
                    flappy.getY() < 0 - FLAPPY_HEIGHT
                ) endGame();

                for (var pipe : allPipes) {
                    // checks if the pipe is even close to flappy, continues if not
                    if (flappy.getX() + 50 <= pipe.getX()) continue;
                    // this works for finding when its x-coor is in the pipes
                    if (flappy.getX() + FLAPPY_WIDTH >= pipe.getX() && 
                        flappy.getX() < pipe.getX() + PIPE_WIDTH
                        // everything in these parenthesis figures out if flappy's y-coord is inside the pipes 
                        && (flappy.getY() >= pipe.getY() && 
                            flappy.getY() <= pipe.getY() + PIPE_HEIGHT ||
                            flappy.getY() + FLAPPY_HEIGHT >= pipe.getY() && 
                            flappy.getY() + FLAPPY_HEIGHT <= pipe.getY() + PIPE_HEIGHT
                        )
                    ) endGame();
                }
            }
        };

        // timer has a chance of getting GC'd.
        // this ensures that if that does happen, 
        // a new timer is constructed to take its place.
        try {
            timer.scheduleAtFixedRate(collisionCheckTask, 0, 1);
        } catch (NullPointerException e) {
            new Timer().scheduleAtFixedRate(collisionCheckTask, 0, 1);
            System.err.println("error with timer");
        }
    }

    protected void endGame() {
        gameWindow.remove(flappy);
        for (var pipe : allPipes)
            gameWindow.remove(pipe);

        // gives a "shutting down" feel
        try { Thread.sleep(300); }
        catch (InterruptedException e) { e.printStackTrace(); }

        var endMenu = new JPanel(null);
        var endText = new JLabel();
        var yes = new JButton("Yes");
        var no = new JButton("No");
        endText.setSize(300, 150);
        endText.setHorizontalTextPosition(JLabel.CENTER);
        endText.setVerticalTextPosition(JLabel.TOP);
        endText.setHorizontalAlignment(JLabel.CENTER);
        endText.setVerticalAlignment(JLabel.TOP);
        endText.setForeground(Color.BLACK);
        endText.setText("<html><div style='text-align: center;'><p>Game Over!</p><p>You score was: " + Score + "<br>Play Again?</p></div></html>");
        endText.setFont(new Font("Impact", Font.PLAIN, 30));

        yes.setFocusable(false);
        yes.setLocation(20, 130);
        yes.addActionListener(e -> { 
            gameWindow.dispose(); 
            // is this bad? probably
            // am i making this in java (a managed language with garbage collection?)
            // yes!
            new FlappyGame(); 
        });
        yes.setSize(100, 50);
        yes.setForeground(Color.BLACK);
        yes.setFont(new Font("Impact", Font.PLAIN, 30));
        yes.setBackground(Color.GREEN);
        yes.setBorderPainted(false);

        no.setFocusable(false);
        no.setLocation(180, 130);
        no.addActionListener(e -> { 
            gameWindow.dispose(); 
            System.exit(0); 
        });
        no.setSize(100, 50);
        no.setForeground(Color.BLACK);
        no.setFont(new Font("Impact", Font.PLAIN, 30));
        no.setBackground(Color.RED);
        no.setBorderPainted(false);

        endMenu.add(yes);
        endMenu.add(no);

        endMenu.setSize(300, 200);
        endMenu.setLocation(gameWindow.getWidth() / 2 - endMenu.getWidth() / 2 - 5, gameWindow.getHeight() / 2 - endMenu.getHeight() / 2);
        endMenu.setBackground(Color.WHITE);
        endMenu.add(endText);
        
        gameWindow.add(endMenu);
        gameWindow.getContentPane().setBackground(Color.BLACK);
    }

    //-------------uneeded-------------//
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
