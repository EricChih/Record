package com.ub.gir.web.entity.db1.master;


import javax.persistence.Entity;
import javax.persistence.Table;

import com.ub.gir.web.entity.BaseDep;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "dep")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class DepDB1Master extends BaseDep {

}
