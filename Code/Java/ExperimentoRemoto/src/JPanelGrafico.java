

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Henrique
 */
public class JPanelGrafico extends JPanel {//implements MouseListener,MouseMotionListener {
      //   implements MouseListener,MouseMotionListener

    private final int listaN = 1000;
    private final int Nshow = 100;
    
    private final int xx1 = 32;
    private final int xx2 = 32;
    
    private final int yy1 = 32+24;
    private final int yy2 = 32;

    private final Color COR_SET = Color.red;
    private final Color COR_ROT = Color.blue;
    private final Color COR_DEF = Color.black;
    
    private JScrollBar JBar = null;
    
    private Vector lista;
    
    //private boolean addstop = false;
    private int tempo = 0;
    
    private int mx,my;

    public void setBar(JScrollBar jb){
        JBar = jb;
    }
    
    public void updateBar(){
        if (JBar == null) return;
        if (lista.size()-1!=listaN)
            JBar.setValues(JBar.getValue(), Nshow, Nshow-(lista.size()-1), Nshow);
        
        //JBar.setValue(Grafico.getN());
    }
    
    public JPanelGrafico(){
        init();
    }
/*
    public void graficostop(boolean b){
        addstop = b;
    }*/
    
    public void init(){
        
        
        //addMouseMotionListener(this);
        //addMouseListener(this);
        
        mx = 0;
        my = 0;
        
        lista = new Vector();
        /*
        for(int i = 0;i < listaN; i++){
            double valor = 127*Math.cos(1.97*i*Math.PI/180)+127;
            lista.addElement( new Point(i, (int) valor));
        }*/
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        updateBar();
        
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        /*
        if (JBar != null)
            g2.drawString(String.valueOf(JBar.getValue()), mx, my);
        */
        
        
        int w = getWidth()-1;
        int h = getHeight()-1;
        
        int w1 = xx1;
        int w2 = w - xx2 - xx1;
        
        int h1 = yy2;
        int h2 = h - yy1 -yy2;
        
        int yt = 8;
        
        g2.setColor(COR_DEF);
        
        
        //g2.drawOval( mx-2, my-2, 4, 4 );
        
        g2.drawString("Rotação (%) x Tempo (s)", w2/2-xx1, h1-10);
        
        //g2.drawRect(w1, h1, w2, h2);
        g2.drawLine(w1, h1-yt, w1, h1+h2+yt); // rotaco
        
        g2.drawLine(w1-3, h1-yt, w1+3, h1-yt); // rotaco flexa
        g2.drawLine(w1-3, h1-yt, w1, h1-yt-6); // rotaco flexa
        g2.drawLine(w1+3, h1-yt, w1, h1-yt-6); // rotaco flexa
        
        g2.drawLine(w1, h1+h2+yt, w1+w2+2, h1+h2+yt); // tempo
        g2.drawLine(w1+w2+2, h1+h2-3+yt, w1+w2+2, h1+h2+3+yt); // tempo flexa
        g2.drawLine(w1+w2+2, h1+h2-3+yt, w1+w2+2+6, h1+h2+yt); // tempo flexa
        g2.drawLine(w1+w2+2, h1+h2+3+yt, w1+w2+2+6, h1+h2+yt); // tempo flexa
        
        // draw rotacao porcentagem scala e numeros
        for(int i = 0; i < 5 ;i++){
            int hh = - (i*h2)/4;
            g2.drawLine(w1-2, h1+h2 + hh, w1, h1+h2 + hh);
            
            int ww = 0;
            if (i==0) ww = +6;
            else if (i==4) ww = -6;
            
            g2.drawString(String.valueOf(25*i),w1-18+ww, h1+h2 + hh+4);
        }
        
        
        //g2.setColor(Color.red);
        
        //boolean showpoint = addstop;
        
        for(int i = 1;i < Nshow; i++){
            int j = i;
            if (lista.size() > Nshow) {
                j = i + JBar.getValue()+lista.size()-Nshow-1;
            }
            
            if (lista.size() <= j) break;
            Point A = (Point)(lista.elementAt(j-1));
            Point B = (Point)(lista.elementAt(j));
            
            int x1 = w1+(w2*(i-1))/(Nshow-1);
            int x2 = w1+(w2*i)/(Nshow-1);
            
            int y11 = (h2+h1)-(A.x)*h2/(255);
            int y21 = (h2+h1)-(B.x)*h2/(255);
            
            int y12 = (h2+h1)-(A.y)*h2/(255);
            int y22 = (h2+h1)-(B.y)*h2/(255);
            
            
            
            
            //// draw quando pausado o grafico!
            
            
            
            boolean bool = lista.size() == listaN;
            /*
            if (showpoint){
                int jj = j-1;
                if (bool) jj += tempo-listaN;
                if ((x2 > mx)&(mx >= x1)){
                    
                    g2.setColor(COR_SET);
                    
                    g2.drawOval( x1-2, y11-2, 4, 4 );
                    
                    String str = "";
                    
                    str += "( "+String.valueOf(jj/20);
                    str += "."+String.valueOf((jj%20)*5);
                    str += " , "+String.valueOf((10000*A.x/255)/100.0)+" )";
                    
                    g2.drawString(str, mx+32, my);
                    
                    showpoint = false;
                }
            }
            */
            //// draw numeros e escala do tempo!
            g2.setColor(COR_DEF);
            int j2 = j-1;            
            if (bool) j2+= tempo;
            if ((j2%20)==0) {
                if (bool) j2 -= listaN;
                
                //g2.drawOval( x1-2, y11-2+yt, 4, 4 );
                g2.drawLine(x1, h2+h1+yt, x1, h2+h1+yt+2);
                g2.drawString(String.valueOf(j2/20), x1-3, h2+h1+yt+14);
                
            }
            if (i+1 == Nshow){
                j2 = j;
                if (bool) j2+= tempo;
                if ((j2%20)==0) {
                    if (bool) j2 -= listaN;
                    //g2.drawOval( x2-2, y21+yt-2, 4, 4 );
                    g2.drawLine(x2, h2+h1+yt, x2, h2+h1+yt+2);
                    g2.drawString(String.valueOf(j2/20), x2-3, h2+h1+yt+14);
                }
            }
            
            
            /*
            g2.drawOval( x1-2, y11-2, 4, 4 );
            if (i+1 == Nshow)
                g2.drawOval( x2-2, y21-2, 4, 4 );
            */
            
            g2.setColor(COR_SET);
            g2.drawLine( x1, y11, x2, y21 );
            
            g2.setColor(COR_ROT);
            g2.drawLine( x1, y12, x2, y22 );
        }
        
        //g2.drawOval(mx-5, my-5,10,10);
    }
    
    public void addPoints(int v1, int v2){
        //if (addstop) return;
        if (JBar.getValue()!=0) return;
        
        while (lista.size() >= listaN)
            lista.removeElementAt(0);
        lista.addElement( new Point(v1, v2));
        tempo++;
    }
/*
    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        //int bu = e.getButton();
        //if (bu == 1) // botão esquerdo do mouse!
        //    addstop = !addstop;
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    */
  
}

















/*
 int y1 = (getHeight()-1)-(JG.getPoint(i)*(getHeight()-1)/255);
            int y2 = (getHeight()-1)-(JG.getPoint(i+1)*(getHeight()-1)/255);

            int x1 = ((getWidth()-1)*(i))/(JG.getN()-1);    //(i)+(getWidth()-1-JG.getN())
            int x2 = ((getWidth()-1)*(i+1))/(JG.getN()-1);
            
            g2.setPaint(Color.blue);
            g2.drawLine(x1, y1, x2, y2);
            
            y1 = (getHeight()-1)-(JG.getSPoint(i)*(getHeight()-1)/255);
            y2 = (getHeight()-1)-(JG.getSPoint(i+1)*(getHeight()-1)/255);
            
            g2.setPaint(Color.red);
            g2.drawLine(x1, y1, x2, y2);
            //if (i == 100) g2.drawOval(x1, y1,5,5);
 */







    /*
    private void drawbagulho(Graphics2D g2,int x, int y, int i){
        
        int v = JG.tempo+((i+JG.getnshow() -(JG.getN()))/ 20);
        if (v >= 0) {
            
            int yy1 = getHeight()-1-JG.getY();
            
            
            //g2.drawLine(x, y, x, yy1);
            //g2.drawOval(x-2, yy1-2,4,4);
            g2.drawLine(x, yy1+4, x, yy1);
            
            String s = ":";
            if ((v % 60)<10) s = ":0";
         
            s = String.valueOf(v / 60)+s+String.valueOf(v % 60);
            g2.drawString(s, x-8, yy1+16);
        }
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        if (JG == null) return;
        
        Graphics2D g2 = (Graphics2D) g;

        //g2.drawRect(0,0,getWidth()-1,getHeight()-1);
        
        
        
        int yy1 = getHeight()-1-JG.getY();
        int yy2 = getHeight()-1-JG.getY()-JG.getY2();
        
        int xx1 = JG.getX();
        int xx2 = getWidth()-1-JG.getX()-JG.getX2();

        
        g2.setPaint(Color.gray);
        
        g2.drawString("Rotação x Tempo", (getWidth()-1)/2-54, 18);
                    
        g2.setPaint(Color.darkGray);
        
        for (int i = 0;i<=4;i++){
            int y1 = (yy1)-((i)*(yy2)/4);
            int y2 = y1;
            
            int x1 = xx1; 
            int x2 = xx2+xx1;

            g2.drawLine(x1, y1, x2, y2);
        }
        
        for (int i = 0;i<=JG.getNshow()*50/1000;i++){
            int y1 = yy1;
            int y2 = -yy2+yy1;
            
            int x1 = xx1+(i*xx2)/(JG.getNshow()*50/1000); 
            int x2 = x1;
            
            g2.drawLine(x1, y1, x2, y2);
        }
        
        
        for (int i = 0;i< JG.getNshow();i++){
            
            int i1 = JG.getnshow()+i;
            
            int y1 = (yy1)-(JG.getPoint(i1)*(yy2)/254);
            int y2 = (yy1)-(JG.getPoint(i1+1)*(yy2)/254);

                     
            // valor da rotacao!
            int x1 = xx1+(xx2*(i))/(JG.getNshow()); 
            int x2 = xx1+(xx2*(i+1))/(JG.getNshow());

            g2.setPaint(Color.gray);
            if ((JG.getn(i1) % 20)==0) {
                drawbagulho(g2,x1,y1,i);
            }
            else if ((i==JG.getNshow()-1)&((JG.getn(i1+1) % 20)==0)) {
                drawbagulho(g2,x2,y2,i);
            }
            
            
            // pinta rotacao
            g2.setPaint(Color.blue);
            g2.drawLine(x1, y1, x2, y2);
            
            // valor do setpoint
            y1 = (yy1)-(JG.getSPoint(i1)*(yy2)/254);
            y2 = (yy1)-(JG.getSPoint(i1+1)*(yy2)/254);
            
            // pinta setpoint
            g2.setPaint(Color.red);
            g2.drawLine(x1, y1, x2, y2);
            

        }
        
        g2.drawOval(mx-2, my-2,4,4);
        
  
    }
    */