package banking;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class BankingSystem {

    private final Map<String, String> accountStorage;
    private final Scanner scanner;
    private SQLiteDataSource database;
    private boolean dbCreated;

    static int id = 101;

    public BankingSystem(String pathToDb) {

        this.accountStorage = new HashMap<>();
        this.scanner = new Scanner(System.in);

        this.database = new SQLiteDataSource();
        this.database.setUrl("jdbc:sqlite:" + pathToDb);

        this.dbCreated = false;
    }

    public void startSystem() {

        if (!dbCreated) {
            createDatabase();
            dbCreated = true;
        }

        while (true) {
            int selection = printMainMenu();
            System.out.println();

            switch (selection) {

                case 1:

                    createNewCard();
                    break;

                case 2:

                    System.out.println("Enter your card number:");
                    String cardNumber = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    String userPin = String.valueOf(scanner.nextInt());
                    scanner.nextLine();
                    System.out.println();

                    if (!accountStorage.containsKey(cardNumber) || !accountStorage.get(cardNumber).equals(userPin)) {
                        System.out.println("Wrong card number or PIN!\n");
                    } else {

                        System.out.println("You have successfully logged in!\n");

                        while (true) {

                            int choice = printAccountMenu();
                            System.out.println();
                            boolean breakLoop = false;

                            switch (choice) {

                                case 1:

                                    System.out.println("Balance: 0\n");
                                    break;

                                case 2:

                                    System.out.println("You have successfully logged out!\n");
                                    breakLoop = true;
                                    break;

                                case 0:

                                    System.out.println("Bye!");
                                    return;
                            }

                            if (breakLoop) {
                                break;
                            }
                        }
                    }
                    break;

                case 0:

                    System.out.println("Bye");
                    return;
            }
        }
    }

    private int printMainMenu() {

        System.out.println("1. Create an account\n" +
                "2. Log into account\n" +
                "0. Exit");

        return Integer.parseInt(scanner.nextLine());
    }

    private int printAccountMenu() {

        System.out.println("1. Balance\n" +
                "2. Log out\n" +
                "0. Exit");

        return Integer.parseInt(scanner.nextLine());
    }

    private String generateNewAccountNumber() {

        String bin = "400000";

        Random random = new Random();
        StringBuilder customerAccountNumber = new StringBuilder(String.valueOf(random.nextInt(Integer.MAX_VALUE)));

        if (customerAccountNumber.length() > 9) {
            customerAccountNumber.deleteCharAt(random.nextInt(customerAccountNumber.length()));
        } else if (customerAccountNumber.length() < 9) {

            while (customerAccountNumber.length() < 9) {
                customerAccountNumber.insert(0, 0);
            }
        }

        String checksum = String.valueOf(generateChecksum(bin + customerAccountNumber));

        return bin + customerAccountNumber + checksum;
    }

    private int generateChecksum(String initialNumber) {

        int total = 0;

        for (int i = 1; i <= initialNumber.length(); i++) {

            int current = ((int) initialNumber.charAt(i - 1) - 48);

            if (i % 2 == 1) {

                current *= 2;

                if (current > 9) {
                    current -= 9;
                }

            }
            total += current;
        }

        int checkSum = 0;
        while ((total + checkSum) % 10 > 0) {
            checkSum++;
        }

        return checkSum;
    }

    private String generateNewPin() {

        Random random = new Random();
        int pin = random.nextInt(10000);

        StringBuilder newPin = new StringBuilder(String.valueOf(pin));

        if (newPin.length() < 4) {
            newPin.insert(0, 0);
        }

        return newPin.toString();
    }

    private void createNewCard() {

        String accountNumber;
        while (true) {

            accountNumber = generateNewAccountNumber();

            if (!accountStorage.containsKey(accountNumber)) {
                break;
            }
        }

        String pin = generateNewPin();
        accountStorage.put(accountNumber, pin);
        saveCardToDb(accountNumber, pin);

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(accountNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
        System.out.println();
    }

    private void createDatabase() {

        String command = "CREATE TABLE IF NOT EXISTS card ("
                + "	id INTEGER PRIMARY KEY,"
                + "	number TEXT,"
                + "	pin TEXT,"
                + " balance INTEGER DEFAULT 0"
                + ");";

        try (Connection connection = this.database.getConnection()) {

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(command);
            }

        } catch (SQLException e) {
            System.out.println("Exception occurred while connecting to db.");
        }
    }

    private void saveCardToDb(String accountNumber, String pin) {

        String command = "INSERT INTO card(id,number,pin,balance) VALUES(?,?,?,?);";

//        '" + id + "','" + accountNumber + "','" + pin + "'

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setInt(1, id);
            statement.setString(2, accountNumber);
            statement.setString(3, pin);
            statement.executeUpdate();
            id++;

        } catch (SQLException e) {
            System.out.println("Exception occurred while saving card to db.");
        }
    }
}
