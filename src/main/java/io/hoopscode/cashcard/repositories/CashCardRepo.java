package io.hoopscode.cashcard.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import io.hoopscode.cashcard.entities.CashCard;

public interface CashCardRepo extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long>{

    CashCard findByIdAndOwner( Long id, String owner );

    Page<CashCard> findByOwner( String owner, PageRequest pagerequest );

}
