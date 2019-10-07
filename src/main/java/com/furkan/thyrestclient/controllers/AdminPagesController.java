package com.furkan.thyrestclient.controllers;

import java.util.List;

import javax.validation.Valid;

import com.furkan.thyrestclient.models.PageRepository;
import com.furkan.thyrestclient.models.data.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AdminPagesController
 */

@Controller
@RequestMapping("/admin/pages")
public class AdminPagesController {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public String index(Model model) {
        
        ResponseEntity<List<Page>> response = restTemplate.exchange("http://localhost:8080/pages/all", 
                                                                    HttpMethod.GET, 
                                                                    null, 
                                                                    new ParameterizedTypeReference<List<Page>>() {});

        List<Page> pages = response.getBody();

        model.addAttribute("pages", pages);
        
        return "admin/pages/index";
    }

    @GetMapping("/add")
    public String add(Page page) {

        return "admin/pages/add";
    }

    @PostMapping("/add")
    public String add(@Valid Page page, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model){

        if(bindingResult.hasErrors()){
            return "admin/pages/add";
        }

        redirectAttributes.addFlashAttribute("message", "Page added");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        String slug = page.getSlug() == "" ? page.getTitle().toLowerCase().replace(" ", "-") : page.getSlug().toLowerCase().replace(" ", "-");

        Page slugExists = pageRepository.findBySlug(slug);

        if ( slugExists != null) {
            redirectAttributes.addFlashAttribute("message", "Slug exists, choose another");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("page", page);

        } else {
            page.setSlug(slug);
            page.setSorting(100);

            restTemplate.postForObject("http://localhost:8080/admin/pages/add", page, Page.class);
        }

        return "redirect:/admin/pages/add";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model) {
        
        Page page = restTemplate.getForObject("http://localhost:8080/admin/pages/edit/{id}", Page.class, id);
        model.addAttribute("page", page);

        return "admin/pages/edit";

    }

    @PostMapping("/edit")
    public String edit(@Valid Page page, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model){

        Page pageCurrent = pageRepository.getOne(page.getId());

        if(bindingResult.hasErrors()){
            model.addAttribute("pageTitle", pageCurrent.getTitle());
            return "admin/pages/edit";
        }

        redirectAttributes.addFlashAttribute("message", "Page edited");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        String slug = page.getSlug() == "" ? page.getTitle().toLowerCase().replace(" ", "-") : page.getSlug().toLowerCase().replace(" ", "-");

        //Page slugExists = pageRepository.findBySlug(page.getId(), slug);

        Page slugExists = pageRepository.findBySlugAndIdNot(slug, page.getId());

        if ( slugExists != null) {
            redirectAttributes.addFlashAttribute("message", "Slug exists, choose another");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("page", page);

        } else {
            page.setSlug(slug);
            

            restTemplate.put("http://localhost:8080/admin/pages/edit", page);
        }

        return "redirect:/admin/pages/edit" + page.getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {

        restTemplate.delete("http://localhost:8080/admin/pages/delete/{id}", id);
        
        redirectAttributes.addFlashAttribute("message", "Page Deleted");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        return "redirect:/admin/pages";
    }
}