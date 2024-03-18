package com.ub.gir.web.dto.shared;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class QueryDetails {
    private String query;
    private Map<String, Object> parameters;
}
