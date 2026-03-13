package com.goAbroad.core.plan.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReorderRequest {
    private List<Long> phaseIds;
    private List<Long> taskIds;
}
