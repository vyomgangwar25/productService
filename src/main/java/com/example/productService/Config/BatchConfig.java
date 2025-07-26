package com.example.productService.Config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.productService.entity.Product;
import com.example.productService.repository.ProductRepo;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	@Autowired
	private ProductRepo productRepo;

	@Bean
	@StepScope
	public FlatFileItemReader<Product> reader(@Value("#{jobParameters['filePath']}") String path) {
		FlatFileItemReader<Product> itemReader = new FlatFileItemReader<>();
		itemReader.setName("productItemReader");

		itemReader.setResource(new ClassPathResource(path)); // or use FileSystemResource if needed

		itemReader.setLinesToSkip(1);

		DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(",");
		tokenizer.setNames("id", "productSKU", "name", "description", "price");

		BeanWrapperFieldSetMapper<Product> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Product.class);

		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		itemReader.setLineMapper(lineMapper);

		return itemReader;
	}

	@Bean
	public ItemProcessor<Product, Product> itemProcessor() {
		return Product -> Product;
	}

	@Bean
	public ItemWriter<Product> itemWriter() {
		return items -> productRepo.saveAll(items);
	}

	@Bean
	public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			FlatFileItemReader<Product> reader) {
		return new StepBuilder("step1", jobRepository).<Product, Product>chunk(10, transactionManager).reader(reader)
				.processor(itemProcessor()).writer(itemWriter()).build();
	}

	@Bean
	public Job importProductJob(JobRepository jobRepository, Step step1) {
		return new JobBuilder("importProductJob", jobRepository).start(step1).build();
	}

}

//✅ 1. JobRepository
//🔹 What it is:
//JobRepository is a Spring Batch component that stores the meta-data of:
//
//Job executions
//
//Step executions
//
//Parameters
//
//Status (COMPLETED, FAILED, etc.)
//
//🔹 Why it’s needed:
//Spring Batch needs a place to track and manage:
//
//What job ran
//
//With what parameters
//
//Whether it succeeded or failed
//
//So it can support restarts, monitoring, deduplication, etc.
//
//🧠 It typically saves this info in a relational database using special Spring Batch tables (like BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION, etc.).
//
//✅ 2. PlatformTransactionManager
//🔹 What it is:
//PlatformTransactionManager is a Spring abstraction for managing transactions.
//
//Spring Batch uses it to:
//
//Begin, commit, or roll back transactions
//
//Ensure that reading, processing, and writing are done safely and atomically (in chunks)
