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

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private BigDecimal balance;

    public enum AccountType {
        DEBIT, CREDIT
    }
}
