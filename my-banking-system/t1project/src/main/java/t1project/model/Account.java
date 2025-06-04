package t1project.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false, unique = true)
    private UUID accountId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    public enum AccountType {
        DEBIT, CREDIT
    }

    public enum Status {
        ARRESTED, BLOCKED, CLOSED, OPEN
    }
}
