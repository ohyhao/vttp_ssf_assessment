package vttp2022.ssf.assessment.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import vttp2022.ssf.assessment.models.Quotation;
import vttp2022.ssf.assessment.services.QuotationService;

@RestController
@RequestMapping(path="/api/po")
public class PurchaseOrderRestController {

    @Autowired
    private QuotationService QuoteSvc;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPurchaseOrder(@RequestBody String payload) {

        InputStream is = new ByteArrayInputStream(payload.getBytes()); 
        JsonReader r = Json.createReader(is);
        JsonObject o = r.readObject();
            
        JsonArray itemsArr = o.getJsonArray("lineItems");
        Map<String, Integer> itemsMap = new HashMap<>();

        for (int i = 0; i < itemsArr.size(); i++) {            
        String item = itemsArr.getJsonObject(i).getString("item");
        Integer quantity = itemsArr.getJsonObject(i).getInt("quantity");
        itemsMap.put(item, quantity);
        }

        System.out.println(">>>>> items: " + itemsMap);
       
        ArrayList<String> keyList = new ArrayList<String>(itemsMap.keySet());

        Quotation quotation = QuoteSvc.getQuotations(keyList).orElse(null);
        
        float total = 0;
        for(Entry<String, Integer> entry : itemsMap.entrySet()) {
            Float unitPrice = quotation.getQuotation(entry.getKey());
            total += entry.getValue() * unitPrice;
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();

            builder  
            .add("invoiceId", quotation.getQuoteId())
            .add("name", o.getString("name"))
            .add("total", total);

    
        return ResponseEntity.ok().body(builder.build().toString());    
    }
}

