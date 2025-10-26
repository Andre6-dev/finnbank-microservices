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
authDb.users.insertOne({
    username: "admin",
    password: "$2a$10$slYQmyNdGzTn7ZLJZwjEDuMxJhbQz3HCCq6DZDVqEW9jP0WbWlDLK", // password: admin123
    email: "admin@nttdata.com",
    roles: ["ADMIN", "USER"],
    active: true,
    createdAt: new Date(),
    updatedAt: new Date()
});

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

print('customer_db initialized');

// ========================================
// PASSIVE_PRODUCT_DB
// ========================================
const passiveProductDb = db.getSiblingDB('passive_product_db');

// Create indexes
passiveProductDb.passive_products.createIndex({"accountNumber": 1}, {unique: true});
passiveProductDb.passive_products.createIndex({"customerId": 1});
passiveProductDb.passive_products.createIndex({"productType": 1});
passiveProductDb.passive_products.createIndex({"status": 1});

print('passive_product_db initialized');

// ========================================
// ACTIVE_PRODUCT_DB
// ========================================
const activeProductDb = db.getSiblingDB('active_product_db');

// Create indexes
activeProductDb.active_products.createIndex({"creditNumber": 1}, {unique: true});
activeProductDb.active_products.createIndex({"customerId": 1});
activeProductDb.active_products.createIndex({"productType": 1});
activeProductDb.active_products.createIndex({"status": 1});
activeProductDb.active_products.createIndex({"hasOverdueDebt": 1});

print('active_product_db initialized');

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

print('transaction_db initialized');

// ========================================
// DEBIT_CARD_DB
// ========================================
const debitCardDb = db.getSiblingDB('debit_card_db');

// Create indexes
debitCardDb.debit_cards.createIndex({"cardNumber": 1}, {unique: true});
debitCardDb.debit_cards.createIndex({"customerId": 1});
debitCardDb.debit_cards.createIndex({"mainAccountId": 1});
debitCardDb.debit_cards.createIndex({"cardStatus": 1});

print('debit_card_db initialized');

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

print('transfer_db initialized');

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

print('yanki_db initialized');

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

// Insert initial exchange rate
bootcoinDb.exchange_rates.insertOne({
    buyRate: 3.80,
    sellRate: 3.75,
    effectiveDate: new Date(),
    createdAt: new Date()
});

print('bootcoin_db initialized with seed data');

print('MongoDB initialization completed successfully!');