package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
@Entity
@Table(name="iclock_biodata",
        uniqueConstraints = @UniqueConstraint(
                name="iclock_biodata_unique_tpl",
                columnNames={"employee_id","bio_no","bio_index","bio_type","bio_format","major_ver"}
        ))
@Getter @Setter @NoArgsConstructor
public class IclockBiodata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employee_id;

    @Lob
    private String bio_tmp;

    private Integer bio_no = 0;
    private Integer bio_index = 0;
    private Integer bio_type;

    private String major_ver;
    private String minor_ver;

    private Integer bio_format = 0;
    private Integer valid = 1;
    private Integer duress = 0;

    private LocalDateTime update_time;
    private String sn;
}