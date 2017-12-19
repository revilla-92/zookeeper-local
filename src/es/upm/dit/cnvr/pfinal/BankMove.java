package es.upm.dit.cnvr.pfinal;

import java.io.Serializable;

public class BankMove implements Serializable {

	private static final long serialVersionUID = 3919309082143348976L;
	private OperationEnum operation;
	private AccountDB accountdb;
	private ClientDB clientdb;
	private double balance;
	private long accountID;
	private long clientID;
	private String name;
	private String IBAN;
	private String DNI;
	
	// Constructor vacio.
	public BankMove() {
		
	}
	
	// Constructor en el que se crea un movimiento bancario para hacer acciones en cuentas y clientes
	public BankMove(double balance, long clientID, long accountID, String name, String IBAN, 
			String DNI, OperationEnum operation) {
		this.operation = operation;
		this.clientID = clientID;
		this.balance = balance;
		this.name = name;
		this.IBAN = IBAN;
		this.DNI = DNI;
	}
	
	// Constructor para pasar la base de datos de clientes y cuentas.
	public BankMove(ClientDB clientdb, AccountDB accountdb, OperationEnum operation) {
		this.operation = operation;
		this.accountdb = accountdb;
		this.clientdb = clientdb;
	}

	
	 /* **********************************************************************
	 ************* Metodos getters, setters, toString y equals. **************
	 *************************************************************************/
	
	public Client getClient() {
		return new Client(this.name, this.DNI, this.clientID);
	}
	
	public void setClient(Client client) {
		this.clientID = client.getID();
		this.name = client.getName();
		this.DNI = client.getDNI();
	}
		
	public Account getAccount() {
		return new Account(this.IBAN, this.clientID, this.balance);
	}
	
	public void setAccount(Account account) {
		this.clientID = account.getClientID();
		this.balance = account.getBalance();
		this.IBAN = account.getIBAN();
	}
	
	public AccountDB getAccountdb() {
		return accountdb;
	}

	public void setAccountdb(AccountDB accountdb) {
		this.accountdb = accountdb;
	}

	public ClientDB getClientdb() {
		return clientdb;
	}

	public void setClientdb(ClientDB clientdb) {
		this.clientdb = clientdb;
	}

	public OperationEnum getOperation() {
		return operation;
	}

	public void setOperation(OperationEnum operation) {
		this.operation = operation;
	}

	public long getClientID() {
		return clientID;
	}

	public void setClientID(long clientID) {
		this.clientID = clientID;
	}
	
	public long getAccountID() {
		return accountID;
	}

	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
	
	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIBAN() {
		return IBAN;
	}

	public void setIBAN(String IBAN) {
		this.IBAN = IBAN;
	}

	public String getDNI() {
		return DNI;
	}

	public void setDNI(String DNI) {
		this.DNI = DNI;
	}
	
	public boolean equals(BankMove bankmove) {
		return (bankmove.getBalance()==balance) && (bankmove.getDNI().equals(DNI)) && 
				(bankmove.getIBAN().equals(IBAN)) && (bankmove.getName().equals(name)) && 
				(bankmove.getOperation().equals(operation));
	}

	@Override
	public String toString() {
		return "BankMove [operation=" + operation + ", accountdb=" + accountdb + ", clientdb=" + clientdb + 
				", balance=" + balance + ", clientID=" + clientID + ", name=" + name + ", IBAN=" + IBAN + 
				", DNI=" + DNI + "]";
	}

}