package com.ub.gir.web.entity.db1.master;

import com.ub.gir.web.entity.BaseDepAgentID;
import com.ub.gir.web.entity.BaseDepMappingCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="dep_mapping_code")
@Data
@AllArgsConstructor
@EqualsAndHashCode( callSuper = true )
@ToString( callSuper = true )
@SuperBuilder
public class DepMappingCodeDB1Master extends BaseDepMappingCode {

}
