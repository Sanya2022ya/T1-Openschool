package t1project.service;

import com.example.transaction_common_dto.ClientAccountCheckResponseDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import t1project.dto.TransactionRequestDTO;
import t1project.model.Account;
import t1project.model.Transaction;
import t1project.repository.AccountRepository;
import t1project.repository.ClientRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceIntegrationTest {

    private WireMockServer wireMockServer;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeAll
    void setup() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();

        wireMockServer.stubFor(post(urlEqualTo("/api/client-check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                              "status": "ACTIVE",
                              "info": "Test client active"
                            }
                        """)));
    }

    @AfterAll
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void shouldCreateTransaction_whenClientIsActive() {
        // Arrange
        UUID clientId = UUID.randomUUID();

        Account account = new Account();
        account.setId(1L);
        account.setClientId(clientId);
        account.setBalance(BigDecimal.valueOf(5000));
        account.setStatus(Account.Status.OPEN);
        account.setType(Account.AccountType.DEBIT);
        accountRepository.save(account);

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId(account.getId());
        request.setAmount(1000.0);

        // Act
        Transaction transaction = transactionService.processNewTransactionRequest(request);

        // Assert
        assertNotNull(transaction);
        assertEquals(Transaction.Status.REQUESTED, transaction.getStatus());
        assertEquals(account.getId(), transaction.getAccountId());
    }
    @Test
    void shouldRejectTransaction_whenClientIsBlocked() {
        UUID clientId = UUID.randomUUID();

        Account account = new Account();
        account.setId(3L);
        account.setClientId(clientId);
        account.setBalance(BigDecimal.valueOf(3000));
        account.setStatus(Account.Status.OPEN);
        account.setType(Account.AccountType.DEBIT);
        accountRepository.save(account);

        wireMockServer.stubFor(post(urlEqualTo("/api/client-check"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                        {
                          "status": "BLOCKED",
                          "info": "Client is blocked"
                        }
                    """)));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId(account.getId());
        request.setAmount(500.0);

        // Act
        Transaction transaction = transactionService.processNewTransactionRequest(request);

        // Assert
        assertNotNull(transaction);
        assertEquals(Transaction.Status.REJECTED, transaction.getStatus());
        assertEquals(account.getId(), transaction.getAccountId());
    }
    @Test
    void shouldRejectTransaction_whenClientCheckServiceFails() {
        // Arrange
        wireMockServer.stubFor(post(urlEqualTo("/api/client-check"))
                .willReturn(serverError())); // 500

        UUID clientId = UUID.randomUUID();

        Account account = new Account();
        account.setClientId(clientId);
        account.setBalance(BigDecimal.valueOf(3000));
        account.setStatus(Account.Status.OPEN);
        account.setType(Account.AccountType.DEBIT);
        accountRepository.save(account);

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId(account.getId());
        request.setAmount(700.0);

        // Act
        Transaction transaction = transactionService.processNewTransactionRequest(request);

        // Assert
        assertNotNull(transaction);
        assertEquals(Transaction.Status.REJECTED, transaction.getStatus(), "Transaction should be rejected due to external service failure");
    }




}
