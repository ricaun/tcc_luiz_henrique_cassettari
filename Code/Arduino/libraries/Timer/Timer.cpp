/*

  Timer.cpp 
  
  V1.0
  
  14/09/2011
  
  Luiz Henrique Cassettari
  
*/

#include "Timer.h"

Timer::Timer(unsigned long ms){
  ligado = false;
  tempo = ms;
  lastnow = 0;
}

void Timer::Set(unsigned long ms){
  tempo = ms;
}

boolean Timer::Get(){
  if (!ligado) return false;
  unsigned long now = millis();
  unsigned long total = now - lastnow;
  if (total >= tempo){
    lastnow = now;
    return true;
  }
  return false;
}

void Timer::Start(){
  ligado = true;
  lastnow = millis();
}

void Timer::Stop(){
  ligado = false;
}


