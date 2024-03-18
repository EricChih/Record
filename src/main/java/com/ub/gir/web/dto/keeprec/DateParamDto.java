package com.ub.gir.web.dto.keeprec;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DateParamDto {
    private Date startDate;
    private Date endDate;
}
