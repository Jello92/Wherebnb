package com.example.wherebnb.dto.host;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostRequestDto {
    private String checkInDate;
    private String checkOutDate;
    private int adultsNum;
    private int childrenNum;
    private boolean infantExist;
    private boolean petExist;
    private String flexibleTripLengths;
    private String month;
}