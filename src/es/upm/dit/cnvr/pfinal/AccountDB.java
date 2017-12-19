package es.upm.dit.cnvr.pfinal;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;
import java.io.File;

@SuppressWarnings("unchecked")
public class AccountDB implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private java.util.HashMap <Long, Account> accountDB; 
	
	
	// Constructor de un clientDB a partir de otra clientDB existente.
	public AccountDB (AccountDB accountDB) {
		this.accountDB = accountDB.getAccountDB();
	}
	
	// Constructor de un clientDB vacio.
	public AccountDB() {
		accountDB = new java.util.HashMap <Long, Account>();
	}

	// Metodo para devolver el clientDB.
	public java.util.HashMap <Long, Account> getAccountDB() {
		return this.accountDB;
	}
	
	/* **********************************************************************
	 ************** Metodos CRUD, toString y createAccountDB. ***************
	 ************************************************************************/
	
	public boolean createAccount(Account account) {		
		if (accountDB.containsKey(account.getID())) {
			return false;
		} else {
			accountDB.put(account.getID(), account);
			return true;
		}		
	}

	public Account readAccount(long id) {
		if (accountDB.containsKey(id)) {
			return accountDB.get(id);
		} else {
			return null;
		}		
	}
	
	public List<Account> readAccountsOfClient(long id) {
		List<Account> accounts = new ArrayList<Account>();
		for (java.util.HashMap.Entry <Long, Account>  entry : accountDB.entrySet()) {
			if(entry.getValue().getClientID() == id) {
				accounts.add(entry.getValue());
			}
	    }
		return accounts;
	}

	public boolean updateAccount(long id, double balance) {
		if (accountDB.containsKey(id)) {
			Account account = accountDB.get(id);
			account.setBalance(balance);
			accountDB.put(account.getID(), account);
			return true;
		} else {
			return false;
		}	
	}

	public boolean deleteAccount(long id) {
		if (accountDB.containsKey(id)) {
			accountDB.remove(id);
			return true;
		} else {
			return false;
		}	
	}
	
	public boolean deleteAccountsOfClient(long id) {
		for (java.util.HashMap.Entry <Long, Account>  entry : accountDB.entrySet()) {
			if(entry.getValue().getClientID() == id) {
				accountDB.remove(entry.getKey());
			}
	    }	
		return true;
	}
	
	public boolean createAccountDB(AccountDB accountDB) {
		System.out.println("Creando una DHT de Account nueva");
		this.accountDB = accountDB.getAccountDB();
		return true;
	}
	
	public String toString() {
		String aux = new String();
		for (java.util.HashMap.Entry <Long, Account>  entry : accountDB.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
	}
	
	public String dumpDB() {
		String fileName = "/tmp/CNVR/dbs/accountDB";
		File dumpFile = new File(fileName);
		
		if(dumpFile.exists()) {
			dumpFile.delete();
		}
		
	    FileOutputStream fos;
		try {
			fos = new FileOutputStream(dumpFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(accountDB);
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
			
			File fileAccountDB = new File(path);
			
			if(fileAccountDB.exists()) {
				
				FileInputStream fis = new FileInputStream(fileAccountDB);
				
				ObjectInputStream ois = new ObjectInputStream(fis);
				this.accountDB = (java.util.HashMap<Long, Account>) ois.readObject();
				
				Set<Entry<Long, Account>> mapValues = accountDB.entrySet();
				int maplength = mapValues.size();
				
				Entry<Long, Account>[] arrayAccountDB = new Entry[maplength];
				mapValues.toArray(arrayAccountDB);

				System.out.print("Last Key:" + arrayAccountDB[maplength - 1].getKey());
				System.out.println(" Last Value:" + arrayAccountDB[maplength - 1].getValue().toString());
				
				Account.setNext_id(arrayAccountDB[maplength - 1].getKey());
				
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