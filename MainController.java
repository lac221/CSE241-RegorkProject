import java.io.*;
import java.sql.*;
import java.util.Scanner;

class MainController {
	public static void main (String[] args){
		try{
			Scanner myScanner = new Scanner(System.in);
			String user = "";
			String password = "";
		
			boolean cont = true;

			while(cont){

				System.out.print("Enter Oracle username: ");
				user = myScanner.next();
				System.out.print("enter Oracle password for " + user + ": ");
				password = myScanner.next();
			
				try(Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user, password);){
			
					System.out.println("Welcome to Regork's online system");
					System.out.println("Please enter the corresponding letter to your position");
			
					System.out.println("[M] - Manager");
					System.out.println("[S] - Supplier"); //supplier
					System.out.println("[R] - Enter a recall"); //recalls
					System.out.println("[Q] - Quit");
					//System.out.println("[E] - Customer Service: Regork General Employee");
				
					userChoice(myScanner, con);	
			
					cont = false;
				} catch (Exception e) {
					System.out.println("login error. Try again.");
				} catch (Throwable e) {
					System.out.println("hey stop that");
					System.exit(0);
				}
			}
		} catch (Throwable e){
			System.out.println("please dont");
			System.exit(0);
		}
	}
	
	public static void userChoice(Scanner myScanner, Connection con){
		String choice = "X";
		boolean cont = true;
		
		while(cont){
			choice = myScanner.next();
			choice = choice.toUpperCase();
			switch(choice){
				case "M":
					System.out.println("Welcome Regork Manager");
					Manager.manager(con, myScanner);
					choice = "";
					break;
				case "S":
					System.out.println("Welcome Regork Supplier");
					Supplier.supplier(con, myScanner);
					break;
				case "R":
					System.out.println("Welcome to the recall entry server");
					Recall.recall(con, myScanner);
					break;
				case "Q":
					try{
						con.close();
						System.exit(0);
					}catch (Exception e){
					}
				default:
					System.out.println("Looks like that option doesn't exist! Please enter one of the following options:");
				}
				System.out.println("[M] - Manager");
				System.out.println("[S] - Supplier"); //supplier
				System.out.println("[R] - Enter a recall"); //recalls
				System.out.println("[Q] - Quit");
		}
		
	}
	
}