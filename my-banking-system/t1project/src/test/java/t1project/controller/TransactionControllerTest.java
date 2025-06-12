package t1project.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import t1project.dto.TransactionRequestDTO;
import t1project.model.Transaction;
import t1project.service.TransactionService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getByAccountId_shouldReturnTransactions() {
        Long accountId = 1L;
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        List<Transaction> expected = Arrays.asList(t1, t2);

        when(transactionService.getByAccountId(accountId)).thenReturn(expected);

        List<Transaction> result = transactionController.getByAccountId(accountId);

        assertEquals(2, result.size());
        verify(transactionService).getByAccountId(accountId);
    }

    @Test
    void createTransaction_shouldReturnOk_ifAccepted() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        Transaction tx = new Transaction();
        UUID txId = UUID.randomUUID();
        tx.setTransactionId(txId);
        tx.setStatus(Transaction.Status.ACCEPTED);

        when(transactionService.processNewTransactionRequest(request)).thenReturn(tx);

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("ID: " + txId.toString()));
    }

    @Test
    void createTransaction_shouldReturn403_ifRejected() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        Transaction tx = new Transaction();
        tx.setStatus(Transaction.Status.REJECTED);

        when(transactionService.processNewTransactionRequest(request)).thenReturn(tx);

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Транзакция отклонена"));
    }

    @Test
    void createTransaction_shouldReturn400_onIllegalArgument() {
        TransactionRequestDTO request = new TransactionRequestDTO();

        when(transactionService.processNewTransactionRequest(request)).thenThrow(new IllegalArgumentException("Некорректные данные"));

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Некорректные данные", response.getBody());
    }

    @Test
    void createTransaction_shouldReturn500_onUnexpectedException() {
        TransactionRequestDTO request = new TransactionRequestDTO();

        when(transactionService.processNewTransactionRequest(request)).thenThrow(new RuntimeException("Что-то пошло не так"));

        ResponseEntity<String> response = transactionController.createTransaction(request);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Внутренняя ошибка сервера.", response.getBody());
    }
}
