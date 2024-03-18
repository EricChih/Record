package com.ub.gir.web.entity.db1.master;


import javax.persistence.Entity;
import javax.persistence.Table;

import com.ub.gir.web.entity.BaseSupAuditList;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


//此Class 為ORM的Entity Bean會和DB的sup_auditlist Table資料相呼應
@Entity
@Table(name = "sup_auditlist")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class SupAuditListDB1Master extends BaseSupAuditList {

}
