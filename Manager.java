import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.Random;

class Manager {
	public static void manager(Connection con, Scanner myScanner){
		
		
		boolean cont = true;
		String toDo = "";

		while(cont){
			try{
				System.out.println("[V] - Add a vendor");
			//	System.out.println("[A] - Analytics");
				System.out.println("[Q] - Quit");
				
				toDo = managerChoice(con, myScanner);
				
				if(toDo.equals("V")){
					addVendor(con);
				}
				if(toDo.equals("Q")){
					System.out.println("Would you like to return to the main menu? (y or n)");
					String response =  "";
					response = myScanner.next();
					response.toLowerCase();
					if(response.equals("y")){
						return;
					}	
					System.exit(0);
				}
				
			
				cont = false;
			} catch (Exception e) {
				System.out.println("Oops");
				System.exit(0);
			}catch (Throwable e){
				System.out.println("Regork doesnt allow that. This manager is fired");
				System.exit(0);
			}
		}
		
		/*
		add a known vendor
		looking for best supplier
			need product_no ###
				look @ historic selling price vs offering price 
				are we overpaying for items from that supplier
			what suppliers make the components? how can we get it the fastest
		avg $ spent per shipment, day, month, year
			$ per supplier
		profit margin on products 
			how much did we buy it for vs how much do we sell it for
		how long is it taking for products to be made and on our shelves from when we order them?
		*/
		

	}
	
	public static String managerChoice(Connection con, Scanner myScanner){
		String choice = "X";
		boolean cont = true;
		
		while(cont){
			choice = myScanner.next();
			choice = choice.toUpperCase();
			switch(choice){
				case "V":
					return choice;
	//			case "A":
	//				return choice;
				case "Q":
					try{
						con.close();
						System.exit(0);
					} catch(Exception e){
						System.out.println("Guess youre stuck here");
					}
				default:
					System.out.println("Looks like that option doesn't exist! Please enter one of the following options:");
				}
				System.out.println("[V] - Add a vendor");
		//		System.out.println("[A] - Analytics");
				System.out.println("[Q] - Quit");
		}
		return "";
	}
	
	public static void addVendor(Connection con){
		try{
			Scanner nameScanner = new Scanner(System.in);
			System.out.println("Please  enter the name of the supplier you would like to add");
			String name;
			String address;
			
				name  = nameScanner.nextLine();

			
			Scanner theScanner = new Scanner(System.in);
			System.out.println("Please  enter the name of the address you would like to add");
			if(theScanner.hasNext()){
				address  = theScanner.nextLine();
			}else{
				System.out.println("Lets try that again");
				return;
			}
			vendorInsert(con, name, address);
			return;
		}catch(Exception e){
			System.out.println("oops. try again");
			return;
		}
	}
	
	public static void vendorInsert(Connection con, String name, String address){
		int added = 0;
		try{
			String q = "INSERT INTO supplier(Supplier_ID,Name,Address) VALUES (?,?,?)";
			PreparedStatement ps = con.prepareStatement(q);
			int supId = generateUniqueSupplier(con);
			ps.setString(1, "" + supId);
		
		//	ps.setDate(2, myDate);
			//System.out.println(name);
			ps.setString(2, name);
		//	System.out.println(address);
			ps.setString(3, address);
			
			added = ps.executeUpdate();
			if(added == 1){
				System.out.println("Welcome to the regork family!");
				System.out.println("We are so excited you supply for us");
				return;
			}
			
		} catch(Exception e){ 
			System.out.println("We  had a problem adding you as a supplier");
			return;
		}
		return;
	}
	
	public static int generateUniqueSupplier(Connection con){
		int m = (int) Math.pow(10, 4);
    	m = m + new Random().nextInt(9 * m);
    	
    	if(Supplier.checkUnique(con, m, "supplier", "supplier_Id")){
    		return m;	
    	}
    	else{
    		m = generateUniqueSupplier(con);
    		return m;
    	}
	}
}