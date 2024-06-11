package com.mapmory.controller.timeline;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mapmory.common.domain.Search;
import com.mapmory.common.util.ContentFilterUtil;
import com.mapmory.common.util.ImageFileUtil;
import com.mapmory.common.util.ObjectStorageUtil;
import com.mapmory.common.util.TextToImage;
import com.mapmory.common.util.TimelineUtil;
import com.mapmory.services.timeline.domain.Category;
import com.mapmory.services.timeline.domain.ImageTag;
import com.mapmory.services.timeline.domain.Record;
import com.mapmory.services.timeline.service.TimelineService;


@Controller("timelineController")
@RequestMapping("/timeline/*")
public class TimelineController {
	@Autowired
	@Qualifier("timelineService")
	private TimelineService timelineService;
	
	@Autowired
	private TextToImage textToImage;
	
	@Autowired
	private ContentFilterUtil contentFilterUtil;
	
    @Autowired
    private ObjectStorageUtil objectStorageUtil;
	
	@Value("${default.time}")
	private String defaultTime;
	
	@Value("${object.timeline.image}")
	private String imageFileFolder;
	
	@Value("${object.timeline.media}")
	private String mediaFileFolder;
	
	@GetMapping("getTimelineList")
	public String getTimelineList(Model model,
			@RequestParam(value="userId", required = true) String userId,
			@RequestParam(value="selectDay", required = false) Date selectDay) throws Exception,IOException{
		if(selectDay==null) {
//		LocalDate today = LocalDate.now();
		LocalDate today = LocalDate.of(2024,5,29);
		selectDay=Date.valueOf(today);
		}
		LocalDate tomorrow = selectDay.toLocalDate();
		
		Search search = Search.builder()
				.userId(userId)
				.selectDay1(selectDay+" "+defaultTime)
				.selectDay2((Date.valueOf(tomorrow.plusDays(1)))+" "+defaultTime)
				.timecapsuleType(0)
				.build();
		model.addAttribute("timelineList", timelineService.getTimelineList(search));
		return "timeline/getTimelineList";
	}
	
	@GetMapping("getTimecapsuleList")
	public String getTimecapsuleList(Model model,
			@RequestParam(value="userId", required = true) String userId) throws Exception,IOException{
		Search search = Search.builder()
				.userId(userId)
				.tempType(1)
				.timecapsuleType(1)
				.build();
		model.addAttribute("timecapsuleList", timelineService.getTimelineList(search));
		return "timeline/getTimecapsuleList";
	}
	
	@GetMapping("getTempTimecapsuleList")
	public String getTempTimecapsuleList(Model model,
			@RequestParam(value="userId", required = true) String userId) throws Exception,IOException{
		Search search = Search.builder()
				.userId(userId)
				.tempType(0)
				.timecapsuleType(1)
				.build();
		model.addAttribute("timecapsuleList", timelineService.getTimelineList(search));
		return "timeline/getTempTimecapsuleList";
	}
	
	@GetMapping("getSummaryRecord")
	public String getSummaryRecord(Model model,
			@RequestParam(value="userId", required = true) String userId,
			@RequestParam(value="selectDate", required = false) Date selectDate) throws Exception,IOException{
		if(selectDate==null) {
//		LocalDate today = LocalDate.now();
//		selectDate = Date.valueOf(today);
		selectDate = Date.valueOf("2024-05-29");
		}
		Search search = Search.builder()
				.userId(userId)
				.selectDate(selectDate)
				.build();
		model.addAttribute("record", timelineService.getSummaryRecord(search));
		return "timeline/getSummaryRecord";
	}
	
	@GetMapping("getSimpleTimeline")
	public String getSimpleTimeline(Model model,
			@RequestParam(value="recordNo", required = true) int recordNo) throws Exception,IOException {
		model.addAttribute("record",timelineService.getDetailTimeline(recordNo));
		return "timeline/getSimpleTimeline";
	}
	
	@GetMapping("getDetailTimeline")
	public String getDetailTimeline(Model model,
			@RequestParam(value="recordNo", required = true) int recordNo) throws Exception,IOException {
		Record record=timelineService.getDetailTimeline(recordNo);
		record.setRecordText(textToImage.processImageTags(record.getRecordText()));
		model.addAttribute("record",record);
		return "timeline/getDetailTimeline";
	}
	
	@GetMapping("deleteTimeline")
	public void deleteTimeline(Model model,
			@RequestParam(value="recordNo", required = true) int recordNo,
			@RequestParam(value="userId", required = true) String userId) throws Exception,IOException {
		timelineService.deleteTimeline(recordNo);
		timelineService.deleteImage(recordNo);
		getTimelineList(model, userId, null);
	}
	
	@GetMapping("getDetailTimecapsule")
	public String getDetailTimecapsule(Model model,
			@RequestParam(value="recordNo", required = true) int recordNo) throws Exception,IOException {
		Record record=timelineService.getDetailTimeline(recordNo);
		record.setRecordText(textToImage.processImageTags(record.getRecordText()));
		model.addAttribute("record",record);
		return "timeline/getDetailTimecapsule";
	}
	
	@GetMapping("updateTimeline")
	public String updateTimelineView(Model model,
			@RequestParam(value="recordNo", required = true) int recordNo) throws Exception,IOException {
		Record record = timelineService.getDetailTimeline(recordNo);
		
		model.addAttribute("hashtagText",TimelineUtil.hashtagListToText(record.getHashtag()));
		model.addAttribute("category", timelineService.getCategoryList());
		model.addAttribute("record",record);
		return "timeline/updateTimeline";
	}
	
	@PostMapping("updateTimeline")
	public String updateTimeline(Model model,
			@ModelAttribute("record") Record record,
			@ModelAttribute("hashtagText") String hashtagText,
			@RequestParam("imageNameText") List<String> imageNameText,
			@RequestParam("imageFile") List<MultipartFile> imageFile) throws Exception,IOException {
		List<ImageTag> imageName=new ArrayList<ImageTag>();
		if( !(imageFile==null) ) {
		
		System.out.println("List<String> imageNameText : "+imageNameText);
		System.out.println("List<MultipartFile> imageFile : "+imageFile);
		
		for (MultipartFile image : imageFile) {
			if (contentFilterUtil.checkBadImage(image) == false) {
				System.out.println("이미지 검사 통과");
				String uuid = ImageFileUtil.getImageUUIDFileName(image.getOriginalFilename());
				objectStorageUtil.uploadFileToS3(image, uuid, imageFileFolder);
				imageName.add(
						ImageTag.builder().recordNo(record.getRecordNo()).imageTagType(1).imageTagText(uuid).build());
			} else {
				System.out.println(image.getOriginalFilename() + " 번째 이미지, 차단");
			}
		}
	}
		record.setImageName(imageName);
		
		record.setHashtag(TimelineUtil.hashtagTextToList(hashtagText, record.getRecordNo()));
		
		record.setUpdateCount(record.getUpdateCount()+1);
		
		if(record.getMediaName()!=null || record.getImageName()!=null || record.getRecordText()!=null) {
			if(record.getRecordAddDate()==null) {
				record.setRecordAddDate(LocalDateTime.now().toString().replace("T", " "));
			}
			record.setTempType(1);
		}else {
			record.setTempType(0);
		}
		
		System.out.println("record.getImageName() : "+record.getImageName());
		System.out.println("record.getHashtag() : "+record.getHashtag());
		timelineService.updateTimeline(record);
		model.addAttribute("record",timelineService.getDetailTimeline(record.getRecordNo()));
		return "timeline/getDetailTimeline";
	}
	
	@GetMapping("addTimecapsule")
	public String addTimecapsuleView(Model model,
			@RequestParam(value="userId", required = false) String userId) throws Exception,IOException {
		model.addAttribute("category", timelineService.getCategoryList());
		model.addAttribute("userId",userId);
		return "timeline/addTimecapsule";
	}
	
	@PostMapping("addTimecapsule")
	public String addTimecapsule(Model model,@ModelAttribute(value="record") Record record) throws Exception,IOException {
		System.out.println("record.getImageName() : "+record.getImageName());
		System.out.println("record.getHashtag() : "+record.getHashtag());
		timelineService.updateTimeline(record);
		model.addAttribute("record",timelineService.getDetailTimeline(record.getRecordNo()));
		return "timeline/getDetailTimeline";
	}
	
	@GetMapping("updateTimecapsule")
	public String updateTimecapsuleView(Model model,
			@RequestParam(value="recordNo", required = false) Integer recordNo) throws Exception,IOException {
		model.addAttribute("category", timelineService.getCategoryList());
		model.addAttribute("record",timelineService.getDetailTimeline(recordNo));
		return "timeline/updateTimecapsule";
	}
	
	@PostMapping("updateTimecapsule")
	public String updateTimecapsule(Model model,@ModelAttribute Record record) throws Exception,IOException {
		record.setUpdateCount(record.getUpdateCount()+1);
		if(record.getMediaName()!=null || record.getImageName()!=null || record.getRecordText()!=null) {
			record.setTempType(1);
		}else {
			record.setTempType(0);
		}
		System.out.println("record.getImageName() : "+record.getImageName());
		System.out.println("record.getHashtag() : "+record.getHashtag());
		timelineService.updateTimeline(record);
		model.addAttribute("record",timelineService.getDetailTimeline(record.getRecordNo()));
		return "timeline/getDetailTimeline";
	}
	
	@GetMapping("addVoiceToText")
	public String addVoiceToText() throws Exception,IOException {
		return "timeline/addVoiceToText";
	}
	
	@GetMapping("getUserCategoryList")
	public String getUserCategoryList(Model model) throws Exception,IOException{
		model.addAttribute("categoryList", timelineService.getCategoryList());
		return "timeline/getUserCategoryList";
	}
	
	@GetMapping("getAdminCategoryList")
	public String getAdminCategoryList(Model model) throws Exception,IOException{
		model.addAttribute("categoryList", timelineService.getCategoryList());
		return "timeline/admin/getAdminCategoryList";
	}
	
	@GetMapping("addCategory")
	public String addCategoryView() throws Exception,IOException {
		return "timeline/admin/addCategory";
	}
	
	@PostMapping("addCategory")
	public String addCategory(Model model,@ModelAttribute Category category) throws Exception,IOException {
		timelineService.addCategory(category);
		model.addAttribute("categoryList",timelineService.getCategoryList());
		return "timeline/admin/getAdminCategoryList";
	}
	
	
	@GetMapping({"getDetailTimeline3"})
	public void getDetailTimeline3(Model model) throws Exception,IOException {
		model.addAttribute("record",timelineService.getDetailTimeline(1));
		model.addAttribute("record2",timelineService.getDetailSharedRecord(1));
	}
	
	@GetMapping({"getDetailTimeline2"})
	public void getTimelineList2(Model model) throws Exception,IOException {
//		List<Record> recordList=new ArrayList<Record>();
		Search search=Search.builder()
				.currentPage(1)
				.limit(3)
				.build();
		model.addAttribute("list",timelineService.getTimelineList(search));
		search=Search.builder()
				.currentPage(1)
				.limit(3)
				.sharedType(1)
				.tempType(1)
				.timecapsuleType(0)
				.build();
		model.addAttribute("list2",timelineService.getTimelineList(search));
		search=Search.builder()
				.currentPage(1)
				.limit(3)
				.sharedType(0)
				.tempType(0)
				.timecapsuleType(0)
				.build();
//		for(Record record:timelineService.getTimelineList(search)) {
//			if(record != null)
//			recordList.add(record);
//		}
//		System.out.println("recordList : "+recordList);
//		model.addAttribute("list3",recordList);
		model.addAttribute("list3",timelineService.getTimelineList(search));
		search=Search.builder()
				.currentPage(3)
				.limit(3)
				.sharedType(0)
				.tempType(1)
				.timecapsuleType(0)
				.build();
		model.addAttribute("list4",timelineService.getTimelineList(search));
		search=Search.builder()
				.currentPage(3)
				.limit(3)
				.sharedType(1)
				.tempType(1)
				.timecapsuleType(0)
				.build();
		model.addAttribute("list5",timelineService.getTimelineList(search));
		search=Search.builder()
				.currentPage(1)
				.limit(5)
				.sharedType(0)
				.tempType(0)
				.timecapsuleType(1)
				.build();
		model.addAttribute("list6",timelineService.getTimelineList(search));
		//d_day보다 현재 날짜가 위에 있으면 갖고오는 조건식
		search=Search.builder()
				.currentPage(1)
				.limit(3)
				.sharedType(0)
				.tempType(1)
				.timecapsuleType(1)
				.build();
		model.addAttribute("list7",timelineService.getTimelineList(search));
		//대민 지원
		search=Search.builder()
				.userId(" user2 ").
				currentPage(1)
				.limit(5)
				.sharedType(1)
				.tempType(1)
				.timecapsuleType(0)
				.build();
		model.addAttribute("list8",timelineService.getTimelineList(search));
		//재용 지원
		search=Search.builder()
				.currentPage(1)
				.limit(5)
				.build();
		model.addAttribute("list9",timelineService.getSharedRecordList(search));
		search=Search.builder()
				.currentPage(2)
				.limit(5)
				.build();
		model.addAttribute("list10",timelineService.getSharedRecordList(search));
		search=Search.builder()
				.timecapsuleType(0)
				.selectDay1("2024-06-04 00:00:00")
				.selectDay2("2024-06-05 00:00:00")
				.build();
		model.addAttribute("list11",timelineService.getTimelineList(search));
		
		//성문 지원
		search=Search.builder()
	 			.latitude(33.450701)
	 			.longitude(126.570667)
	 			.radius(110)
	 			.limit(10)
	 			.userId("user1")
	 			.followType(1)
	 			.currentPage(1)
				.build();
		model.addAttribute("list12",timelineService.getMapRecordList(search));
		search=Search.builder()
				.latitude(33.450701)
	 			.longitude(126.570667)
	 			.radius(110)
	 			.limit(10)
	 			.userId("user1")
	 			.sharedType(1)
	 			.currentPage(1)
				.build();
		model.addAttribute("list13",timelineService.getMapRecordList(search));
		search=Search.builder()
				.latitude(37.56789)
	 			.longitude(127.345678)
	 			.radius(110)
	 			.limit(10)
	 			.userId("user1")
	 			.currentPage(1)
				.build();
		model.addAttribute("list14",timelineService.getMapRecordList(search));
		search=Search.builder()
				.latitude(33.450701)
	 			.longitude(126.570667)
	 			.radius(110)
	 			.limit(5)
	 			.currentPage(1)
				.build();
		model.addAttribute("list15",timelineService.getMapRecordList(search));
		search=Search.builder()
				.userId("user1")
				.selectDate(Date.valueOf("2024-05-29"))
				.build();
		model.addAttribute("list16",timelineService.getSummaryRecord(search));
	}
	
	public void updateTimeline(Model model) throws Exception,IOException{
//		model.addAttribute("record",timelineService.getDetailTimeline(1));
	}
	
	
	//아래 미사용
//	@GetMapping({"getDetailTimeline2"})
//	public void getDetailTimeline2(Model model) throws Exception,IOException {
//		model.addAttribute("record",timelineService.getDetailTimeline2(1));
//	}
}
