package com.shopizer.restpopulators.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.shopizer.business.entity.common.Currency;
import com.shopizer.business.entity.customer.Customer;
import com.shopizer.business.entity.order.Order;
import com.shopizer.business.entity.order.OrderChannelEnum;
import com.shopizer.business.entity.order.OrderComment;
import com.shopizer.business.entity.order.OrderStatusEnum;
import com.shopizer.business.entity.order.OrderStatusHistory;
import com.shopizer.business.entity.order.OrderTotal;
import com.shopizer.business.repository.customer.CustomerRepository;
import com.shopizer.business.services.price.PriceService;
import com.shopizer.restentity.customer.RESTCustomer;
import com.shopizer.restentity.order.RESTOrder;
import com.shopizer.restentity.order.RESTOrderComment;
import com.shopizer.restentity.order.RESTOrderStatusHistory;
import com.shopizer.restentity.order.RESTOrderTotal;
import com.shopizer.restpopulators.DataPopulator;
import com.shopizer.restpopulators.customer.CustomerPopulator;
import com.shopizer.utils.DateUtil;

@Component
public class OrderPopulator implements DataPopulator<RESTOrder, Order> {
	
	
	@Inject
	private PriceService priceService;
	
	@Inject
	private OrderTotalPopulator orderTotalPopulator;
	
	@Inject
	private OrderCommentPopulator orderCommentPopulator;
	
	@Inject
	private OrderStatusHistoryPopulator orderStatusHistoryPopulator;
	
	@Inject
	private CustomerPopulator customerPopulator;
	
	@Inject
	private CustomerRepository customerRepository;

	@Override
	public Order populateModel(RESTOrder source, Locale locale) throws Exception {
		
		Order target = new Order();
		target.setId(source.getId());
		target.setDescription(source.getDescription());
		target.setNumber(Long.parseLong(source.getOrderNumber()));
		target.setOrder(source.getOrder());
		target.setCreator(source.getCreator());
		target.setLastUpdator(source.getLastUpdator());
		if(!StringUtils.isBlank(source.getChannel())) {
			target.setChannel(OrderChannelEnum.valueOf(source.getChannel()));
		}
		if(!StringUtils.isBlank(source.getStatus())) {
			target.setStatus(OrderStatusEnum.valueOf(source.getStatus()));
		}
		
		BigDecimal total = priceService.toPrice(source.getTotal());
		target.setTotal(total);
		
		if(!CollectionUtils.isEmpty(source.getOrderTotals())) {
			List<OrderTotal> totals = new ArrayList<OrderTotal>();
			for(RESTOrderTotal ot : source.getOrderTotals()) {
				OrderTotal orderTotal = orderTotalPopulator.populateModel(ot, locale);
				totals.add(orderTotal);
			}
			target.setOrderTotals(totals);
		}
		
		if(!CollectionUtils.isEmpty(source.getComments())) {
			List<OrderComment> comments = new ArrayList<OrderComment>();
			for(RESTOrderComment oc : source.getComments()) {
				OrderComment orderComment = orderCommentPopulator.populateModel(oc, locale);
				comments.add(orderComment);
			}
			target.setComments(comments);
		}
		
		if(!CollectionUtils.isEmpty(source.getOrderStatusHistory())) {
			List<OrderStatusHistory> statusHistory = new ArrayList<OrderStatusHistory>();
			for(RESTOrderStatusHistory os : source.getOrderStatusHistory()) {
				OrderStatusHistory history = orderStatusHistoryPopulator.populateModel(os, locale);
				statusHistory.add(history);
			}
			target.setStatusHistory(statusHistory);
		}
		
		target.setTotal(total);
		
		String customerId = source.getCustomer().getId();
		Customer customer = customerRepository.findOne(customerId);
		target.setCustomer(customer);

		if(source.getCreated() != null) {
			target.setCreated(DateUtil.getDate(source.getCreated()));
		}
		
		if(source.getEstimated() != null) {
			target.setEstimated(DateUtil.getDate(source.getEstimated()));
		}
		
		if(source.getDeliveryEstimated() != null) {
			target.setDeliveryEstimated(DateUtil.getDate(source.getDeliveryEstimated()));
		}
		
		
		target.setCode(String.valueOf(source.getOrderNumber()));
		return target;
		

		
	}

	@Override
	public RESTOrder populateWeb(Order source, Locale locale) throws Exception {
		// TODO Auto-generated method stub
		
		
		RESTOrder target = new RESTOrder();
		target.setId(source.getId());
		target.setDescription(source.getDescription());
		target.setOrderNumber(String.valueOf(source.getNumber()));
		target.setOrder(source.getOrder());
		if(source.getChannel() != null) {
			target.setChannel(source.getChannel().name());
		}
		if(source.getStatus() != null) {
			target.setStatus(source.getStatus().name());
		}
		
		//target.setChannel(source.getChannel().name());
		//target.setStatus(source.getStatus().name());
		target.setCreator(source.getCreator());
		target.setLastUpdator(source.getLastUpdator());
		
		Currency CAD = new Currency("CAD");
		
		String total = priceService.formatAmountNoCurrency(CAD, source.getTotal(), locale);
		target.setTotal(total);
		
		if(!CollectionUtils.isEmpty(source.getOrderTotals())) {
			List<RESTOrderTotal> totals = new ArrayList<RESTOrderTotal>();
			for(OrderTotal ot : source.getOrderTotals()) {
				RESTOrderTotal orderTotal = orderTotalPopulator.populateWeb(ot, locale);
				totals.add(orderTotal);
			}
			target.setOrderTotals(totals);
		}
		
		if(!CollectionUtils.isEmpty(source.getComments())) {
			List<RESTOrderComment> comments = new ArrayList<RESTOrderComment>();
			for(OrderComment oc : source.getComments()) {
				RESTOrderComment orderComment = orderCommentPopulator.populateWeb(oc, locale);
				comments.add(orderComment);
			}
			target.setComments(comments);
		}
		
		if(!CollectionUtils.isEmpty(source.getStatusHistory())) {
			List<RESTOrderStatusHistory> statusHistory = new ArrayList<RESTOrderStatusHistory>();
			for(OrderStatusHistory os : source.getStatusHistory()) {
				RESTOrderStatusHistory history = orderStatusHistoryPopulator.populateWeb(os, locale);
				statusHistory.add(history);
			}
			target.setOrderStatusHistory(statusHistory);
		}
		
		target.setTotal(total);
		
		String customerId = source.getCustomer().getId();
		Customer customer = customerRepository.findOne(customerId);
		
		RESTCustomer restCustomer = customerPopulator.populateWeb(customer, locale);
		
		target.setCustomer(restCustomer);

		if(source.getCreated() != null) {
			target.setCreated(DateUtil.formatDate(source.getCreated()));
		}
		
		if(source.getModified() != null) {
			target.setModified(DateUtil.formatDate(source.getModified()));
		}
		
		if(source.getEstimated() != null) {
			target.setEstimated(DateUtil.formatDate(source.getEstimated()));
		}
		
		if(source.getDeliveryEstimated() != null) {
			target.setDeliveryEstimated(DateUtil.formatDate(source.getDeliveryEstimated()));
		}
		

		return target;
	}

	public OrderCommentPopulator getOrderCommentPopulator() {
		return orderCommentPopulator;
	}

	public void setOrderCommentPopulator(OrderCommentPopulator orderCommentPopulator) {
		this.orderCommentPopulator = orderCommentPopulator;
	}

}
