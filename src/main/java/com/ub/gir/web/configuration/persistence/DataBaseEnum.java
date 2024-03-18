/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.persistence;


import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Getter
@RequiredArgsConstructor
public enum DataBaseEnum {

    MYSQL (1,DataSourceCatalog.MYSQL),
    POSTGRESQL(2,DataSourceCatalog.POSTGRESQL),
    MSSQL(3,DataSourceCatalog.MSSQL),
    ORACLE(4,DataSourceCatalog.ORACLE),
    ;

    private final int dataBaseCode;

    private final String dataBaseName;

    public static DataBaseEnum findEnum(int dataBaseCode) {
        return Stream.of(values())
                .filter(e -> e.dataBaseCode == dataBaseCode)
                .findFirst()
                .orElse(null);
    }

    public static DataBaseEnum findEnum(String enumName) {
        return Stream.of(values())
                .filter(e -> e.name().equalsIgnoreCase(enumName))
                .findFirst()
                .orElse(null);
    }

    public static boolean exists(String enumName) {
        return Stream.of(values())
                .anyMatch(e -> e.name().equals(enumName));
    }

}
