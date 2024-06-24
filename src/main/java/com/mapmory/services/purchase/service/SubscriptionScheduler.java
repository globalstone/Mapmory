package com.mapmory.services.purchase.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mapmory.services.product.service.ProductService;
import com.mapmory.services.purchase.domain.Subscription;

@Component
public class SubscriptionScheduler {
	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private ProductService productService;

	@Scheduled(cron = "0 0 0 * * *") //매일 자정에 실행
	public void processSubscriptions() throws Exception {
		List<Subscription> subscriptions = subscriptionService.getTodaySubscriptionList(); // 오늘 구독 결제일인 레코드 리스트
		
		for (Subscription subscription : subscriptions) {
			try {
				Subscription updatedSubscription = updateSubscription(subscription);
				
                subscriptionService.schedulePay(updatedSubscription, productService.getSubscription());
                subscriptionService.addSubscriptionFromScheduler(updatedSubscription);
                
			} catch (Exception e) {
				e.printStackTrace(); //결제 실패 처리 로직
			}//try~catch
		}//for end
	}//processSubscriptions: 매일 자정 결제일인 실행
	
	private Subscription updateSubscription(Subscription subscription) {
		LocalDateTime now = LocalDateTime.now();
		
        subscription.setMerchantUid("subscription_" + subscription.getUserId() + "_" + now);
        subscription.setSubscriptionStartDate(now);
        subscription.setSubscriptionEndDate(now.plusMonths(1));
        subscription.setNextSubscriptionPaymentDate(now.plusMonths(1));
        
        return subscription;
    }//updateSubscription: merchantUid, 결제일, 구독 시작일, 구독 종료일 업데이트
}
