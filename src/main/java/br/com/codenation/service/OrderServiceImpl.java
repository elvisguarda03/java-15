package br.com.codenation.service;

import br.com.codenation.model.OrderItem;
import br.com.codenation.model.Product;
import br.com.codenation.repository.ProductRepository;
import br.com.codenation.repository.ProductRepositoryImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderServiceImpl implements OrderService {
	private static final Double SALE20 = Double.valueOf(0.2);
	private ProductRepository productRepository = new ProductRepositoryImpl();

	/**
	 * Calculate the sum of all OrderItems
	 */
	@Override
	public Double calculateOrderValue(List<OrderItem> items) {
		return items.stream()
				.map(item -> {
					Product product = findById(item.getProductId());
					return product.getIsSale() ? calculateDiscount(product.getValue()) * item.getQuantity()
							: product.getValue() * item.getQuantity();
				})
				.reduce(0.0, Double::sum);
	}

	/**
	 * Map from idProduct List to Product Set
	 */
	@Override
	public Set<Product> findProductsById(List<Long> ids) {
		return ids.stream()
				.map(id -> findById(id))
				.filter(product -> !Objects.isNull(product))
				.collect(Collectors.toSet());
	}

	/**
	 * Calculate the sum of all Orders(List<OrderIten>)
	 */
	@Override
	public Double calculateMultipleOrders(List<List<OrderItem>> orders) {
		return orders.stream()
				.map(orderItems -> orderItems.stream()
						.collect(Collectors.toList()))
				.mapToDouble(items -> calculateOrderValue(items))
				.reduce(0.0, Double::sum);
	}

	/**
	 * Group products using isSale attribute as the map key
	 */
	@Override
	public Map<Boolean, List<Product>> groupProductsBySale(List<Long> productIds) {
		Set<Product> products = findProductsById(productIds);

		Map<Boolean, List<Product>> mapSales1 = products.stream()
				.filter(product -> product.getIsSale().equals(true))
				.collect(Collectors.groupingBy(Product::getIsSale,
						Collectors.toList()));
		Map<Boolean, List<Product>> mapSales2 = products.stream()
				.filter(product -> product.getIsSale().equals(false))
				.collect(Collectors.groupingBy(Product::getIsSale,
						Collectors.toList()));

		return Stream.concat(mapSales1.entrySet().stream(),
				mapSales2.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Double calculateDiscount(Double value) {
		return value - (value * SALE20);
	}

	private Product findById(Long id) {
		return productRepository.findById(id)
				.orElse(null);
	}
}