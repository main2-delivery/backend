package com.main2.delivery.owner.dto.menu;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class MenuFlagRequest {
    
    @NotNull(message = "값을 입력해주세요")
    private Boolean value;
}
