package com.ub.gir.web.entity.db1.master;

import com.ub.gir.web.entity.BaseDepAgentID;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="dep_agentid")
@Data
@AllArgsConstructor
@EqualsAndHashCode( callSuper = true )
@ToString( callSuper = true )
@SuperBuilder
public class DepAgentIDDB1Master extends BaseDepAgentID {

}
