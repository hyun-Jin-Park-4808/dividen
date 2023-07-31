package com.example.dividend.persist.entity;

import com.example.dividend.model.Dividend;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "DIVIDEND")
@Getter
@ToString
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = { "companyId", "date" }
                )
        }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId; // 배당금 정보가 어떤 회사의 정보인지 알기 위함.

    private LocalDateTime date;

    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend) {
        this.companyId = companyId; // 생성자 메서드에서 인자로 받은 companyId를 this.companyId 에 대입
        this.date = dividend.getDate();
        this.dividend = dividend.getDividend();
    }
}
