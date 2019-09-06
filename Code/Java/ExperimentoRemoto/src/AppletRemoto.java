













import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AppletRemoto.java
 *
 * Created on 12/09/2011, 09:41:05
 */
/**
 * 24/09/2011, 10:37:32
 * 
 * @author Henrique
 */
public class AppletRemoto extends javax.swing.JApplet implements Runnable {

    private static Thread AppletThread; 
    private static boolean running = false;
    
    private static EtherShield client = null;
    
    public static Timer TempoCom = new Timer(50);
    public static Timer Tempo = new Timer(500);
    public static Timer TempoClientEntra = new Timer(5000);
    public static Timer TempoClientSai = new Timer(3000);
    
    public final static int PID_STATUS = 0;
    public final static int PID_LIGA = 1;
    public final static int PID_SET = 2;
    public final static int PID_KP = 3;
    public final static int PID_KI = 4;
    public final static int PID_KD = 5;
    public final static int TACO = 6;

    
    void ler_data(int i){
      if (client.Vazio()) return;
      
      int a = client.LinhaInt();
      
      switch(i){
        case PID_LIGA:
          //pid.ligado = client.LinhaInt();
          break;
        case PID_SET:
          SliderSetPoint.setValue(a);
          break;    
        case PID_KP:
          SliderKp.setValue(a);
          break;   
        case PID_KI:
          SliderKi.setValue(a);
          break;   
        case PID_KD:
          SliderKd.setValue(a);
          break;   
        case TACO:
          while(!client.Vazio()){
             PanelGrafico.addPoints(a, client.LinhaInt());
             a = client.LinhaInt();
          }
          PanelGrafico.repaint();
          break;
      }
    }

    public void envia_data(int i){
      switch(i){
        case PID_STATUS:
          envia_data(PID_LIGA);
          envia_data(PID_SET);
          envia_data(PID_KP);
          envia_data(PID_KI);
          envia_data(PID_KD);
          break;
        case PID_LIGA:
          int v = 0;
          if (ButtonMotor.isSelected()) v = 1;            
          client.setSendString(i,v);
          break;
        case PID_SET:
          client.setSendString(i,SliderSetPoint.getValue());
          break;    
        case PID_KP:
          client.setSendString(i,SliderKp.getValue());
          break;   
        case PID_KI:
          client.setSendString(i,SliderKi.getValue());
          break;   
        case PID_KD:
          client.setSendString(i,SliderKd.getValue());
          break;   
        case TACO:
          client.setSendString(i,0);
          break;   
      }
    }
    
    @Override
    public void init() {
         
        
        //client = new EtherShield("rexlab.satc.edu.br",8020);
        client = new EtherShield("189.8.209.30",8020);
        //client = new EtherShield("10.21.42.50",80);
        //client = new EtherShield("10.0.5.35",80);
        change_look("Windows");
        initComponents();
        
        setSize(435, 500);
        
        AppletThread = new Thread(this);
        AppletThread.start(); 

        TempoClientEntra.Start();
        Tempo.Start();
        running = true;

    }

    private void Saiu(){
        TextSetPoint.setText("0.0");
        TextKp.setText("0.0");
        TextKi.setText("0.0");
        TextKd.setText("0.0");
        
        boolean bool = false;
        
        SliderSetPoint.setEnabled(bool);
        SliderKp.setEnabled(bool);
        SliderKi.setEnabled(bool);
        SliderKd.setEnabled(bool);
        
        ButtonMotor.setEnabled(bool);
        
        SliderSetPoint.setValue(0);
        SliderKp.setValue(0);
        SliderKi.setValue(0);
        SliderKd.setValue(0);
        ButtonMotor.setSelected(false);
        
        ButtonMotorAtualizaText();
    }
    
    private void Entrou(){
        boolean bool = true;
        
        SliderSetPoint.setEnabled(bool);
        SliderKp.setEnabled(bool);
        SliderKi.setEnabled(bool);
        SliderKd.setEnabled(bool);
        
        ButtonMotor.setEnabled(bool);
        ButtonMotor.setSelected(!bool);
        
        BarGrafico.setValue(0);
//        PanelGrafico.graficostop(!bool);
        
        ButtonMotorAtualizaText();
        
    }
    
    private void ShowHelp(int valor){
        switch(valor){
            case 0:
                LabelHelp.setText("Aguarde...");
                PanelColorHelp.setBackground(Color.yellow);
                break;
            case 1:
                LabelHelp.setText("Conectando...");
                PanelColorHelp.setBackground(Color.yellow);
                break;
            case 2:
                LabelHelp.setText("Online!");
                PanelColorHelp.setBackground(Color.green);
                break;
            case 3:
                LabelHelp.setText("Falha na conexão...");
                PanelColorHelp.setBackground(Color.red);
                break;
            case 4:
                LabelHelp.setText("Experimento ocupado...");
                PanelColorHelp.setBackground(Color.red);
                break;
            case 5:
                LabelHelp.setText("Fatal error...");
                PanelColorHelp.setBackground(Color.red);
                break;
        }
    }
    
    private int contador = 0;
    private boolean pronto = true;
    @Override
    public void run(){
        
        ShowHelp(0);
        
        PanelGrafico.setBar(BarGrafico);

        Saiu();
        
        LabelTempo.setText(temporeal());
        
        while(running){
            
            if (Tempo.Get()){
                LabelTempo.setText(temporeal());
            }
            
            
            if (TempoClientSai.Get()){ // auto sair
                TempoCom.Stop();
                TempoClientSai.Stop();
                TempoClientEntra.Start();
                client.Sair();
                Saiu();
                ShowHelp(3);
            }
            
            if (TempoClientEntra.Get()){ // auto entrar
                TempoClientEntra.Stop();
                ShowHelp(1);
                client.Entrar();
                
                if (!client.Connected()){
                    TempoClientEntra.Start();
                    Saiu();
                    ShowHelp(3);
                }
            }
            
            if (TempoCom.Get() ){
                
                if (50<=contador++) pronto = true;
                if (pronto){
                    envia_data(TACO);
                    client.Send();
                    contador = 0;
                    pronto = false;
                }
                
                
                
            }
            
            while (client.read()){
                
                pronto = true;
                
                TempoClientSai.Start();
                
                if (client.Entrou()){
                    TempoClientEntra.Stop();
                    TempoCom.Start();
                    ShowHelp(2);
                    Entrou();
                    client.setSendString(PID_STATUS, 0);
                }
                
                if (client.Saiu()){
                    TempoClientSai.Stop();
                    TempoClientEntra.Start();
                    TempoCom.Stop();
                    ShowHelp(4);
                    Saiu();
                    client.Sair();
                }
                
                if (client.Error()){
                    TempoClientSai.Stop();
                    TempoClientEntra.Start();
                    TempoCom.Stop();
                    ShowHelp(5);
                    Saiu();
                    client.Sair();
                }
                
                if (client.Dado()){
                    int i = client.LinhaInt();
                    ler_data(i);
                }
            }
            
        }
    }
    
    @Override
    public void destroy() {
        running = false;
        AppletThread = null;
        client.Sair();
    }

    
    public void change_look(String str){
        // usado para mudar o look do applet!
        UIManager.LookAndFeelInfo[] lookAndFeelArray = UIManager.getInstalledLookAndFeels();
        try {                  
            for (int i =0; i < lookAndFeelArray.length; i++) {
                // use String and method getName() instead of array index
                if (lookAndFeelArray[i].getName().equals(str)) {
                    UIManager.setLookAndFeel(lookAndFeelArray[i].getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                }
            }
        }
        catch (Exception exception) {
//            exception.printStackTrace();
        }
    }
    
    private String temporeal(){
        String timeString = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat( "hh:mm:ss", Locale.getDefault() );
        Date date = cal.getTime();
        timeString = formatter.format( date );
        return timeString;
    }
    
    private void ButtonMotorAtualizaText(){
        if (ButtonMotor.isSelected()){
            ButtonMotor.setText("Desligar Motor");
            BarGrafico.setValue(0);
        }
        else {
            ButtonMotor.setText("Ligar Motor");
            BarGrafico.setValue(0);
        }
    }
    
    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        PanelPrincipal = new javax.swing.JPanel();
        PanelControlePID = new javax.swing.JPanel();
        TextKp = new javax.swing.JTextField();
        SliderKp = new javax.swing.JSlider();
        TextKi = new javax.swing.JTextField();
        SliderKi = new javax.swing.JSlider();
        TextKd = new javax.swing.JTextField();
        SliderKd = new javax.swing.JSlider();
        LabelKd = new javax.swing.JLabel();
        LabelKp = new javax.swing.JLabel();
        LabelKi = new javax.swing.JLabel();
        TextSetPoint = new javax.swing.JTextField();
        SliderSetPoint = new javax.swing.JSlider();
        LabelSetPoint = new javax.swing.JLabel();
        ButtonMotor = new javax.swing.JToggleButton();
        PanelHelp = new javax.swing.JPanel();
        LabelHelp = new javax.swing.JLabel();
        PanelColorHelp = new javax.swing.JPanel();
        LabelTempo = new javax.swing.JLabel();
        PanelGrafico = new JPanelGrafico();
        /*
        PanelGrafico = new javax.swing.JPanel();
        */
        BarGrafico = new javax.swing.JScrollBar();

        PanelPrincipal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        PanelPrincipal.setPreferredSize(new java.awt.Dimension(345, 500));
        PanelPrincipal.setLayout(new java.awt.GridBagLayout());

        PanelControlePID.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        TextKp.setEditable(false);
        TextKp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextKp.setText("00000");

        SliderKp.setMaximum(5000);
        SliderKp.setValue(0);
        SliderKp.setEnabled(false);
        SliderKp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                SliderKpStateChanged(evt);
            }
        });

        TextKi.setEditable(false);
        TextKi.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextKi.setText("00000");

        SliderKi.setMaximum(5000);
        SliderKi.setValue(0);
        SliderKi.setEnabled(false);
        SliderKi.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                SliderKiStateChanged(evt);
            }
        });

        TextKd.setEditable(false);
        TextKd.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextKd.setText("00000");

        SliderKd.setMaximum(5000);
        SliderKd.setValue(0);
        SliderKd.setEnabled(false);
        SliderKd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                SliderKdStateChanged(evt);
            }
        });

        LabelKd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelKd.setText("Derivativo");

        LabelKp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelKp.setText("Proporcional");

        LabelKi.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelKi.setText("Integral");

        TextSetPoint.setEditable(false);
        TextSetPoint.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextSetPoint.setText("00000");

        SliderSetPoint.setMaximum(255);
        SliderSetPoint.setValue(0);
        SliderSetPoint.setEnabled(false);
        SliderSetPoint.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                SliderSetPointStateChanged(evt);
            }
        });

        LabelSetPoint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelSetPoint.setText("SetPoint");

        ButtonMotor.setText("Motor");
        ButtonMotor.setEnabled(false);
        ButtonMotor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonMotorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelControlePIDLayout = new javax.swing.GroupLayout(PanelControlePID);
        PanelControlePID.setLayout(PanelControlePIDLayout);
        PanelControlePIDLayout.setHorizontalGroup(
            PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelControlePIDLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PanelControlePIDLayout.createSequentialGroup()
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(LabelKd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(LabelKi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(LabelKp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SliderKd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SliderKp, 0, 0, Short.MAX_VALUE)
                            .addComponent(SliderKi, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TextKd, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addComponent(TextKi)
                            .addComponent(TextKp)))
                    .addGroup(PanelControlePIDLayout.createSequentialGroup()
                        .addComponent(LabelSetPoint, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SliderSetPoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TextSetPoint, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                    .addComponent(ButtonMotor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        PanelControlePIDLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {LabelKd, LabelKi, LabelKp, LabelSetPoint});

        PanelControlePIDLayout.setVerticalGroup(
            PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelControlePIDLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelControlePIDLayout.createSequentialGroup()
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelKp, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                            .addComponent(SliderKp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelKi)
                            .addComponent(SliderKi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SliderKd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LabelKd)))
                    .addGroup(PanelControlePIDLayout.createSequentialGroup()
                        .addComponent(TextKp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TextKi, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TextKd, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ButtonMotor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelControlePIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TextSetPoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SliderSetPoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LabelSetPoint, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                .addContainerGap())
        );

        PanelControlePIDLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {ButtonMotor, LabelKd, LabelKi, LabelKp, LabelSetPoint, SliderKd, SliderKi, SliderKp, SliderSetPoint, TextKd, TextKi, TextKp, TextSetPoint});

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        PanelPrincipal.add(PanelControlePID, gridBagConstraints);

        PanelHelp.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        LabelHelp.setText("Aguarde!");

        PanelColorHelp.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout PanelColorHelpLayout = new javax.swing.GroupLayout(PanelColorHelp);
        PanelColorHelp.setLayout(PanelColorHelpLayout);
        PanelColorHelpLayout.setHorizontalGroup(
            PanelColorHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        PanelColorHelpLayout.setVerticalGroup(
            PanelColorHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        LabelTempo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LabelTempo.setText("00:00:00");

        javax.swing.GroupLayout PanelHelpLayout = new javax.swing.GroupLayout(PanelHelp);
        PanelHelp.setLayout(PanelHelpLayout);
        PanelHelpLayout.setHorizontalGroup(
            PanelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanelColorHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                .addComponent(LabelTempo, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        PanelHelpLayout.setVerticalGroup(
            PanelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(PanelColorHelp, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PanelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(LabelTempo)
                        .addComponent(LabelHelp, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 36;
        PanelPrincipal.add(PanelHelp, gridBagConstraints);

        PanelGrafico.setBackground(new java.awt.Color(204, 204, 204));
        PanelGrafico.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        PanelGrafico.setPreferredSize(new java.awt.Dimension(200, 200));

        BarGrafico.setMaximum(0);
        BarGrafico.setOrientation(javax.swing.JScrollBar.HORIZONTAL);

        javax.swing.GroupLayout PanelGraficoLayout = new javax.swing.GroupLayout(PanelGrafico);
        PanelGrafico.setLayout(PanelGraficoLayout);
        PanelGraficoLayout.setHorizontalGroup(
            PanelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelGraficoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BarGrafico, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );
        PanelGraficoLayout.setVerticalGroup(
            PanelGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelGraficoLayout.createSequentialGroup()
                .addContainerGap(334, Short.MAX_VALUE)
                .addComponent(BarGrafico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 155;
        gridBagConstraints.ipady = 323;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        PanelPrincipal.add(PanelGrafico, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void SliderSetPointStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SliderSetPointStateChanged
        // TODO add your handling code here:
        int v = SliderSetPoint.getValue();
//        client.setSendString(0,"set "+String.valueOf(v));
        envia_data(PID_SET);
        TextSetPoint.setText(String.valueOf((v*10000/255)/100.0));
        
    }//GEN-LAST:event_SliderSetPointStateChanged

    private void SliderKpStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SliderKpStateChanged
        // TODO add your handling code here:
        int v = SliderKp.getValue();
        envia_data(PID_KP);
        TextKp.setText(String.valueOf(((float)(v)/1000)));
    }//GEN-LAST:event_SliderKpStateChanged

    private void SliderKiStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SliderKiStateChanged
        // TODO add your handling code here:
        int v = SliderKi.getValue();
        envia_data(PID_KI);
        TextKi.setText(String.valueOf(((float)(v)/1000)));
    }//GEN-LAST:event_SliderKiStateChanged

    private void SliderKdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SliderKdStateChanged
        // TODO add your handling code here:
        int v = SliderKd.getValue();
        envia_data(PID_KD);
        TextKd.setText(String.valueOf(((float)(v)/1000)));
    }//GEN-LAST:event_SliderKdStateChanged

    private void ButtonMotorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonMotorActionPerformed
        // TODO add your handling code here:
        ButtonMotorAtualizaText();
        
        boolean bool = ButtonMotor.isSelected();
        
        int v = 0;
        if (bool){
            v = 1;
        }
        
        SliderKp.setEnabled(!bool);
        SliderKi.setEnabled(!bool);
        SliderKd.setEnabled(!bool);
        
        
        envia_data(PID_LIGA);
    }//GEN-LAST:event_ButtonMotorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollBar BarGrafico;
    private javax.swing.JToggleButton ButtonMotor;
    private javax.swing.JLabel LabelHelp;
    private javax.swing.JLabel LabelKd;
    private javax.swing.JLabel LabelKi;
    private javax.swing.JLabel LabelKp;
    private javax.swing.JLabel LabelSetPoint;
    private javax.swing.JLabel LabelTempo;
    private javax.swing.JPanel PanelColorHelp;
    private javax.swing.JPanel PanelControlePID;
    private JPanelGrafico PanelGrafico;
    /*
    private javax.swing.JPanel PanelGrafico;
    */
    private javax.swing.JPanel PanelHelp;
    private javax.swing.JPanel PanelPrincipal;
    private javax.swing.JSlider SliderKd;
    private javax.swing.JSlider SliderKi;
    private javax.swing.JSlider SliderKp;
    private javax.swing.JSlider SliderSetPoint;
    private javax.swing.JTextField TextKd;
    private javax.swing.JTextField TextKi;
    private javax.swing.JTextField TextKp;
    private javax.swing.JTextField TextSetPoint;
    // End of variables declaration//GEN-END:variables
}
