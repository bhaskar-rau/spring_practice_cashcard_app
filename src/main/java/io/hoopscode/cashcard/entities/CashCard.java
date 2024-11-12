package io.hoopscode.cashcard.entities;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CashCard {

    @Id
    public Long id;

    public Double amount;

    public String owner;
    
}
