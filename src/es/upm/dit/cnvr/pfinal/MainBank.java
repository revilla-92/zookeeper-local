package es.upm.dit.cnvr.pfinal;

import java.util.Scanner;

public class MainBank {
	
	public MainBank() {
		
	}
	
	public boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}

	public static void main(String[] args) {
		
		Scanner sc      = new Scanner(System.in);
		boolean correct = false;
		boolean exit    = false;
		boolean debug   = false;
		long idCliente  = 0;
		long idCuenta   = 0;
		int menuKey	    = 0;
		double balance  = 0;
		String name	    = "";
		String dni	    = "";
		int size = 3;
		
		// Creamos el banco principal
		MainBank mainBank = new MainBank();
		
		// Si le pasamos parametros al ejecutarlo los recogemos y procesamos.
		if(args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				if (args[i].equals("--debug")) {
					debug = true;
				}
				String [] arg = args[i].split("=");
				if(arg[0].equals("--size") && arg.length > 1 && mainBank.isInteger(arg[1])) {
					size = Integer.parseInt(arg[1]);
				}
			}
		}
		
		// Trazas informativas.
		System.out.println("Hemos activado el modo debug: " + debug);
		System.out.println("El numero de servidores que vamos a tener siempre fijos es: " + size);

		// Creamos un banco.
		Bank bank = new Bank(debug, size);
		
		/*
		 * Mientras no indiquemos lo contrario (pulsando la tecla 0) se ejecutara en bucle una interfaz
		 * con un menu para realizar acciones (siempre numeros enteros). Una vez elegida una accion del 
		 * menu principal se realizan acciones que mostraran los resultados por pantalla.
		 */
		while (!exit) {
			try {
				correct = false;
				menuKey = 0;
				while (!correct) {
					System. out .println(">>> Seleccione entre las siguientes opciones: \n"
							+ "1) Crear Cliente: Crea un nuevo cliente en la BBDD.\n"
							+ "2) Crear Cuenta: Crea una nueva cuenta en la BBDD.\n"
							+ "3) Buscar Cliente (ID): Busca un cliente por su ID en la BBDD.\n"
							+ "4) Buscar Clientes (todos): Busca todos los clientes en la BBDD.\n"
							+ "5) Buscar Cuenta(s) por cliente: Busca la(s) cuenta(s) de un determinado cliente.\n"
							+ "6) Actualizar Cliente: Actualiza la informacion de un cliente.\n"
							+ "7) Actualizar Cuenta: Actualiza el balance de una cuenta.\n"
							+ "8) Eliminar Cliente: Elimina la informacion de un cliente y sus cuentas asociadas.\n"
							+ "9) Eliminar Cuenta: Elimina la información de una cuenta.\n"
							+ "0) Exit.\n");				
					if (sc.hasNextInt()) {
						menuKey = sc.nextInt();
						if(menuKey >= 0 && menuKey <= 9) {
							correct = true;
						}
					} else {
						sc.next();
						System.out.println("Por favor introduzca un opcion valida.");
					}
				}
				
				// En funcion de la tecla del menu pulsado realizamos las correspondientes acciones:
				switch (menuKey) {
				
					case 1: 
						System.out.println();
						System.out.println(" Ha seleccionado: 1) Crear Cliente.");
						System.out.println(">>> Introduzca el nombre del cliente (String): ");
						name = sc.next();
						System.out.println(">>> Introduzca el DNI del cliente: ");
						dni = sc.next();
						bank.createClient(name, dni);
						break;
					
					case 2:
						System.out.println();
						System.out.println("Ha seleccionado: 2) Crear Cuenta.");
						IBAN nuevoIBAN = new IBAN();
						System.out.println(">>> Introduzca el identificador del cliente: ");
						if(sc.hasNextLong()) {
							idCliente = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						System.out.println(">>> Que balance tiene la cuenta: ");
						if(sc.hasNextInt()) {
							balance = sc.nextDouble();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						bank.createAccount(nuevoIBAN.toString(), idCliente, balance);
						break;
						
					case 3:
						System.out.println();
						System.out.println("Ha seleccionado: 3) Buscar Cliente (ID).");
						System.out.println(">>> Introduzca el identificador del cliente: ");
						if(sc.hasNextLong()) {
							idCliente = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						System.out.println("\n" + bank.readClient(idCliente).toString() + "\n");
						break;
						
					case 4:
						System.out.println();
						System.out.println("Ha seleccionado: 4) Buscar Clientes (todos).");
						System.out.println("\n" + bank.getClientDB() + "\n");
						break;
						
					case 5:
						System.out.println();
						System.out.println("Ha seleccionado: 5) Buscar Cuenta(s) por cliente.");
						System.out.println(">>> Introduzca el identificador del cliente: ");
						if(sc.hasNextLong()) {
							idCliente = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						System.out.println("\n" + bank.readAccountsOfClient(idCliente) + "\n");
						break;
						
					case 6:
						System.out.println();
						System.out.println("Ha seleccionado: 6) Actualizar Cliente.");
						System.out.println(">>> Introduzca el identificador del cliente: ");
						if(sc.hasNextLong()) {
							idCliente = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						System.out.println(">>> Introduzca el nombre del cliente (String): ");
						name = sc.next();
						System.out.println(">>> Introduzaca el DNI del cliente: ");
						dni = sc.next();
						bank.updateClient(idCliente, name, dni);
						break;
						
					case 7:
						System.out.println();
						System.out.println("Ha seleccionado: 7) Actualizar Cuenta.");
						System.out.println(">>> Introduzca la ID de la cuenta a modificar: ");
						if(sc.hasNextLong()) {
							idCuenta = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						System.out.println(">>> Introduzca el nuevo balance: ");
						if(sc.hasNextDouble()) {
							balance = sc.nextDouble();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						bank.updateAccount(idCuenta, balance);
						break;
						
					case 8:
						System.out.println();
						System.out.println("Ha seleccionado: 8) Eliminar Cliente.");
						System.out.println(">>> Introduzca la ID del cliente a eliminar: ");
						if(sc.hasNextLong()) {
							idCliente = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						bank.deleteClient(idCliente);
						break;
						
					case 9:
						System.out.println();
						System.out.println("Ha seleccionado: 9) Eliminar Cuenta.");
						System.out.println(">>> Introduzca la ID de la cuenta a eliminar: ");
						if(sc.hasNextLong()) {
							idCuenta = sc.nextLong();
						}else {
							sc.next();
							System.out.println("Por favor introduzca un numero.");
						}
						bank.deleteAccount(idCuenta);
						break;
						
					case 0:
						exit = true;	
						sc.close();
						return;
						
					default:
						break;	
						
				}
				
			} catch (Exception e) {
				System.out.println("Exception at Main. Error read data");
			}
		}
		sc.close();
	}

}