package com.example.productService.service;

import java.io.File;
import java.util.Optional;

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

			JobParameters jobParameters = new JobParametersBuilder()
					.addString("filePath", tempFile.getAbsolutePath())
					.addLong("timestamp", System.currentTimeMillis())
					.toJobParameters();

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

}
