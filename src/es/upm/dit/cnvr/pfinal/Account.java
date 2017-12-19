package es.upm.dit.cnvr.pfinal;

import java.io.Serializable;

/**
 * Clase que implementa el tipo de objeto que se almacena en la base de datos respecto a la informacion
 * de las cuentas del banco. En definitiva esta clase es un POJO con sus metodos accesores y modificadores
 * asi como un metodo equals y toString.
 * @author DanielRevilla
 * @version 22/10/2017
 */
public class Account implements Serializable{

	private static final long serialVersionUID = 5101726199201031707L;
	private long id;
	private String IBAN;
	private long clientID;
	private double balance;
	private static long next_id = 0;
	
	// Constructor que autogenera un valor de ID. Especialmente para las nuevas cuentas.
	public Account(String IBAN, long clientID, double balance) {
		this.id = ++Account.next_id;
		this.clientID = clientID;
		this.balance = balance;
		this.IBAN = IBAN;
	}
	
	// Constructor que permite elegir el valor de ID en caso de querer modificar una cuenta.
	public Account(long id, String IBAN, long clientID, double balance) {
		this.id = id;
		this.IBAN = IBAN;
		this.balance = balance;
		this.clientID = clientID;
	}
	
	
	 /* **********************************************************************
	 ************* Metodos getters, setters, toString y equals. **************
	 *************************************************************************/
	
	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String getIBAN() {
		return IBAN;
	}

	public void setIBAN(String IBAN) {
		this.IBAN = IBAN;
	}

	public long getClientID() {
		return clientID;
	}

	public void setClientID(long clientID) {
		this.clientID = clientID;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public static long getNext_id() {
		return next_id;
	}

	public static void setNext_id(long next_id) {
		Account.next_id = next_id;
	}
	
	@Override
	public String toString() {
		return "Account [id=" + id + ", IBAN=" + IBAN + ", clientID=" + clientID + ", balance=" + balance + "]";
	}

	public boolean equals(Account account) {
		return (account.getBalance() == balance) && (account.getIBAN().equals(IBAN)) && 
				(account.getClientID()==clientID);
	}

}
