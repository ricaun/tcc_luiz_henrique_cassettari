/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Henrique
 */
public class Timer {
    
    private int tempo;
    private long lastnow;
    private boolean ligado = false;
    
    public Timer(int t){
        tempo = t;
    }
    public void Set(int t){
        tempo = t;
    }
    
    public void Start(){
        ligado = true;
        lastnow = System.currentTimeMillis();
    }
    
    public void Stop(){
        ligado = false;
    }
    
    public boolean Get(){
        if (!ligado) return false;
        long now = System.currentTimeMillis();
        long total = now - lastnow;
        if (total >= tempo){
            lastnow = now;
            return true;
        }
        return false;
    }
}
