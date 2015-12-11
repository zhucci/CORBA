import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.Properties;

/*----Added-------*/
import java.util.Properties;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
/*----------------*/

class StationServant extends StationPOA {

	Map<String, TubeCallback> HM = new HashMap<String, TubeCallback>();
	
	//TubeCallback tubeRef;
	//String tubeNum;
	
	// Method of registration a cellphone in base station
	public int register (TubeCallback objRef, String phoneNum) {
		HM.put(phoneNum, objRef);
		System.out.println("Base Station: The cellphone ( "+phoneNum+" ) is registered.");
		return (1);
		};

	// Method of transfer a message "message" from one cellphone to another
	public int sendSMS (String fromNum, String toNum, String message) {
		System.out.println("Base Station: The cellphone ( "+fromNum+" ) is sending a message to the cellphone ( "+toNum+" )...");
		
		if(HM.containsKey(toNum))
		{
			HM.get(toNum).sendSMS(fromNum, message);
		}
		else
		{
			//System.out.println("Tube unregistered. Sending message to Server StationServer2");
			//stationReference.sendSMS(fromNum, toNum, message);
			String mes = "The cellphone with number - " + toNum + " isn\'t registered in current Base Station.";
			try 
			{
				mes = new String(mes.getBytes("UTF-8"), "ISO-8859-1");
			} 
			catch(UnsupportedEncodingException uee) 
			{
				uee.printStackTrace();
			}
			System.out.println("Base Station: Impossible to send a message to "+toNum);
			HM.get(fromNum).sendSMS("BASE STATION", mes);
		}
		return (1);
    };
};

// Base Station Class
public class StationServer {

	public static void main(String args[]) {
		try{
			// Create and Initialize ORB object
			ORB orb = ORB.init(args, null);

			// Get a reference and activation of POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// Create a servant for CORBA object of this Base Station 
			StationServant servant = new StationServant();

			// Get the object reference to this Servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
			Station sref = StationHelper.narrow(ref);
			  
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// Link the object reference with the name
			String name = "BaseStation";
			NameComponent path[] = ncRef.to_name( name );
			ncRef.rebind(path, sref);

			System.out.println("Server is ready.");
			System.out.println("Server is idling ...");
			// Waiting for clients (cellphones) activities
			orb.run();
		} 
		catch (Exception e) {
			System.err.println("The next error is occured : " + e);
			e.printStackTrace(System.out);
		};
	};
};
