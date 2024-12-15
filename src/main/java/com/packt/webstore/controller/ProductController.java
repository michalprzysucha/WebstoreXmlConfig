package com.packt.webstore.controller;

import com.packt.webstore.domain.Product;
import com.packt.webstore.exception.NoProductsFoundUnderCategoryException;
import com.packt.webstore.exception.ProductNotFoundException;
import com.packt.webstore.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @RequestMapping
    public String list(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    @RequestMapping("/all")
    public String allProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";

//        alternatywna wersja:

//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.addObject("products", productService.getAllProducts());
//        modelAndView.setViewName("products");
//        return modelAndView;

    }

    @RequestMapping("/{category}")
    public String getProductsByCategory(Model model, @PathVariable("category") String productCategory) {
        List<Product> products = productService.getProductsByCategory(productCategory);
        if (products == null || products.isEmpty()){
            throw new NoProductsFoundUnderCategoryException();
        }
        model.addAttribute("products", products);
        return "products";
    }

    @RequestMapping("/filter/{ByCriteria}")
    public String getProductsByFilter(@MatrixVariable(pathVar = "ByCriteria") Map<String, List<String>> filterParams,
                                      Model model) {
        model.addAttribute("products", productService.getProductsByFilter(filterParams));
        return "products";
    }

    @RequestMapping("/product")
    public String getProductById(@RequestParam("id") String productId, Model model) {
        model.addAttribute("product", productService.getProductById(productId));
        return "product";
    }

    @RequestMapping("/{category}/{price}")
    public String filterProducts(@PathVariable String category,
                                 @MatrixVariable(pathVar = "price") Map<String, List<String>> priceFilters,
                                 @RequestParam String manufacturer, Model model) {
        Set<Product> filterMatch = productService.getProductsByFilter(priceFilters);
        filterMatch.retainAll(productService.getProductsByManufacturer(manufacturer));
        filterMatch.retainAll(productService.getProductsByCategory(category));
        model.addAttribute("products", filterMatch);
        return "products";
    }

//    @RequestMapping(value = "/add", method = RequestMethod.GET)
//    public String getAddNewProductForm(Model model){
//        Product newProduct = new Product();
//        model.addAttribute("newProduct", newProduct);
//        return "addProduct";
//    }

//    metoda, ktora dziala dokladnie jak ta wyzej
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String getAddNewProductForm(@ModelAttribute("newProduct") Product product){
        return "addProduct";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String processAddNewProductForm(@ModelAttribute("newProduct") @Valid Product productToBeAdded,
                                           BindingResult result, HttpServletRequest request) {
        if (result.hasErrors()) {
            return "addProduct";
        }
        String[] suppressedFields = result.getSuppressedFields();
        if (suppressedFields.length > 0) {
            throw new RuntimeException("Próba wiązania niedozwolonych pól: " +
                    StringUtils.arrayToCommaDelimitedString(suppressedFields));
        }
        MultipartFile productImage = productToBeAdded.getProductImage();
        MultipartFile manual = productToBeAdded.getManual();
        String rootDirectory = request.getSession().getServletContext().getRealPath("/");
        if (productImage != null && !productImage.isEmpty()) {
            try{
                Files.createDirectories(Paths.get(rootDirectory, "WEB-INF", "resources", "images"));
                Files.createDirectories(Paths.get(rootDirectory, "WEB-INF", "resources", "pdf"));
                productImage.transferTo(new File(rootDirectory +
                        "WEB-INF\\resources\\images\\" + productToBeAdded.getProductId() + ".png"));
                manual.transferTo(new File(rootDirectory +
                        "WEB-INF\\resources\\pdf\\" + productToBeAdded.getProductId() + ".pdf"));
            } catch (IOException e) {
                throw new RuntimeException("Niepowodzenie podczas próby zapisu pliku produktu", e);
            }
        }
        productService.addProduct(productToBeAdded);
        return "redirect:/products";
    }

    @RequestMapping("/invalidPromoCode")
    public String invalidPromoCode(){
        return "invalidPromoCode";
    }

    @InitBinder
    public void initializeBinder(WebDataBinder binder) {
        binder.setDisallowedFields("unitsInOrder", "discontinued");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ModelAndView handleError(HttpServletRequest request, ProductNotFoundException exception){
        ModelAndView mav = new ModelAndView();
        mav.addObject("invalidProductId", exception.getProductId());
        mav.addObject("exception", exception);
        mav.addObject("url", request.getRequestURL() + "?" + request.getQueryString());
        mav.setViewName("productNotFound");
        return mav;
    }
}
