package com.alja.reservation.controller.dto;

import lombok.Data;
import java.util.List;

@Data
public class AvailabilityResponseDTO {

    private List<String> roomIds;

}
