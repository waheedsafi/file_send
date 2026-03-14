package Zkteco.zkteco.domain.iclock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "iclock_terminal_command")
public class TerminalCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_iclock_terminal_command_terminal"))
    private Terminal terminal;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "commit_time", nullable = false)
    private OffsetDateTime commitTime;

    @Column(name = "transfer_time")
    private OffsetDateTime transferTime;

    @Column(name = "return_time")
    private OffsetDateTime returnTime;

    @Column(name = "return_value")
    private Integer returnValue;

    @Column(name = "package")
    private Integer packet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(OffsetDateTime commitTime) {
        this.commitTime = commitTime;
    }

    public OffsetDateTime getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(OffsetDateTime transferTime) {
        this.transferTime = transferTime;
    }

    public OffsetDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(OffsetDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public Integer getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Integer returnValue) {
        this.returnValue = returnValue;
    }

    public Integer getPacket() {
        return packet;
    }

    public void setPacket(Integer packet) {
        this.packet = packet;
    }
}
