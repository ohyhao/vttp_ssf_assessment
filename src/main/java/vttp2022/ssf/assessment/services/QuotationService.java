package vttp2022.ssf.assessment.services;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;


import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp2022.ssf.assessment.models.Quotation;

@Service
public class QuotationService {

    private static final String URL = "https://quotation.chuklee.com/%s";

    public Optional<Quotation> getQuotations(List<String> items) {

        JsonArrayBuilder Arrbuilder = Json.createArrayBuilder();

        for (String item:items) {
            Arrbuilder.add(item);
        }

        String quoteUrl = UriComponentsBuilder.fromUriString(URL.formatted("quotation")).toUriString();

        RequestEntity<String> req = RequestEntity
            .post(quoteUrl)
            .accept(MediaType.APPLICATION_JSON) 
            .contentType(MediaType.APPLICATION_JSON)
            .body(Arrbuilder.build().toString());

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = null;
        
        try {
            // Throws an exception if status code >= 400
            resp = template.exchange(req, String.class);
        } catch (Exception ex) {
            System.err.printf("Exception: %s\n", ex.getMessage());
            return Optional.empty();
        }

        InputStream is = new ByteArrayInputStream(resp.getBody().getBytes());
        JsonReader r = Json.createReader(is);
        JsonObject q = r.readObject();

        Quotation quote = new Quotation();

        quote.setQuoteId(q.getString("quoteId")); 
        
        JsonArray quoteArr = q.getJsonArray("quotations");

        for (int i = 0; i < quoteArr.size(); i++) {            
        String item = quoteArr.getJsonObject(i).getString("item");
        Double unitPrice = quoteArr.getJsonObject(i).getJsonNumber("unitPrice").doubleValue();
        quote.addQuotation(item, unitPrice.floatValue());
        }

        return Optional.of(quote);
    }

}
