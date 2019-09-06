/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Henrique
 * @data 29/08/2011
 * 
 * Recriado para utilizar no JLED
 */
import java.io.*;
import java.net.*;


public class EtherShield {
    
    
    private final static String KEY1 = "on_entrar";
    private final static String KEY2 = "off_sair";//
    private final static String KEY3 = "ERROR!";
    private final static String KEY4 = "dado";
    
    private final static int SENDSTRINGMAX = 10;
    
    private static boolean key = false;
    
    private static Socket client = null;
    private static PrintWriter out = null;
    private static BufferedReader in = null;
    
    private static boolean connected = false;
    
    private static String ip = "192.168.1.125";
    private static int port = 80;

    private static StringBuffer ReadBuffer = new StringBuffer("");
    private static String ReadString = "";
    
    private static StringBuffer SendBuffer = new StringBuffer("");
    
    private static String SendString[] = new String[SENDSTRINGMAX];
    
    public EtherShield(){}
    
    public EtherShield(String h, int p){
        ip = h;
        port = p;
    }
    
    public void SetIp(String h){
        ip = h;
    }
    
    public void SetPort(int p){
        port = p;
    }
    
    public void SetIpPort(String h, int p){
        ip = h;
        port = p;
    }
    
    public boolean Connected(){
        return connected;
    }
 
    public boolean Key(){
        return key;
    }
    
    private void Connect() {
        try {
            System.out.println("Conectando...");
            client = new Socket(ip,port);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            connected = true;
            System.out.println("Conectado!");
        } 
        catch (Exception exception) {  
            connected = false;
            key = false;
            System.out.println("Falhou!");
        }
        
        for(int i = 0;i < SENDSTRINGMAX;i++){
            SendString[i] = "";
        }
        
    }
    
    private void Close(){
        try {
            if (client != null){
                client.close();
                client = null;
            } 
        } 
        catch (IOException ex) {
            client = null;
        }
        try {
            if (in != null){
                in.close();
                in = null;
            }
        } 
        catch (IOException ex) {
            in = null;
        }
        
        if (out != null){
            out.close();
            out = null;
        }
        connected = false;
        key = false;
        System.out.println("Desconectado!");
    }
    
    public void print(String s){
        SendBuffer.append(s); 
    }
    
    public void println(String s){
        SendBuffer.append(s);
        SendBuffer.append('\n');  
    }

    public void setSendString(int n,int i){
        if (n < 0) return;
        else if (n >= SENDSTRINGMAX) return;
        
        
        String s = String.valueOf(i);
        String s2 = String.valueOf(n);
                
        SendString[n] = KEY4+" "+s2+" "+s+"\n";
    }
    
    public void Send(){
        if (connected){

            for(int i = 0;i < SENDSTRINGMAX;i++){
                if (SendString[i] != null)
                out.print(SendString[i]);
                SendString[i] = "";
            }

            
            out.print(SendBuffer);
            out.flush();
            System.out.println(SendBuffer);
            System.out.println("Send!!!");
        }
        SendBuffer.setLength(0);
    }
    
    
    public void Sair(){
        println(KEY2);
        Send();
        Close();
    }
    
    public void Entrar(){
        Connect();
        println(KEY1);
        Send();
    }
    
    public boolean Entrou(){
        if (key) return false;
        if (readLine(KEY1)){
            key = true;
            return true;
        }
        return false;
    }
    
    public boolean Saiu(){
        if (readLine(KEY2)){
            Close();
            return true;
        }
        return false;
    }
    
    public boolean Error(){
        if (readLine(KEY3)){
            Close();
            return true;
        }
        return false;
    }
    
    public boolean Dado(){
        if (Linha(KEY4)){
            return true;
        }
        return false;
    }
    
    public boolean Linha(String s){
        if (!key) return false;
        return (readLine(s));
    }
    
    public int LinhaInt(){
        readDado();
        int valor;
        try{
            valor = Integer.valueOf(ReadString);
        }catch (Exception e) {
            valor = 0;
        }
        ReadString = "";
        return valor;
    }
    
    public boolean Vazio(){
        return (ReadBuffer.length()==0);
    }
    
    public boolean read(){
        
        ReadString = "";
        ReadBuffer.setLength(0);// = new StringBuffer("");
        if (!connected) return false;
        if (in == null) return false;

        try {
            /*
            boolean b = false;
            while (in.ready()){
                b = true;
                String s = in.readLine();
                System.out.println(s);
                ReadBuffer.append(s);
                ReadBuffer.append(" ");
            }
            //System.out.println(ReadBuffer);
            return b;
            */
            /*
            while(in.ready()){
                int i = in.read();
                if (i == -1) return false;
                char c = (char) i;
                System.out.print(c);
                
                if (c == '\n') return true;
                ReadBuffer.append(c);
                
                
                //return true;
            }
            */
            
               if(in.ready()){
                
                ReadBuffer.append(in.readLine());
                System.out.println(ReadBuffer);
                return true;
            }
            
        } catch (IOException ex) {} 
        return false;
    }
    
    private boolean readLine(String s){
        readDado();
        
        if (ReadString.equals(s)){
            
            ReadString = "";
            return true;
        }
        return false;
    }
    
    private void readDado(){
        if (ReadString.length() != 0) return;
        int n = ReadBuffer.indexOf(" ");
        if (n == -1) n = ReadBuffer.length();    
        ReadString = ReadBuffer.substring(0, n);
        ReadBuffer.delete(0, n+1);
        
        //System.out.print(">> ");
        //System.out.println(ReadString);
        
    }
    
}
