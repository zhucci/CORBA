import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;

class TubeCallbackServant extends TubeCallbackPOA {
	String myNum;//Cellphone number

	TubeCallbackServant (String num) {
		//this.myNum = num;
		myNum = num;
	};

	// Treating a received message
	public int sendSMS(String fromNum, String message) {
		System.out.println(myNum+": A message received from ( "+fromNum+" ). Message body : "+message);
		return (0);
	};
 
	public String getNum() {
		return (myNum);
	};
};

// Class for a handle thread
class ORBThread extends Thread {
	ORB myOrb;

	ORBThread(ORB orb) {
		myOrb = orb;
	};

	// Thread run
	public void run() {
		myOrb.run();
	};
};
 
// Class for a cellphone imitation
public class Tube {

	public static void main(String args[]) {
		try {
			String myNum = args[0];

			ORB orb = ORB.init(args, null);

			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			TubeCallbackServant listener  = new TubeCallbackServant(myNum);
			rootPOA.activate_object(listener);

			TubeCallback ref = TubeCallbackHelper.narrow(rootPOA.servant_to_reference(listener));

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);

			NameComponent nc = new NameComponent("BaseStation", "");
			NameComponent path[] = {nc};
			Station stationRef = StationHelper.narrow(ncRef.resolve(path));

			// The cellphone registration in Base Station 
			stationRef.register(ref, myNum);
			System.out.println("The cellphone ( "+myNum+" ) is currently registred in Base Station.");

			ORBThread orbThr = new ORBThread(orb);
			orbThr.start();

			// Infinite loop to read messages from the cellphone and send it to the Base Station
			BufferedReader inpt  = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while (true) {
				
				msg = inpt.readLine();
				
				int delimitterIndex;
				if( (delimitterIndex = msg.indexOf(":")) <= 0 ){
					System.out.println("The cellphone ( "+myNum+" ): Error - there is no address in your message!");
					continue;
				}
				stationRef.sendSMS(myNum, msg.substring(0, delimitterIndex), msg.substring(delimitterIndex+1));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		};
	};
};
