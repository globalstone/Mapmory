package com.mapmory.services.purchase.service.impl;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapmory.services.purchase.dao.SubscriptionDao;
import com.mapmory.services.purchase.domain.IamportToken;
import com.mapmory.services.purchase.domain.Purchase;
import com.mapmory.services.purchase.domain.Subscription;
import com.mapmory.services.purchase.service.PurchaseService;
import com.mapmory.services.purchase.service.SubscriptionService;

@Service("subscriptionServiceImpl")
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

	///// Field /////
	
	@Autowired
	private SubscriptionDao subscriptionDao;
	
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	
	@Value("${portOne.imp_key}")
	private String impKey;

	@Value("${portOne.imp_secret}")
	private String portOneImpSecret;

	///// Method /////
	
	@Override
	public void addSubscription(Subscription subscription) throws Exception {
		Purchase purchase = Purchase.builder()
							.price(1000)
							.paymentMethod(subscription.getNextPaymentMethod())
							.cardType(subscription.getNextSubscriptionCardType())
							.lastFourDigits(subscription.getNextSubscriptionLastFourDigits())
							.userId(subscription.getUserId())
							.purchaseDate(subscription.getSubscriptionStartDate())
							.productNo(2)
							.build();
		
		purchaseService.addPurchase(purchase);
		subscriptionDao.addSubscription(subscription); // DB에 구독 정보 저장
	}//addSubscription

	@Override
	public Subscription getDetailSubscription(String userId) throws Exception {
		Subscription subscription = subscriptionDao.getDetailSubscription(userId);
		
		return subscription;
	}//getDetailSubscription

	@Override
	public void updatePaymentMethod(Subscription subscription) throws Exception {
		subscriptionDao.updatePaymentMethod(subscription);
	}//updatePaymentMethod

	@Override
	public void deleteSubscription(String userId) throws Exception {
		subscriptionDao.deleteSubscription(userId);
	}//deleteSubscription
	
	@Override
	public boolean requestSubscription(Subscription subscription) throws Exception {

		String token = getPortOneToken();
		Gson str = new Gson();
		token = token.substring(token.indexOf("response") + 10);
		token = token.substring(0, token.length() - 1);

		IamportToken iamportToken = str.fromJson(token, IamportToken.class);
		String accessToken = iamportToken.getAccess_token();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(accessToken);
		
		Map<String, Object> map = new HashMap<>();
		map.put("customer_uid", subscription.getCustomerUid());
		map.put("merchant_uid", subscription.getMerchantUid());
		map.put("amount", "1000");
		map.put("name", "정기 구독 결제");

		Gson var = new Gson();
		String requestJson = var.toJson(map);
		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		
		String resultJson = restTemplate.postForObject("https://api.iamport.kr/subscribe/payments/again", entity, String.class);

		ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode rootNode = objectMapper.readTree(resultJson);
		String status = rootNode.get("response").get("status").asText();
		
		if(status.equals("paid")) {
			return true;
		}
		
		return false;
	}//구독 결제 요청
	
	public Subscription schedulePay(Subscription subscription) throws Exception {
		Subscription nextSubscription = getDetailSubscription(subscription.getUserId()) != null
			    ? getDetailSubscription(subscription.getUserId())
			    : subscription;
		
		String getAccessToken = getPortOneToken();
		
		LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
		
		long timestamp = 0;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		
		//cal.add(Calendar.DATE, +31);
		cal.add(Calendar.MINUTE, +1);
		String date = sdf.format(cal.getTime());
		
		try {
			Date stp = sdf.parse(date);
			timestamp = stp.getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		 Gson str = new Gson(); 
		 getAccessToken = getAccessToken.substring(getAccessToken.indexOf("response") +10); 
		 getAccessToken = getAccessToken.substring(0, getAccessToken.length() - 1);
		 IamportToken iamportToken = str.fromJson(getAccessToken, IamportToken.class);
		 String accessToken = iamportToken.getAccess_token();
		 
		 RestTemplate restTemplate = new RestTemplate();
		 HttpHeaders headers = new HttpHeaders();
		 headers.setContentType(MediaType.APPLICATION_JSON);
		 headers.setBearerAuth(accessToken);//헤더에 access token 추가
		 
		 JsonObject scheduleJson = new JsonObject();
		 scheduleJson.addProperty("merchant_uid", "subscription_"+subscription.getUserId() +"_"+ formattedDateTime);
		 scheduleJson.addProperty("schedule_at", timestamp);
		 scheduleJson.addProperty("amount", 1000);
		 scheduleJson.addProperty("name", "정기 결제 구독");
		 
		 JsonArray jsonArr = new JsonArray();
		 jsonArr.add(scheduleJson);
		 
		 JsonObject requestSubscriptionJson = new JsonObject();
		 requestSubscriptionJson.addProperty("customer_uid", subscription.getCustomerUid()); 
		 requestSubscriptionJson.add("schedules", jsonArr);
		 
		 String json = str.toJson(requestSubscriptionJson); 
		 
		 HttpEntity<String> entity = new HttpEntity<>(json, headers);
		 
		 String resultJson = restTemplate.postForObject("https://api.iamport.kr/subscribe/payments/schedule", entity, String.class);
		 
		 ObjectMapper objectMapper = new ObjectMapper();
	     JsonNode rootNode = objectMapper.readTree(resultJson);

	     // response 배열에서 merchant_uid와 schedule_at 추출
	     String merchantUid = rootNode.get("response").get(0).get("merchant_uid").asText();
	     long scheduleAtUnixTime = rootNode.get("response").get(0).get("schedule_at").asLong()+3;
	     
	     // Unix 시간을 LocalDateTime으로 변환
	     LocalDateTime nextSubscriptionPaymentDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(scheduleAtUnixTime), ZoneId.systemDefault());
		 
	     nextSubscription.setSubscribed(true);
		 nextSubscription.setMerchantUid(merchantUid);
		 nextSubscription.setNextSubscriptionPaymentDate(nextSubscriptionPaymentDate);
		 nextSubscription.setSubscriptionStartDate(nextSubscriptionPaymentDate.plusMinutes(-1));
		 nextSubscription.setSubscriptionEndDate(nextSubscriptionPaymentDate);
		 
		 return nextSubscription;
	}//schedulePay: 정기결제 예약 등록하는 곳

	@Override
	public String getPortOneToken() throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		
		//서버로 요청할 Header
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
		
	    Map<String, Object> map = new HashMap<>();
	    map.put("imp_key", impKey);
	    map.put("imp_secret", portOneImpSecret);
	   
	    Gson var = new Gson();
	    String json=var.toJson(map);
		//서버로 요청할 Body
	   
	    HttpEntity<String> entity = new HttpEntity<>(json, headers);
	    
	    String result = restTemplate.postForObject("https://api.iamport.kr/users/getToken", entity, String.class);
	    
		return result;
	}//getPortOneToken
}