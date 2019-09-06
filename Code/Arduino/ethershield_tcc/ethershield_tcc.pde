/*
*  Arquivo: ethershield_tcc
*  Versão: 1.0
*  Autor: Luiz Henrique Cassettari
*  Descrisão: Arquivo principal do Arduino.
*             Utilizado para controlar a rotação do motor com uma saída PWM, possui um filtro digital, algoritmo do controlador PID, e lógica de controle pela rede internet.
*  Data: 15/11/2011
*/

// --------------- defines --------------- //

#define PID_PIN 9
#define ENTRAR "on_entrar"
#define SAIR "off_sair"
#define DATA "dado"

// --------------- PID --------------- //

#include <Timer.h>

static Timer pid_timer(50);

boolean motor = false; // status do motor = ligado ou desligado

int kp = 1000; // constante proporcional
int ki = 500; // constante integral
int kd = 0; // constante derivativa

int setpoint = 204; // setpoint do controlador pid
int saida = 0; // saida do controlador pid

long erro_somatorio = 0; // somatorio dos erros
int erro_anterior = 0; // erro anterior

void fazer_pid(int entrada){
  
  if ((kp==0)&(ki==0)&(kd==0)){
      saida = setpoint;
      return;
  }
  
  int erro; // declaração do erro
  float dt = (float) 50/1000; // periodo de amostragem igual a 50ms
  float kps = (float) kp/1000;
  float kis = (float) ki/1000;
  float kds = (float) kd/1000;
  
  //kis *= dt;
  //kds /= dt;
    
  erro = setpoint - entrada; // calculo do erro
  erro_somatorio += (long) erro; // adiciona erro ao erro_somatorio
  
  float p = (float) (erro);
  float i = (float) (erro_somatorio);
  float d = (float) (erro - erro_anterior);
  
  saida = p*kps+i*kis+d*kds;
  
  erro_anterior = erro; // guarda erro para proxima interação 
}

// --------------- Filtro --------------- //

static int nfiltro = 50; // valor da potencia do filtro

byte filtro_digital(){
  float n = (float) nfiltro/10000; // potencia do filtro passa baixa
  static float filtro; // declara variavel filtro como float estatico 
  filtro = n*(analogRead(0)/4) + filtro*(1-n); // equação do filtro passa baixa
  return (byte) filtro;  // retorna valor filtrado
}

// --------------- Comandos net --------------- //

String string_taco = "";
static boolean ocupado = false;
static byte contador = 0;

enum data {PID_STATUS, PID_LIGA, PID_SET, PID_KP, PID_KI, PID_KD, TACO};

void envia_d(int i){
  client_print(DATA);
  client_write(' ');
  client_print(i);
  client_write(' ');
}

void ler_data(int i, long l){

  switch(i){
    case PID_LIGA:
      motor = l;
      if (motor) string_taco = "";
      break;
    case PID_SET:
      setpoint = l;
      break;    
    case PID_KP:
      kp = l;
      break;   
    case PID_KI:
      ki = l;
      break;   
    case PID_KD:
      kd = l;
      break;   
  }
}

void envia_data(int i){
  switch(i){
    case PID_STATUS:
      envia_data(PID_LIGA);
      envia_data(PID_SET);
      envia_data(PID_KP);
      envia_data(PID_KI);
      envia_data(PID_KD);
      break;
    case PID_LIGA:
      envia_d(i);
      client_println(motor);
      break;
    case PID_SET:
      envia_d(i);
      client_println(setpoint);
      break;    
    case PID_KP:
      envia_d(i);
      client_println(kp);
      break;   
    case PID_KI:
      envia_d(i);
      client_println(ki);
      break;   
    case PID_KD:
      envia_d(i);
      client_println(kd);
      break;   
    case TACO:
      client_print(DATA);
      client_write(' ');
      client_print(i);
      client_println_string(string_taco);
      break;   
  }
}

void limpa_tudo(){
  erro_anterior = 0;
  erro_somatorio = 0;
  analogWrite(PID_PIN,0);
}

// --------------- setup --------------- //

void setup(){
  server_init(); // inicia o chip enc28j60
  pid_timer.Start();
  pinMode(PID_PIN, OUTPUT); 
  analogWrite(PID_PIN,0);
  analogReference(EXTERNAL); // Ativa referência analogica
}

// --------------- loop --------------- //

void loop(){
  byte tacos = filtro_digital();  
  if (pid_timer.Get()){
    
    if (ocupado){
      if (100 <=contador++){
        ocupado = false;
        contador = 0;
        motor = false;
      }
    }
    if (motor){
      fazer_pid(tacos);
      if (saida < 0) saida = 0;
      else if (saida > 255) saida = 255;
      analogWrite(PID_PIN,saida);
    }
    else {
      limpa_tudo();
    }
    if (string_taco.length()<200){
      string_taco += " ";
      string_taco += (int) motor*setpoint;
      string_taco += " ";
      string_taco += (int) tacos;
    }
  }
  
  if (client_receive()){ // caso recebeu algum pacote de dados
    String string = "";
    
    while(client_available()){ // enquanto tiver dados para ler
      String str = "";
      client_read_line(str);
      string += str;
      string += "\n";
    }
    
    while(string.length()){
      String str = "";
      string_line(string,str);
      
      if (string_test(str,"GET")){
        client_println(contador*50);
        client_println(ocupado);
        client_close();
        break;
      }
      
      if (string_test(str,"fil")){
        nfiltro = string_valor(str);
        break;
      }
      
      if (string_test(str,ENTRAR)){
        if (ocupado){
          client_println(SAIR);
          break;
        }
        ocupado = true;
        client_println(ENTRAR);
        string_taco = "";
      }
      if (string_test(str,SAIR)){
        client_println(SAIR);
        ocupado = false;
        contador = 0;
        motor = false;
      }
      if (string_test(str,DATA)){
        contador = 0;
        int i = string_valor(str);
        if (str.length())        
          ler_data(i,string_valor(str));
        envia_data(i);
      }
    }
    
    client_send(); // envia o pacote
  } 
}

// --------------- Comandos string --------------- //

boolean string_test(String &str,const char* s){
  boolean ret = true;
  
  int n = str.indexOf(' ');  
  if (n == -1) n = str.length();
  
  int i;
  for(i = 0; s[i]!='\0'; i++){
    if (s[i] != str[i]) ret = false;
  }
  if (ret){
    if (i==n){
      str = str.substring(i+1);
      return true;
    }
  }
  return false;
}

void string_line(String &str, String &s){
  int n = str.indexOf('\n');  
  s = str.substring(0,n);
  str = str.substring(n+1);
}

long string_valor(String &str){
  int n = str.indexOf(' ');  
  if (n == -1) n = str.length();
  String s = str.substring(0,n);
  str = str.substring(n+1);
  return s.toInt();
}

// ---------------  --------------- //
