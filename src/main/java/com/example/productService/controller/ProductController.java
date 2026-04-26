package com.example.productService.controller;

import com.example.productService.DTO.InventoryResponse;
import com.example.productService.DTO.ProductRequestDTO;
import com.example.productService.service.Interface.InventoryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.productService.apiResponse.ApiResponse;
import com.example.productService.entity.Product;
import com.example.productService.service.ProductService;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private InventoryClient inventoryClient;

	@PostMapping(value = "/add")
	public ResponseEntity<ApiResponse> add(@RequestBody Product product) {
		ApiResponse response = new ApiResponse();
		log.info("initating productController.add() function");
		response = productService.addProduct(product);

		return ResponseEntity.ok(response);
	}

//	@PostMapping(value = "/uploadCsv")
//	public ResponseEntity<ApiResponse> uploadCsv(@RequestParam MultipartFile file) {
//
//		ApiResponse response = new ApiResponse();
//		log.info("initating productController.uploadCsv function");
//		response = productService.readCsvAndSave(file);
//
//		return ResponseEntity.ok(response);
//	}
	
	@PostMapping(value="/uploadCsv")
	public ResponseEntity<?>uploadCsv(@RequestParam("csvFile") MultipartFile file){
		String abc="";
		if(file.isEmpty())
		{
			return(new ResponseEntity<>("uploaded file is empty",HttpStatus.NO_CONTENT));
		}
		else if(!file.getContentType().equals("text/csv")) {
			return(new ResponseEntity<>("please upload a valid csv file",HttpStatus.BAD_REQUEST));
		}
		log.info("initating productController.upload()");
		try {
			 abc=productService.readCsvAndSaveDb(file);
		} catch (Exception e) {
			
		abc=e.getMessage().toString();
		}
		
		return ResponseEntity.ok(abc);

	}

	@GetMapping("/getAll")
	public ResponseEntity<?> getAll() {
		log.info("initating productController.update()");
		List<Product> product = productService.getAll();
		return new ResponseEntity(product, HttpStatus.OK);
	}

	@PutMapping("update/{id}")
	public ResponseEntity<ApiResponse> update(@PathVariable Integer Id, @RequestBody Product product) {
		ApiResponse apiResponse = new ApiResponse();
		log.info("initating productController.update()");
		apiResponse = productService.update(Id, product);
		return ResponseEntity.ok(apiResponse);
	}

	@DeleteMapping("delete/{id}")
	public ResponseEntity<ApiResponse> delete(@PathVariable Integer id) {
		ApiResponse apiResponse = new ApiResponse();
		log.info("initating productController.delete()");
		apiResponse = productService.delete(id);
		return ResponseEntity.ok(apiResponse);
	}

	@GetMapping("/order/{id}")
	public ResponseEntity<?> getProductForOrder(@PathVariable Integer id) {

		Product product = productService.findProduct(id);

		if (product == null) {
			return ResponseEntity
					.badRequest()
					.body("Product not found with id: " + id);
		}

		ProductRequestDTO response=new ProductRequestDTO();
		response.setId(product.getId());
		response.setName(product.getName());
		response.setPrice(product.getPrice());

		return ResponseEntity.ok(response);
	}


}
