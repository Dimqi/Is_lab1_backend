package com.example.is_lab1.entities.importHistory;

import com.example.is_lab1.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_type", nullable = false)
    private ImportType importType;

    @Column(name = "records_count")
    private Integer recordsCount;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
}