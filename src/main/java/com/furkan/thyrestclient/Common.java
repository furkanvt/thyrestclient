package com.furkan.thyrestclient;

import java.util.List;

import com.furkan.thyrestclient.models.data.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestTemplate;

/**
 * Common
 */

@ControllerAdvice
public class Common {

    @Autowired
    private RestTemplate restTemplate;

    @ModelAttribute
    public void sharedData(Model model) {
        
        ResponseEntity<List<Page>> response = restTemplate.exchange("http://localhost:8080/pages/all", 
                                                                    HttpMethod.GET, 
                                                                    null, 
                                                                    new ParameterizedTypeReference<List<Page>>() {});
        
        List<Page> pages = response.getBody();

        model.addAttribute("cpages", pages);
    }
}