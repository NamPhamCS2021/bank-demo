package com.example.bankDemo.controller;

import com.example.bankDemo.entity.*;
import com.example.bankDemo.enums.*;
import com.example.bankDemo.repository.*;
import com.example.bankDemo.security.entity.User;
import com.example.bankDemo.security.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
class DataPopulationService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final PeriodicalPaymentRepository periodicalPaymentRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;
    private final PeriodicalReportRepository periodicalReportRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Transactional
    public void populateAllData() {
        System.out.println("Starting data population...");

        List<Customer> customers = createCustomersWithUsers(50);
        System.out.println("Created " + customers.size() + " customers with users");

        List<Account> accounts = createAccountsForCustomers(customers);
        System.out.println("Created " + accounts.size() + " accounts");

        List<Transaction> transactions = createTransactions(accounts, 200);
        System.out.println("Created " + transactions.size() + " transactions");

        createAlertsForTransactions(transactions);
        System.out.println("Created alerts for transactions");

        createPeriodicalPayments(accounts, 30);
        System.out.println("Created periodical payments");

        createAccountStatusHistory(accounts);
        System.out.println("Created account status history");

        createPeriodicalReports(10);
        System.out.println("Created periodical reports");

        System.out.println("Data population completed!");
    }

    @Transactional
    public List<Customer> createCustomersWithUsers(int count) {
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Customer customer = new Customer();
            customer.setFirstName(faker.name().firstName());
            customer.setLastName(faker.name().lastName());
            customer.setEmail(faker.internet().emailAddress());
            customer.setPhoneNumber(faker.phoneNumber().phoneNumber().replaceAll("[^0-9]", ""));
            customer.setType(random.nextBoolean() ? CustomerType.PERSONAL : CustomerType.CORPORATE);

            // Create corresponding user
            User user = new User();
            user.setUsername(customer.getEmail());
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(UserRole.USER);
            user.setCustomer(customer);
            customer.setUser(user);

            // Save user first (as shown in your implementation)
            userRepository.save(user);
            customers.add(customerRepository.save(customer));
        }

        return customers;
    }

    @Transactional
    public List<Account> createAccountsForCustomers(List<Customer> customers) {
        List<Account> accounts = new ArrayList<>();

        for (Customer customer : customers) {
            // Create 1-3 accounts per customer
            int accountCount = random.nextInt(3) + 1;

            for (int i = 0; i < accountCount; i++) {
                Account account = new Account();
                account.setCustomer(customer);
                account.setBalance(new BigDecimal(faker.number().numberBetween(100, 100000)));
                account.setStatus(AccountStatus.values()[random.nextInt(AccountStatus.values().length)]);
                account.setAccountLimit(new BigDecimal(faker.number().numberBetween(1000, 50000)));
                // accountNumber and openingDate will be set by @PrePersist

                accounts.add(accountRepository.save(account));
            }
        }

        return accounts;
    }

    @Transactional
    public List<Transaction> createTransactions(List<Account> accounts, int count) {
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Transaction transaction = new Transaction();

            // Random account for transaction
            Account account = accounts.get(random.nextInt(accounts.size()));
            transaction.setAccount(account);

            // First decide the transaction type
            TransactionType type = TransactionType.values()[random.nextInt(TransactionType.values().length)];
            transaction.setType(type);

            // Set receiver based on transaction type
            if (type == TransactionType.TRANSFER) {
                // TRANSFER must have a receiver
                Account receiver;
                do {
                    receiver = accounts.get(random.nextInt(accounts.size()));
                } while (receiver.equals(account)); // Ensure receiver is different from sender
                transaction.setReceiver(receiver);
            }
            // WITHDRAWAL and DEPOSIT should not have a receiver (receiver remains null)

            transaction.setAmount(new BigDecimal(faker.number().numberBetween(10, 5000)));
            transaction.setLocation(faker.address().city());

            // Set random past date
            LocalDateTime pastDate = faker.date()
                    .past(365, TimeUnit.DAYS)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            transaction.setCreatedAt(pastDate);

            // checked and createdAt will be set by @PrePersist, but we override createdAt
            transactions.add(transactionRepository.save(transaction));
        }

        return transactions;
    }

    @Transactional
    public void createAlertsForTransactions(List<Transaction> transactions) {
        // Create alerts for 20% of transactions
        int alertCount = (int) (transactions.size() * 0.2);

        for (int i = 0; i < alertCount; i++) {
            Alert alert = new Alert();

            Transaction transaction = transactions.get(random.nextInt(transactions.size()));
            alert.setTransaction(transaction);
            alert.setType(AlertType.values()[random.nextInt(AlertType.values().length)]);
            alert.setDescription(faker.lorem().sentence());
            // status and timestamp will be set by @PrePersist

            alertRepository.save(alert);
        }
    }

    @Transactional
    public void createPeriodicalPayments(List<Account> accounts, int count) {
        for (int i = 0; i < count; i++) {
            PeriodicalPayment payment = new PeriodicalPayment();

            Account account = accounts.get(random.nextInt(accounts.size()));
            payment.setAccount(account);
            payment.setDescription(faker.commerce().productName() + " subscription");
            payment.setAmount(new BigDecimal(faker.number().numberBetween(10, 200)));
            payment.setPeriod(Period.values()[random.nextInt(Period.values().length)]);
            // status, startedAt, and endedAt will be set by @PrePersist

            periodicalPaymentRepository.save(payment);
        }
    }

    @Transactional
    public void createAccountStatusHistory(List<Account> accounts) {
        for (Account account : accounts) {
            // Create 1-3 status history entries per account
            int historyCount = random.nextInt(3) + 1;

            for (int i = 0; i < historyCount; i++) {
                AccountStatusHistory history = new AccountStatusHistory();
                history.setAccount(account);
                history.setStatus(AccountStatus.values()[random.nextInt(AccountStatus.values().length)]);

                // Set random past timestamp
                LocalDateTime pastDate = faker.date()
                        .past(180, TimeUnit.DAYS)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                history.setTimestamp(pastDate);

                accountStatusHistoryRepository.save(history);
            }
        }
    }

    @Transactional
    public void createPeriodicalReports(int count) {
        for (int i = 0; i < count; i++) {
            PeriodicalReport report = new PeriodicalReport();

            report.setNumberOfTransactions((long) faker.number().numberBetween(100, 1000));
            report.setTotalAmount(new BigDecimal(faker.number().numberBetween(10000, 100000)));
            report.setAverageAmount(new BigDecimal(faker.number().numberBetween(50, 500)));
            report.setMaximumAmount(new BigDecimal(faker.number().numberBetween(1000, 5000)));
            report.setMinimumAmount(new BigDecimal(faker.number().numberBetween(1, 50)));

            LocalDateTime startDate = faker.date()
                    .past(30, TimeUnit.DAYS)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            report.setStartAt(startDate);
            report.setEndAt(startDate.plusDays(7)); // Week-long reports
            // timestamp will be set by @PrePersist

            periodicalReportRepository.save(report);
        }
    }

    // Helper method to create admin user
    @Transactional
    public User createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);

        return userRepository.save(admin);
    }

    // Clean up method
    @Transactional
    public void clearAllData() {
        System.out.println("Clearing all data...");

        alertRepository.deleteAll();
        accountStatusHistoryRepository.deleteAll();
        periodicalPaymentRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        periodicalReportRepository.deleteAll();

        System.out.println("All data cleared!");
    }
}

// Controller for easy testing
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataPopulationController {

    private final DataPopulationService dataPopulationService;

    @PostMapping("/populate")
    public ResponseEntity<String> populateData() {
        try {
            dataPopulationService.populateAllData();
            return ResponseEntity.ok("Data population completed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during data population: " + e.getMessage());
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearData() {
        try {
            dataPopulationService.clearAllData();
            return ResponseEntity.ok("All data cleared successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during data clearing: " + e.getMessage());
        }
    }

    @PostMapping("/admin-user")
    public ResponseEntity<String> createAdminUser() {
        try {
            dataPopulationService.createAdminUser();
            return ResponseEntity.ok("Admin user created successfully! Username: admin, Password: admin123");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating admin user: " + e.getMessage());
        }
    }

    @PostMapping("/populate-custom")
    public ResponseEntity<String> populateCustomData(
            @RequestParam(defaultValue = "50") int customers,
            @RequestParam(defaultValue = "200") int transactions,
            @RequestParam(defaultValue = "30") int payments,
            @RequestParam(defaultValue = "10") int reports) {
        try {
            System.out.println("Starting custom data population...");

            List<Customer> customerList = dataPopulationService.createCustomersWithUsers(customers);
            System.out.println("Created " + customerList.size() + " customers with users");

            List<Account> accounts = dataPopulationService.createAccountsForCustomers(customerList);
            System.out.println("Created " + accounts.size() + " accounts");

            List<Transaction> transactionList = dataPopulationService.createTransactions(accounts, transactions);
            System.out.println("Created " + transactionList.size() + " transactions");

            dataPopulationService.createAlertsForTransactions(transactionList);
            System.out.println("Created alerts for transactions");

            dataPopulationService.createPeriodicalPayments(accounts, payments);
            System.out.println("Created " + payments + " periodical payments");

            dataPopulationService.createAccountStatusHistory(accounts);
            System.out.println("Created account status history");

            dataPopulationService.createPeriodicalReports(reports);
            System.out.println("Created " + reports + " periodical reports");

            return ResponseEntity.ok(String.format(
                    "Custom data population completed! Created: %d customers, %d accounts, %d transactions, %d payments, %d reports",
                    customers, accounts.size(), transactions, payments, reports
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during custom data population: " + e.getMessage());
        }
    }
}