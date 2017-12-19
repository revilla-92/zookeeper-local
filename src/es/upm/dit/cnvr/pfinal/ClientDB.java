package es.upm.dit.cnvr.pfinal;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.io.File;

@SuppressWarnings("unchecked")
public class ClientDB implements Serializable {

	private static final long serialVersionUID = 1L;
	private java.util.HashMap <Long, Client> clientDB; 

	// Constructor de un clientDB a partir de otra clientDB existente.
	public ClientDB (ClientDB clientDB) {
		this.clientDB = clientDB.getClientDB();
	}
	
	// Constructor de un clientDB vacio.
	public ClientDB() {
		clientDB = new java.util.HashMap <Long, Client>();
	}

	// Metodo para devolver el clientDB.
	public java.util.HashMap <Long, Client> getClientDB() {
		return this.clientDB;
	}
	
	
	 /* **********************************************************************
	 ***************** Metodos CRUD, toString y createBank. ******************
	 *************************************************************************/
	
	public boolean createClient(Client client) {		
		if (clientDB.containsKey(client.getID())) {
			return false;
		} else {
			clientDB.put(client.getID(), client);
			return true;
		}		
	}

	public Client readClient(long id) {
		if (clientDB.containsKey(id)) {
			return clientDB.get(id);
		} else {
			return null;
		}		
	}

	public boolean updateClient (long id, String name, String DNI) {
		if (clientDB.containsKey(id)) {
			Client client = clientDB.get(id);
			client.setDNI(DNI);
			client.setName(name);
			clientDB.put(client.getID(), client);
			return true;
		} else {
			return false;
		}	
	}

	public boolean deleteClient(long id) {
		if (clientDB.containsKey(id)) {
			clientDB.remove(id);
			return true;
		} else {
			return false;
		}	
	}
	
	public boolean createClientDB(ClientDB clientDB) {
		System.out.println("Creando una DHT de clientes nueva");
		this.clientDB = clientDB.getClientDB();
		return true;
	}
	
	public String toString() {
		String aux = new String();
		for (java.util.HashMap.Entry <Long, Client>  entry : clientDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
	
	
	public String dumpDB() {
		String fileName = "/tmp/CNVR/dbs/clientDB";
		File dumpFile = new File(fileName);
		
		if(dumpFile.exists()) {
			dumpFile.delete();
		}
		
	    FileOutputStream fos;
		try {
			fos = new FileOutputStream(dumpFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(clientDB);
	        oos.flush();
	        oos.close();
	        fos.close();
	        return fileName;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public boolean loadDB (String path) {
		try {
			File fileClientDB = new File(path);
			
			if(fileClientDB.exists()) {
				FileInputStream fis = new FileInputStream(fileClientDB);
				
				ObjectInputStream ois = new ObjectInputStream(fis);
				this.clientDB = (java.util.HashMap<Long, Client>) ois.readObject();

				Set<Entry<Long, Client>> mapValues = clientDB.entrySet();
				int maplength = mapValues.size();
				
				Entry<Long, Client>[] arrayClientDB = new Entry[maplength];
				mapValues.toArray(arrayClientDB);

				System.out.print("Last Key:" + arrayClientDB[maplength - 1].getKey());
				System.out.println(" Last Value:" + arrayClientDB[maplength - 1].getValue().toString());
				
				Client.setNext_id(arrayClientDB[maplength - 1].getKey());
				
				ois.close();
				fis.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}