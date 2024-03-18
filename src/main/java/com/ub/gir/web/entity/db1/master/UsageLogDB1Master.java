package com.ub.gir.web.entity.db1.master;


import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import com.ub.gir.web.entity.BaseUsageLog;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


//此Class 為ORM的Entity Bean會和DB的Table資料相呼應
@Entity
@Table(name = "usagelog")
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class UsageLogDB1Master extends BaseUsageLog {

}
