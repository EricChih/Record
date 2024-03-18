package com.ub.gir.web.entity.db1.master;


import javax.persistence.Entity;
import javax.persistence.Table;

import com.ub.gir.web.entity.BaseGirConfig;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "girconfig")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class GirConfigDB1Master extends BaseGirConfig {

}
