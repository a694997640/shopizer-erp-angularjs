package com.shopizer.business.services.order;

import com.shopizer.business.entity.order.Order;

public interface OrderService {
	
	long nextOderId();
	
	Order save(Order order) throws Exception;

}
