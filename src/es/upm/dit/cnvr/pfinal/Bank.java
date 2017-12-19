package es.upm.dit.cnvr.pfinal;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher;
import java.util.Random;
import java.util.List;

public class Bank implements Watcher {
	
	 /* *********************************************************************
	 ***************************** ATRIBUTOS ********************************
	 ************************************************************************/

	// Timeout de la sesion de Zookeeper.
	private static final int SESSION_TIMEOUT = 5000;

	// Directorios y nombres donde creamos los nodos.
	private static String rootMembers = "/members";
	private static String aMember = "/member-";
	private String myId;

	// Directorios de las ordenes.
	private static String bankMove = "/bankMoves";
	private static String bankMoveChild = "/move_";
	
	// Directorios para la BD
	private static String rootDB = "/dbs";
	private static String db = "/db_";

	// Path del lider.
	String leaderPath = new String();
	String currentPath = new String();

	// Direcciones del conjunto de Zookeeper donde poder conectarnos.
	String[] hosts = { "127.0.0.1:2181", "127.0.0.1:2182", "127.0.0.1:2183" };

	// Declaramos una instancia Zookeeper para conectarnos al conjunto ZooKeeper.
	private ZooKeeper zk;

	// Numero y path de servidores conectados.
	private List<String> members;

	// Bases de datos:
	private String pathAccountDB;
	private AccountDB accountDB;
	private String pathClientDB;
	private ClientDB clientDB;

	// Variable para activar/desactivar el modo debug.
	static boolean debug = false;
	
	// Variable para saber cuantos procesos como minimo deben de estar activos
	boolean sizeReached = false;
	int size = 3;
	
	// Comandos para relanzar un proceso en caso de caiga un proceso.
	String[] orderedCommands = new String[] {
		// 1. Export CLASSPATH
		"export CLASSPATH=$CLASSPATH:/tmp/CNVR/pfinal.jar:$CLASSPATH:/tmp/CNVR/lib/*",
		// 2. Launch server
		"java -Djava.net.preferIPv4Stack=true es.upm.dit.cnvr.pfinal.MainBank --size " + this.size
	};

	 /* *********************************************************************
	 *************************** CONFIGURACION ******************************
	 ************************************************************************/
	
	public Bank(boolean debug, int size) {

		// Argumento --debug para habilitar las trazas mientras debugeamos.
		Bank.debug = debug;
		this.size = size;
		
		if(debug) {
			orderedCommands[1] += " --debug";
		}

		// Inicializamos las bases de datos de clientes y cuentas.
		accountDB = new AccountDB();
		clientDB = new ClientDB();

		// Seleccionamos un servidor aleatorio del conjunto Zookeeper.
		Random rand = new Random();
		int i = rand.nextInt(hosts.length);

		// Creamos una sesion y esperamos hasta que se cree. Una vez creada el watcher es notificado.
		try {
			if (zk == null) {

				// Creamos la instancia Zookeeper.
				zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, cWatcher);

			}
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}

		// Creamos los nodos
		if (zk != null) {

			// Creamos la carpeta members e incluimos un proceso.
			try {

				// Comprobamos que los directorios /members, /bankMoves y /barrier estan creados y sino se crean.
				Stat s = zk.exists(rootMembers, false);
				if (s == null) {
					zk.create(rootMembers, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				Stat d = zk.exists(bankMove, false);
				if (d == null) {
					zk.create(bankMove, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				Stat e = zk.exists(rootDB, false);
				if (e == null) {
					zk.create(rootDB, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				
				// Creamos un nodo dentro de /members y conseguimos su ID.
				currentPath = zk.create(rootMembers + aMember, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);

				// Imprimimos el nodo creado.
				myId = currentPath.replace(rootMembers + "/", "");
				System.out.println("Se ha creado un znode con ID: " + myId);

				// Actualizamos el watcher de las operaciones y DBs.
				members = zk.getChildren(rootMembers, watcherMember, s);
				zk.getChildren(bankMove, watcherBankMove, d);
				
				// Imprimimos la lista de los nodos creados.
				List<String> resultadoEleccion = eleccionLider(members);
				printListMembers(resultadoEleccion);

				// Comprobamos si hay el numero de servidores (size) requerido.
				if(members.size() == 3) {
					sizeReached = true;
				}
				
				// Recogemos los nodos creados en la DB por si tenemos que cargas las DBs.
				List<String> dbs = zk.getChildren(rootDB, false);
				java.util.Collections.sort(dbs);
				
				// Si no soy el proceso lider y hay bases de datos creadas, las cargo.
				if(!myId.equals(leaderPath) && !dbs.isEmpty()) {
					
					// Recojo el ultimo dump realizado.
					String pathDB = dbs.get(dbs.size() -1);
					
					Logger.debug("El path del nodo que contiene las DBs es: " + rootDB + "/" + pathDB);
					
					Stat f = zk.exists(rootDB + "/" + pathDB, false);
					byte[] data = zk.getData(rootDB + "/" + pathDB, false, f);
					String DBs = SerializationUtils.deserialize(data);
					
					String[] pathsDBs = DBs.split(":");
					Logger.debug("El path de accountDB es: " + pathsDBs[0] + 
							" y el path de clientDB es: " + pathsDBs[1]);
					
					if(pathsDBs[0] != null || pathsDBs[0].length() > 0 || pathsDBs[0] != "null") {
						Logger.debug("Copiando backup de accountDB en " + pathsDBs[0]);
						accountDB.loadDB(pathsDBs[0]);
					}
					if(pathsDBs[1] != null || pathsDBs[1].length() > 0 || pathsDBs[1] != "null") {
						Logger.debug("Copiando backup de clientDB en " + pathsDBs[1]);
						clientDB.loadDB(pathsDBs[1]);
					}
				}

			} catch (KeeperException e) {
				System.out.println("La sesion con Zookeeper falla. Cerrando.");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}
		}
	}

	
	 /* *********************************************************************
	 ************************* ELECCION DEL LIDER ***************************
	 ************************************************************************/
	
	public List<String> eleccionLider(List<String> list) {
		leaderPath = list.get(0);
		for (String s : list) {
			if (leaderPath.compareTo(s) > 0)
				leaderPath = s;
		}
		Logger.debug("El nodo/proceso lider es: " + rootMembers + "/" + leaderPath);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(leaderPath)) {
				list.set(i, list.get(i) + " (lider)");
			} else {
				list.set(i, list.get(i) + " (follower)");
			}
		}
		return list;
	}

	private void printListMembers(List<String> list) {
		System.out.println("Miembros restantes: " + list.size());
		System.out.print("Los miembros actuales son: ");
		for (String s : list) {
			System.out.print(s + ", ");
		}
		System.out.println();
	}
	
	
	 /* *********************************************************************
	 ***************************** WATCHERS *********************************
	 ************************************************************************/

	public void process(WatchedEvent event) {
		try {
			System.out.println("Entro en process");
			List<String> list = zk.getChildren(rootMembers, watcherMember);
			printListMembers(list);
		} catch (Exception e) {
			System.out.println("Error in project");
		}
	}
	
	// Watcher para crear la sesion de Zookeeper.
	private Watcher cWatcher = new Watcher() {
		public void process(WatchedEvent e) {
			System.out.println("Sesion creada");
			System.out.println(e.toString());
			notify();
		}
	};
	
	// Watcher para vigilar los miembros que se crean. 
	private Watcher watcherMember = new Watcher() {
		public void process(WatchedEvent event) {
			System.out.println("\n\n------------------Watcher Member------------------\n");
			try {
				System.out.println("Se han producido cambios en el nodo " + rootMembers);
				List<String> list = zk.getChildren(rootMembers, watcherMember);
				
				// Si ya hemos alcanzado por primera los tres miembros activamos el flag sizeReached
				if(list.size() == 3) {
					System.out.println("Se ha alcanzado el numero de servidores deseado");
					sizeReached = true;
				}
				
				// Traza para ver los valores de las variables.
				Logger.debug("El valor de sizeReached es: " + sizeReached + "\n"
						+ "El valor de list.size() es: " + list.size() + "\n" 
						+ "El valor de isLeader es: " + myId.equals(leaderPath));
				
				List<String> resultadoEleccion = eleccionLider(list);
				printListMembers(resultadoEleccion);

				// Una vez activado el flag vigilamos que cuando tengamos menos del numero de servidores 
				// especificados para que el lider levante otro proceso para mantener siempre el mismo numero. 
				if(sizeReached && list.size() < 3 && myId.equals(leaderPath)) {
					
					System.out.println("Se tenian " + size + " maquinas y ahora no, levantando otra.");
					
					String[] c = new String[] {"/bin/bash", "-c", "xterm -hold -e '" + 
							String.join(" && ", orderedCommands) + "' &"};
					
					new ProcessBuilder(c).start();
	
				}
	
			} catch (Exception e) {
				System.out.println("Exception: wacherMember");
			}
		}
	};
	
	// Watcher para ver las operaciones que se hacen.
	private Watcher watcherBankMove = new Watcher() {
		public void process(WatchedEvent event) {
			
			System.out.println("\n\n------------------Watcher BankMove------------------\n");

			try {
			
				Logger.debug("Se han producido cambios en el nodo " + bankMove);
				List<String> list = zk.getChildren(bankMove, watcherBankMove);
				
				Logger.debug("La lista de getChildren, ¿esta vacia? " + list.isEmpty() +". Y procedemos a ordenarla.");
				java.util.Collections.sort(list);

				if(!list.isEmpty()) {
					
					String pathToProcess = list.get(list.size()-1);

					Logger.debug("Vamos a procesar el proceso: " + pathToProcess + ".");
					
					Stat s = zk.exists(bankMove + "/" + pathToProcess, false);
					byte[] data = zk.getData(bankMove + "/" + pathToProcess, false, s);
					BankMove move = SerializationUtils.deserialize(data);
					
					Logger.debug("El movimiento que se va a procesar tiene los siguientes valores: " + move.toString());
					
					switch (move.getOperation()){
					
						case CREATE_CLIENT:
							Logger.debug("El evento " + pathToProcess + " es una operacion de crear client.");
							Client clientToCreate = new Client(move.getName(), move.getDNI());
							Logger.debug("El cliente que se va a añadir a la BBDD es: " + clientToCreate.toString());
							clientDB.createClient(clientToCreate);
							if(myId.equals(leaderPath)) {
								pathClientDB = clientDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						case UPDATE_CLIENT:
							Logger.debug("El evento " + pathToProcess + " es una operacion de update client.");
							clientDB.updateClient(move.getClientID(), move.getName(), move.getDNI());
							if(myId.equals(leaderPath)) {
								pathClientDB = clientDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						case DELETE_CLIENT:
							Logger.debug("El evento " + pathToProcess + " es una operacion de delete client.");
							accountDB.deleteAccountsOfClient(move.getClientID());
							clientDB.deleteClient(move.getClientID());
							if(myId.equals(leaderPath)) {
								pathClientDB = clientDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						case CREATE_ACCOUNT:
							Logger.debug("El evento " + pathToProcess + " es una operacion de crear account.");
							Account account = new Account(move.getIBAN(), move.getClientID(), move.getBalance());
							Logger.debug("La cuenta que se va a crear es: " + account.toString());
							accountDB.createAccount(account);
							if(myId.equals(leaderPath)) {
								pathAccountDB = accountDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						case UPDATE_ACCOUNT:
							Logger.debug("El evento " + pathToProcess + " es una operacion de update account.");
							accountDB.updateAccount(move.getAccountID(), move.getBalance());
							if(myId.equals(leaderPath)) {
								pathAccountDB = accountDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						case DELETE_ACCOUNT:	
							Logger.debug("El evento " + pathToProcess + " es una operacion de delete account.");
							accountDB.deleteAccount(move.getAccountID());
							if(myId.equals(leaderPath)) {
								pathAccountDB = accountDB.dumpDB();
								byte[] paths = SerializationUtils.serialize(pathAccountDB + ":" + pathClientDB);
								zk.create(rootDB + db, paths, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
							}
							break;
							
						default:
							break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception: watcherBankMove");
			}
		}
	};


	/* **********************************************************************
	 ******************************* METODOS ********************************
	 ************************************************************************/

	public boolean createClient(String name, String DNI) {

		Logger.debug("Entro en createClient.");
		
		// Recogemos la informacion del cliente a crear y creamos un movimiento bancario.
		BankMove move = new BankMove();
		move.setName(name);
		move.setDNI(DNI);
		move.setOperation(OperationEnum.CREATE_CLIENT);

		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);

		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	
	public Client readClient(long ID) {
		return clientDB.readClient(ID);
	}

	public boolean updateClient(long clientID, String name, String DNI) {
		
		Logger.debug("Entro en updateClient.");
		
		// Recogemos la informacion del cliente a crear y creamos un movimiento bancario.
		Client client = new Client(DNI, name, clientID);
		BankMove move = new BankMove();
		move.setClient(client);
		move.setOperation(OperationEnum.UPDATE_CLIENT);

		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);

		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	
	public boolean deleteClient(Long clientID) {
		
		Logger.debug("Entro en deleteClient.");

		// Recogemos la informacion de la ID del cliente a eliminar.
		BankMove move = new BankMove();
		move.setClientID(clientID);
		move.setOperation(OperationEnum.DELETE_CLIENT);
		
		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);
		
		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	public boolean createAccount(String IBAN, long idClient, double balance) {
		
		Logger.debug("Entro en createAccount.");
		
		// Recogemos la informacion del cliente a crear y creamos un movimiento bancario.
		BankMove move = new BankMove();
		move.setClientID(idClient);
		move.setBalance(balance);
		move.setIBAN(IBAN);
		move.setOperation(OperationEnum.CREATE_ACCOUNT);

		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);

		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			
		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	public Account readAccount(long ID) {
		return accountDB.readAccount(ID);
	}

	public String readAccountsOfClient(long clientID) {
		String result = "";
		List<Account> list = accountDB.readAccountsOfClient(clientID);
		for (Account i : list) {
			result += i.toString() + "\n";
		}
		return result;
	}

	public boolean updateAccount(long id, double balance) {
		
		Logger.debug("Entro en updateAccount.");
		
		// Recogemos la informacion del cliente a crear y creamos un movimiento bancario.
		BankMove move = new BankMove();
		move.setAccountID(id);
		move.setBalance(balance);
		move.setOperation(OperationEnum.UPDATE_ACCOUNT);

		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);

		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			
		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	public boolean deleteAccount(Long accountID) {
		
		Logger.debug("Entro en deleteAccount.");

		// Recogemos la informacion de la ID del cliente a eliminar.
		BankMove move = new BankMove();
		move.setAccountID(accountID);
		move.setOperation(OperationEnum.DELETE_ACCOUNT);
		
		// Creamos un proceso en /bankMoves y de esta forma el Watcher en dicho nodo notifica el evento.
		byte[] data = SerializationUtils.serialize(move);
		
		try {
			zk.getChildren(bankMove, watcherBankMove);
			zk.exists(bankMove, watcherBankMove);
			
			Logger.debug("Voy a crear el nodo en /bankMoves.");
			
			zk.create(bankMove + bankMoveChild, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		} catch (KeeperException | InterruptedException e) {
			Logger.debug("Error creando el cliente.");
			e.printStackTrace();
		}
		return true;
	}

	public String getClientDB() {
		return clientDB.toString();
	}

	
	/* **********************************************************************
	 ************************** METODOS AUXILIARES **************************
	 ************************************************************************/

	public void setSize(int size) {
		this.size = size;
	}
}