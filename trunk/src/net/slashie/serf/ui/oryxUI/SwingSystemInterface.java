package net.slashie.serf.ui.oryxUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import net.slashie.libjcsi.CharKey;
import net.slashie.serf.game.SworeGame;
import net.slashie.utils.ImageUtils;
import net.slashie.utils.Position;
import net.slashie.utils.swing.CallbackKeyListener;

public class SwingSystemInterface implements Runnable{ 
	public void run(){
	}
	private SwingInterfacePanel sip;

	private StrokeNClickInformer aStrokeInformer;
	private Position caretPosition = new Position(0,0);
	private Hashtable images = new Hashtable();
	
	//private JTextArea invTextArea;
	private JFrame frameMain;
	private Point posClic;

	public void addMouseListener(MouseListener listener){
		frameMain.removeMouseListener(listener);
		frameMain.addMouseListener(listener);
	}
	
	public void addMouseMotionListener(MouseMotionListener listener){
		frameMain.removeMouseMotionListener(listener);
		frameMain.addMouseMotionListener(listener);
	}
	
	public void setCursor(Cursor c){
		frameMain.setCursor(c);
	}
	
	public void setIcon(Image icon){
		frameMain.setIconImage(icon);
	}
	
	public void setTitle(String title){
		frameMain.setTitle(title);
	}
	
	public void setVisible(boolean bal){
		frameMain.setVisible(bal);
	}
	
	public SwingSystemInterface (){
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		frameMain = new JFrame();
		frameMain.setBounds((size.width - 800)/2,(size.height-600)/2,800,600);
		frameMain.getContentPane().setLayout(new GridLayout(1,1));
		frameMain.setUndecorated(true);
		frameMain.setVisible(true);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameMain.setBackground(Color.BLACK);
		frameMain.setFocusable(true);
		frameMain.getContentPane().setLayout(null);
		
		JLayeredPane layeredPane = new JLayeredPane();
		frameMain.getContentPane().add(layeredPane);
		layeredPane.setBounds(0,0,800,600);
		sip = new SwingInterfacePanel();
		sip.setBounds(0,0,800,600);
		layeredPane.add(sip);
		
		
		aStrokeInformer = new StrokeNClickInformer();
		frameMain.addKeyListener(aStrokeInformer);
		frameMain.addMouseListener(aStrokeInformer);
		sip.init();
		
		frameMain.addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent e) {
				if (e.getY() > 24)
					return;
		        frameMain.setLocation(e.getX()-posClic.x+frameMain.getLocation().x, e.getY()-posClic.y+frameMain.getLocation().y);
			}
			
			public void mouseMoved(MouseEvent e) {}
        	
        });
		frameMain.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {}

			public void mouseEntered(MouseEvent e) {}

			public void mouseExited(MouseEvent e) {}

			public void mousePressed(MouseEvent e) {
				posClic = e.getPoint();
			}

			public void mouseReleased(MouseEvent e) {}
        });
	}
	
	/*public SwingSystemInterface(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice  gs = ge.getDefaultScreenDevice();
		// Determine if the display mode can be changed
		/*boolean canChg = gs.isDisplayChangeSupported();
		if (canChg) {
			System.out.println("Can change screen size");
	        // Change the screen size and number of colors
	        DisplayMode displayMode = gs.getDisplayMode();
	        int screenWidth = 800;
	        int screenHeight = 600;
	        int bitDepth = 8;
	        displayMode = new DisplayMode(
	            screenWidth, screenHeight, bitDepth, displayMode.getRefreshRate());
	        try {
	            gs.setDisplayMode(displayMode);
	        } catch (Throwable e) {
	        	System.out.println("Desired display mode is not supported; leave full-screen mode");
	            gs.setFullScreenWindow(null);
	        }
	    //} 
		if (gs.isFullScreenSupported()){
			System.out.println("Fullscreen supported");
    		Frame frame = new Frame(gs.getDefaultConfiguration());
    		Window win = new Window(frame);
    		frame.setLayout(new GridLayout(1,1));
    		sip = new SwingInterfacePanel();
    		frame.add(sip);
    		frame.setVisible(true);
    		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		aStrokeInformer = new StrokeInformer();
    		frame.addKeyListener(aStrokeInformer);
    		sip.init();
    		gs.setFullScreenWindow(win);
    		win.validate();
    	} else {
    		JFrame frame = new JFrame();
    		frame.setBounds(0,0,800,600);
    		frame.getContentPane().setLayout(new GridLayout(1,1));
    		sip = new SwingInterfacePanel();
    		frame.getContentPane().add(sip);
    		frame.setVisible(true);
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		aStrokeInformer = new StrokeInformer();
    		frame.addKeyListener(aStrokeInformer);
    		sip.init();
    	}
		
		//frame.setBounds(0,0,800,600);
		
		invTextArea = new JTextArea();
		invTextArea.setVisible(false);
		add(invTextArea);
		
	}*/
	
	public void cls(){
		sip.cls();
	}
	
	public void drawImage(String filename){
		Image im = (Image)images.get(filename);
		if (im == null){
			try {
				im = ImageUtils.createImage(filename);
			} catch (Exception e){
				SworeGame.crash("Exception trying to create image "+filename, e);
			}
			images.put(filename, im);
		}
		sip.drawImage(im);
		sip.repaint();
	}
	
	public void drawImage(Image image){
		sip.drawImage(image);
		sip.repaint();
	}
	
	public void refresh(){
		sip.repaint();
	}
	
	public void printAtPixel(int x, int y, String text){
		sip.print(x, y, text);
	}
	
	public void printAtPixel(int x, int y, String text, Color color){
		sip.print(x, y, text, color);
	}
	
	public void printCentered(int y, String text, Color color){
		FontMetrics metrics = getGraphics2D().getFontMetrics(sip.getFont());
		printAtPixel((int)(sip.getWidth()/2.0d)-(int)(metrics.stringWidth(text)), y, text, color);
	}

	public void print(int x, int y, String text, Color color){
		sip.print(x*10, y*24, text, color);
	}
	
	public void print(int x, int y, String text){
		sip.print(x*10, y*24, text);
	}
	
	public void waitKey (int keyCode){
		CharKey x = new CharKey(CharKey.NONE);
		while (x.code != keyCode)
			x = inkey();
	}
	
	public void waitKeys (int... keyCodes){
		CharKey x = new CharKey(CharKey.NONE);
		while (true){
			x = inkey();
			for (int keyCode: keyCodes){
				if (x.code == keyCode)
					return;
			}
			
		}
	}
	
	public void drawImage(int scrX, int scrY, Image img){
		sip.drawImage(scrX, scrY, img);
	}
	
	public void drawImage(int scrX, int scrY, String filename){
		//System.out.println("Inadequate drawImage "+filename);
		Image im = (Image)images.get(filename);
		if (im == null){
			try {
				im = ImageUtils.createImage(filename);
			} catch (Exception e){
				SworeGame.crash("Exception trying to create image "+filename, e);
			}
			images.put(filename, im);
		}
		sip.drawImage(scrX, scrY, im);
	}

	public void drawImageCC(int consoleX, int consoleY, Image img){
		drawImage(consoleX*10, consoleY*24, img);
	}

	public void drawImageCC(int consoleX, int consoleY, String img){
		drawImage(consoleX*10, consoleY*24, img);
	}
	
	public synchronized CharKey inkey(){
	    aStrokeInformer.informKey(Thread.currentThread());
	    try {
			this.wait();
		} catch (InterruptedException ie) {}
		CharKey ret = new CharKey(aStrokeInformer.getInkeyBuffer());
		return ret;
	}
	private static final CharKey NULL_CHARKEY = new CharKey(CharKey.NONE);
	public synchronized CharKey inkey(long wait){
	    aStrokeInformer.informKey(Thread.currentThread());
	    try {
			this.wait(wait);
			return NULL_CHARKEY;
		} catch (InterruptedException ie) {}
		CharKey ret = new CharKey(aStrokeInformer.getInkeyBuffer());
		return ret;
	}
	
	public Graphics2D getGraphics2D(){
		return sip.getCurrentGraphics();
	}
	
	public void setFont(Font fnt){
		sip.setFontFace(fnt);
	}
	
	public void setColor(Color color){
		sip.setColor(color);
	}
	
	public String input(int xpos,int ypos, Color textColor, int maxLength){
		String ret = "";
		CharKey read = new CharKey(CharKey.NONE);
		saveBuffer();
		while (true){
			restore();
			printAtPixel(xpos, ypos, ret+"_", textColor);
			refresh();
			while (read.code == CharKey.NONE)
				read = inkey();
			if (read.code == CharKey.ENTER)
				break;
			if (read.code == CharKey.BACKSPACE){
				if (ret.equals("")){
					read.code = CharKey.NONE;
					continue;
				}
				if (ret.length() > 1)
					ret = ret.substring(0, ret.length() -1);
				else
					ret = "";
                caretPosition.x--;
				//print(caretPosition.x, caretPosition.y, " ");
            }
			else {
				if (ret.length() >= maxLength){
					read.code = CharKey.NONE;
					continue;
				}
				if (!read.isAlphaNumeric() && read.code != CharKey.SPACE){
					read.code = CharKey.NONE;
					continue;
				}
					
				String nuevo = read.toString();
				//print(caretPosition.x, caretPosition.y, nuevo, Color.WHITE);
				ret +=nuevo;
				caretPosition.x++;
			}
			read.code = CharKey.NONE;
		}
		return ret;
	}
	
	public void saveBuffer(){
		sip.saveBuffer();
	}
	
	public void saveBuffer(int buffer){
		sip.saveBuffer(buffer);
	}
	
	public void restore(){
		sip.restore();
	}
	
	public void restore(int buffer){
		sip.restore(buffer);
	}
	
	public void flash(Color c){
		sip.flash(c);
	}
	
	public void add(Component c){
		sip.add(c);
		sip.validate();
	}
	
	public void changeZOrder(Component c, int zOrder){
		sip.setComponentZOrder(c, zOrder);
		sip.validate();
	}
	
	public void remove(Component c){
		sip.remove(c);
		sip.validate();
	}
	
	public void recoverFocus(){
		frameMain.requestFocus();
	}

	public void removeMouseMotionListener(MouseMotionListener listener) {
		frameMain.removeMouseMotionListener(listener);
	}
	
	public void removeMouseListener(MouseListener listener){
		frameMain.removeMouseListener(listener);
	}

	public StrokeNClickInformer getStrokeInformer() {
		return aStrokeInformer;
	}
	
	public static int charCode(KeyEvent x){
    	int code = x.getKeyCode();
    	if(x.isControlDown()) {
			return CharKey.CTRL;
		}
    	if (code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z){
    		if (x.getKeyChar() >= 'a'){
	    		int diff = KeyEvent.VK_A - CharKey.a;
   		 		return code-diff;
   			} else {
	   			int diff = KeyEvent.VK_A - CharKey.A;
   		 		return code-diff;
   			}
    	}

		switch (x.getKeyCode()){
			case KeyEvent.VK_SPACE:
				return CharKey.SPACE;
			case KeyEvent.VK_COMMA:
				return CharKey.COMMA;
			case KeyEvent.VK_PERIOD: 
				return CharKey.DOT;
			case KeyEvent.VK_DELETE:
				return CharKey.DELETE;
			case KeyEvent.VK_NUMPAD0:
				return CharKey.N0;
			case KeyEvent.VK_NUMPAD1:
				return CharKey.N1;
			case KeyEvent.VK_NUMPAD2:
				return CharKey.N2;
			case KeyEvent.VK_NUMPAD3:
				return CharKey.N3;
			case KeyEvent.VK_NUMPAD4:
				return CharKey.N4;
			case KeyEvent.VK_NUMPAD5:
				return CharKey.N5;
			case KeyEvent.VK_NUMPAD6:
				return CharKey.N6;
			case KeyEvent.VK_NUMPAD7:
				return CharKey.N7;
			case KeyEvent.VK_NUMPAD8:
				return CharKey.N8;
			case KeyEvent.VK_NUMPAD9:
				return CharKey.N9;
			case KeyEvent.VK_0:
				return CharKey.N0;
			case KeyEvent.VK_1:
				return CharKey.N1;
			case KeyEvent.VK_2:
				return CharKey.N2;
			case KeyEvent.VK_3:
				return CharKey.N3;
			case KeyEvent.VK_4:
				return CharKey.N4;
			case KeyEvent.VK_5:
				return CharKey.N5;
			case KeyEvent.VK_6:
				return CharKey.N6;
			case KeyEvent.VK_7:
				return CharKey.N7;
			case KeyEvent.VK_8:
				return CharKey.N8;
			case KeyEvent.VK_9:
				return CharKey.N9;
			case KeyEvent.VK_F1:
				return CharKey.F1;
			case KeyEvent.VK_F2:
				return CharKey.F2;
			case KeyEvent.VK_F3:
				return CharKey.F3;
			case KeyEvent.VK_F4:
				return CharKey.F4;
			case KeyEvent.VK_F5:
				return CharKey.F5;
			case KeyEvent.VK_F6:
				return CharKey.F6;
			case KeyEvent.VK_F7:
				return CharKey.F7;
			case KeyEvent.VK_F8:
				return CharKey.F8;
			case KeyEvent.VK_F9:
				return CharKey.F9;
			case KeyEvent.VK_F10:
				return CharKey.F10;
			case KeyEvent.VK_F11:
				return CharKey.F11;
			case KeyEvent.VK_F12:
				return CharKey.F12;
			case KeyEvent.VK_ENTER:
				return CharKey.ENTER;
			case KeyEvent.VK_BACK_SPACE:
				return CharKey.BACKSPACE;
			case KeyEvent.VK_ESCAPE:
				return CharKey.ESC;
			case KeyEvent.VK_UP:
				return CharKey.UARROW;
			case KeyEvent.VK_DOWN:
				return CharKey.DARROW;
			case KeyEvent.VK_LEFT:
				return CharKey.LARROW;
			case KeyEvent.VK_RIGHT:
				return CharKey.RARROW;
			

		}
		if (x.getKeyChar() == '.')
    		return CharKey.DOT;
    	if (x.getKeyChar() == '?')
    		return CharKey.QUESTION;
		return -1;
	}
	
	public class StrokeNClickInformer extends StrokeInformer implements MouseListener{
		public void mousePressed(MouseEvent e) {
			if (keyListener != null){
				bufferCode = CharKey.NONE;
			    keyListener.interrupt();
			}
		}
		
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}

	public void addKeyListener(KeyListener keyListener) {
		frameMain.removeKeyListener(keyListener);
		frameMain.addKeyListener(keyListener);	
	}

	public void removeKeyListener(KeyListener keyListener) {
		frameMain.removeKeyListener(keyListener);
	}

	public Font getFont() {
		return sip.getGraphicsFont();
	}

	
	public Cursor getCursor() {
		return frameMain.getCursor();
	}
}

class SwingInterfacePanel extends JPanel{
	private Image bufferImage;
	private Graphics bufferGraphics;
	
	private Image backImage;
	private Graphics backGraphics;
	
	private Image[] backImageBuffers;
	private Graphics[] backGraphicsBuffers;
	
	public void cls(){
		Color oldColor = bufferGraphics.getColor();
		bufferGraphics.setColor(Color.BLACK);
		bufferGraphics.fillRect(0,0,800,600);
		bufferGraphics.setColor(oldColor);
	}
	
	public void setColor(Color color){
		bufferGraphics.setColor(color);
	}
	
	public void setFontFace(Font f){
		bufferGraphics.setFont(f);
	}
	
	public Font getGraphicsFont(){
		return bufferGraphics.getFont();
	}
	
	public Graphics2D getCurrentGraphics(){
		return (Graphics2D)bufferGraphics;
	}
	
	public SwingInterfacePanel(){
		setLayout(null);
		setBorder(new LineBorder(Color.GRAY));
	}
	/*
	boolean isTransparent;
	Image transparentImage = null;
	public void activateTransparency (){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		
		
		try {
			transparentImage = ImageUtils.createImage("res/transparent.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		bufferImage = gc.createCompatibleImage(800, 600, Transparency.BITMASK);
		bufferImage = transparentImage;
        bufferGraphics = bufferImage.getGraphics();
        bufferGraphics.setColor(Color.WHITE);
        backImage = gc.createCompatibleImage(800, 600, Transparency.BITMASK);
        backImage = transparentImage;
        backGraphics = backImage.getGraphics();
        backImageBuffers = new Image[5];
        backGraphicsBuffers = new Graphics[5];
        for (int i = 0 ; i < 5; i++){
        	backImageBuffers[i] = gc.createCompatibleImage(800, 600, Transparency.BITMASK);
        	backImageBuffers[i] = transparentImage;
        	backGraphicsBuffers[i] = backImageBuffers[i].getGraphics();
        	
        }
	
		Graphics2D g = (Graphics2D) bufferImage.getGraphics();
		g.setBackground(new Color(0,0,0,0));
		g.clearRect(0,0,800,600);
		
		isTransparent = true;
	}*/
	
	public void init(){
		bufferImage = createImage(800, 600);
        bufferGraphics = bufferImage.getGraphics();
        bufferGraphics.setColor(Color.WHITE);
        backImage = createImage(800, 600);
        backGraphics = backImage.getGraphics();
        backImageBuffers = new Image[5];
        backGraphicsBuffers = new Graphics[5];
        for (int i = 0 ; i < 5; i++){
        	backImageBuffers[i] = createImage(800, 600);
        	backGraphicsBuffers[i] = backImageBuffers[i].getGraphics();
        }
        
	}
	
	public void drawImage(Image img){
		bufferGraphics.drawImage(img, 0, 0,this);
	}
	
	public void drawImage(int scrX, int scrY, Image img){
		bufferGraphics.drawImage(img, scrX, scrY,this);
	}
	
	public void print(int x, int y, String text){
		bufferGraphics.drawString(text, x,y);
		//repaint();
	}
	
	public void print(int x, int y, String text, Color c){
		Color old = bufferGraphics.getColor(); 
		bufferGraphics.setColor(c);
		bufferGraphics.drawString(text, x,y);
		bufferGraphics.setColor(old);
		//repaint();
	}
	
	public void saveBuffer(){
		backGraphics.drawImage(bufferImage,0,0,this);
	}
	
	public void saveBuffer(int buffer){
		cleanBuffer(buffer);
		backGraphicsBuffers[buffer].drawImage(bufferImage,0,0,this);
	}
	
	private void cleanBuffer(int buffer) {
		//backImageBuffers[buffer] = transparentImage;
    	backGraphicsBuffers[buffer] = backImageBuffers[buffer].getGraphics();
	}

	public void restore(){
		bufferGraphics.drawImage(backImage, 0,0,this);
	}
	
	public void restore(int buffer){
		/*try {
			transparentImage = ImageUtils.createImage("res/transparent.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		bufferImage = transparentImage;
        bufferGraphics = bufferImage.getGraphics();*/
		bufferGraphics.drawImage(backImageBuffers[buffer], 0,0,this);
	}
	
	public void flash(Color c){
		
	}
	
	public void paintComponent(Graphics g){
		//if (!isTransparent)
			super.paintComponent(g);
		if (bufferImage != null){
			g.drawImage(bufferImage, 0,0,this);
		}
	}

	public Component add(Component comp) {
		return super.add(comp);
	}
}

class StrokeInformer implements KeyListener{
	protected int bufferCode;
	protected transient Thread keyListener;

	public StrokeInformer(){
		bufferCode = -1;
	}

	public void informKey (Thread toWho){
		keyListener = toWho;
	}

	public int getInkeyBuffer(){
		return bufferCode;
	}

	public void keyPressed(KeyEvent e) {
	    bufferCode = SwingSystemInterface.charCode(e);
	    //if (!e.isShiftDown())
	    if (keyListener != null)
		    keyListener.interrupt();
    }

    public void keyReleased(KeyEvent e) {}
    
    public void keyTyped(KeyEvent e) {}
}

