import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.text.SimpleDateFormat;


class Supplier{
		
	/*
	receiving an order
		check that you can make all products in the order 
			if you cant
				do you have them on hand
					if not order them from someone else
		once you have all ingredients 
			manufacture the product (insert into specific product)
			ship them to whoever ordered them (insert into shipment)
	adding a product that you offer
		see if any other suppliers already can make that product
			if yes add yourself as a known vendor (insert into can_make)
			if no insert that product into generic product then add yourself as a known vendor
	placing an order
		check inventory levels 
			something that compares # received vs # sent
			specific product product id received but not sent to someone else? 
		find someone who can make the product 
			if multiple can...who do you want to buy it from?
		have them make it and send it (goes back to receiving an order steps)
	*/
	
	public static void supplier(Connection con, Scanner myScanner){
	
		Scanner scan = new Scanner(System.in);
		boolean authorized = false;
		int myId = -1;
		System.out.print("Please enter your supplier number: ");
		while(!authorized){
			if(scan.hasNextInt()){
				try{
				
						myId = scan.nextInt();
				
					String q; 
					ResultSet result;
				
					q = "select * from supplier where supplier_id = ?";
					PreparedStatement ps = con.prepareStatement(q);
					ps.setInt(1, myId);
				
					result = ps.executeQuery();
					
					if(!result.next()){
						System.out.println("Looks like you are not a supplier for Regork :(");
						System.out.println("We would still love for you to sell to us!");
						System.out.println("Our Manager would love to help you do this please contact them!");
						System.out.println("Do you want us to take you there? (y or n): ");
						String response =  "";
						response = scan.next();
						response.toLowerCase();
						if(response.equals("y")){
							System.out.println("taking you to the Regork manager....");
							System.out.println();
							Manager.addVendor(con);
							return;
						}
						System.exit(0);
						//System.out.println("Would you like us to do this? (y or n)");
						
						
					} else{
						do{
							System.out.print("Just to confirm you are:  ");
							System.out.println(result.getString("name"));
							System.out.println();
							System.out.print("Located at:  ");
							System.out.println(result.getString("address"));
							System.out.println();
						} while(result.next());
						System.out.print("Please type y or n to confirm: ");
						Scanner newScanner = new Scanner(System.in);
						while(newScanner.hasNext()){
							String answer = newScanner.next().toLowerCase();
							if(answer.equals("y")){
								System.out.println();
								System.out.println("Welcome supplier, what would you like to do today?");
								System.out.println();
								authorized = true;
								break;
							}
							else if(answer.equals("n")){
								System.out.println("Please enter supplier num again");
								break;
								
							}
							else{
								System.out.print("Please enter y or n: ");
								break;
							}
						}
					}
				} catch(Exception e){
					System.out.println("Looks like something went wrong. Lets try that again.");
					continue;
			}
			}	
			else{
				System.out.println("Please enter a number");
					scan.next();
					continue;
				}
		}
		
		boolean supplier = true;
		
		while(supplier){
				//System.out.println("[R] - Fill an order");
			System.out.println("[O] - Add a product that you offer");
			System.out.println("[P] - Place an order");
			System.out.println("[Q] - quit");
	
			String toDo = userChoice(myScanner, con);
	
			if(toDo.equals("P")){
				System.out.println("How many products are on the order?");  
				int toOrder = myScanner.nextInt();
				int[] prodNums = new int[toOrder];
				System.out.println("Are the product numbers listed on the order? (y or n)?");
				boolean yOrN = true;
				while(yOrN){
					if(myScanner.hasNext()){
					String answer = myScanner.next().toLowerCase();
					if(answer.equals("y")){
						prodNums = lookupByNumber(con, myScanner, prodNums, toOrder, myId);
						yOrN = false;
					}
					else if(answer.equals("n")){
				
						for(int i = 0; i < toOrder; i++){
							List<String> resultId = new ArrayList<String>();
							int idResultFlag = 0;
							System.out.print("Please enter the name of the product on the order ");
							String name = myScanner.next();
							System.out.println();
						
							resultId = lookupByName(con, myScanner, myId, name);
						
							if(resultId.size() > 0){
								boolean fillingNums = true;
								while(fillingNums){
									System.out.print("Which of these do you want to order?");
									if(myScanner.hasNextInt()){
										prodNums[i] = myScanner.nextInt();
										for(String id: resultId){
									//System.out.println("" + prodNums[i]);
								//	System.out.println(id);
											if(id.equals("" + prodNums[i])){
												idResultFlag = 1;
											}
										}
										if(idResultFlag == 1){
											fillingNums = false;
										}
										else{
											System.out.println("Looks like that wasn't one of the products we listed! please enter one of the product numbers above");
										}	
									} 
									else{
										myScanner.next();
										System.out.println("Please enter a valid product number");
									}
								}
								yOrN = false;
							}
						}
					}
					else{
						System.out.print("Please enter y or n: ");
						continue;
					}
				}
				}

				placeOrder(con, myScanner, prodNums, toOrder, myId);
				continue;
			
			}
		
			if(toDo.equals("O")){
				System.out.println("How many products would you like to add that you offer?");  
				int toOffer = myScanner.nextInt();
				int[] prodNums = new int[toOffer];
				addProd(con, myScanner, prodNums, toOffer, myId);
				continue;
			}

			System.out.println("Would you like to return to the main menu? (y or n)");
			String response =  "";
			response = myScanner.next();
			response.toLowerCase();
			if(response.equals("y")){
				return;
			}
			try{
				con.close();
				System.exit(0);
			}catch(Exception e){
				System.out.println("Looks like you're stuck here");
			}
		}

	}
	
	public static String userChoice(Scanner myScanner, Connection con){
		String choice = "X";
		boolean cont = true;
		
		while(cont){
			choice = myScanner.next();
			choice = choice.toUpperCase();
			switch(choice){
				case "R":
					return choice;
				case "P":
					return choice;
				case "O":
					return choice;
				case "Q":
					try{
						con.close();
						System.exit(0);
					} catch(Exception e){
						System.out.print("Something went wrong");
						continue;
					}
				default:
					System.out.println("Looks like that option doesn't exist! Please enter one of the following options:");
				}
				System.out.println("[R] - Fill an order");
				System.out.println("[P] - Add a product that you offer");
				System.out.println("[O] - Place an order");
		}
		return "";
		
	}
	
	public static int[] lookupByNumber(Connection con, Scanner myScanner, int[] prodNums, int toOrder, int myId){
		for(int i = 0; i < toOrder; i++){
			System.out.println("Please enter the " + (i + 1) + " product number: ");
			boolean fillingNums = true;
				while(fillingNums){
					if(myScanner.hasNextInt()){
						prodNums[i] = myScanner.nextInt();
						fillingNums = false;
						//MAKE THIS ACTUALLY DO SOMETHING
						break;
					}
					else{
						myScanner.next();
						System.out.println("Please enter a product number");
					}
				}
			}
		return prodNums;
	}
	
	public static List<String> lookupByName(Connection con, Scanner myScanner,/* int[] prodNums, int toOrder,*/ int myId, String name){
		List<String> resultId = new ArrayList<String>();
		try{
				String q; 
				ResultSet result;
				
				q = "select * from Generic_product where name like ?";
				PreparedStatement ps = con.prepareStatement(q);
				name = name + "%";
				ps.setString(1, name);
				
				result = ps.executeQuery();
				ResultSetMetaData rsmd = result.getMetaData();
				if(!result.next()){
					//figure out something better to do than this
					System.out.println("There are no products with that name.");
				}
				else{
					String headers = String.format("%-10s %-50s", rsmd.getColumnName(1), rsmd.getColumnName(2));
					System.out.println(headers);
					do{
						String out = String.format("%-10s %-50s", result.getString(rsmd.getColumnName(1)), result.getString(rsmd.getColumnName(2)));
						resultId.add(result.getString(rsmd.getColumnName(1)));
						System.out.println(out);
					} while(result.next());
					
				}
				
			}
		catch (Exception e){
			System.out.println("Problem looking up supplier");
			System.exit(0);
		}
		return resultId;
	}
	
	public static void placeOrder(Connection con, Scanner myScanner, int[] prodNums, int toOrder, int myId){
		List<String> offers;
		int[] supplierChoice = new int[toOrder];
		int[] quantity = new int[toOrder];
		try{
			for(int i = 0; i < toOrder; i++){
				offers = new ArrayList<String>();
				System.out.println("Here are the people who make that product");
				offers = whoMakes(con, myScanner, prodNums[i], myId);
				
				if(offers.size() == 0){
					System.out.println("Since no one sells this product (we hope that you have offered to sell it) we need to cancel this order");
					return;
				}
				boolean fillingNums = true;
				while(fillingNums){
				int resultFlag = -1;
					System.out.print("Which supplier would you like to order from? enter the id: ");
						if(myScanner.hasNextInt()){
							supplierChoice[i] = myScanner.nextInt();
								for(String id: offers){
									if(id.equals("" + supplierChoice[i])){
										resultFlag = 1;
									}
								}
								if(resultFlag == 1){
									System.out.println();
									System.out.println("How many would you like to buy? Looks like this supplier can only make 5 at a time ");
									System.out.print("Enter a number less than or equal to 5: ");
									boolean qtyTest = true;
									while(qtyTest){
										if(myScanner.hasNextInt()){
											int temp = myScanner.nextInt();
											if(temp <= 5){
												quantity[i] = temp;
												qtyTest = false;
											}
											else{
												System.out.println("That number isnt in the right range. Please try again");
												continue;
											}
										}
										else{
											System.out.println("Please enter a number less than or equal to 5");
											continue;
										}
										break;
									}
								}
								else{
									System.out.println("Looks like that wasn't one of the listed suppliers! please enter one of the supplier numbers above");
									continue;
								}
							break;		
						}
						else{
							myScanner.next();
							System.out.println("Please enter a valid supplier number");
						}
				}
				
				System.out.println();
				System.out.println("We are sending your order for " + quantity[i] + " units of product_no " + prodNums[i] + " from supplier " + supplierChoice[i]);
				
				int shipId;
				shipId  =  insertShipment(con, myScanner, myId, supplierChoice[i]);
				System.out.println();
				
				for(int j = 0; j < quantity[i]; j++){
					int prodAdd = 0;
					prodAdd = insertSpecificProduct(con, myScanner, supplierChoice[i], shipId, prodNums[i]);
					if(prodAdd <= 0){
						System.out.println("something went wrong");
						break;
					}
				}
			}
			
			 //insert products/shipments
			System.out.println("Your package is in the mail!");
			System.out.println("You can expect it tomorrow!");
			            
			System.out.println("    ____.----' ____.----.\\");
			System.out.println("    \\                    \\");
			System.out.println("     \\                    \\");
			System.out.println("      \\                    \\");
			System.out.println("       \\          ____.----'`--.__");
			System.out.println("       \\___.----'          |     `--.____");
			System.out.println("      /`-._                |       __.-' \\");
			System.out.println("      /     `-._            ___.---'       \\");
			System.out.println("     /          `-.____.---'                \\");
			System.out.println("    /            / | \\                       \\");
			System.out.println("   /            /  |  \\                   _.--'");
			System.out.println("   `-.         /   |   \\            __.--'");
			System.out.println("      `-._    /    |    \\     __.--'     |");
			System.out.println("        | `-./     |     \\_.-'           |");
			System.out.println("        |          |                     |");
			System.out.println("        |          |                     |");
			System.out.println("        |          |                     |");
			System.out.println("        |          |                     |");
			System.out.println("        |          |                     |");
			System.out.println("        |          |                     |");
			System.out.println(" _______|          |                     |_______________");
			System.out.println("        `-.        |                  _.-'");
			System.out.println("           `-.     |           __..--'");
			System.out.println("              `-.  |      __.-'");
			System.out.println("                 `-|__.--'");
			

			System.out.println("Would you like to return to the supplier menu? (y or n)");
			String response =  "";
			if(myScanner.hasNext()){
				response = myScanner.next();
				response.toLowerCase();
				if(response.equals("y")){
					return;
				}
			}
			
			con.close();
			System.exit(0);
		
		}
		catch(Exception e){
			System.out.println("Error in shipment");
			return;
		}
	}
	
	public static void addProd(Connection con, Scanner myScanner, int[] prodNums, int toOffer, int myId){
		int added = 0;
		boolean val = false;
		String[] myProducts = new String[toOffer];
		List<String> resultId = new ArrayList<String>();
		for(int i = 0; i < toOffer; i++){
			System.out.println("Please enter the name of the product that you would like to offer");
			String name = myScanner.next();
			myProducts[i] = name;
			System.out.println();
			
			resultId = lookupByName(con, myScanner, /* prodNums, toOffer,*/ myId, name);
			
			//does anyone else already make it
			int numResults = resultId.size();
			if(numResults > 0){
				//Which of these would you like to offer (if there are more than one) ex 3 kinds of wine
				
				
				System.out.println("Looks like someone already makes that product. Do you still want to make it also?");
				System.out.println("If yes enter the corresponding product number. if no press 0");
				
				
				while(myScanner.hasNextInt()){
					int answer = -1;
					answer = myScanner.nextInt();
					if(answer == 0){
						System.out.println("Okay, We wont add this product to the ones that you make."); 
						break;
					}
					else if(answer > 0){
						System.out.println("How much do you want to sell " + name + " for?");
						double myPrice = -1;
						while(myScanner.hasNextDouble()){
							myPrice = myScanner.nextDouble();
							if(myPrice < .01){
								System.out.println("Please enter number greater than zero. You cant sell things for free!");
								continue;
							}
							break;
						}
						
						val = canMakeInsert(con, myScanner, myId, answer, myPrice);
						
						if(!val){
							System.out.println("You already make this. Returning to the menu");
							break;
						}
						else{
							System.out.println("You are now a verified vendor for " + answer + " at price $" + myPrice);
							return;
						}
						
					}
					else{
						System.out.print("Please enter a number");
						continue;
					}
				}
			}
			if(numResults == 0){
				System.out.println("Looks like no one makes that product, we will add you to our list of known suppliers");
				//if not insert into generic product then can make
				//what do you want the product number to be
				System.out.print("Please enter a positive integer <= 99999 to be your product number: ");	
				Boolean unique = false;
				try{
					int answer = -1;
					while(myScanner.hasNextInt()){
						answer = myScanner.nextInt();
						if(answer > 99999 || answer < 1){
							System.out.println("Please enter a valid number");
						}
						
						unique = checkUnique(con, answer, "Generic_Product", "Product_no");
						
						if(!unique){
							System.out.print("That Product_No has already been used, please come up with a new one");
							continue;
						}
						else{
							break;
						}
					}
					added = genericProductInsert(con, myScanner, name, answer);
					
					System.out.println("How much do you want to sell " + name + " for?");
					double myPrice = -1;
					while(myScanner.hasNextDouble()){
						myPrice = myScanner.nextDouble();
						if(myPrice < .01){
							System.out.println("Please enter number greater than zero. You cant sell things for free!");
							continue;
						}
						break;
					}
					boolean  worked = false;
					worked = canMakeInsert(con, myScanner, myId, answer, myPrice);
					if(worked){
						System.out.println("You are now a verified vendor for " + answer + " at price $" + myPrice);
						return;
					}
				} catch(Exception e){
					System.out.println("We had a problem adding you as a supplier for " + name);
				}
					
			}
		}
		return;
	}
	
	public static void orderReciept(Connection con, Scanner myScanner, int myId){
	}
	
	public static int genericProductInsert(Connection con, Scanner myScanner, String name, int answer){
		int added = 0;
		
		 try{
           String q = "INSERT INTO Generic_product(Product_No,Name) VALUES (?, ?)";
           PreparedStatement ps = con.prepareStatement(q);
           ps.setInt(1, answer);
           ps.setString(2, name);
           added = ps.executeUpdate();
           ps.close();
         } catch(Exception e){System.out.println("insert error");}
	
		return added;
	}
	
	public static boolean canMakeInsert(Connection con, Scanner myScanner, int myId, int answer, double myPrice){
		int added = 0;
		boolean result = false;
	
		 try{
           String q = "INSERT INTO Can_make(supplier_ID,Product_No,offering_price) VALUES (?,?,?)";
           PreparedStatement ps = con.prepareStatement(q);
           ps.setInt(1, myId);
           ps.setInt(2, answer);
           ps.setDouble(3, myPrice);
           
           added = ps.executeUpdate();
           result = true;
           ps.close();
         } catch(Exception e){System.out.println("insert failed");}
	
		return result;
	}
	
	public static boolean checkUnique(Connection con, int answer, String table, String attribute){
		try{
           String q = "Select * from " + table + " where " + attribute + " = ?";
           PreparedStatement ps = con.prepareStatement(q);
           ResultSet result;
           ps.setInt(1, answer);
           
           result = ps.executeQuery();
           
           if(!result.next()){
           		ps.close();
           		return true;
			}
			else{
				ps.close();
				return false;
			}
         } catch(Exception e){System.out.println("Error checking id");}
		return false;
	}
	
	public static List<String> whoMakes(Connection con, Scanner myScanner, int prodNum, int myId){
		List<String> suppliers = new ArrayList<String>();
		try{
			String q = "select * from can_make where product_no = ? and supplier_id <> ?";
			PreparedStatement ps = con.prepareStatement(q);
			ResultSet result;
			ps.setInt(1, prodNum);
			ps.setInt(2, myId);
			
			result = ps.executeQuery();
			
			if(!result.next()){
				System.out.println("Looks like no one makes that...Guess we cant order it :(");
				System.out.println("Maybe you could sell this product for us!");
				System.out.println("Our Manager would love to help you do this please contact them!");
				System.out.println("Do you want us to take you there? (y or n): ");
				String response =  "";
				response = myScanner.next();
				response.toLowerCase();
				if(response.equals("y")){
					System.out.println("taking you to the Regork manager....");
					System.out.println();
					Manager.addVendor(con);
					return suppliers;
				}
				
				return suppliers;
			}
			ResultSetMetaData rsmd = result.getMetaData();
			String headers = String.format("%-20s %-50s %-10s", rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3));
			System.out.println(headers);
			do{
				String out = String.format("%-20s %-50s %-10s", result.getString(rsmd.getColumnName(1)), result.getString(rsmd.getColumnName(2)), result.getString(rsmd.getColumnName(3)));
				suppliers.add(result.getString(rsmd.getColumnName(1)));
				System.out.println(out);
			} while(result.next());
			return suppliers;
		} catch(Exception e){System.out.println("error finding suppliers");
		}
		
		return suppliers;
	}
	
	public static int insertShipment(Connection con, Scanner myScanner, int myId, int supplierChoice){
		int added = 0;
		try{
			String q = "INSERT INTO Shipment(Shipment_ID,Date_Sent,Supplier_ID,Destination_Supplier_ID) VALUES (?,DATE '2019-04-07',?,?)";
			PreparedStatement ps = con.prepareStatement(q);
			int shipId = generateUniqueShip(con);
			ps.setString(1, "" + shipId);
		
		//	ps.setDate(2, myDate);
			ps.setString(2, "" + supplierChoice);
			ps.setString(3, "" + myId);
			
			added = ps.executeUpdate();
			if(added == 1){
				return shipId;
			}
			
		} catch(Exception e){ 
			System.out.println("Error inserting shipment");
			return 0;
		}
		return 0;
	}
	public static int insertSpecificProduct(Connection con, Scanner myScanner, int supplierId, int shipId, int prodNum){
		int added = 0;
		try{
			String q = "insert into Specific_Product (Product_ID, Date_Made, Selling_Price, Batch_ID, Recall_Status, Supplier_ID, Shipment_ID, Product_No) values (?, DATE '2019-04-03', ?, ?, 'False', ?, ?, ?)";
			PreparedStatement ps = con.prepareStatement(q);
			int prodId = generateUniqueProd(con);
		//	System.out.println(prodId);
			ps.setString(1, "" + prodId);
			double price = randPrice();
		//	System.out.println(price);
			ps.setDouble(2, price);
			int batch = randBatch();
		//	System.out.println(batch);
			ps.setInt(3, batch);
		//	System.out.println(supplierId);
			ps.setInt(4, supplierId);
		//	System.out.println(shipId);
			ps.setInt(5, shipId);
			//System.out.println(prodNum);
			ps.setInt(6, prodNum);
			
			added = ps.executeUpdate();
			
		} catch(Exception e){ 
			System.out.println("Error inserting");
			return 0;
		}
		return added;
	}
	
	public static int generateUniqueShip(Connection con){
		int m = (int) Math.pow(10, 4);
    	m = m + new Random().nextInt(9 * m);
    	
    	if(checkUnique(con, m, "Shipment", "Shipment_id")){
    		return m;	
    	}
    	else{
    		m = generateUniqueShip(con);
    		return m;
    	}
	}
	
	public static int generateUniqueProd(Connection con){
		int m = (int) Math.pow(10, 4);
    	m = m + new Random().nextInt(9 * m);
    	
    	if(checkUnique(con, m, "Specific_product", "Product_Id")){
    		return m;	
    	}
    	else{
    		m = generateUniqueProd(con);
    		return m;
    	}
	}
	
	public static double randPrice(){
		double x = (Math.random() * ((25 - .01) + 1)) + .01;
		return ((int)(x * 100))/100.0;
	}
	
	public static int randBatch(){
		int m = (int) Math.pow(10, 4);
    	m = m + new Random().nextInt(9 * m);
    	return m;
	}
	
	public static String generateDate(){
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		String formattedDate = formatter.format(LocalDate.now());
		return formattedDate;
	}
	 
}