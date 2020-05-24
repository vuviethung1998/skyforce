package skyforce.client.ui.ingamescreem;

import com.google.common.eventbus.Subscribe;
import skyforce.client.Client;
import skyforce.client.ui.ScreenManager;
import skyforce.common.Constants;
import skyforce.common.EventBuz;
import skyforce.entity.Bullet;
import skyforce.entity.Enemy;
import skyforce.entity.Player;
import skyforce.packet.PlayerActionPacket;
import skyforce.packet.UpdateGamePacket;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class InGameScreen extends JPanel implements KeyListener {
    private HashMap<Integer, Player> players;
    private ArrayList<Enemy> enemies;

    private static Canvas canvas;

    public InGameScreen(int width, int height) {
        setSize(width, height);
        setVisible(true);

        players = new HashMap<>();
        enemies = new ArrayList<>();

        renderCanvas();

        EventBuz.getInstance().register(this);
        ScreenManager.getInstance().getWindow().addKeyListener(this);
        ScreenManager.getInstance().getWindow().setFocusable(true);
        ScreenManager.getInstance().getWindow().requestFocus();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        switch (keycode) {
            case KeyEvent.VK_SPACE:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.FIRE_PRESSED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: FIRE_PRESSED\n", Client.getConnectionId());
                break;
            case KeyEvent.VK_LEFT:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.LEFT_PRESSED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: LEFT_PRESSED\n", Client.getConnectionId());

                break;
            case KeyEvent.VK_RIGHT:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.RIGHT_PRESSED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: RIGHT_PRESSED\n", Client.getConnectionId());
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keycode = e.getKeyCode();
        switch (keycode) {
            case KeyEvent.VK_SPACE:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.FIRE_RELEASED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: FIRE_RELEASED\n", Client.getConnectionId());
                break;
            case KeyEvent.VK_LEFT:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.LEFT_RELEASED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: LEFT_RELEASED\n", Client.getConnectionId());
                break;
            case KeyEvent.VK_RIGHT:
                Client.sendObject(new PlayerActionPacket(PlayerActionPacket.Action.RIGHT_RELEASED));
                System.out.printf( "[CLIENT: %d] sent PlayerActionPacket: RIGHT_RELEASED\n", Client.getConnectionId());
                break;
        }
    }

    private void renderCanvas() {
        canvas = new Canvas();
        canvas.setFocusable(false);
        canvas.setPreferredSize(new Dimension(Constants.IN_GAME_SCREEN_WIDTH, Constants.IN_GAME_SCREEN_HEIGHT));
        add(canvas);
        canvas.setVisible(true);

        LoadImage.init();
    }

    @Subscribe
    public void onUpdateGame(UpdateGamePacket e) {
        this.players = e.players;
        this.enemies = e.enemies;

        renderUI();
    }

    private void renderUI() {
        BufferStrategy buffer = canvas.getBufferStrategy();
        if (buffer == null) {
            canvas.createBufferStrategy(3);
            return;
        }

        Graphics g = buffer.getDrawGraphics();
        g.clearRect(0,0,Constants.IN_GAME_SCREEN_WIDTH, Constants.IN_GAME_SCREEN_HEIGHT);

        // draw
        g.drawImage(LoadImage.image, 50 ,50, Constants.GAME_WIDTH, Constants.GAME_HEIGHT, null);
        for (Enemy e : enemies) {
            if (e.getX() >= 50 && e.getX() <= 450 - 25 && e.getY() <= 450 - 25 && e.getY() >= 50) {
                e.render(g);
            }
        }

        for(Map.Entry<Integer, Player> entry : players.entrySet()) {
            Player p = entry.getValue();
            p.render(g);

            for (Bullet bullet : Player.bullets) {
                bullet.render(g);
            }

        }

        g.setColor(Color.BLUE);

        buffer.show();
        g.dispose();
    }

    private void exitGame() {
        EventBuz.getInstance().unregister(this);
    }

    @Override
    protected void finalize() throws Throwable {
        ScreenManager.getInstance().getWindow().removeKeyListener(this);
        EventBuz.getInstance().unregister(this);
        super.finalize();
    }
}
