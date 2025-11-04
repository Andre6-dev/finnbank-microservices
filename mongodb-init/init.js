// Banking Microservices - MongoDB Initialization Script

// Connect and authenticate
const adminDb = db.getSiblingDB('admin');
print('Andre')

print('Authenticated successfully as root user');

// Create databases for each microservice
const databases = [
    'auth_db',
    'customer_db',
    'passive_product_db',
    'active_product_db',
    'transaction_db',
    'debit_card_db',
    'transfer_db',
    'yanki_db',
    'bootcoin_db'
];

databases.forEach(function (dbName) {
    print('Creating database: ' + dbName);
    const targetDb = db.getSiblingDB(dbName);

    // Create a dummy collection to initialize the database
    targetDb.createCollection('dummy_collection');
    targetDb.dummy_collection.insertOne({initialized: true});

    print('Database ' + dbName + ' created successfully');
});

// ========================================
// AUTH_DB
// ========================================
const authDb = db.getSiblingDB('auth_db');

// Create indexes
authDb.users.createIndex({"username": 1}, {unique: true});
authDb.users.createIndex({"email": 1}, {unique: true});

// Insert seed data
authDb.users.insertMany([
    {
        username: "admin",
        password: "$2a$10$GrBjbQJKc7uO6AGr.AHmFeuChIoQKeLC/Gu3ukFpDRYt/EDrtAHQq", // password: password123
        email: "admin@nttdata.com",
        roles: ["ADMIN", "USER"],
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        username: "jperez",
        password: "$2a$10$GrBjbQJKc7uO6AGr.AHmFeuChIoQKeLC/Gu3ukFpDRYt/EDrtAHQq",
        email: "jperez@email.com",
        roles: ["USER"],
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        username: "mgarcia",
        password: "$2a$10$GrBjbQJKc7uO6AGr.AHmFeuChIoQKeLC/Gu3ukFpDRYt/EDrtAHQq",
        email: "mgarcia@email.com",
        roles: ["USER"],
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('auth_db initialized with seed data');

// ========================================
// CUSTOMER_DB
// ========================================
const customerDb = db.getSiblingDB('customer_db');

// Create indexes
customerDb.customers.createIndex({"documentNumber": 1}, {unique: true});
customerDb.customers.createIndex({"email": 1});
customerDb.customers.createIndex({"customerType": 1});
customerDb.customers.createIndex({"profileType": 1});

// Insert seed data
customerDb.customers.insertMany([
    {
        _id: ObjectId("650000000000000000000001"),
        documentType: "DNI",
        documentNumber: "12345678",
        firstName: "Juan",
        lastName: "Pérez",
        email: "jperez@email.com",
        phone: "987654321",
        address: "Av. Lima 123, Lima",
        customerType: "PERSONAL", // PERSONAL o BUSINESS
        profileType: "REGULAR", // REGULAR, VIP, PYME
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000002"),
        documentType: "DNI",
        documentNumber: "87654321",
        firstName: "María",
        lastName: "García",
        email: "mgarcia@email.com",
        phone: "912345678",
        address: "Jr. Arequipa 456, Lima",
        customerType: "PERSONAL",
        profileType: "VIP",
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("650000000000000000000003"),
        documentType: "RUC",
        documentNumber: "20123456789",
        firstName: "Empresa",
        lastName: "SAC",
        email: "contacto@empresa.com",
        phone: "016001234",
        address: "Av. Industrial 789, Lima",
        customerType: "BUSINESS",
        profileType: "PYME",
        active: true,
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('customer_db initialized with seed data');

// ========================================
// PASSIVE_PRODUCT_DB
// ========================================
const passiveProductDb = db.getSiblingDB('products_db');

// Create indexes
passiveProductDb.passive_products.createIndex({"accountNumber": 1}, {unique: true});
passiveProductDb.passive_products.createIndex({"customerId": 1});
passiveProductDb.passive_products.createIndex({"productType": 1});
passiveProductDb.passive_products.createIndex({"status": 1});

// Insert seed data
passiveProductDb.passive_products.insertMany([
    // Cuentas de Ahorro
    {
        _id: ObjectId("651000000000000000000001"),
        productType: "SAVINGS_ACCOUNT",
        accountNumber: "1001234567890",
        customerId: ObjectId("650000000000000000000001"),
        balance: 5000.00,
        currency: "PEN",
        interestRate: 2.5,
        monthlyTransactionLimit: 10,
        transactionsThisMonth: 3,
        maintenanceFee: 0.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("651000000000000000000002"),
        productType: "SAVINGS_ACCOUNT",
        accountNumber: "1001234567891",
        customerId: ObjectId("650000000000000000000002"),
        balance: 15000.00,
        currency: "PEN",
        interestRate: 3.0,
        monthlyTransactionLimit: 20,
        transactionsThisMonth: 5,
        maintenanceFee: 0.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    // Cuentas Corrientes
    {
        _id: ObjectId("651000000000000000000003"),
        productType: "CHECKING_ACCOUNT",
        accountNumber: "2001234567890",
        customerId: ObjectId("650000000000000000000001"),
        balance: 8000.00,
        currency: "PEN",
        maintenanceFee: 15.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("651000000000000000000004"),
        productType: "CHECKING_ACCOUNT",
        accountNumber: "2001234567891",
        customerId: ObjectId("650000000000000000000003"),
        balance: 50000.00,
        currency: "PEN",
        maintenanceFee: 20.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    // Plazo Fijo
    {
        _id: ObjectId("651000000000000000000005"),
        productType: "FIXED_TERM",
        accountNumber: "3001234567890",
        customerId: ObjectId("650000000000000000000002"),
        balance: 20000.00,
        currency: "PEN",
        interestRate: 6.5,
        termMonths: 12,
        maturityDate: new Date(new Date().setMonth(new Date().getMonth() + 12)),
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("651000000000000000000006"),
        productType: "SAVINGS_ACCOUNT",
        accountNumber: "1001234567892",
        customerId: ObjectId("650000000000000000000003"),
        balance: 75000.00,
        currency: "PEN",
        interestRate: 2.0,
        monthlyTransactionLimit: 50,
        transactionsThisMonth: 8,
        maintenanceFee: 0.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('passive_product_db initialized with seed data');

// ========================================
// ACTIVE_PRODUCT_DB
// ========================================
const activeProductDb = db.getSiblingDB('products_db');

// Create indexes
activeProductDb.active_products.createIndex({"creditNumber": 1}, {unique: true});
activeProductDb.active_products.createIndex({"customerId": 1});
activeProductDb.active_products.createIndex({"productType": 1});
activeProductDb.active_products.createIndex({"status": 1});
activeProductDb.active_products.createIndex({"hasOverdueDebt": 1});

// Insert seed data
activeProductDb.active_products.insertMany([
    {
        _id: ObjectId("652000000000000000000001"),
        productType: "PERSONAL_LOAN",
        creditNumber: "4001234567890",
        customerId: ObjectId("650000000000000000000001"),
        creditLimit: 10000.00,
        currentDebt: 3500.00,
        availableCredit: 6500.00,
        interestRate: 18.5,
        currency: "PEN",
        paymentDay: 15,
        minimumPayment: 350.00,
        hasOverdueDebt: false,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("652000000000000000000002"),
        productType: "CREDIT_CARD",
        creditNumber: "4001234567891",
        customerId: ObjectId("650000000000000000000002"),
        creditLimit: 15000.00,
        currentDebt: 5200.00,
        availableCredit: 9800.00,
        interestRate: 35.0,
        currency: "PEN",
        paymentDay: 20,
        minimumPayment: 520.00,
        hasOverdueDebt: false,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("652000000000000000000003"),
        productType: "BUSINESS_CREDIT",
        creditNumber: "4001234567892",
        customerId: ObjectId("650000000000000000000003"),
        creditLimit: 100000.00,
        currentDebt: 45000.00,
        availableCredit: 55000.00,
        interestRate: 12.5,
        currency: "PEN",
        paymentDay: 30,
        minimumPayment: 4500.00,
        hasOverdueDebt: false,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("652000000000000000000004"),
        productType: "PERSONAL_LOAN",
        creditNumber: "4001234567893",
        customerId: ObjectId("650000000000000000000001"),
        creditLimit: 5000.00,
        currentDebt: 2000.00,
        availableCredit: 3000.00,
        interestRate: 22.0,
        currency: "PEN",
        paymentDay: 10,
        minimumPayment: 200.00,
        hasOverdueDebt: true,
        overdueAmount: 500.00,
        overdueSince: new Date("2025-01-10"),
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("652000000000000000000005"),
        productType: "CREDIT_CARD",
        creditNumber: "4001234567894",
        customerId: ObjectId("650000000000000000000003"),
        creditLimit: 50000.00,
        currentDebt: 12000.00,
        availableCredit: 38000.00,
        interestRate: 28.0,
        currency: "PEN",
        paymentDay: 25,
        minimumPayment: 1200.00,
        hasOverdueDebt: false,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('active_product_db initialized with seed data');

// ========================================
// TRANSACTION_DB
// ========================================
const transactionDb = db.getSiblingDB('transaction_db');

// Create indexes
transactionDb.transactions.createIndex({"transactionNumber": 1}, {unique: true});
transactionDb.transactions.createIndex({"productId": 1});
transactionDb.transactions.createIndex({"transactionType": 1});
transactionDb.transactions.createIndex({"status": 1});
transactionDb.transactions.createIndex({"transactionDate": -1});

// Insert seed data
transactionDb.transactions.insertMany([
    {
        transactionNumber: "TXN20250101000001",
        customerId: ObjectId("650000000000000000000001"),
        productId: ObjectId("651000000000000000000001"),
        transactionType: "DEPOSIT",
        amount: 1000.00,
        currency: "PEN",
        description: "Depósito en efectivo",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-15"),
        createdAt: new Date("2025-01-15"),
        updatedAt: new Date("2025-01-15")
    },
    {
        transactionNumber: "TXN20250101000002",
        customerId: ObjectId("650000000000000000000001"),
        productId: ObjectId("651000000000000000000001"),
        transactionType: "WITHDRAWAL",
        amount: 500.00,
        currency: "PEN",
        description: "Retiro cajero automático",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-20"),
        createdAt: new Date("2025-01-20"),
        updatedAt: new Date("2025-01-20")
    },
    {
        transactionNumber: "TXN20250101000003",
        customerId: ObjectId("650000000000000000000001"),
        productId: ObjectId("652000000000000000000001"),
        transactionType: "CREDIT_CHARGE",
        amount: 1500.00,
        currency: "PEN",
        description: "Compra en tienda",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-22"),
        createdAt: new Date("2025-01-22"),
        updatedAt: new Date("2025-01-22")
    },
    {
        transactionNumber: "TXN20250101000004",
        customerId: ObjectId("650000000000000000000001"),
        productId: ObjectId("652000000000000000000001"),
        transactionType: "PAYMENT",
        amount: 500.00,
        currency: "PEN",
        description: "Pago de crédito",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-25"),
        createdAt: new Date("2025-01-25"),
        updatedAt: new Date("2025-01-25")
    },
    {
        transactionNumber: "TXN20250101000005",
        customerId: ObjectId("650000000000000000000001"),
        productId: ObjectId("651000000000000000000003"),
        transactionType: "TRANSFER_OUT",
        amount: 2000.00,
        currency: "PEN",
        description: "Transferencia a terceros",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-26"),
        createdAt: new Date("2025-01-26"),
        updatedAt: new Date("2025-01-26")
    }
]);

print('transaction_db initialized with seed data');

// ========================================
// DEBIT_CARD_DB
// ========================================
const debitCardDb = db.getSiblingDB('debit_card_db');

// Create indexes
debitCardDb.debit_cards.createIndex({"cardNumber": 1}, {unique: true});
debitCardDb.debit_cards.createIndex({"customerId": 1});
debitCardDb.debit_cards.createIndex({"mainAccountId": 1});
debitCardDb.debit_cards.createIndex({"cardStatus": 1});

// Insert seed data
debitCardDb.debit_cards.insertMany([
    {
        cardNumber: "5412751234567890",
        customerId: ObjectId("650000000000000000000001"),
        mainAccountId: ObjectId("651000000000000000000001"),
        associatedAccountIds: [
            '651000000000000000000001',
            '651000000000000000000003'
        ],
        cardType: "VISA",
        expirationDate: new Date("2028-12-31"),
        cvv: "123",
        cardHolderName: "JUAN PEREZ",
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
]);

print('debit_card_db initialized with seed data');

// ========================================
// TRANSFER_DB
// ========================================
const transferDb = db.getSiblingDB('transfer_db');

// Create indexes
transferDb.transfers.createIndex({"transferNumber": 1}, {unique: true});
transferDb.transfers.createIndex({"sourceAccountId": 1});
transferDb.transfers.createIndex({"destinationAccountId": 1});
transferDb.transfers.createIndex({"status": 1});
transferDb.transfers.createIndex({"transferDate": -1});

// Insert seed data
transferDb.transfers.insertMany([
    {
        transferNumber: "TRF20250101000001",
        sourceAccountId: ObjectId("651000000000000000000003"),
        destinationAccountId: ObjectId("651000000000000000000001"),
        amount: 2000.00,
        currency: "PEN",
        transferType: "INTERBANK",
        description: "Pago de servicios",
        destinationBank: "BCP",
        destinationAccountNumber: "1001234567890",
        status: "COMPLETED",
        transferDate: new Date("2025-01-26"),
        createdAt: new Date("2025-01-26"),
        updatedAt: new Date("2025-01-26")
    },
    {
        transferNumber: "TRF20250101000002",
        sourceAccountId: ObjectId("651000000000000000000001"),
        destinationAccountId: ObjectId("651000000000000000000002"),
        amount: 500.00,
        currency: "PEN",
        transferType: "SAME_BANK",
        description: "Préstamo personal",
        destinationBank: "NTTDATA_BANK",
        destinationAccountNumber: "1001234567891",
        status: "COMPLETED",
        transferDate: new Date("2025-01-27"),
        createdAt: new Date("2025-01-27"),
        updatedAt: new Date("2025-01-27")
    },
    {
        transferNumber: "TRF20250101000003",
        sourceAccountId: ObjectId("651000000000000000000004"),
        destinationAccountId: ObjectId("651000000000000000000002"),
        amount: 5000.00,
        currency: "PEN",
        transferType: "INTERBANK",
        description: "Pago a proveedor",
        destinationBank: "INTERBANK",
        destinationAccountNumber: "1001234567891",
        status: "PENDING",
        transferDate: new Date(),
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('transfer_db initialized with seed data');

// ========================================
// YANKI_DB
// ========================================
const yankiDb = db.getSiblingDB('yanki_db');

// Create indexes
yankiDb.yankis.createIndex({"documentNumber": 1}, {unique: true});
yankiDb.yankis.createIndex({"phoneNumber": 1}, {unique: true});
yankiDb.yankis.createIndex({"email": 1});
yankiDb.yankis.createIndex({"status": 1});

yankiDb.yanki_transactions.createIndex({"yankiId": 1});
yankiDb.yanki_transactions.createIndex({"transactionType": 1});
yankiDb.yanki_transactions.createIndex({"transactionDate": -1});

// Insert seed data - Yanki accounts
yankiDb.yankis.insertMany([
    {
        _id: ObjectId("653000000000000000000001"),
        documentNumber: "12345678",
        phoneNumber: "987654321",
        email: "jperez@email.com",
        balance: 500.00,
        currency: "PEN",
        associatedDebitCard: "5412751234567890",
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("653000000000000000000002"),
        documentNumber: "87654321",
        phoneNumber: "912345678",
        email: "mgarcia@email.com",
        balance: 1200.00,
        currency: "PEN",
        associatedDebitCard: "5412759876543210",
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("653000000000000000000003"),
        documentNumber: "11223344",
        phoneNumber: "998877665",
        email: "lrodriguez@email.com",
        balance: 250.00,
        currency: "PEN",
        associatedDebitCard: null,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

// Insert seed data - Yanki transactions
yankiDb.yanki_transactions.insertMany([
    {
        yankiId: ObjectId("653000000000000000000001"),
        transactionType: "SEND",
        amount: 100.00,
        currency: "PEN",
        destinationPhoneNumber: "912345678",
        description: "Pago de almuerzo",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-25"),
        createdAt: new Date("2025-01-25")
    },
    {
        yankiId: ObjectId("653000000000000000000002"),
        transactionType: "RECEIVE",
        amount: 100.00,
        currency: "PEN",
        sourcePhoneNumber: "987654321",
        description: "Pago de almuerzo",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-25"),
        createdAt: new Date("2025-01-25")
    },
    {
        yankiId: ObjectId("653000000000000000000001"),
        transactionType: "RECHARGE",
        amount: 200.00,
        currency: "PEN",
        description: "Recarga desde tarjeta",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-26"),
        createdAt: new Date("2025-01-26")
    },
    {
        yankiId: ObjectId("653000000000000000000003"),
        transactionType: "PAYMENT",
        amount: 50.00,
        currency: "PEN",
        merchantName: "Tienda XYZ",
        description: "Compra en comercio",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-27"),
        createdAt: new Date("2025-01-27")
    }
]);

print('yanki_db initialized with seed data');

// ========================================
// BOOTCOIN_DB
// ========================================
const bootcoinDb = db.getSiblingDB('bootcoin_db');

// Create indexes
bootcoinDb.bootcoin_wallets.createIndex({"documentNumber": 1}, {unique: true});
bootcoinDb.bootcoin_wallets.createIndex({"phoneNumber": 1});
bootcoinDb.bootcoin_wallets.createIndex({"status": 1});

bootcoinDb.exchange_rates.createIndex({"effectiveDate": -1});

bootcoinDb.bootcoin_transactions.createIndex({"transactionNumber": 1}, {unique: true});
bootcoinDb.bootcoin_transactions.createIndex({"buyerWalletId": 1});
bootcoinDb.bootcoin_transactions.createIndex({"sellerWalletId": 1});
bootcoinDb.bootcoin_transactions.createIndex({"status": 1});
bootcoinDb.bootcoin_transactions.createIndex({"transactionDate": -1});

// Insert seed data - Bootcoin wallets
bootcoinDb.bootcoin_wallets.insertMany([
    {
        _id: ObjectId("654000000000000000000001"),
        documentNumber: "12345678",
        phoneNumber: "987654321",
        email: "jperez@email.com",
        bootcoinBalance: 50.00,
        penBalance: 1000.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("654000000000000000000002"),
        documentNumber: "87654321",
        phoneNumber: "912345678",
        email: "mgarcia@email.com",
        bootcoinBalance: 100.00,
        penBalance: 2500.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("654000000000000000000003"),
        documentNumber: "11223344",
        phoneNumber: "998877665",
        email: "lrodriguez@email.com",
        bootcoinBalance: 25.00,
        penBalance: 500.00,
        status: "ACTIVE",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

// Insert exchange rates history
bootcoinDb.exchange_rates.insertMany([
    {
        buyRate: 3.80,
        sellRate: 3.75,
        effectiveDate: new Date(),
        createdAt: new Date()
    },
    {
        buyRate: 3.78,
        sellRate: 3.73,
        effectiveDate: new Date(new Date().setDate(new Date().getDate() - 1)),
        createdAt: new Date(new Date().setDate(new Date().getDate() - 1))
    },
    {
        buyRate: 3.82,
        sellRate: 3.77,
        effectiveDate: new Date(new Date().setDate(new Date().getDate() - 2)),
        createdAt: new Date(new Date().setDate(new Date().getDate() - 2))
    }
]);

// Insert seed data - Bootcoin transactions
bootcoinDb.bootcoin_transactions.insertMany([
    {
        transactionNumber: "BTC20250101000001",
        buyerWalletId: ObjectId("654000000000000000000001"),
        sellerWalletId: ObjectId("654000000000000000000002"),
        bootcoinAmount: 10.00,
        penAmount: 37.50,
        exchangeRate: 3.75,
        transactionType: "BUY",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-24"),
        createdAt: new Date("2025-01-24"),
        updatedAt: new Date("2025-01-24")
    },
    {
        transactionNumber: "BTC20250101000002",
        buyerWalletId: ObjectId("654000000000000000000003"),
        sellerWalletId: ObjectId("654000000000000000000002"),
        bootcoinAmount: 5.00,
        penAmount: 18.75,
        exchangeRate: 3.75,
        transactionType: "BUY",
        status: "COMPLETED",
        transactionDate: new Date("2025-01-25"),
        createdAt: new Date("2025-01-25"),
        updatedAt: new Date("2025-01-25")
    },
    {
        transactionNumber: "BTC20250101000003",
        buyerWalletId: ObjectId("654000000000000000000002"),
        sellerWalletId: ObjectId("654000000000000000000001"),
        bootcoinAmount: 15.00,
        penAmount: 57.00,
        exchangeRate: 3.80,
        transactionType: "SELL",
        status: "PENDING",
        transactionDate: new Date(),
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('bootcoin_db initialized with seed data');

print('MongoDB initialization completed successfully!');