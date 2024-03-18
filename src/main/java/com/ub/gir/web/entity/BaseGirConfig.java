package com.ub.gir.web.entity;


import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public abstract class BaseGirConfig {

    @Id
    private int ID;

    private String IDType;
    private String IDCode;
    private String IDName;
    private int IDOrder = 0;
    private String Memo;

    @Override
    public String toString() {
        return "girconfig data: [ID=" + ID + ", IDType=" + IDType + ", IDCode=" + IDCode + ", IDName=" +
                IDName + ", IDOrder=" + IDOrder + ", Memo=" + Memo + "]";
    }

}
