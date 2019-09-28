import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

class Recall {
	public static void recall(Connection con, Scanner myScanner){
	
	/*
	specific product # of bad item
		find all products in the same batch and recall them
		if it is a component of another product recall that one as well. 
		If end product recall everything in it
	specific id not know
		name of product and time from that it was purchased 
		recall all on hand inventory at that time
	option: would you like to notify the supplier
		contact info for the supplier of recalled product and or components 
	*/
	
	boolean cont = true;
	int completed = 0;
	int timesThrough = 0;
		System.out.print("Please enter the product number of the item that you believe should be recalled");
		while(cont){
			try{
				int productNum;
				if(myScanner.hasNextInt()){
					productNum = myScanner.nextInt();
					int batch = prodExists(con, myScanner, productNum);
					if(batch != -1){
						List<String> products = prodInBatch(con, myScanner, batch);
						for(String product : products){
							completed += recalledIt(con, myScanner, product);
							System.out.println(completed);
						}
						if(completed == products.size()){
								System.out.println("Product has been recalled..returning you to main menu");
								return;	
						}
					}
					else{
						timesThrough += 1;
						if(timesThrough > 4){
							System.out.println("it seems like you're trying to recall a lot of products that dont exist");
							System.out.println("It is Regorks policy that after 4 reports you cant report anymore");
							System.out.println("sending you back to the main menu");
							return;
						}
						System.out.println("According to our records that instance of a product does not exist");
						System.out.println("Please try again. Enter the product number of an item you believe should be recalled");
						continue;
					}
				
				}
				cont = false;
			} catch (Exception e) {
				System.out.println("recall add error");
			}
		}
		
		
	}
	
	public static int prodExists(Connection con, Scanner myScanner, int productNum){
		try{
			String q = "Select Batch_id from specific_product where product_id = ?";
			PreparedStatement ps = con.prepareStatement(q);
			ResultSet result;
			ps.setInt(1, productNum);
           
           result = ps.executeQuery();
           
           if(!result.next()){
           	return -1;
           }
           else{
           	return result.getInt("batch_id");
           }
		} catch(Exception e){
		return -1;
		}
	}
	public static int recalledIt(Connection con, Scanner myScanner, String productNum){
		try{
			String q = "update specific_product SET Recall_status = 'true' where product_id = ?";
			PreparedStatement ps = con.prepareStatement(q);
			ps.setString(1, productNum);
		
			int i = ps.executeUpdate();
			return i;
		}catch(Exception e){
		}
		return 0;
	}
	public static List<String> prodInBatch(Connection con, Scanner myScanner, int batch){
		List<String> products = new ArrayList<String>();
		try{
			String q = "select product_id from specific_product where batch_id = ?";
			PreparedStatement ps = con.prepareStatement(q);
			ResultSet result;
			ps.setInt(1, batch);
		
			result = ps.executeQuery();
			
			if(!result.next()){
				return products;
			}
			else{
				do{
					products.add(result.getString("product_id"));
				}while(result.next());
			}
		}catch(Exception e){
		}
		return products;
		
	}
}