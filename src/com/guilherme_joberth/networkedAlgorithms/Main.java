package com.guilherme_joberth.networkedAlgorithms;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.GeneticAlgorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.HighSequenceRestriction;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.RepeatedRestriction;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.Restriction;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.SequenceOfThreeRestriction;
import com.guilherme_joberth.networkedAlgorithms.network.AbstractNode;
import com.guilherme_joberth.networkedAlgorithms.network.MasterNode;
import com.guilherme_joberth.networkedAlgorithms.network.Node;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Main {

    public static void main(String[] args) {

        Main program = new Main();
        program.run();

    }

    List<AbstractNode> created;

    MasterNode masterNode;

	private Integer ids;

    Main(){

        this.created = new LinkedList<>();
        this.ids = 1;
    }

    void run() {


    	JFrame frame = new JFrame("Network P2P");
		frame.getContentPane().setBackground(Color.gray);
		
		
		JPanel panelLabelMain = new JPanel();
        JLabel mainLabel = new JLabel();
        mainLabel.setText("Network P2P");
        mainLabel.setFont(new Font("Serif", Font.BOLD, 45));
        mainLabel.setForeground(Color.WHITE);
        panelLabelMain.add(mainLabel);
        panelLabelMain.setBackground(Color.gray);
        panelLabelMain.setBounds(100,100,800,100);
     
        JPanel panelButton = new JPanel();
        JButton buttonMaster = new JButton("Master");
        JButton buttonNode = new JButton("Node");
        JLabel labelButtonPanel = new JLabel();
        labelButtonPanel.setText("Escolha sua opção para conectar-se: ");
        panelButton.setLayout(null);
        labelButtonPanel.setLocation(50, 10);
        labelButtonPanel.setSize(300, 100);
        buttonMaster.setLocation(250, 150);
        buttonMaster.setSize(300, 30);
        buttonNode.setLocation(250, 230);
        buttonNode.setSize(300, 30);
        panelButton.add(labelButtonPanel);
        panelButton.add(buttonMaster);
        panelButton.add(buttonNode);
        
        panelButton.setBounds(100,270,800,400); 
     
        frame.setSize(1000, 700);  
        frame.setLocation(300, 50);
      
        frame.getContentPane().add(panelLabelMain);
        frame.getContentPane().add(panelButton);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);    
        frame.setVisible(true);
        
        buttonMaster.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (masterNode == null) {
                    try {
						createMasterNode(ids, frame, panelButton, panelLabelMain);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    Main.this.ids++;
                }
				
			}
		});
        
        
        buttonNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					createNode(Main.this.ids, frame, panelButton, panelLabelMain);
					Main.this.ids += 1;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

        

        if (created != null){

            for ( AbstractNode n : created) {
                n.end();
            }
        }

    }

    public static int getInt(){

        try{

            return Integer.parseInt(getString());

        }catch (NumberFormatException e){

            e.printStackTrace();

            return getInt();
        }
    }

    static String getString(){

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return input;
    }

    private void createMasterNode(int id, JFrame frame, JPanel panelbutton, JPanel panelMain) throws IOException{
    	
    	panelMain.setVisible(false);
		panelbutton.setVisible(false);
		
		JPanel panelMaster = new JPanel();
		panelMaster.setLocation(0, 0);
		panelMaster.setSize(900, 900);
		
		panelMaster.setLayout(null);
		
		BufferedImage img = ImageIO.read(new File("src/com/guilherme_joberth/networkedAlgorithms/server.png"));
		BufferedImage resized = resize(img,170, 200);
		ImageIcon icon = new ImageIcon(resized);
		JLabel lbl = new JLabel();
	    lbl.setIcon(icon);
	    lbl.setLocation(150,150);
	    lbl.setSize(170,200);
	    panelMaster.add(lbl);
		
		
		JLabel inputPortLabel = new JLabel();
		inputPortLabel.setText("Porta de entrada: ");
		inputPortLabel.setLocation(400, 150);
		inputPortLabel.setSize(200, 30);
		
		JTextField inputPort = new JTextField();
		inputPort.setLocation(400,180);
		inputPort.setSize(300,30);
		panelMaster.add(inputPort);
		panelMaster.add(inputPortLabel);
		
		JLabel portMasterLabel = new JLabel();
		portMasterLabel.setText("Porta de saída: ");
		portMasterLabel.setLocation(400, 220);
		portMasterLabel.setSize(200, 30);
		
		JTextField outputPort = new JTextField();
		outputPort.setLocation(400,260);
		outputPort.setSize(300,30);
		panelMaster.add(outputPort);
		panelMaster.add(portMasterLabel);
		
		
		JButton send = new JButton("Requisitar");
		send.setLocation(500, 350);
		send.setSize(200, 30);
		panelMaster.add(send);
		send.setVisible(true);
		
		JButton algoritmGenetic = new JButton("Algoritimo genetico");
		algoritmGenetic.setLocation(450, 350);
		algoritmGenetic.setSize(200, 30);
		panelMaster.add(algoritmGenetic);
		algoritmGenetic.setVisible(false);
		
		JButton back_master = new JButton("Parar");
		back_master.setLocation(700, 350);
		back_master.setSize(150, 30);
		panelMaster.add(back_master);
		back_master.setVisible(false);
		
		JTextArea textArea = new JTextArea(5, 20);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setLocation(150, 450);
		scrollPane.setSize(700, 200);
		panelMaster.add(scrollPane);

		JButton backButton = new JButton("Voltar");
		backButton.setLocation(110,110);
		backButton.setSize(100,30);
		panelMaster.add(backButton);
		
		frame.getContentPane().add(panelMaster);

		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send.setVisible(false);
				algoritmGenetic.setVisible(true);
				inputPort.setEditable(false);
				back_master.setVisible(true);
				outputPort.setEditable(false);
				
				MasterNode master = new MasterNode(Integer.parseInt(inputPort.getText()), Integer.parseInt(inputPort.getText()), id, textArea);
		        created.add(master);
		        masterNode = master;

		        Thread t = new Thread(master);
		        t.start();
			}
		});
		
		algoritmGenetic.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
                List<Restriction> restrictions = new LinkedList<>();

                restrictions.add(new RepeatedRestriction());
                restrictions.add(new HighSequenceRestriction());
                restrictions.add(new SequenceOfThreeRestriction());

                Algorithm alg = new GeneticAlgorithm(restrictions, ids, 150);
                textArea.append("[MAIN] Creating algorithm #" + ids + "\n");
                System.out.println("[MAIN] Creating algorithm #" + ids);
                ids++;

                Thread t = new Thread(() -> masterNode.startAlgorithm(alg));
                t.start();
			}
		});
		
		back_master.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				back_master.setVisible(false);
				algoritmGenetic.setVisible(false);
				inputPort.setEditable(true);
				outputPort.setEditable(true);
				send.setVisible(true);
				return;
			}
		});
		
		backButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				panelMaster.setVisible(false);
				panelMain.setVisible(true);
				panelbutton.setVisible(true);
			}
		});

    }

    private void createNode(int id, JFrame frame, JPanel panelbutton, JPanel panelMain) throws IOException{
    	
		panelMain.setVisible(false);
		panelbutton.setVisible(false);
		
		JPanel panelMaster = new JPanel();
		panelMaster.setLocation(0, 0);
		panelMaster.setSize(900, 900);
		
		panelMaster.setLayout(null);
		
		BufferedImage img = ImageIO.read(new File("src/com/guilherme_joberth/networkedAlgorithms/host.png"));
		BufferedImage resized = resize(img,200, 200);
		ImageIcon icon = new ImageIcon(resized);
		JLabel lbl = new JLabel();
	    lbl.setIcon(icon);
	    lbl.setLocation(150,150);
	    lbl.setSize(200,200);
	    panelMaster.add(lbl);
		
		
		JLabel labelIp = new JLabel();
		labelIp.setText("IP : ");
		labelIp.setLocation(400, 120);
		labelIp.setSize(200, 30);
		
		JTextField fieldIp = new JTextField();
		fieldIp.setLocation(400,150);
		fieldIp.setSize(300,30);
		panelMaster.add(fieldIp);
		panelMaster.add(labelIp);
		
		JLabel portMasterLabel = new JLabel();
		portMasterLabel.setText("Porta da Master: ");
		portMasterLabel.setLocation(400, 180);
		portMasterLabel.setSize(200, 30);
		
		JTextField portMaster = new JTextField();
		portMaster.setLocation(400,210);
		portMaster.setSize(300,30);
		panelMaster.add(portMaster);
		panelMaster.add(portMasterLabel);
		
		JLabel localPortLabel = new JLabel();
		localPortLabel.setText("Porta de entrada: ");
		localPortLabel.setLocation(400, 240);
		localPortLabel.setSize(200, 30);
		
		JTextField localPort = new JTextField();
		localPort.setLocation(400,270);
		localPort.setSize(300,30);
		panelMaster.add(localPort);
		panelMaster.add(localPortLabel);
		
		JLabel outputPortLabel = new JLabel();
		outputPortLabel.setText("Porta da saida: ");
		outputPortLabel.setLocation(400, 300);
		outputPortLabel.setSize(200, 30);
		
		JTextField outputPort = new JTextField();
		outputPort.setLocation(400,330);
		outputPort.setSize(300,30);
		panelMaster.add(outputPort);
		panelMaster.add(outputPortLabel);
		
		JButton send = new JButton("Requisitar");
		send.setLocation(500, 380);
		send.setSize(200, 30);
		panelMaster.add(send);
		
		JTextArea textArea = new JTextArea(5, 20);
		//JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setLocation(150, 450);
		textArea.setSize(700, 200);
		panelMaster.add(textArea);

		JButton backButton = new JButton("Voltar");
		backButton.setLocation(110,110);
		backButton.setSize(100,30);
		panelMaster.add(backButton);
		
		frame.getContentPane().add(panelMaster);
		
		final Integer aux_id = id;
		
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				fieldIp.setEditable(false);
				portMaster.setEditable(false);
				localPort.setEditable(false);
				outputPort.setEditable(false);
				Node n = new Node(fieldIp.getText(), Integer.parseInt(portMaster.getText()), Integer.parseInt(localPort.getText()), Integer.parseInt(outputPort.getText()), aux_id, textArea);
		        created.add(n);
		        
		        Thread t = new Thread(n);
		        t.start();
			}
		});
		
		backButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				panelMaster.setVisible(false);
				panelMain.setVisible(true);
				panelbutton.setVisible(true);
			}
		});
		
		
    }
    
    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
	


}
