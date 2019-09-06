/*
*  Arquivo: tcp_ethershield
*  Versão: 2.0
*  Autor: Luiz Henrique Cassettari
*  Descrisão: Arquivo utilizado para simplificar a biblioteca do ethernet shield da nuelectronics.com.
*             A biblioteca não foi modificada, somente adicionada funções para simplificar o programa principal.
*  Data: 10/11/2011
*/


// Configuração do ethernet
byte mac[6] = {0x54,0x55,0x58,0x10,0x00,0x24};
//byte ip[4] = {10,21,42,50};
byte ip[4] = {10,10,10,20};
int port = 80;

// Configuração do buffer
#define BUFFER_SIZE 500



// -----------------------------------------------------------------------------------------------------------------------------------------------------
// Incluindo bibliotecas e variáveis
// -----------------------------------------------------------------------------------------------------------------------------------------------------

#include <etherShield.h>

EtherShield es = EtherShield();

static byte buf[BUFFER_SIZE+1];

static struct {
  boolean close;
  int n_rec; 
  int i_rec;
  int i_env;
} client;
// -----------------------------------------------------------------------------------------------------------------------------------------------------

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// server_init
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void server_init(){
  /*initialize enc28j60*/
   es.ES_enc28j60Init(mac);
   es.ES_enc28j60clkout(2); // change clkout from 6.25MHz to 12.5MHz
   delay(10);
/*
	es.ES_enc28j60PhyWrite(PHLCON,0x880);
	delay(500);

	es.ES_enc28j60PhyWrite(PHLCON,0x990);
	delay(500);

	es.ES_enc28j60PhyWrite(PHLCON,0x880);
	delay(500);

	es.ES_enc28j60PhyWrite(PHLCON,0x990);
	delay(500);
*/
  es.ES_enc28j60PhyWrite(PHLCON,0x476);
  delay(100);      

  es.ES_init_ip_arp_udp_tcp(mac,ip,port);
}





// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_close
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_close(){
  client.close = true;
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_clear
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_clear(){
  client.close = false;

  client.i_rec = 0;
  client.n_rec = 0;

  client.i_env = 0;
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_available
// -----------------------------------------------------------------------------------------------------------------------------------------------------

int client_available(){
  return client.n_rec;
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_read 
// -----------------------------------------------------------------------------------------------------------------------------------------------------

byte client_read(){
  byte b = buf[TCP_DATA_P + client.i_rec++];
  client.n_rec--;
  return b;
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_write 
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_write(byte b){
  buf[TCP_DATA_P + client.i_env++] = b;
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_read_line (string)
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_read_line(String &s){
  s = "";
  char c;
  while(client.n_rec){
      c = (char) client_read();
      if (c == '\n') break;
      s += c;
  }
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_print (string)
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_print_string(String &s){
  int len = s.length();
  for(int i = 0; i < len; i++)
    client_write(s[i]);
  s = "";
}

void client_println_string(String &s){
  client_print_string(s);
  client_write('\n');
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_print (const char)
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_print(const char *s){
  for(int i = 0; s[i] != '\0'; i++)
    client_write(s[i]);
}
void client_println(const char *s){
  client_print(s);
  client_write('\n');
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_print (byte)
// -----------------------------------------------------------------------------------------------------------------------------------------------------
void client_print(byte b){
  String s = (int) b;;
  client_print_string(s);
}
void client_println(byte b){
  client_print(b);
  client_write('\n');
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_print (int)
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_print(int i){
  String s = i;
  client_print_string(s);
}
void client_println(int i){
  client_print(i);
  client_write('\n');
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_print (long)
// -----------------------------------------------------------------------------------------------------------------------------------------------------

void client_print(long l){
  String s = l;
  client_print_string(s);
}
void client_println(long l){
  client_print(l);
  client_write('\n');
}




// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_send
// -----------------------------------------------------------------------------------------------------------------------------------------------------
void client_send() {

    if (client.i_env == 0) client_println("ERROR!");
  
    es.ES_make_tcp_ack_from_any(buf); // send ack for http get
    
    if (client.close) 
      es.ES_make_tcp_ack_with_data(buf,client.i_env); // send data and close 
    else
    es.ES_tcp_client_send_packet (
                                       buf,
                                       (buf[TCP_DST_PORT_H_P]<<8) + buf[TCP_DST_PORT_L_P],                                             // destination port
                                       port,                                   // source port
                                       TCP_FLAG_ACK_V | TCP_FLAG_PUSH_V,                        // flag
                                       0,                                              // (bool)maximum segment size
                                       0,                                              // (bool)clear sequence ack number
                                       0,                                              // 0=use old seq, seqack : 1=new seq,seqack no data : >1 new seq,seqack with data
                                       client.i_env,                           // tcp data length
                                       &buf[ETH_DST_MAC],
				       &buf[IP_DST_P]
				      );
}

// -----------------------------------------------------------------------------------------------------------------------------------------------------
// client_receive
// -----------------------------------------------------------------------------------------------------------------------------------------------------

int client_receive(){
    
  int plen;
    
  plen = es.ES_enc28j60PacketReceive(BUFFER_SIZE, buf);

  /*plen will ne unequal to zero if there is a valid packet (without crc error) */
  if(plen!=0){
	           
    client_clear();
    
    // arp is broadcast if unknown but a host may also verify the mac address by sending it to a unicast address.
    if(es.ES_eth_type_is_arp_and_my_ip(buf,plen)){
      es.ES_make_arp_answer_from_request(buf);
      return 0;
    }

    // check if ip packets are for us:
    if(es.ES_eth_type_is_ip_and_my_ip(buf,plen)==0){
      return 0;
    }
    
    if(buf[IP_PROTO_P]==IP_PROTO_ICMP_V && buf[ICMP_TYPE_P]==ICMP_TYPE_ECHOREQUEST_V){
      es.ES_make_echo_reply_from_request(buf,plen);
      return 0;
    }
    
    if (((buf[TCP_DST_PORT_H_P]<<8) + buf[TCP_DST_PORT_L_P]) != port) return 0;
    
    // tcp port www start, compare only the lower byte
    if ((buf[IP_PROTO_P]==IP_PROTO_TCP_V)  ){

      if (buf[TCP_FLAGS_P] & TCP_FLAGS_SYN_V){
         es.ES_make_tcp_synack_from_syn(buf); // make_tcp_synack_from_syn does already send the syn,ack
         return 0;     
      }
      
      if (buf[TCP_FLAGS_P] & TCP_FLAGS_ACK_V){
        es.ES_init_len_info(buf); // init some data structures
        int dat_p=es.ES_get_tcp_data_pointer();
        if (dat_p==0){ // we can possibly have no data, just ack:
          if (buf[TCP_FLAGS_P] & TCP_FLAGS_FIN_V){
            es.ES_make_tcp_ack_from_any(buf);
          }
          return 0;
          
        }
        
        
        plen = ((buf[IP_TOTLEN_H_P]<<8)|(buf[IP_TOTLEN_L_P]&0xff)-IP_HEADER_LEN-(buf[TCP_HEADER_LEN_P]>>4)*4);
        
        if (plen > BUFFER_SIZE-TCP_DATA_P) plen = BUFFER_SIZE-TCP_DATA_P;
        
        client.n_rec = plen;
        
        return (plen > 0);
      }
    }
  }
  return 0;
}
// -------------------------------------------------------------------------------------




