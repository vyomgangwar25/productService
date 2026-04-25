package com.example.productService.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer Id;

	@Column(nullable = false, unique = true)
	private String productSKU;

	private String name;

	private String description;

	private BigDecimal price;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	private String category;

	private String brand;

	private String model;

	private BigDecimal weight;

	private String dimensions;

	private BigDecimal costPrice;

	private BigDecimal salePrice;

	private Boolean active;

	private Boolean taxable;

	private String barcode;

	private String manufacturedBy;

	private String madeIn;

}
