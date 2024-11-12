package io.hoopscode.cashcard;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.hoopscode.cashcard.entities.CashCard;
import net.minidev.json.JSONArray;

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity( "/cashcards/99", String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.OK );
		DocumentContext documentcontext = JsonPath.parse( response.getBody() );
		Number id = documentcontext.read( "@.id" );
		Double amount = documentcontext.read( "@.amount" );
		assertThat( id ).isEqualTo( 99 );
		assertThat( amount ).isEqualTo( 123.45 );

	}

	@Test
	void shouldReturnACashCardWithUnknownId() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity( "/cashcards/1000", String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.NOT_FOUND);
		assertThat( response.getBody() ).isBlank();
		
	}

	@Test
	void shouldCreateANewCashCard() {

		CashCard cashCard = new CashCard( null, 200.33, null );
		ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1","abc123").postForEntity( "/cashcards", cashCard, Void.class );
		assertThat( createResponse.getStatusCode() ).isEqualTo( HttpStatus.CREATED );

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1","abc123").getForEntity( locationOfNewCashCard, String.class );
		assertThat( getResponse.getStatusCode() ).isEqualTo( HttpStatus.OK );
		DocumentContext documentContext = JsonPath.parse( getResponse.getBody() );
		Number id = documentContext.read( "@.id" );
		Double amount = documentContext.read( "@.amount" );
		assertThat( id ).isNotNull();
		assertThat( amount ).isEqualTo( 200.33 );

	}

	@Test
	void shouldReturnAllCashCardsWhenListIsRequested() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards", String.class);
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.OK );
		DocumentContext documentContext = JsonPath.parse( response.getBody() );
		int cashCardCount = documentContext.read( "$.length()" );
		assertThat( cashCardCount ).isEqualTo( 4 );
		JSONArray ids = documentContext.read( "$..id" );
		assertThat( ids ).containsExactlyInAnyOrder(  99, 100, 101, 1);
		JSONArray amounts = documentContext.read( "$..amount" );
		assertThat( amounts ).containsExactlyInAnyOrder(  123.45, 1.00, 150.00, 200.33 );

	}

	@Test
	void shouldReturnAPageOfCashCards() {

		ResponseEntity<String> pageResponse = restTemplate.withBasicAuth("sarah1","abc123").getForEntity( "/cashcards?page=0&size=1", String.class );
		DocumentContext documentContext = JsonPath.parse( pageResponse.getBody() );
		JSONArray page = documentContext.read( "$[*]");
		assertThat( page.size() ).isEqualTo(1);

	}

	@Test
	void shouldReturnASortedPageOfCashcards() {

		ResponseEntity<String> sortResponse = restTemplate.withBasicAuth("sarah1","abc123").getForEntity( "/cashcards?page=0&size=1&sort=amount,desc",String.class );
		DocumentContext documentContext = JsonPath.parse( sortResponse.getBody() );
		JSONArray sortedPage = documentContext.read( "$[*]" );
		assertThat( sortedPage.size() ).isEqualTo( 1 );
		double amount = documentContext.read( "$[0].amount");
		assertThat( amount ).isEqualTo( 150.00 );
	}

	@Test
	void shouldReturnSortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {

		ResponseEntity<String> defaultSortedResponse = restTemplate.withBasicAuth("sarah1","abc123").getForEntity("/cashcards", String.class);
		assertThat( defaultSortedResponse.getStatusCode() ).isEqualTo( HttpStatus.OK );
		DocumentContext documentContext = JsonPath.parse( defaultSortedResponse.getBody() );
		JSONArray page = documentContext.read( "$[*]" );
		assertThat( page.size() ).isEqualTo( 4 );
		JSONArray amounts = documentContext.read( "$..amount" );
		assertThat( amounts ).containsExactly(1.00, 123.45, 150.00 , 200.33 );

	}

	@Test
	void shouldNotReturnCashCardWhenUsingBadCredentials() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("badname", "bad123" ).getForEntity("/cashcards/99", String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.UNAUTHORIZED );
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("hank-owns-no-cards","qrs456").getForEntity("/cashcards/99",String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.FORBIDDEN );
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1","abc123" ).getForEntity("/cashcards/10", String.class );
		assertThat( response.getStatusCode() ).isEqualTo( HttpStatus.NOT_FOUND );
	}

}
