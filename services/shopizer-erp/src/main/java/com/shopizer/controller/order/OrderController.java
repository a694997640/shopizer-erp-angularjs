package com.shopizer.controller.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.shopizer.business.entity.order.Order;
import com.shopizer.business.entity.order.OrderTotalTypeEnum;
import com.shopizer.business.repository.order.OrderRepository;
import com.shopizer.business.services.order.OrderIdService;
import com.shopizer.restentity.common.RESTValue;
import com.shopizer.restentity.order.RESTOrder;
import com.shopizer.restentity.order.RESTOrderTotal;
import com.shopizer.restentity.order.RESTTotal;
import com.shopizer.restpopulators.order.OrderPopulator;

@RestController
public class OrderController {
	
	@Inject
	private OrderPopulator orderPopulator;
	
	@Inject
	private OrderIdService orderIdService;
	
	@Inject
	private OrderRepository orderRepository;

	
	@GetMapping("/api/order/nextOrderId")
	public ResponseEntity<RESTValue<Long>> nextOrderId(Locale locale) throws Exception {
		
		long orderId = orderIdService.nextOderId();
		
		RESTValue<Long> restValue= new RESTValue<Long>();
		restValue.setValue(orderId);
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<RESTValue<Long>>(restValue, headers, HttpStatus.OK);
		
	}
	
	@PostMapping("/api/order")
	public ResponseEntity<RESTOrder> createOrder(@Valid @RequestBody RESTOrder order, Locale locale, UriComponentsBuilder ucBuilder) throws Exception {


		Order o = orderPopulator.populateModel(order, locale);
		
		//check if customer exists
		Order lookupOrder = orderRepository.findOne(o.getId());
		if(lookupOrder != null) {
			throw new Exception("Order with id " + o.getId() + " already exists, use the update method");
		}
		
		//TODO calculate order total
		
		o.setCreated(new Date());
		orderRepository.save(o);
		
		RESTOrder restOrder = orderPopulator.populateWeb(o, locale);
			
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/order/{id}").buildAndExpand(o.getId()).toUri());
		return new ResponseEntity<RESTOrder>(restOrder, headers, HttpStatus.CREATED);

		
	}
	
	@PutMapping("/api/order/{id}")
	public ResponseEntity<RESTOrder> updateOrder(@PathVariable String id, @Valid @RequestBody RESTOrder order, Locale locale, UriComponentsBuilder ucBuilder) throws Exception {

		Order o = orderPopulator.populateModel(order, locale);
		
		o.setModified(new Date());
		orderRepository.save(o);
		
		RESTOrder restOrder = orderPopulator.populateWeb(o, locale);
			
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/order/{id}").buildAndExpand(o.getId()).toUri());
		return new ResponseEntity<RESTOrder>(restOrder, headers, HttpStatus.OK);

		
	}
	
	@GetMapping("/api/order/{id}")
	public ResponseEntity<RESTOrder> getCustomer(@PathVariable String id, Locale locale, UriComponentsBuilder ucBuilder) throws Exception {

		Order o = orderRepository.findOne(id);
		
		if(o==null) {
			throw new Exception("Order id " + id + " not found");
		}
		
		RESTOrder restOrder = orderPopulator.populateWeb(o, locale);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/order/{id}").buildAndExpand(o.getId()).toUri());
		return new ResponseEntity<RESTOrder>(restOrder, headers, HttpStatus.OK);

		
	}
	
	@PostMapping("/api/order/total")
	public ResponseEntity<RESTTotal> calculateOrderTotal(@RequestBody List<RESTOrderTotal> orderTotals, Locale locale, UriComponentsBuilder ucBuilder) throws Exception {


		Validate.notNull(orderTotals, "Requires a list of order total to calculate price");
		
		BigDecimal orderTotal = new BigDecimal(0);
		orderTotal.setScale(2, RoundingMode.HALF_EVEN);
		
		for(RESTOrderTotal ot : orderTotals) {
			
			BigDecimal numericValue = null;
			try {
				numericValue = new BigDecimal(ot.getValue());
			} catch(Exception e) {
				throw new Exception("Cannot parse " + ot.getValue() + " numeric value");
			}
			
			OrderTotalTypeEnum type = OrderTotalTypeEnum.OTHER;
			
			if(!StringUtils.isBlank(ot.getType())) {
				type = OrderTotalTypeEnum.valueOf(ot.getType());
			}
	
			
			if(type.name().equals(OrderTotalTypeEnum.DEPOSIT.name())) {
				orderTotal = orderTotal.subtract(numericValue);
			} else {
				orderTotal = orderTotal.add(numericValue);
			}
			
			
		}
		
		//prepare display
		NumberFormat totalFormat = NumberFormat.getCurrencyInstance(Locale.US);
		totalFormat.setMinimumFractionDigits( 2 );
		totalFormat.setMaximumFractionDigits( 2 );
		
		String totalText = totalFormat.format(orderTotal.doubleValue());

		RESTTotal restTotal = new RESTTotal();
		restTotal.setTotal(totalText);


		return new ResponseEntity<RESTTotal>(restTotal, HttpStatus.OK);

		
	}
	
	

}
