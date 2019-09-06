/*

  Timer.h 
  
  V1.0
  
  14/09/2011
  
  Luiz Henrique Cassettari
  
*/
#ifndef Timer_h
#define Timer_h

#include "WProgram.h"

class Timer{
  public:
    Timer(unsigned long ms);
    void Set(unsigned long ms);
    boolean Get();
    void Start();
    void Stop();
  private:
    unsigned long lastnow;
    unsigned long tempo;
    boolean ligado;
};

#endif
