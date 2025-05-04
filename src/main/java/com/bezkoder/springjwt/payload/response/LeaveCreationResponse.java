package com.bezkoder.springjwt.payload.response;

// LeaveCreationResponse.java

import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveCreationResponse {
    private LeaveDTO leave;
    private String predictionMessage;
}

