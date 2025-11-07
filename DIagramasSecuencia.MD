# Diagramas de Secuencia - Sistema de Microservicios Bancarios

## 1. Auth Service - Login y Autenticación

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AuthService
    participant MongoDB
    participant Redis
    
    Note over Client,Redis: Flujo de Login
    
    Client->>APIGateway: POST /auth/login<br/>{username, password}
    APIGateway->>AuthService: Forward login request
    AuthService->>MongoDB: findByUsername(username)
    MongoDB-->>AuthService: User data
    
    alt User exists and password valid
        AuthService->>AuthService: Validate password<br/>(BCrypt)
        AuthService->>AuthService: Generate JWT Token
        AuthService->>Redis: Store token<br/>(optional cache)
        AuthService-->>APIGateway: {token, user info}
        APIGateway-->>Client: 200 OK + JWT Token
    else Invalid credentials
        AuthService-->>APIGateway: 401 Unauthorized
        APIGateway-->>Client: Invalid credentials
    end
    
    Note over Client,Redis: Flujo de Request Autenticado
    
    Client->>APIGateway: GET /customers<br/>Authorization: Bearer {token}
    APIGateway->>APIGateway: Extract JWT from header
    APIGateway->>Redis: Check token cache
    
    alt Token in cache
        Redis-->>APIGateway: Valid token
    else Token not in cache
        APIGateway->>AuthService: Validate token
        AuthService->>AuthService: Verify JWT signature
        AuthService-->>APIGateway: Token valid
        APIGateway->>Redis: Cache token
    end
    
    APIGateway->>CustomerService: Forward request<br/>with user context
    CustomerService-->>APIGateway: Response
    APIGateway-->>Client: 200 OK + Data
```

## 2. Customer Service - Crear Cliente

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant CustomerService
    participant MongoDB
    participant Kafka
    participant Redis
    
    Client->>APIGateway: POST /customers<br/>{customer data}
    APIGateway->>APIGateway: Validate JWT
    APIGateway->>CustomerService: Forward request
    
    CustomerService->>CustomerService: Validate request data<br/>(Bean Validation)
    
    CustomerService->>MongoDB: findByDocumentNumber()
    MongoDB-->>CustomerService: Check if exists
    
    alt Document already exists
        CustomerService-->>APIGateway: 409 Conflict
        APIGateway-->>Client: Customer already exists
    else Document not exists
        CustomerService->>CustomerService: Apply business rules<br/>(customer type validation)
        CustomerService->>MongoDB: save(customer)
        MongoDB-->>CustomerService: Customer saved
        
        CustomerService->>Redis: Cache customer data<br/>key: customer:{id}
        Redis-->>CustomerService: Cached
        
        CustomerService->>Kafka: Publish CustomerCreatedEvent<br/>topic: customer-events
        Kafka-->>CustomerService: Event published
        
        CustomerService-->>APIGateway: 201 Created + Customer
        APIGateway-->>Client: Customer created successfully
    end
```

## 3. Passive Product Service - Crear Cuenta Bancaria

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant PassiveService
    participant CustomerService
    participant ActiveService
    participant MongoDB
    participant Kafka
    participant Redis
    
    Client->>APIGateway: POST /passive-products<br/>{account data}
    APIGateway->>PassiveService: Forward request
    
    PassiveService->>PassiveService: Validate request
    
    Note over PassiveService: Validate Customer
    PassiveService->>CustomerService: GET /customers/{customerId}
    CustomerService-->>PassiveService: Customer data
    
    PassiveService->>PassiveService: Get customer profile<br/>(PERSONAL/BUSINESS, VIP/PYME)
    
    Note over PassiveService: Business Rules Validation
    
    alt Customer is PERSONAL STANDARD
        PassiveService->>MongoDB: Count accounts by customer<br/>and type (SAVINGS/CHECKING)
        MongoDB-->>PassiveService: Account count
        
        alt Already has account of this type
            PassiveService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: Personal client can only have<br/>one account of this type
        end
    end
    
    alt Customer is VIP or PYME
        PassiveService->>ActiveService: GET /active-products/customer/{id}
        ActiveService-->>PassiveService: Credit cards list
        
        alt No credit card found
            PassiveService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: VIP/PYME requires credit card
        end
    end
    
    alt Customer is BUSINESS
        PassiveService->>PassiveService: Validate product type
        
        alt Product is SAVINGS or FIXED_TERM
            PassiveService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: Business cannot have<br/>savings or fixed term
        end
    end
    
    Note over PassiveService: Create Account
    
    PassiveService->>PassiveService: Generate account number
    PassiveService->>PassiveService: Set initial balance
    PassiveService->>PassiveService: Calculate fees
    
    PassiveService->>MongoDB: save(passiveProduct)
    MongoDB-->>PassiveService: Account created
    
    PassiveService->>Redis: Cache account<br/>key: account:{id}
    
    PassiveService->>Kafka: Publish AccountCreatedEvent
    
    PassiveService-->>APIGateway: 201 Created + Account
    APIGateway-->>Client: Account created successfully
```

## 4. Active Product Service - Crear Crédito

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant ActiveService
    participant CustomerService
    participant MongoDB
    participant Kafka
    
    Client->>APIGateway: POST /active-products<br/>{credit data}
    APIGateway->>ActiveService: Forward request
    
    ActiveService->>ActiveService: Validate request
    
    ActiveService->>CustomerService: GET /customers/{customerId}
    CustomerService-->>ActiveService: Customer data
    
    alt Customer is PERSONAL
        ActiveService->>MongoDB: Count personal loans<br/>by customer
        MongoDB-->>ActiveService: Loan count
        
        alt Already has personal loan
            ActiveService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: Personal client can only<br/>have one personal loan
        end
    end
    
    ActiveService->>ActiveService: Calculate credit limit
    ActiveService->>ActiveService: Set interest rate
    ActiveService->>ActiveService: Initialize balances
    
    ActiveService->>MongoDB: save(activeProduct)
    MongoDB-->>ActiveService: Credit created
    
    ActiveService->>Kafka: Publish CreditCreatedEvent
    
    ActiveService-->>APIGateway: 201 Created + Credit
    APIGateway-->>Client: Credit created successfully
```

## 5. Transaction Service - Realizar Depósito

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant TransactionService
    participant PassiveService
    participant MongoDB as TransactionDB
    participant Kafka
    
    Client->>APIGateway: POST /transactions/deposit<br/>{productId, amount}
    APIGateway->>TransactionService: Forward request
    
    TransactionService->>TransactionService: Validate amount > 0
    
    Note over TransactionService: Get Account Details
    TransactionService->>PassiveService: GET /passive-products/{id}
    PassiveService-->>TransactionService: Account details
    
    TransactionService->>TransactionService: Check account status<br/>(ACTIVE)
    
    alt Account is BLOCKED or INACTIVE
        TransactionService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Account not available
    end
    
    Note over TransactionService: Check Transaction Limits
    TransactionService->>TransactionService: Check monthly transactions<br/>vs max free transactions
    
    alt Exceeds free limit
        TransactionService->>TransactionService: Calculate commission
    else Within free limit
        TransactionService->>TransactionService: Commission = 0
    end
    
    Note over TransactionService: Create Transaction
    TransactionService->>TransactionService: Generate transaction number
    TransactionService->>TransactionService: Record balance before
    TransactionService->>TransactionService: Calculate balance after
    
    TransactionService->>MongoDB: save(transaction)<br/>status: PENDING
    MongoDB-->>TransactionService: Transaction saved
    
    Note over TransactionService: Update Account Balance
    TransactionService->>PassiveService: PUT /passive-products/{id}/balance<br/>{newBalance}
    
    alt Balance updated successfully
        PassiveService-->>TransactionService: Balance updated
        TransactionService->>MongoDB: update transaction<br/>status: COMPLETED
        TransactionService->>Kafka: Publish TransactionCompletedEvent
        
        alt Commission charged
            TransactionService->>Kafka: Publish CommissionChargedEvent
        end
        
        TransactionService-->>APIGateway: 200 OK + Transaction
        APIGateway-->>Client: Deposit successful
    else Balance update failed
        TransactionService->>MongoDB: update transaction<br/>status: FAILED
        TransactionService->>Kafka: Publish TransactionFailedEvent
        TransactionService-->>APIGateway: 500 Error
        APIGateway-->>Client: Deposit failed
    end
```

## 6. Transaction Service - Realizar Retiro

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant TransactionService
    participant PassiveService
    participant MongoDB
    participant Kafka
    
    Client->>APIGateway: POST /transactions/withdrawal<br/>{productId, amount}
    APIGateway->>TransactionService: Forward request
    
    TransactionService->>PassiveService: GET /passive-products/{id}
    PassiveService-->>TransactionService: Account details
    
    TransactionService->>TransactionService: Validate sufficient balance
    
    alt Insufficient balance
        TransactionService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Insufficient funds
    end
    
    alt Account type is FIXED_TERM
        TransactionService->>TransactionService: Check movement day
        
        alt Not movement day
            TransactionService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: Fixed term only allows<br/>withdrawals on specific day
        end
    end
    
    TransactionService->>TransactionService: Calculate commission
    TransactionService->>TransactionService: Total amount = amount + commission
    
    TransactionService->>MongoDB: save(transaction)<br/>status: PENDING
    
    TransactionService->>PassiveService: PUT /passive-products/{id}/balance<br/>{newBalance}
    PassiveService-->>TransactionService: Balance updated
    
    TransactionService->>MongoDB: update status: COMPLETED
    TransactionService->>Kafka: Publish TransactionCompletedEvent
    
    TransactionService-->>APIGateway: 200 OK
    APIGateway-->>Client: Withdrawal successful
```

## 7. Debit Card Service - Crear Tarjeta de Débito

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant DebitCardService
    participant PassiveService
    participant MongoDB
    participant Kafka
    
    Client->>APIGateway: POST /debit-cards<br/>{customerId, mainAccountId}
    APIGateway->>DebitCardService: Forward request
    
    Note over DebitCardService: Get All Customer Accounts
    DebitCardService->>PassiveService: GET /passive-products/customer/{id}
    PassiveService-->>DebitCardService: List of accounts
    
    alt No accounts found
        DebitCardService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Customer must have at least<br/>one account
    end
    
    DebitCardService->>DebitCardService: Validate main account exists<br/>in customer accounts
    
    alt Main account not found
        DebitCardService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Invalid main account
    end
    
    Note over DebitCardService: Create Debit Card
    DebitCardService->>DebitCardService: Generate card number
    DebitCardService->>DebitCardService: Generate CVV (encrypted)
    DebitCardService->>DebitCardService: Set expiration date<br/>(+3 years)
    DebitCardService->>DebitCardService: Associate all accounts
    DebitCardService->>DebitCardService: Set priority order
    
    DebitCardService->>MongoDB: save(debitCard)
    MongoDB-->>DebitCardService: Card created
    
    DebitCardService->>Kafka: Publish DebitCardCreatedEvent
    
    DebitCardService-->>APIGateway: 201 Created + Card
    APIGateway-->>Client: Debit card created
```

## 8. Debit Card Service - Realizar Pago con Tarjeta

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant DebitCardService
    participant PassiveService
    participant TransactionService
    participant MongoDB
    
    Client->>APIGateway: POST /debit-cards/{id}/payment<br/>{amount}
    APIGateway->>DebitCardService: Forward request
    
    DebitCardService->>MongoDB: findById(cardId)
    MongoDB-->>DebitCardService: Card details
    
    DebitCardService->>DebitCardService: Validate card status<br/>(ACTIVE)
    DebitCardService->>DebitCardService: Validate not expired
    
    Note over DebitCardService: Get Main Account Balance
    DebitCardService->>PassiveService: GET /passive-products/{mainAccountId}/balance
    PassiveService-->>DebitCardService: Main account balance
    
    alt Main account has sufficient balance
        DebitCardService->>TransactionService: POST /transactions/withdrawal<br/>{mainAccountId, amount}
        TransactionService-->>DebitCardService: Transaction completed
        
        DebitCardService-->>APIGateway: 200 OK
        APIGateway-->>Client: Payment successful
    else Insufficient balance in main
        Note over DebitCardService: Try Associated Accounts (in order)
        loop For each associated account
            DebitCardService->>PassiveService: GET /passive-products/{accountId}/balance
            PassiveService-->>DebitCardService: Account balance
            
            alt Account has sufficient balance
                DebitCardService->>TransactionService: POST /transactions/withdrawal<br/>{accountId, amount}
                TransactionService-->>DebitCardService: Transaction completed
                
                DebitCardService-->>APIGateway: 200 OK
                APIGateway-->>Client: Payment successful<br/>(from account {accountId})
            end
        end
        
        alt No account with sufficient balance
            DebitCardService-->>APIGateway: 400 Bad Request
            APIGateway-->>Client: Insufficient funds in all<br/>associated accounts
        end
    end
```

## 9. Debit Card Service - Consultar Últimos Movimientos

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant DebitCardService
    participant PassiveService
    participant TransactionService
    participant MongoDB
    
    Client->>APIGateway: GET /debit-cards/{id}/movements
    APIGateway->>DebitCardService: Forward request
    
    DebitCardService->>MongoDB: findById(cardId)
    MongoDB-->>DebitCardService: Card with associated accounts
    
    Note over DebitCardService: Get transactions from main account
    DebitCardService->>TransactionService: GET /transactions/product/{mainAccountId}<br/>?limit=10&sort=desc
    TransactionService-->>DebitCardService: Main account transactions
    
    DebitCardService->>DebitCardService: Build response with<br/>last 10 movements
    
    DebitCardService-->>APIGateway: 200 OK + Movements
    APIGateway-->>Client: Last 10 movements
```

## 10. Transfer Service - Transferencia Entre Cuentas Propias

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant TransferService
    participant PassiveService
    participant TransactionService
    participant MongoDB
    participant Kafka
    
    Client->>APIGateway: POST /transfers/own-accounts<br/>{sourceAccountId, destinationAccountId, amount}
    APIGateway->>TransferService: Forward request
    
    Note over TransferService: Validate Accounts
    TransferService->>PassiveService: GET /passive-products/{sourceAccountId}
    PassiveService-->>TransferService: Source account
    
    TransferService->>PassiveService: GET /passive-products/{destinationAccountId}
    PassiveService-->>TransferService: Destination account
    
    TransferService->>TransferService: Validate both accounts<br/>belong to same customer
    
    alt Accounts belong to different customers
        TransferService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Use third-party transfer
    end
    
    TransferService->>TransferService: Validate sufficient balance
    
    alt Insufficient balance
        TransferService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Insufficient funds
    end
    
    Note over TransferService: Process Transfer (Saga Pattern)
    TransferService->>TransferService: Generate transfer number
    TransferService->>MongoDB: save(transfer)<br/>status: PENDING
    
    Note over TransferService: Step 1: Withdraw from source
    TransferService->>TransactionService: POST /transactions/withdrawal<br/>{sourceAccountId, amount}
    
    alt Withdrawal successful
        TransactionService-->>TransferService: Transaction completed
        
        Note over TransferService: Step 2: Deposit to destination
        TransferService->>TransactionService: POST /transactions/deposit<br/>{destinationAccountId, amount}
        
        alt Deposit successful
            TransactionService-->>TransferService: Transaction completed
            
            TransferService->>MongoDB: update transfer<br/>status: COMPLETED
            TransferService->>Kafka: Publish TransferCompletedEvent
            
            TransferService-->>APIGateway: 200 OK
            APIGateway-->>Client: Transfer successful
        else Deposit failed
            Note over TransferService: Compensating Transaction
            TransferService->>TransactionService: POST /transactions/deposit<br/>{sourceAccountId, amount}<br/>(Rollback)
            
            TransferService->>MongoDB: update transfer<br/>status: FAILED
            TransferService->>Kafka: Publish TransferFailedEvent
            
            TransferService-->>APIGateway: 500 Error
            APIGateway-->>Client: Transfer failed
        end
    else Withdrawal failed
        TransferService->>MongoDB: update transfer<br/>status: FAILED
        TransferService->>Kafka: Publish TransferFailedEvent
        
        TransferService-->>APIGateway: 500 Error
        APIGateway-->>Client: Transfer failed
    end
```

## 11. Report Service - Generar Reporte Consolidado de Cliente

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant ReportService
    participant CustomerService
    participant PassiveService
    participant ActiveService
    participant TransactionService
    participant DebitCardService
    
    Client->>APIGateway: GET /reports/customer/{id}/consolidated
    APIGateway->>ReportService: Forward request
    
    Note over ReportService: Gather Customer Data
    ReportService->>CustomerService: GET /customers/{id}
    CustomerService-->>ReportService: Customer details
    
    Note over ReportService: Gather Passive Products
    ReportService->>PassiveService: GET /passive-products/customer/{id}
    PassiveService-->>ReportService: List of accounts
    
    Note over ReportService: Gather Active Products
    ReportService->>ActiveService: GET /active-products/customer/{id}
    ActiveService-->>ReportService: List of credits
    
    Note over ReportService: Gather Debit Cards
    ReportService->>DebitCardService: GET /debit-cards/customer/{id}
    DebitCardService-->>ReportService: List of cards
    
    Note over ReportService: Gather Transactions
    ReportService->>TransactionService: GET /transactions/customer/{id}
    TransactionService-->>ReportService: Transaction history
    
    Note over ReportService: Generate Report
    ReportService->>ReportService: Aggregate all data
    ReportService->>ReportService: Calculate totals
    ReportService->>ReportService: Calculate statistics
    ReportService->>ReportService: Generate Excel/PDF
    
    ReportService-->>APIGateway: 200 OK + Report file
    APIGateway-->>Client: Download report
```

## 12. Report Service - Reporte de Promedio Diario de Saldo

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant ReportService
    participant PassiveService
    participant TransactionService
    participant MongoDB
    
    Client->>APIGateway: GET /reports/customer/{id}/daily-average<br/>?month=10&year=2025
    APIGateway->>ReportService: Forward request
    
    ReportService->>PassiveService: GET /passive-products/customer/{id}
    PassiveService-->>ReportService: Customer accounts
    
    loop For each account
        ReportService->>TransactionService: GET /transactions/product/{accountId}<br/>?startDate&endDate
        TransactionService-->>ReportService: Transactions in period
        
        ReportService->>ReportService: Calculate daily balances
        ReportService->>ReportService: Calculate average
    end
    
    ReportService->>ReportService: Aggregate results
    ReportService->>ReportService: Generate report
    
    ReportService-->>APIGateway: 200 OK + Report
    APIGateway-->>Client: Daily average report
```

## 13. Yanki Service - Enviar Pago

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant YankiService
    participant MongoDB
    participant Kafka
    
    Client->>APIGateway: POST /yanki/send<br/>{phoneNumber, recipientPhone, amount}
    APIGateway->>YankiService: Forward request
    
    YankiService->>MongoDB: findByPhoneNumber(senderPhone)
    MongoDB-->>YankiService: Sender wallet
    
    YankiService->>MongoDB: findByPhoneNumber(recipientPhone)
    MongoDB-->>YankiService: Recipient wallet
    
    alt Recipient not found
        YankiService-->>APIGateway: 404 Not Found
        APIGateway-->>Client: Recipient does not have Yanki
    end
    
    YankiService->>YankiService: Validate sender balance
    
    alt Insufficient balance
        YankiService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Insufficient funds
    end
    
    Note over YankiService: Process Payment (Transaction)
    YankiService->>MongoDB: Update sender balance<br/>-amount
    YankiService->>MongoDB: Update recipient balance<br/>+amount
    
    Note over YankiService: Record Transactions
    YankiService->>MongoDB: save sender transaction<br/>type: SEND
    YankiService->>MongoDB: save recipient transaction<br/>type: RECEIVE
    
    YankiService->>Kafka: Publish YankiPaymentEvent
    
    YankiService-->>APIGateway: 200 OK
    APIGateway-->>Client: Payment sent successfully
```

## 14. Yanki Service - Cargar Saldo desde Cuenta Bancaria

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant YankiService
    participant DebitCardService
    participant PassiveService
    participant TransactionService
    participant MongoDB
    
    Client->>APIGateway: POST /yanki/load<br/>{yankiId, amount}
    APIGateway->>YankiService: Forward request
    
    YankiService->>MongoDB: findById(yankiId)
    MongoDB-->>YankiService: Yanki wallet
    
    YankiService->>YankiService: Validate associated debit card
    
    alt No debit card associated
        YankiService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Yanki not associated with<br/>debit card
    end
    
    Note over YankiService: Get Main Account from Debit Card
    YankiService->>DebitCardService: GET /debit-cards/{cardId}
    DebitCardService-->>YankiService: Card with main account
    
    YankiService->>PassiveService: GET /passive-products/{mainAccountId}/balance
    PassiveService-->>YankiService: Account balance
    
    YankiService->>YankiService: Validate sufficient balance
    
    alt Insufficient balance
        YankiService-->>APIGateway: 400 Bad Request
        APIGateway-->>Client: Insufficient funds in account
    end
    
    Note over YankiService: Process Load (Saga)
    YankiService->>TransactionService: POST /transactions/withdrawal<br/>{mainAccountId, amount}
    TransactionService-->>YankiService: Withdrawal successful
    
    YankiService->>MongoDB: Update Yanki balance<br/>+amount
    YankiService->>MongoDB: Save yanki transaction<br/>type: LOAD
    
    YankiService-->>APIGateway: 200 OK
    APIGateway-->>Client: Balance loaded successfully
```

## 15. BootCoin Service - Solicitar Compra de BootCoin

```mermaid
sequenceDiagram
    participant Buyer
    participant APIGateway
    participant BootCoinService
    participant MongoDB
    participant Kafka
    
    Buyer->>APIGateway: POST /bootcoin/transactions/buy-request<br/>{amount, paymentMethod, paymentDetails}
    APIGateway->>BootCoinService: Forward request
    
    BootCoinService->>MongoDB: findById(buyerWalletId)
    MongoDB-->>BootCoinService: Buyer wallet
    
    Note over BootCoinService: Get Current Exchange Rate
    BootCoinService->>MongoDB: getCurrentExchangeRate()
    MongoDB-->>BootCoinService: Exchange rate (buy rate)
    
    BootCoinService->>BootCoinService: Calculate BootCoin amount<br/>= soles amount / buy rate
    
    Note over BootCoinService: Create Buy Request
    BootCoinService->>BootCoinService: Generate transaction number
    BootCoinService->>MongoDB: save(bootCoinTransaction)<br/>status: PENDING
    MongoDB-->>BootCoinService: Transaction saved
    
    BootCoinService->>Kafka: Publish BuyRequestCreatedEvent
    
    BootCoinService-->>APIGateway: 201 Created + Transaction
    APIGateway-->>Buyer: Buy request created<br/>Transaction #: {number}
```

## 16. BootCoin Service - Aceptar y Completar Intercambio

```mermaid
sequenceDiagram
    participant Seller
    participant APIGateway
    participant BootCoinService
    participant YankiService
    participant PassiveService
    participant TransferService
    participant MongoDB
    participant Kafka
    
    Note over Seller: Seller accepts buy request
    Seller->>APIGateway: POST /bootcoin/transactions/{number}/accept<br/>{sellerWalletId}
    APIGateway->>BootCoinService: Forward request
    
    BootCoinService->>MongoDB: findByTransactionNumber(number)
    MongoDB-->>BootCoinService: Transaction details
    
    BootCoinService->>MongoDB: findById(sellerWalletId)
    MongoDB-->>BootCoinService: Seller wallet
    
    BootCoinService->>BootCoinService: Validate seller has<br/>sufficient BootCoins
    
    alt Insufficient BootCoins
        BootCoinService-->>APIGateway: 400 Bad Request
        APIGateway-->>Seller: Insufficient BootCoins
    end
    
    BootCoinService->>MongoDB: update transaction<br/>status: ACCEPTED<br/>sellerWalletId: {id}
    
    BootCoinService->>Kafka: Publish TransactionAcceptedEvent
    
    BootCoinService-->>APIGateway: 200 OK
    APIGateway-->>Seller: Request accepted<br/>Waiting for payment validation
    
    Note over BootCoinService: Complete transaction (triggered by validation)
    BootCoinService->>BootCoinService: Validate payment method
    
    alt Payment method is YANKI
        BootCoinService->>YankiService: Verify Yanki payment<br/>{sellerPhone, amount}
        YankiService-->>BootCoinService: Payment verified
    else Payment method is BANK_TRANSFER
        BootCoinService->>PassiveService: Verify transfer<br/>{accountNumber, amount}
        PassiveService-->>BootCoinService: Transfer verified
    end
    
    Note over BootCoinService: Execute Exchange
    BootCoinService->>MongoDB: Update buyer wallet<br/>+BootCoins
    BootCoinService->>MongoDB: Update seller wallet<br/>-BootCoins
    
    BootCoinService->>MongoDB: update transaction<br/>status: COMPLETED<br/>completedDate: now
    
    BootCoinService->>Kafka: Publish TransactionCompletedEvent
    
    BootCoinService-->>APIGateway: 200 OK
    APIGateway-->>Seller: Transaction completed
```

## 17. Event-Driven Communication - CustomerDeletedEvent

```mermaid
sequenceDiagram
    participant CustomerService
    participant Kafka
    participant PassiveService
    participant ActiveService
    participant DebitCardService
    
    Note over CustomerService: Customer deletion triggered
    CustomerService->>CustomerService: Soft delete customer
    CustomerService->>Kafka: Publish CustomerDeletedEvent<br/>topic: customer-events
    
    Note over Kafka: Event distributed to subscribers
    
    Kafka->>PassiveService: CustomerDeletedEvent
    activate PassiveService
    PassiveService->>PassiveService: Find all accounts<br/>for customerId
    PassiveService->>PassiveService: Block all accounts<br/>status: BLOCKED
    PassiveService->>PassiveService: Log deletion event
    deactivate PassiveService
    
    Kafka->>ActiveService: CustomerDeletedEvent
    activate ActiveService
    ActiveService->>ActiveService: Find all credits<br/>for customerId
    ActiveService->>ActiveService: Block all credits<br/>status: BLOCKED
    ActiveService->>ActiveService: Log deletion event
    deactivate ActiveService
    
    Kafka->>DebitCardService: CustomerDeletedEvent
    activate DebitCardService
    DebitCardService->>DebitCardService: Find all debit cards<br/>for customerId
    DebitCardService->>DebitCardService: Block all cards<br/>status: BLOCKED
    DebitCardService->>DebitCardService: Log deletion event
    deactivate DebitCardService
```

## 18. Circuit Breaker Pattern - Resilience

```mermaid
sequenceDiagram
    participant PassiveService
    participant CircuitBreaker
    participant CustomerService
    
    Note over CircuitBreaker: State: CLOSED (Normal)
    
    PassiveService->>CircuitBreaker: Call CustomerService
    CircuitBreaker->>CustomerService: Forward request
    CustomerService-->>CircuitBreaker: 200 OK
    CircuitBreaker-->>PassiveService: Success
    
    Note over CircuitBreaker: Failures start occurring
    
    loop Multiple failures
        PassiveService->>CircuitBreaker: Call CustomerService
        CircuitBreaker->>CustomerService: Forward request
        CustomerService--xCircuitBreaker: Timeout/Error
        CircuitBreaker->>CircuitBreaker: Record failure
        CircuitBreaker-->>PassiveService: Error
    end
    
    Note over CircuitBreaker: State: OPEN (Failure threshold reached)
    
    PassiveService->>CircuitBreaker: Call CustomerService
    CircuitBreaker-->>PassiveService: Fast fail (no call made)<br/>Return cached/default data
    
    Note over CircuitBreaker: Wait period (e.g., 30 seconds)
    
    Note over CircuitBreaker: State: HALF_OPEN (Testing)
    
    PassiveService->>CircuitBreaker: Call CustomerService
    CircuitBreaker->>CustomerService: Test request
    
    alt Service recovered
        CustomerService-->>CircuitBreaker: 200 OK
        CircuitBreaker->>CircuitBreaker: Reset failure count
        Note over CircuitBreaker: State: CLOSED
        CircuitBreaker-->>PassiveService: Success
    else Service still failing
        CustomerService--xCircuitBreaker: Error
        Note over CircuitBreaker: State: OPEN
        CircuitBreaker-->>PassiveService: Fast fail
    end
```

---

## Leyenda de Símbolos

- `->`: Llamada síncrona
- `-->`: Respuesta
- `->>`: Llamada asíncrona
- `-->>`: Respuesta asíncrona
- `--x`: Error/Fallo
- `-.->`: Llamada opcional/condicional
- `Note`: Comentario explicativo
- `alt/else`: Condición alternativa
- `loop`: Bucle
- `activate/deactivate`: Activación de participante