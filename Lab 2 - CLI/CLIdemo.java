package com.mybank.tui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import com.mybank.domain.Account;
import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[] { "help", "customers", "customer", "report", "exit" };
    }

    public void run() {
        AnsiConsole.systemInstall();
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<Completer>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        Customer cust = Bank.getCustomer(i);
                        String balanceStr = (cust.getNumberOfAccounts() > 0)
                                ? String.format("$%.2f", cust.getAccount(0).getBalance())
                                : "No accounts";

                        System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + balanceStr);
                    }
                } else {
                    System.out.println(ANSI_RED + "Your bank has no customers!" + ANSI_RESET);
                }

            } else if (line.indexOf("customer") != -1) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }

                    if (custNo >= 0 && custNo < Bank.getNumberOfCustomers()) {
                        Customer cust = Bank.getCustomer(custNo);

                        String accType = "No Account";
                        String balanceStr = "0.00";

                        if (cust.getNumberOfAccounts() > 0) {
                            accType = cust.getAccount(0) instanceof CheckingAccount ? "Checking" : "Savings";
                            balanceStr = String.format("%.2f", cust.getAccount(0).getBalance());
                        }

                        AttributedStringBuilder a = new AttributedStringBuilder()
                                .append("\nThis is detailed information about customer #")
                                .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                                .append("!");

                        System.out.println(a.toAnsi());

                        System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                        System.out.println("-------------------------------------------------------");
                        System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$"
                                + balanceStr);
                    } else {
                        System.out.println(
                                ANSI_RED + "ERROR! Customer with ID " + custNo + " does not exist!" + ANSI_RESET);
                    }
                } catch (Exception e) {
                    System.out.println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if ("report".equals(line)) {
                System.out.println("\n" + ANSI_GREEN + "=== CUSTOMER REPORT ===" + ANSI_RESET);

                if (Bank.getNumberOfCustomers() > 0) {
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        Customer customer = Bank.getCustomer(i);
                        System.out.println("\nCustomer: " + customer.getLastName() + ", " + customer.getFirstName());

                        if (customer.getNumberOfAccounts() > 0) {
                            for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                                Account account = customer.getAccount(j);
                                String acctType = "Unknown Account";

                                if (account instanceof SavingsAccount) {
                                    acctType = "Savings Account";
                                } else if (account instanceof CheckingAccount) {
                                    acctType = "Checking Account";
                                }

                                System.out.printf("    #%d - %s: $%.2f\n", j, acctType, account.getBalance());
                            }
                        } else {
                            System.out.println("    (No opened accounts for this customer)");
                        }
                    }
                } else {
                    System.out.println(ANSI_RED + "Your bank has no customers to generate a report!" + ANSI_RESET);
                }
                System.out.println(ANSI_GREEN + "=======================" + ANSI_RESET);

            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out.println(ANSI_RED
                        + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printWelcomeMessage() {
        System.out.println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET
                + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");
    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customers\t\t- Show list of customers");
        System.out.println("customer 'index'\t- Show customer details");
        System.out.println("report\t\t\t- Generate and show customers report");
        System.out.println("exit\t\t\t- Exit the app");
    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            String line = reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            return null;
        } catch (EndOfFileException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            java.io.File file = new java.io.File("test.dat");
            if (file.exists()) {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
                String line;

                // Читаємо перший непустий рядок — це кількість кастомерів
                int numCustomers = 0;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        numCustomers = Integer.parseInt(line);
                        break;
                    }
                }

                for (int i = 0; i < numCustomers; i++) {
                    // Шукаємо рядок з ім'ям клієнта
                    String customerLine = "";
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            customerLine = line;
                            break;
                        }
                    }

                    if (customerLine.isEmpty())
                        break;

                    // Ділимо рядок клієнта по табах або пробілах
                    String[] custParts = customerLine.split("\\s+");
                    String firstName = custParts[0];
                    String lastName = custParts[1];
                    int numAccounts = Integer.parseInt(custParts[2]);

                    Bank.addCustomer(firstName, lastName);
                    Customer cust = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);

                    // Зчитуємо рахунки для цього клієнта
                    for (int j = 0; j < numAccounts; j++) {
                        String accountLine = "";
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                accountLine = line;
                                break;
                            }
                        }

                        String[] accParts = accountLine.split("\\s+");
                        String accType = accParts[0];
                        double balance = Double.parseDouble(accParts[1]);

                        if ("S".equals(accType)) {
                            double interestRate = Double.parseDouble(accParts[2]);
                            cust.addAccount(new SavingsAccount(balance, interestRate));
                        } else if ("C".equals(accType)) {
                            double overdraft = Double.parseDouble(accParts[2]);
                            cust.addAccount(new CheckingAccount(balance, overdraft));
                        }
                    }
                }
                br.close();
                System.out.println("Текстову базу даних test.dat успішно розпарсено! Завантажено клієнтів: "
                        + Bank.getNumberOfCustomers());
            } else {
                throw new Exception("Файл test.dat не знайдено поруч з проєктом!");
            }
        } catch (Exception e) {
            System.err.println("Помилка парсингу тексту: " + e.getMessage() + ". Вмикаємо дефолтних клієнтів.");
            Bank.addCustomer("John", "Doe");
            Bank.getCustomer(0).addAccount(new CheckingAccount(2000));
        }

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}