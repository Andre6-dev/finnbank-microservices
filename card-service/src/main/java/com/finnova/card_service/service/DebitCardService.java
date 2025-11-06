package com.finnova.card_service.service;

import com.finnova.card_service.client.ProductClient;
import com.finnova.card_service.client.TransactionClient;
import com.finnova.card_service.event.publisher.DebitCardEventPublisher;
import com.finnova.card_service.exception.CardNotFoundException;
import com.finnova.card_service.exception.InsufficientBalanceException;
import com.finnova.card_service.exception.InvalidOperationException;
import com.finnova.card_service.model.dto.AssociateAccountRequest;
import com.finnova.card_service.model.dto.CreateDebitCardRequest;
import com.finnova.card_service.model.dto.PaymentRequest;
import com.finnova.card_service.model.dto.TransactionDto;
import com.finnova.card_service.model.dto.WithdrawalRequest;
import com.finnova.card_service.model.entity.DebitCard;
import com.finnova.card_service.model.enums.CardStatus;
import com.finnova.card_service.repository.DebitCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final ProductClient productClient;
    private final TransactionClient transactionClient;
    private final DebitCardEventPublisher eventPublisher;

    // ========== CREATE ==========

    /**
     * Create a new debit card
     */
    public Mono<DebitCard> createDebitCard(CreateDebitCardRequest request) {
        log.info("Creating debit card for customer: {}", request.getCustomerId());

        String cardNumber = generateCardNumber();

        // Verify card number is unique
        return debitCardRepository.existsByCardNumber(cardNumber)
                .flatMap(exists -> {
                    if (exists) {
                        // If by chance the number exists, regenerate
                        return createDebitCard(request);
                    }

                    DebitCard debitCard = DebitCard.builder()
                            .cardNumber(cardNumber)
                            .customerId(request.getCustomerId())
                            .cardType(request.getCardType())
                            .cvv(generateCVV())
                            .expirationDate(LocalDate.now().plusYears(5))
                            .cardHolderName(request.getCardholderName())
                            .associatedAccountIds(new ArrayList<>())
                            .status(CardStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return debitCardRepository.save(debitCard)
                            .flatMap(savedCard ->
                                    eventPublisher.publishCardCreated(savedCard)
                                            .thenReturn(savedCard)
                            );
                });
    }

    // ========== ASSOCIATE ACCOUNTS ==========

    /**
     * Associate an account to the debit card
     */
    public Mono<DebitCard> associateAccount(String cardId, AssociateAccountRequest request) {
        log.info("Associating account {} to card {}", request.getAccountId(), cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    // Validate account exists
                    return productClient.getAccount(request.getAccountId())
                            .flatMap(account -> {
                                // Validate account belongs to same customer
                                if (!account.getCustomerId().equals(card.getCustomerId())) {
                                    return Mono.error(new InvalidOperationException(
                                            "Account does not belong to the card owner"));
                                }

                                // Check if already associated
                                if (card.getAssociatedAccountIds().contains(request.getAccountId())) {
                                    return Mono.just(card);
                                }

                                // Add account to associated list
                                card.getAssociatedAccountIds().add(request.getAccountId());

                                // If no main account, set this as main
                                if (card.getMainAccountId() == null) {
                                    card.setMainAccountId(request.getAccountId());
                                }

                                card.setUpdatedAt(LocalDateTime.now());
                                return debitCardRepository.save(card)
                                        .flatMap(updatedCard ->
                                                eventPublisher.publishAccountAssociated(updatedCard, request.getAccountId())
                                                        .thenReturn(updatedCard)
                                        );
                            });
                });
    }

    /**
     * Set main account
     */
    public Mono<DebitCard> setMainAccount(String cardId, String accountId) {
        log.info("Setting main account {} for card {}", accountId, cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    if (!card.getAssociatedAccountIds().contains(accountId)) {
                        return Mono.error(new InvalidOperationException(
                                "Account is not associated with this card"));
                    }

                    card.setMainAccountId(accountId);
                    card.setUpdatedAt(LocalDateTime.now());
                    return debitCardRepository.save(card);
                });
    }

    // ========== PAYMENT ==========

    /**
     * Make payment with debit card
     * If main account has insufficient balance, try associated accounts in order
     */
    public Mono<TransactionDto> makePayment(String cardId, PaymentRequest request) {
        log.info("Processing payment for card: {}", cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    // Validate card is active
                    if (card.getStatus() != CardStatus.ACTIVE) {
                        return Mono.error(new InvalidOperationException("Card is not active"));
                    }

                    // Validate has associated accounts
                    if (card.getMainAccountId() == null) {
                        return Mono.error(new InvalidOperationException("No main account set"));
                    }

                    // Find account with sufficient balance
                    return findAccountWithBalance(card, request.getAmount())
                            .flatMap(accountId -> {
                                String description = buildDescription(request.getDescription(),
                                        request.getMerchantName(), "Payment");
                                return transactionClient.withdrawal(accountId, request.getAmount(), description);
                            });
                });
    }

    // ========== WITHDRAWAL ==========

    /**
     * Make withdrawal with debit card
     */
    public Mono<TransactionDto> makeWithdrawal(String cardId, WithdrawalRequest request) {
        log.info("Processing withdrawal for card: {}", cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    // Validate card is active
                    if (card.getStatus() != CardStatus.ACTIVE) {
                        return Mono.error(new InvalidOperationException("Card is not active"));
                    }

                    // Validate has associated accounts
                    if (card.getMainAccountId() == null) {
                        return Mono.error(new InvalidOperationException("No main account set"));
                    }

                    // Find account with sufficient balance
                    return findAccountWithBalance(card, request.getAmount())
                            .flatMap(accountId -> {
                                String description = request.getDescription() != null
                                        ? request.getDescription()
                                        : "ATM withdrawal";
                                return transactionClient.withdrawal(accountId, request.getAmount(), description);
                            });
                });
    }

    // ========== DEPOSIT ==========

    /**
     * Make deposit to debit card main account
     */
    public Mono<TransactionDto> makeDeposit(String cardId, com.finnova.card_service.model.dto.DepositRequest request) {
        log.info("Processing deposit for card: {}", cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    // Validate card is active
                    if (card.getStatus() != CardStatus.ACTIVE) {
                        return Mono.error(new InvalidOperationException("Card is not active"));
                    }

                    // Validate has associated accounts
                    if (card.getMainAccountId() == null) {
                        return Mono.error(new InvalidOperationException("No main account set"));
                    }

                    // Deposit to main account
                    String description = request.getDescription() != null
                            ? request.getDescription()
                            : "Deposit via debit card";
                    return transactionClient.deposit(card.getMainAccountId(), request.getAmount(), description);
                });
    }

    // ========== QUERIES ==========

    /**
     * Get main account balance
     */
    public Mono<BigDecimal> getMainAccountBalance(String cardId) {
        log.info("Fetching main account balance for card: {}", cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMap(card -> {
                    if (card.getMainAccountId() == null) {
                        return Mono.error(new InvalidOperationException("No main account set"));
                    }
                    return productClient.getAccountBalance(card.getMainAccountId());
                });
    }

    /**
     * Get last 10 movements from all associated accounts
     */
    public Flux<TransactionDto> getLast10Movements(String cardId) {
        log.info("Fetching last 10 movements for card: {}", cardId);

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")))
                .flatMapMany(card -> {
                    if (card.getAssociatedAccountIds().isEmpty()) {
                        return Flux.empty();
                    }

                    // Get transactions from all associated accounts
                    return Flux.fromIterable(card.getAssociatedAccountIds())
                            .flatMap(transactionClient::getLast10Movements)
                            .sort((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                            .take(10);
                });
    }

    /**
     * Get debit card by ID
     */
    public Mono<DebitCard> getDebitCardById(String id) {
        return debitCardRepository.findById(id)
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")));
    }

    /**
     * Get all debit cards for a customer
     */
    public Flux<DebitCard> getDebitCardsByCustomer(String customerId) {
        return debitCardRepository.findByCustomerId(customerId);
    }

    /**
     * Get all debit cards
     */
    public Flux<DebitCard> getAllDebitCards() {
        return debitCardRepository.findAll();
    }

    // ========== UPDATE & DELETE ==========

    /**
     * Update debit card
     */
    public Mono<DebitCard> updateDebitCard(String id, DebitCard debitCard) {
        log.info("Updating debit card: {}", id);
        return debitCardRepository.findById(id)
                .flatMap(existingCard -> {
                    // Update only allowed fields
                    existingCard.setCardHolderName(debitCard.getCardHolderName());
                    existingCard.setStatus(debitCard.getStatus());
                    existingCard.setUpdatedAt(LocalDateTime.now());
                    return debitCardRepository.save(existingCard);
                })
                .switchIfEmpty(Mono.error(new CardNotFoundException("Debit card not found")));
    }

    /**
     * Delete (cancel) debit card
     */
    public Mono<Void> deleteDebitCard(String id) {
        log.info("Cancelling debit card: {}", id);
        return debitCardRepository.findById(id)
                .flatMap(card -> {
                    card.setStatus(CardStatus.CANCELLED);
                    card.setUpdatedAt(LocalDateTime.now());
                    return debitCardRepository.save(card);
                })
                .then();
    }

    // ========== HELPER METHODS ==========

    /**
     * Find account with sufficient balance trying main account first, then associated accounts
     */
    private Mono<String> findAccountWithBalance(DebitCard card, BigDecimal amount) {
        // Try main account first
        return productClient.getAccountBalance(card.getMainAccountId())
                .flatMap(balance -> {
                    if (balance.compareTo(amount) >= 0) {
                        return Mono.just(card.getMainAccountId());
                    }

                    // Try other associated accounts in order
                    return Flux.fromIterable(card.getAssociatedAccountIds())
                            .filter(accountId -> !accountId.equals(card.getMainAccountId()))
                            .flatMap(accountId ->
                                    productClient.getAccountBalance(accountId)
                                            .map(bal -> new AccountBalance(accountId, bal))
                            )
                            .filter(ab -> ab.balance.compareTo(amount) >= 0)
                            .next()
                            .map(ab -> ab.accountId)
                            .switchIfEmpty(Mono.error(new InsufficientBalanceException(
                                    "Insufficient balance in all associated accounts")));
                });
    }

    /**
     * Build transaction description
     */
    private String buildDescription(String description, String merchantName, String type) {
        StringBuilder sb = new StringBuilder(type + " with debit card");
        if (merchantName != null && !merchantName.isEmpty()) {
            sb.append(" - Merchant: ").append(merchantName);
        }
        if (description != null && !description.isEmpty()) {
            sb.append(" - ").append(description);
        }
        return sb.toString();
    }

    /**
     * Generate card number (Visa format: 4...)
     */
    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder("4"); // Visa
        for (int i = 0; i < 15; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    /**
     * Generate CVV
     */
    private String generateCVV() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    // Helper class for account balance
    @lombok.AllArgsConstructor
    private static class AccountBalance {
        String accountId;
        BigDecimal balance;
    }
}
