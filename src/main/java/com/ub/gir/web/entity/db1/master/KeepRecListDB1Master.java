package com.ub.gir.web.entity.db1.master;


import javax.persistence.Entity;
import javax.persistence.Table;

import com.ub.gir.web.entity.BaseKeepRecList;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


//此Class 為ORM的Entity Bean會和DB的cfg_person Table資料相呼應
@Entity
@Table(name = "keepreclist")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class KeepRecListDB1Master extends BaseKeepRecList {

}
