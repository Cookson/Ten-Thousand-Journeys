package erpoge.gui;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class Window extends JFrame {
	private static final long serialVersionUID = 1L;
	public TextAreaOutputStream taos;
	public Window() {
		super();
		this.setTitle("Эрпоге консоль");
		this.setSize(1024,768);
		this.setLocation(200,200);

        JTextArea ta = new JTextArea();
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        ta.setFont(font);
        ta.setBackground(Color.BLACK);
        ta.setForeground(Color.LIGHT_GRAY);
        TextAreaOutputStream taos = new TextAreaOutputStream(ta, 200);
        this.taos = taos;
        PrintStream ps = new PrintStream(taos);
        System.setOut(ps);
        System.setErr(ps);

        JScrollPane jsp = new JScrollPane(ta);
        this.add(jsp);
//        frame.pack();
        this.setVisible(true);
		this.addWindowListener(new WindowListener() {
	
			public void windowActivated(WindowEvent event) {
	
			}
	
			public void windowClosed(WindowEvent event) {
	
			}
	
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
	
			public void windowDeactivated(WindowEvent event) {
	
			}
	
			public void windowDeiconified(WindowEvent event) {
	
			}
	
			public void windowIconified(WindowEvent event) {
	
			}
	
			public void windowOpened(WindowEvent event) {
	
			}
		});
	}
}
