package com.mapmory.services.purchase.service.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mapmory.common.domain.Search;
import com.mapmory.common.util.PurchaseUtil;
import com.mapmory.exception.purchase.SubscriptionException;
import com.mapmory.services.product.domain.Product;
import com.mapmory.services.purchase.dao.SubscriptionDao;
import com.mapmory.services.purchase.domain.Subscription;
import com.mapmory.services.purchase.service.SubscriptionService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.AgainPaymentData;
import com.siot.IamportRestClient.request.ScheduleData;
import com.siot.IamportRestClient.request.ScheduleEntry;
import com.siot.IamportRestClient.request.UnscheduleData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.response.Schedule;

@Service("subscriptionServiceImpl")
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

	///// Field /////

	private IamportClient iamportClient;
	
	@Autowired
	private SubscriptionDao subscriptionDao;
	
	@Value("${portOne.imp_key}")
	private String portOneImpkey;

	@Value("${portOne.imp_secret}")
	private String portOneImpSecret;
	
	///// Constructor /////
	@PostConstruct
    private void init() {
        this.iamportClient = new IamportClient(portOneImpkey, portOneImpSecret);
    }

	///// Method /////
	
    @Override
    public boolean addSubscription(Subscription subscription) throws Exception{
    	Subscription currentSubscription = getDetailSubscription(subscription.getUserId());
    	
    	if (currentSubscription == null || !(currentSubscription.isSubscribed()) ) {
    		
    		if(subscriptionDao.addSubscription(subscription) == 1 ) {
    			
    			return true;
    		}else {
                throw new SubscriptionException("결제 중 에러 발생");
    		}
    	} else {
            throw new SubscriptionException("이미 구독 중입니다");
    	}
    }// addSubscription: 구독 DB에 저장

    @Override
    public Subscription getDetailSubscription(String userId) throws Exception{
    	Subscription subscription = subscriptionDao.getDetailSubscription(userId);
    	
    	if( subscription != null && subscription.isSubscribed()) {
    		subscription.setSubscriptionStartDateString(PurchaseUtil.purchaseDateChange(subscription.getSubscriptionStartDate()));
    		subscription.setSubscriptionEndDateString(PurchaseUtil.purchaseDateChange(subscription.getSubscriptionEndDate()));
    		
    		if(subscription.getNextSubscriptionPaymentDate() != null) {
    			subscription.setNextSubscriptionPaymentDateString(PurchaseUtil.purchaseDateChange(subscription.getNextSubscriptionPaymentDate()));
    			subscription.setNextSubscriptionPaymentMethodString(PurchaseUtil.paymentChange(subscription.getNextSubscriptionPaymentMethod()));
    		}
    	}
    	
    	
        return subscription;
    }// getDetailSubscription: 구독 상세 정보

    @Override
    public boolean updatePaymentMethod(Subscription subscription) throws Exception{
    	
    	return subscriptionDao.updatePaymentMethod(subscription) == 1;
    }// updatePaymentMethod: 구독 결제 수단 변경

    @Override
    public boolean cancelSubscriptionPortOne(String userId) throws Exception{
		Subscription subscription = getDetailSubscription(userId);

		try {
			UnscheduleData unscheduleData = new UnscheduleData(subscription.getCustomerUid());
			IamportResponse<List<Schedule>> scheduleResponse = iamportClient.unsubscribeSchedule(unscheduleData);

			return scheduleResponse.getCode() == 0;
		} catch (Exception e) {
			throw new SubscriptionException("구독 해지 실패!!", e);
		}
    }// cancelSubscription: 구독 해지
    
    @Override
    public boolean cancelSubscription(String userId) throws Exception{
    	
    	return subscriptionDao.cancelSubscription(userId) == 1;
    }// updatePaymentMethod: 구독 결제 수단 변경

    @Override
    public boolean requestSubscription(Subscription subscription, Product product) throws Exception{
    	
    	AgainPaymentData againPaymentData = new AgainPaymentData(
    			subscription.getCustomerUid(),
                subscription.getMerchantUid(),
                BigDecimal.valueOf(product.getPrice()));
    	
    	againPaymentData.setName(product.getProductTitle());
    	
    	try {
        	IamportResponse<Payment> againPayment = iamportClient.againPayment(againPaymentData);
        	
        	return againPayment.getCode() == 0;
    	}catch(Exception e) {
            return false;
    	}

    }// requestSubscription: 첫 구독 시 결제 요청

    public boolean schedulePay(Subscription subscription, Product product) {
		ScheduleData scheduleData = new ScheduleData(subscription.getCustomerUid());

		ScheduleEntry scheduleEntry = new ScheduleEntry(subscription.getMerchantUid(),
				Date.from(subscription.getNextSubscriptionPaymentDate().atZone(ZoneId.of("Asia/Seoul")).toInstant()),
				BigDecimal.valueOf(product.getPrice()) );
		scheduleEntry.setName(product.getProductTitle());
		scheduleData.addSchedule(scheduleEntry);

		try {
			IamportResponse<List<Schedule>> subscribeSchedule = iamportClient.subscribeSchedule(scheduleData);
			
			if(subscribeSchedule.getCode() == 1) {
			}
			return subscribeSchedule.getCode() == 0;
		}
		catch (Exception e) {
			throw new SubscriptionException();
		}
    }// schedulePay: 정기결제 예약 등록하는 곳

    @Override
    public List<Subscription> getTodaySubscriptionList() throws Exception{
    	
        return subscriptionDao.getTodaySubscriptionList();
    }// getTodaySubscriptionList: 오늘 결제 목록

    @Override
    public boolean deleteSubscription(int subscriptionNo) throws Exception{
    	
        return subscriptionDao.deleteSubscription(subscriptionNo) == 1;
    }// deleteSubscription: 구독 삭제

	@Override
	public boolean reSubscription(Subscription subscription) throws Exception {
		return subscriptionDao.reSubscription(subscription) == 1;
	}

	@Override
	public List<Subscription> getSubscriptionList(Search search) throws Exception {
		int offset = (search.getCurrentPage() - 1) * search.getPageSize();
   	 	search.setOffset(offset);
        search.setLimit(search.getPageSize());

		List<Subscription> subscriptionList = subscriptionDao.getSubscriptionList(search);
   	 	
		for(Subscription subscription : subscriptionList) {
			subscription.setSubscriptionStartDateString(PurchaseUtil.purchaseDateChange(subscription.getSubscriptionStartDate()));
			subscription.setSubscriptionEndDateString(PurchaseUtil.purchaseDateChange(subscription.getSubscriptionEndDate()));
		}
				
		return subscriptionList;
	}
}