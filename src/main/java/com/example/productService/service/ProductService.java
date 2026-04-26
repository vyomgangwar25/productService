package com.example.productService.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.productService.apiResponse.ApiResponse;
import com.example.productService.entity.Product;
import com.example.productService.repository.ProductRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
	@Autowired
	private ProductRepo productRepository;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	public ApiResponse addProduct(Product product) {
		log.info("initating add product service!!");
		ApiResponse apiResponse = new ApiResponse();
		try {
			if (product != null) {
				productRepository.save(product);
				apiResponse.setMessage("product sucessfully saved to db!!");
				apiResponse.setStatus(HttpStatus.OK);
			}

		} catch (Exception e) {
			apiResponse.setMessage("error occured during save" + e.getMessage());
			apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return apiResponse;
	}

	public ApiResponse readCsvAndSave(MultipartFile file) {
		ApiResponse response = new ApiResponse();
		try {

			File tempFile = File.createTempFile("products_", ".csv");
			file.transferTo(tempFile);

			JobParameters jobParameters = new JobParametersBuilder().addString("filePath", tempFile.getAbsolutePath())
					.addLong("timestamp", System.currentTimeMillis()).toJobParameters();

			jobLauncher.run(job, jobParameters);

			response.setMessage("Job started successfully.");
			response.setStatus(HttpStatus.OK);

		} catch (Exception e) {
			response.setMessage("Job failed: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		return response;
	}

	public String readCsvAndSaveDb(MultipartFile file) throws IOException, InterruptedException, ExecutionException {

		InputStream inputStream = file.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String headerLine = bufferedReader.readLine();
		if (headerLine == null) {
			throw new RuntimeException("CSV file is empty");
		}
		String[] csvHeaders = headerLine.split(",");
		List<String> expectedHeaders = List.of("productSKU", "name", "description", "price", "quantity", "category",
				"brand", "model", "weight", "dimensions", "costPrice", "salePrice", "active", "taxable", "barcode",
				"manufacturedBy", "madeIn");
		for (int i = 0; i < csvHeaders.length; i++) {
			if (!expectedHeaders.contains(csvHeaders[i])) {
				throw new RuntimeException("invalid entity template");
			}
		}
		String line;
		int count = 0;
		int batchCount = 0;
		List<Product> batch = new ArrayList<>();
		List<List<Product>> batches = new ArrayList<>();
		while ((line = bufferedReader.readLine()) != null) {
			String[] data = line.split(",");

			// create user
			Product product = new Product();

			product.setProductSKU(data[0].trim());
			product.setName(data[1].trim());
			product.setDescription(data[2].trim());
			product.setPrice(new BigDecimal(data[3].trim()));
			product.setQuantity(Integer.parseInt(data[4].trim()));
			product.setCategory(data[5].trim());
			product.setBrand(data[6].trim());
			product.setModel(data[7].trim());
			product.setWeight(new BigDecimal(data[8].trim()));
			product.setDimensions(data[9].trim());
			product.setCostPrice(new BigDecimal(data[10].trim()));
			product.setSalePrice(new BigDecimal(data[11].trim()));
			product.setBarcode(data[12].trim());
			product.setManufacturedBy(data[13].trim());
			product.setMadeIn(data[14].trim());
			product.setActive(Boolean.parseBoolean(data[15].trim()));
			product.setTaxable(Boolean.parseBoolean(data[16].trim()));
			batch.add(product);

			if (batch.size() == 1000) {
				// productRepository.saveAll(batch);
				batches.add(new ArrayList<>(batch));
				count++;
				log.info("{} batched add so far...", count);
				batch.clear();
			}

		}
		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<Future<?>> futures = new ArrayList<>();
		
		for (List<Product> b : batches) {

			futures.add(executor.submit(() -> productRepository.saveAll(b)));
			batchCount++;
			log.info("{} batches saved in db so far...", batchCount);
		}
		for (Future<?> f : futures) {
			f.get();
		}

		executor.shutdown();

		return "data saved";

	}

	public ApiResponse update(Integer id, Product product) {
		log.info("initating update product service!!");
		ApiResponse apiResponse = new ApiResponse();
		try {
			if (id != null) {
				Optional<?> existProduct = productRepository.findById(id);
				if (!existProduct.isEmpty()) {

					productRepository.save(product);
				} else {
					apiResponse.setMessage("giving id is not found in database");
				}

			}
		} catch (Exception e) {
			apiResponse.setMessage("exception occurs during update" + e.getMessage());
			apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return apiResponse;

	}

	public List<Product> getAll() {
		List<Product> products = productRepository.findAll();
		return products;
	}

	public ApiResponse delete(Integer id) {
		log.info("initating delete product service!!");
		ApiResponse apiResponse = new ApiResponse();
		try {
			if (id != null && productRepository.existsById(id)) {
				productRepository.deleteById(id);
				apiResponse.setMessage("");
			}
		} catch (Exception e) {
			apiResponse.setMessage("exception occurs during delete" + e.getMessage());
			apiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

		}

		return apiResponse;
	}

	public Product findProduct(Integer id) {
		return productRepository.findById(id).orElse(null);
	}

}
