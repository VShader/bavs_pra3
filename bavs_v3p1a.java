import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;  

import java.util.* ;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;


class OutputFrame extends Frame{
  /*
  Simple frame for
    output texts to a window,
    switch logging on and off,
    save window contents to file
  */

  TextArea the_text=new TextArea() ;
  Font listFont=new Font("Monospaced",Font.PLAIN,12) ;
  boolean is_on=true ;
  String last_line="" ;
  BufferedWriter input_writer ;
  String list_name ;
  String list_path="" ;

  String add_crlf(String s)
    { String s_crlf="" ;
      char c ; int k,len ;
      len=s.length() ;
      for ( k=0 ; k<len ; k++ )
        { c=s.charAt(k) ;
          if ( c=='\n' ) { s_crlf=s_crlf+(char)13+(char)10 ; }
              else { s_crlf=s_crlf+c ; }
        }
    return s_crlf ;
    }

  OutputFrame(String title)
   { super(title) ;
   list_name=title ;
   setSize(300,170) ;
   the_text.setFont(listFont);
   Panel pan_a=new Panel() ;
   Button on_but   =new Button("ON") ;
   Button off_but  =new Button("OFF") ;
   Button list_but =new Button("SAVE") ;
   Button clear_but=new Button("CLEAR") ;
   pan_a.add(on_but   )  ;
   pan_a.add(off_but  )  ;
   pan_a.add(list_but )  ;
   pan_a.add(clear_but)  ;
   on_but.addActionListener(new on_listener());
   off_but.addActionListener(new off_listener());
   list_but.addActionListener(new list_listener());
   clear_but.addActionListener(new clear_listener());
   setLayout(new BorderLayout()) ;
   add("Center",the_text) ;
   add("South",pan_a) ;
  // deprecation    show();
    setVisible(true) ;

 //   addWindowListener(new lister_wlistener()) ;
    }

  void checkit()
    { if ( the_text.getCaretPosition()>10000 )
      { the_text.replaceRange("-- TEXT DELETED --\n",0,5000); }
    }

  void listln(String s)
    { if ( is_on)
        { checkit() ; the_text.append(s); the_text.append("\n") ; last_line="" ;} }

  void list(String s) {
     if ( is_on) {
       checkit() ;
       the_text.append(s);
       last_line=last_line+s ;
       if ( last_line.length()>128)
          { the_text.append("\\LF\n") ; last_line="" ; }
      } }

  void start() { is_on=true ; }

  void stop() {  is_on=false ; }

  void save_edit_area(String full_name)
    { try { input_writer=new BufferedWriter(new FileWriter(full_name)) ; }
    catch(Exception e) { listln("ERR WRopen["+full_name+"] e="+e) ; return ; }

    String s=the_text.getText() ;
    s=add_crlf(s) ;
    try { input_writer.write(s); }
    catch(Exception e) { listln("ERR write["+full_name+"] e="+e) ; return ; }

    try {input_writer.close() ; }
    catch(Exception e) {listln("ERR WRclose"+e) ; return ; }
    } // end save_edit_area

  /*  Button handlers :  */
  class clear_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { the_text.setText(""); ;
    last_line="" ; }
    } // end class clear_listener

  class on_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { start() ; }
    } // end class on_listener

  class off_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)  { stop() ; }
    } // end class on_listener

  class list_listener implements ActionListener
    { public void actionPerformed(ActionEvent e)
       {   String full_name=list_name+".LST" ;  save_edit_area(full_name) ;  }
    } // end class on_listener

// class lister_wlistener extends WindowAdapter
//   {
//   public void windowClosing(WindowEvent e)   { }
//   public void windowIconified(WindowEvent e)   {  }
//   public void windowDeiconified(WindowEvent e)  {  }
//   }

   } // end class output_frame


/* ----------------------------------------------------------------*/


class ByteBuffer
{ byte[] contents =new byte[512] ;   // buffer for various things as bytes
  int ptr ;                          // index of first unused byte in buffer

  void reset() { ptr=0 ; }

  void putByte(byte b)  { contents[ptr++]=b ;  }
  void putByte(int i)  { contents[ptr++]=(byte) i ;  }

  byte getByte()  { return(contents[ptr++]) ; }

  int getUnsignedByte() { 
    int i=contents[ptr++] ;
    if (i<0) i=i+256 ;  
    return(i) ;  
    }

  void putInt(int i) { 
    putByte( (byte)(i>>0) ) ;  
    putByte( (byte)(i>>8) ) ;
    putByte( (byte)(i>>16) ) ;
    putByte( (byte)(i>>24) ) ;
    }

 int getInt() { 
    int v=getUnsignedByte()      ;
    v=v+(getUnsignedByte() <<8)  ;
    v=v+(getUnsignedByte() <<16) ;
    v=v+(getUnsignedByte() <<24) ;
    return(v) ;
  }

  void putLong(long i) { 
    putByte( (byte)(i>>0) ) ;  
    putByte( (byte)(i>>8) ) ;
    putByte( (byte)(i>>16) ) ;
    putByte( (byte)(i>>24) ) ;
    putByte( (byte)(i>>32) ) ;  
    putByte( (byte)(i>>40) ) ;
    putByte( (byte)(i>>48) ) ;
    putByte( (byte)(i>>56) ) ;
    }
 

  long getLong() { 
    long v=getUnsignedByte()      ;   
    v=v+( ( (long)getUnsignedByte() ) <<8)  ;
    v=v+( ( (long)getUnsignedByte() ) <<16)  ;
    v=v+( ( (long)getUnsignedByte() ) <<24)  ;
    v=v+( ( (long)getUnsignedByte() ) <<32)  ;
    v=v+( ( (long)getUnsignedByte() ) <<40)  ;
    v=v+( ( (long)getUnsignedByte() ) <<48)  ;
    v=v+( ( (long)getUnsignedByte() ) <<56)  ;
 
    return(v) ;
  }


  void putString(String S) { 
    int k ; int v ;
    putByte(S.length()) ;
    for (k=0 ; k<S.length() ; k++ ){
      putByte((byte) S.charAt(k)) ;
      }
    }

  String getString() {
    int l=getUnsignedByte() ;
    String s="" ;
    for (int i=0 ; i<l ; i++) {
      s=s+(char)getByte() ; 
      }
    return(s) ;
    }

} /* end class byte_buffer */

/* ----------------------------------------------------------------*/

class Packet{
  DatagramSocket socket ;
  DatagramPacket Datagramm ;
  int id ;
  int delays ;
  }

/* ----------------------------------------------------------------*/

class NetworkSimulator extends Thread { 
  final int tick_time=100 ; // ms
  int local_time=0 ;
  OutputFrame output=new OutputFrame("NET-SIM") ;;
  Random Random_loss=new Random(123) ;
  Random Random_delay=new Random(456) ;
  double p_loss=0 ;    // standard setting: no loss
  double p_send=1.0 ;  // standard setting: direct delivery

synchronized void delay(){ 
  try { wait(tick_time) ; local_time++ ; }
  catch(Exception e) { System.out.println("Exception in my_timer e="+e);  System.exit(0); } ;
  }

public void run() { 
  output.listln("NET Start..");
  while(true){
    delay() ; 
    consume() ; 
    }
  }

Vector packetList=new Vector() ;

synchronized public void consume(){ 
  Packet pp ;
  int i=0 ;
  synchronized(packetList) {
    while (i< packetList.size()) { 
      if ( Random_delay.nextFloat()<p_send) {
        pp=(Packet) packetList.remove(i) ;
        packetList.trimToSize();
        try{ pp.socket.send(pp.Datagramm); }
        catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
        output.listln("OUT("+pp.id+") after "+pp.delays+" ticks") ;
        }
       else {
        pp=(Packet) packetList.get(i) ;
        pp.delays++ ; 
        i++ ; 
        }
      } // end while
    } // end synchronized
  }

synchronized public void packetSend(Packet p)  {
  // output.list("SEND "+p.id+" ADR="+p.Datagramm.getAddress()+" port "+p.Datagramm.getPort()) ;
  float r=Random_loss.nextFloat() ;
  if ( r > p_loss) {
    if (p_send>=1.0) {
      try{ p.socket.send(p.Datagramm); }
      catch(Exception ex) { System.out.print("caught "+ex) ; System.exit(0) ; }
      output.listln("DIRECT("+p.id+")");
      }
     else {
      p.delays=0 ;
      synchronized( packetList) {  packetList.add(p) ; } ;
      output.listln("IN("+p.id+")");
      }
    }
   else {
    output.listln("LOST("+p.id+")");
    }
  } // end method
} // end class


/* ----------------------------------------------------------------*/

class VsComm
{ DatagramSocket VS_socket ;
  DatagramPacket inDatagram ;
  byte[] OutBuffer ;
  DatagramPacket outDatagram;
  byte[] inBuffer =new byte[512] ;
  Packet out_packet ;
  NetworkSimulator network ;
  int message_id ;

   VsComm(int port_no , NetworkSimulator net)
    {
    network=net ;
    System.out.println("Try socket get "+port_no);
    try{ VS_socket = new DatagramSocket(port_no); }
    catch(Exception ex) { System.out.print("vs_comm : caught : "+ex) ; System.exit(0) ; }
    System.out.println("VS_COMM: got a VS socket :") ;
    inDatagram=new DatagramPacket(inBuffer,inBuffer.length) ;
   }

  void send(int destinationPort , InetAddress destinationAddress) { 
    outDatagram =
         new DatagramPacket(OutBuffer,OutBuffer.length, destinationAddress,destinationPort);
    out_packet=new Packet() ;
    out_packet.Datagramm=outDatagram ;
    out_packet.socket=VS_socket ;
    out_packet.id=message_id ;
    network.packetSend(out_packet);
    } /* end send */


  int receive(int timeout) {
    try{
      VS_socket.setSoTimeout(timeout);
      inDatagram=new DatagramPacket(inBuffer,inBuffer.length) ;
      VS_socket.receive(inDatagram);
      } catch(Exception ex) { System.out.println("RX timeout ") ; return(1) ; }
//    System.out.println("got VS packet from: "+inDatagram.getAddress()+
//    " port "+inDatagram.getPort()+"  Length="+inDatagram.getLength() );
    inBuffer=inDatagram.getData() ;
    return(0) ;
    } // end receive
 } // end class vs_comm


 /* ----------------------------------------------------------------*/

class Receiver extends Thread { 
  NetworkSimulator network ;
  ByteBuffer rxBuffer=new ByteBuffer() ;
  int id ;
  VsComm RX_comm ;

  public void run() { 
    System.out.println("RX_tst.run executed");
    OutputFrame out=new OutputFrame("RX_TEST") ;
    out.listln("Server Start..");
    RX_comm=new VsComm(4712, network) ;
    out.listln("RX Socket Start..");

    while(true) { 
      out.list("wait:");
      RX_comm.receive(0) ;
      rxBuffer.contents=RX_comm.inDatagram.getData() ;
      rxBuffer.reset() ;
      id=rxBuffer.getInt() ;
      out.listln("GOT message "+id ) ;
      } // end while
    } // end method
  } // end class

/* ----------------------------------------------------------------*/


class NetworkTester { 
  NetworkSimulator network ;
  VsComm tester_comm ;
  InetAddress destination ;
  ByteBuffer testBuffer ;
  OutputFrame output ;

synchronized void waitSecond() {  
  try  { wait(1000) ; }
  catch(Exception e) { System.out.println("Exception in my_timer e="+e);  System.exit(0); } ;
  } // end method


  void action() {
    output=new OutputFrame("RX_TX_test") ;  // get a listing window for sender
    network=new NetworkSimulator() ;        // get a network for sending and receiving
    network.start();                         // start the network simulation
    network.p_send=0.05 ;                    // with these parameters 
    network.p_loss=0.5 ;

    Receiver receiver=new Receiver() ;         // get a receiver 
    receiver.network=network ;             // connect it to the network
    receiver.start() ;                     // and start it
  
    tester_comm=new VsComm(4789, network) ;  // get sender

    try { destination=InetAddress.getByName("127.0.0.1")  ; }
    catch(Exception e){ System.out.println("E="+e) ; }

    int messageNumber=0 ;
    
    while(true) {
// built up new message
      messageNumber++ ;
      testBuffer=new ByteBuffer() ;

      testBuffer.reset() ;
      testBuffer.putInt(messageNumber) ;
      long time=System.currentTimeMillis() ;

// send message to network
      tester_comm.OutBuffer=testBuffer.contents ;
      tester_comm.message_id=messageNumber+100000 ;
      tester_comm.send(4712,destination );
      output.listln("sent at time "+time+" message no: "+messageNumber ) ;
      waitSecond() ;
      } // end while
    }
  // end method
  } // end class


/* ----------------------------------------------------------------*/

public class bavs_v3p1a {

  public static void main(String[] args) {
    System.out.println("Hello Verteilte Systeme") ;
    NetworkTester netTest=new NetworkTester() ;
    netTest.action() ;
    }
  }


