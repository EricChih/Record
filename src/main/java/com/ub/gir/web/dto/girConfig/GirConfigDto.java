package com.ub.gir.web.dto.girConfig;

import lombok.Data;

@Data
public class GirConfigDto {
    private int ID;
    private String IDType;
    private String IDCode;
    private String IDName;
    private int IDOrder = 0;
    private String Memo;
}