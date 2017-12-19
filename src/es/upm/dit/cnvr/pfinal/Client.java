package es.upm.dit.cnvr.pfinal;

import java.io.Serializable;

/**
 * Clase que implementa el tipo de objeto que se almacena en la base de datos respecto a la informacion
 * de los clientes del banco. En definitiva esta clase es un POJO con sus metodos accesores y modificadores
 * asi como un metodo equals y toString.
 * @author DanielRevilla
 * @version 22/10/2017
 */
public class Client implements Serializable{

	private static final long serialVersionUID = 1372459751788380367L;
	
	// Atributos
	private static long next_id = 0;
	private String name;
	private String DNI;
	private long id;
	
	// Constructor que autogenera un valor de ID
	public Client(String name, String DNI) {
		this.id = ++Client.next_id;
		this.name = name;
		this.DNI = DNI;	
	}
	
	// Constructor que permite elegir el valor de ID en caso de querer modificar un cliente.
	public Client(String name, String DNI, long id) {
		this.name = name;
		this.DNI = DNI;
		this.id = id;
	}
	
	
	 /* **********************************************************************
	 ************* Metodos getters, setters, toString y equals. **************
	 *************************************************************************/
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDNI() {
		return DNI;
	}
	
	public void setDNI(String DNI) {
		this.DNI = DNI;
	}
	
	public long getID() {
		return id;
	}
	
	public void setID(long id) {
		this.id = id;
	}
	
	public static long getNext_id() {
		return next_id;
	}

	public static void setNext_id(long next_id) {
		Client.next_id = next_id;
	}

	public String toString() {
		return "Client [Client ID= "+ id + ", name=" + name + ", DNI=" + DNI + "]";
	}

	public boolean equals(Client client) {
		return client.getName().equals(this.name) && client.getDNI().equals(this.DNI) && (client.getID() == this.id);
	}

}