import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class Assignment{
	
	//database configuration
	String url = "jdbc:mysql://<hostaddress>";
	String database = "/atm_database";
	String username = "admin";
	String password = "";
	
	//user account number
	int accountNumber;
	
	//MySQL connection
	Connection con;
	Statement stmt;
	
	
	public Assignment() {
		
		try {
			con = DriverManager.getConnection(url+database,username,password);
			stmt = con.createStatement();
			System.out.println("Database Connected!!!");
		} catch (SQLException e) {
			System.out.println(e);
		}
		
	}
	
	public void checkAccountExist(int cardNumber) throws Exception{
		
		try {
			ResultSet rs = stmt.executeQuery("select accountNumber from ATM where cardNumber="+cardNumber);
			
			if(rs.next()) {
				accountNumber = rs.getInt(1);
			}else {
				throw new Exception("Card Number is invalid!!!");
			}
			
		}catch (SQLException e) {
			System.out.println(e);
		}
		
	}
	
	public void validateATMPin (String atmPIN) throws Exception {
		
		if(atmPIN.length() != 4) {
			throw new Exception("Enter 4 digit ATM PIN!!!");
		}
		
	}
	
	public void checkATMPin (int cardNumber,String atmPIN) throws Exception {

		try {
			
			validateATMPin(atmPIN);
			
			ResultSet rs = stmt.executeQuery("select atmPIN from ATM where cardNumber="+cardNumber);
			rs.next();
			
			if(!atmPIN.equals(rs.getString(1))){
				stmt.executeUpdate("insert into Logs (Description,accountNumber) values ('Entered Invalid Pin',"+accountNumber+")");
				throw new Exception("Entered Invalid Pin");
			}
		
		} catch (SQLException e) {
			System.out.println(e);
		}
		
	}
	
	public void showTransactions () {
		
		try {
			
			ResultSet rs = stmt.executeQuery("select * from transactions where accountNumber="+accountNumber);
			
			if(!rs.next()) {
				System.out.println("No Transaction Found!!!");
			}else {
				do {
					System.out.println(rs.getInt(1)+", "+rs.getString(2)+", "+rs.getInt(3)+", "+rs.getInt(5));
				}while(rs.next());
			}
			
		} catch (SQLException e) {
			System.out.println(e);
		}
		
	}
	
	public void showHistory () {
		
		try {
			ResultSet rs = stmt.executeQuery("select * from Logs where accountNumber="+accountNumber);
			if(!rs.next()) {
				System.out.print("No history Found!!!");
			}else {
				do {
					System.out.println(rs.getInt(1)+", "+rs.getString(2));
				}while(rs.next());
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
		
		
	}
	
	public void withdrawMoney (int amount) throws Exception {
		
		try {
			
			ResultSet rs = stmt.executeQuery("select currentBalance from customers where accountNumber="+accountNumber);
			rs.next();
			int currentBalance = rs.getInt(1);
			
			if(currentBalance < amount) {
				stmt.executeUpdate("insert into Logs (Description,accountNumber) values ('Cash withdrawl worth "+amount+" failed due to insufficient balance',"+accountNumber+")");
				throw new Exception("Insufficient Balance!!!");
			}else {
				currentBalance -= amount;
				stmt.executeUpdate("update customers set currentBalance="+currentBalance+" where accountNumber="+accountNumber);
				stmt.executeUpdate("insert into transactions (Description,amount,accountNumber,updatedAmount) values ('Cash withdrawl',"+amount+","+accountNumber+","+currentBalance+")");
				System.out.println("Cash withdrawl successful!!");
			}
			
		} catch (SQLException e) {
			System.out.println(e);
		}
		
		
	}
	
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		Assignment obj = new Assignment();
		
		System.out.print("Enter card Number: ");
		int cardNumber = sc.nextInt();
		sc.nextLine();
		
		try {
			
			//check if card number is valid or not
			obj.checkAccountExist(cardNumber);
			
			System.out.print("Enter 4 digit ATM PIN: ");
			String atmPIN = sc.nextLine();
			
			//validate and compare ATM pin with database
			obj.checkATMPin(cardNumber, atmPIN);
						
			//authentication completed
			
			System.out.println("Commands:\nShow All transactions: 1\nShow History: 2\nWithdraw Money: 3\nExit: 4");
			System.out.print("Enter command: ");
			String command = sc.nextLine();
						
			while(!command.equals("4")) {
				switch(command) {
					case "1":
						obj.showTransactions();
						break;
					case "2":
						obj.showHistory();
						break;
					case "3":
						System.out.print("Enter amount to be withdrawn: ");
						int amount = sc.nextInt();
						sc.nextLine();
						obj.withdrawMoney(amount);
						break;
					default:
						command = "4";
						break;
									
				}System.out.print("Enter command: ");
				command = sc.nextLine();
			}
			
			obj.con.close();
					
		} catch (Exception e) {
			System.out.println(e);
		}
		
		sc.close();
		
	}

}
