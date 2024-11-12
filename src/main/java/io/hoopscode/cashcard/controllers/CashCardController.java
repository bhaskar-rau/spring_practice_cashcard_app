package io.hoopscode.cashcard.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.hoopscode.cashcard.entities.CashCard;
import io.hoopscode.cashcard.repositories.CashCardRepo;

@RestController
@RequestMapping( "/cashcards" )
public class CashCardController {

    @Autowired
    CashCardRepo cashCardRepo;

    @GetMapping( "/{requestedId}" )
    public ResponseEntity<CashCard> getInstance( @PathVariable Long requestedId, Principal principal ) {

        Optional<CashCard> cashCard = Optional.ofNullable( cashCardRepo.findByIdAndOwner( requestedId, principal.getName() ) );
        if( cashCard.isPresent() ) {
            return ResponseEntity.ok( cashCard.get() );
        }
        else {
            return ResponseEntity.notFound().build();
        }
          
    }

    @PostMapping
    public ResponseEntity<Void> createInstance( @RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal ) {

        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.getAmount(), principal.getName() );
        CashCard savedCashCard = cashCardRepo.save( cashCardWithOwner );
        URI locationOfNewCashCard = ucb.path( "cashcards/{id}" ).buildAndExpand( savedCashCard.getId() ).toUri();
        return ResponseEntity.created( locationOfNewCashCard ).build();
        
    }

    @GetMapping
    public ResponseEntity<List<CashCard>> getInstances( Pageable pageable, Principal principal ) {

        Page<CashCard> page = cashCardRepo.findByOwner( principal.getName(),PageRequest.of( pageable.getPageNumber(),pageable.getPageSize(),pageable.getSortOr( Sort.by( Sort.Direction.ASC, "amount" ) ) ) );
        return ResponseEntity.ok( page.getContent() );
        
    }



}
