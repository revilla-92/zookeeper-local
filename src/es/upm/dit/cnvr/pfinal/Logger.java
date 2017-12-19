package es.upm.dit.cnvr.pfinal;

public class Logger {
		
	public static void debug(String msg) {
		if (Bank.debug) {
			System.out.println("=============================================================");
			System.out.println(msg);
			System.out.println("=============================================================");
		}
	}
}
