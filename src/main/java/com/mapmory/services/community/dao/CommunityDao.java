package com.mapmory.services.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mapmory.common.domain.Search;
import com.mapmory.services.community.domain.CommunityLogs;
import com.mapmory.services.community.domain.Reply;
import com.mapmory.services.community.domain.Report;
import com.mapmory.services.user.domain.FollowBlock;

@Mapper
public interface CommunityDao {
	
	//댓글 목록 조회
	public List<Object> getReplyList(Search search, int recordNo) throws Exception;
	
	//내가 쓴 댓글 목록 조회
	public List<Object> getUserReplyList(Search search, String userId) throws Exception;
		
	//댓글 작성
	public void addReply(Reply reply) throws Exception;
	
	//댓글 선택
	public Reply getReply(int replyNo) throws Exception;
	
	//댓글 수정
	public void updateReply(Reply reply) throws Exception;
	
	//댓글 삭제
	public void deleteReply(String userId, int replyNo) throws Exception;

	//커뮤니티 활동 추가
	public void addCommunityLogs(CommunityLogs communityLogs) throws Exception;
	
	//커뮤니티 활동 로그 선택
	public CommunityLogs getCommunityLogs(int commmunityLogsNo) throws Exception;
	
	//커뮤니티 활동 수정
	public void updateCommunityLogs(CommunityLogs communityLogs) throws Exception;
	
	//커뮤니티 활동 삭제
	public void deleteCommunityLogs(int communityLogsNo, String userId) throws Exception;	
	
	//커뮤니티 로그 목록 조회
	public List<Object> getCommunityLogsList(Search search, String userId, int logsType) throws Exception;		
	
	//신고 정보 가져오기
	public void addReport(Report report) throws Exception;
	
	//신고하기 제출
	public void doReport(Report report) throws Exception;
	
	//신고 목록 조회 
	public List<Object> getUSerReportList(Search search, String userId) throws Exception;
	
	//전체 신고 목록 조회(관리자)
	public List<Object> getAdminReportList(Search search, int role) throws Exception;
	
	//신고 내용 상세 조회
	public Report getReport(int reportNo) throws Exception;
	
	//신고 처리(관리자)
	public void confirmReport(int reportNo) throws Exception;
		
	//사용자 차단
	public void addBlockUser(FollowBlock followBlock) throws Exception;
	
	//차단한 사용자 목록 조회
	public List<Object> getBlockedList(Search search, String userId) throws Exception;
	
	//차단 사용자 선택
	public FollowBlock getBlockedUser(String userId, String targetId) throws Exception;
	
	//사용자 차단해제
	public void deleteBlockedUser(String userId, String targetId) throws Exception;
		
	//기록에 대한 댓글 개수
	public int getReplyTotalCount(Search search, int recordNo) throws Exception;
	
	//내가 작성한 댓글 개수
	public int getReplyUserTotalCount(Search search, String userId) throws Exception;
		
	//좋아요 개수
	public int getReactionLikeTotalCount(Search search, int replyNo) throws Exception;
	
	//싫어요 개수
	public int getReactionDisLikeTotalCount(Search search, int replyNo) throws Exception;
		
	//사용자 신고 총 개수
	public int getUserReportTotalCount(Search search, String userId) throws Exception;
	
	//신고 총 개수
	public int getAdminReportTotalCount(Search search) throws Exception;
	
	//신고 미처리 총 개수
	public int getUnConfirmReportTotalCount(Search search) throws Exception;
	
	//차단 총 개수
	public int getBlockedTotalCount(Search search, String userId) throws Exception;
	
}