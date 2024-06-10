package com.mapmory.services.purchase.service;

import java.io.IOException;
import java.util.List;

import com.mapmory.common.domain.Search;
import com.mapmory.services.purchase.domain.Purchase;
import com.mapmory.services.purchase.dto.PurchaseDTO;
import com.siot.IamportRestClient.exception.IamportResponseException;

public interface PurchaseService {
	
	//insert
	public boolean addPurchase(Purchase purchase) throws Exception;
	
	//selectOne
	public PurchaseDTO getDetailPurchase(int purchaseNo) throws Exception;
	
	//selectList
	public List<PurchaseDTO> getPurchaseList(Search search) throws Exception;

	//count
	public int getPurchaseTotalCount(Search search) throws Exception;
	
	//Iamport api 결제 검증
	public boolean validatePurchase(String impUid, Purchase purchase) throws IamportResponseException, IOException;
}
