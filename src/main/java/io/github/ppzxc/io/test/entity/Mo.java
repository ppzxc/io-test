package io.github.ppzxc.io.test.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "msg_id", nullable = false)
    private String msgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MoType type;

    @Column(name = "is_autoreply", nullable = false)
    private boolean autoreply;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_autoreply")
    private MoType typeAutoreply;

    @Column(name = "`from`", nullable = false)
    private String from;

    @Column(name = "from_telco", nullable = false)
    private String fromTelco;

    @Column(name = "`to`", nullable = false)
    private String to;

    @Column(name = "to_telco", nullable = false)
    private String toTelco;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "body_autoreply", columnDefinition = "text")
    private String bodyAutoreply;

    @Column(name = "attach_count", nullable = false)
    private int attachCount;

    @Column(name = "forward_count", nullable = false)
    private int forwardCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "cdr", nullable = false)
    private CdrStatus cdr;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "agentyn")
    private String agentyn;

    @Column(name = "agentdate")
    private LocalDateTime agentdate;
}
