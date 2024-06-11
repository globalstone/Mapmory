package com.mapmory.controller.user;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mapmory.common.util.ContentFilterUtil;
import com.mapmory.common.util.ObjectStorageUtil;
import com.mapmory.services.user.domain.Login;
import com.mapmory.services.user.domain.User;
import com.mapmory.services.user.service.LoginService;
import com.mapmory.services.user.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	@Qualifier("userServiceImpl")
	private UserService userService;
	
	@Autowired
	private LoginService loginService;
	
	
	@Autowired
	@Qualifier("objectStorageUtil")
	private ObjectStorageUtil objectStorageUtil;
	
	@Autowired
	private ContentFilterUtil contentFilterUtil;
	
	@Value("${object.profile.folderName}")
	private String PROFILE_FOLDER_NAME;


	@GetMapping("/testUpdateProfile")
	public void testGetUpdateProfile(Model model) throws Exception {
		
		String userId = "user1";
		
		User user = userService.getDetailUser(userId);
		
		String cdnPath = objectStorageUtil.getImageUrl(user.getProfileImageName(), PROFILE_FOLDER_NAME);
		
		model.addAttribute("profileImage", cdnPath);
		
	}
	
	@PostMapping("/testUpdateProfile")
	public void testPostUpdateProfile(@RequestParam(name = "profile") MultipartFile file, @RequestParam String introduction, Model model) throws Exception {
		
		String userId = "user1";
		
		if(contentFilterUtil.checkBadImage(file)) {
			System.out.println("부적절한 이미지입니다.");
		}
		
		boolean result = userService.updateProfile(file, userId, file.getOriginalFilename(), introduction);
		System.out.println(result);
		
		User user = userService.getDetailUser(userId);
		
		String cdnPath = objectStorageUtil.getImageUrl(user.getProfileImageName(), PROFILE_FOLDER_NAME);
		
		model.addAttribute("profileImage", cdnPath);
	}
	
	@GetMapping("/login")
	public void getLogin() {
		
		// userService.setupForTest();
		System.out.println("login test");
	}
	
	@PostMapping("/login")
	public void postLogin(@ModelAttribute Login login, HttpSession session, HttpServletResponse response) throws Exception{
		
		System.out.println("cookie 요청");
		
		if ( !loginService.login(login, userService.getPassword(login.getUserId())) )
			throw new Exception("아이디 또는 비밀번호가 잘못되었습니다.");

		String userId = login.getUserId();
		byte role = userService.getDetailUser(userId).getRole();
		String sessionId = UUID.randomUUID().toString();
		if ( !loginService.insertSessionInRedis(login, role, sessionId))
			throw new Exception("redis에 값이 저장되지 않음.");
		
		Cookie cookie = new Cookie("JSESSIONID", sessionId);
		cookie.setPath("/");
		// cookie.setDomain("mapmory.life");
		// cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		
		if(role == 1)
			response.sendRedirect("/map/map");
		else
			response.sendRedirect("/user/admin/adminMain");
	}
}
