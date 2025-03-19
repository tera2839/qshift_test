package com.quickshift.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quickshift.Session.AdminSession;
import com.quickshift.entity.AdminRequest;
import com.quickshift.entity.Calendar;
import com.quickshift.entity.Member;
import com.quickshift.entity.Store;
import com.quickshift.entity.Timeplan;
import com.quickshift.form.AddStoreForm;
import com.quickshift.form.AdminLoginForm;
import com.quickshift.form.CreateAccountForm;
import com.quickshift.security.CustomSecurityContextRepository;
import com.quickshift.service.AdminService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {
	
	private final AdminSession aSession;
	private final AdminService aService;
	private final AuthenticationManager authenticationManager; 
	
	@GetMapping("/")
	public String showIndex(
			@ModelAttribute
			AdminLoginForm form
			) {
		return "index";
	}

	@PostMapping("/completeLogin")
	public String completeLogin(
	        @Valid @ModelAttribute AdminLoginForm form,
	        BindingResult br,
	        HttpServletRequest request,  // HttpServletRequestを引数に追加
	        HttpServletResponse response,
	        Model model // HttpServletResponseを引数に追加
	) {
	    if (br.hasErrors()) {
	        return "index";  // バリデーションエラーの場合はログイン画面を再表示
	    }

	    // Spring Security の認証機能を使って認証を実行
	    try {
	        UsernamePasswordAuthenticationToken authenticationToken = 
	            new UsernamePasswordAuthenticationToken(form.getMail(), form.getPass());

	        // 認証を実行
	        Authentication authentication = authenticationManager.authenticate(authenticationToken);
	        System.out.println(authentication);

	        // 認証情報を SecurityContext に保存
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        // カスタム SecurityContextRepository を使ってセキュリティコンテキストをセッションに保存
	        SecurityContext securityContext = SecurityContextHolder.getContext();
	        CustomSecurityContextRepository securityContextRepository = new CustomSecurityContextRepository();
	        securityContextRepository.saveContext(securityContext, request, response);  // カスタムリポジトリに保存

	        // 認証が成功した場合
	        if (authentication.isAuthenticated()) {
	            System.out.println("Authenticated user: " + authentication.getName());
	        } else {
	            System.out.println("No authenticated user.");
	        }


	        return "redirect:/selectStore";  // ログイン後のリダイレクト先
	    } catch (AuthenticationException e) {
	        br.rejectValue("mail", null, "メールアドレスもしくはパスワードが間違っています");
	        return "index";  // 認証失敗の場合は再度ログイン画面を表示
	    }
	}
//	@PostMapping("/completeLogin")//上の元のコード
//	public String completeLogin(
//			@Valid @ModelAttribute
//			AdminLoginForm form,
//			BindingResult br
//			) {
//		
//		if(!service.isCanLogin(form.getMail(), form.getPass())) {
//			br.rejectValue("mail", null, "メールアドレスもしくはパスワード違います");
//			return "index";
//		}
//		if(br.hasErrors()) {
//			return "index";
//		}
//		
//		session.setAdmin(service.findByAdminMail(form.getMail()));
//		return "redirect:/selectStore";
//	}
	
	@GetMapping("/selectStore")
	public String showSelectStore(Model model) {
		
		List<Store> stores = aService.findStoreByAdmin(aSession.getAdmin());
		model.addAttribute("stores", stores);
		aSession.setStore(null);
		
		return "selectStore";
	}
	
	@RequestMapping("/adminHome")
	public String showAdminHome(
			@RequestParam(value = "id", required = false) String id
			) {
		
		if(id != null) {
			Store store = aService.findByStoreId(Long.parseLong(id));
			aSession.setStore(store);
		}
		
		return "adminHome";
	}
	/*------------------------------------------------------------
	  シフト作成
	 ------------------------------------------------------------*/
	@PostMapping("/managementMember")
	public String showManagementMember(
			Model model
			) {
		
		List<Member> members = aService.findMemberByStore(aSession.getStore());
		model.addAttribute("members", members);
		
		return "managementMember"; 
	}
	
	@PostMapping("/completeManagementMember")
	public String completeManagementMember(
			@RequestParam("name") String[] names
			) {
		
		List<Member> members = aService.findMemberByStore(aSession.getStore());
		
		for(Member member : members) {
			
			aService.updateMemberName(member.getId(), null);
		}
		
		for(int i = 0; i < names.length; i++) {
			
			Member member = members.get(i);
			aService.updateMemberName(member.getId(), names[i]);
		}
		
		return "redirect:/timeplan";
	}
	
	@GetMapping("/timeplan")
	public String showMTimePlan(
			Model model
			) {
		
		List<Timeplan> timeplans = aService.findTimeplanByStore(aSession.getStore());
		model.addAttribute("timeplans", timeplans);
		
		return "timeplan";
	}
	
	@PostMapping("/yearAndMonth")
	public String showYearAndMonth(
			@RequestParam("values") String[] values
			) {
		
		List<Timeplan> plans = aService.findTimeplanByStore(aSession.getStore());
		
		for(Timeplan plan : plans) {
			
			aService.updateTimeplan(plan.getId(), null, null, null);
		}
		
		for(int i = 0; i < values.length; i++) {
			
			String name = "";
			String fromTime = "";
			String toTime = "";
			
			String[] str =  values[i].split(":");
			
			if(str.length == 3) {
				name = str[0];
				fromTime = str[1];
				toTime = str[2];
				
				Timeplan plan = plans.get(i);
				
				aService.updateTimeplan(plan.getId(), name, fromTime, toTime);
			}
		}
		
		return "yearAndMonth";
	}
	
	@PostMapping("/completeYearAndMonth")
	public String completeYearAndMonth(
			@RequestParam("year") String year,
			@RequestParam("month") String month
			) {
		
		Calendar calendar = new Calendar();
		calendar.setYear(year);
		calendar.setMonth(month);
		aSession.setCalendar(calendar);
		
		return "redirect:/adminRequest";
	}
	
	@GetMapping("/adminRequest")
	public String showAdminRequest(
			Model model
			) {
		
		List<Timeplan> timeplans = aService.findTimeplanByStore(aSession.getStore());
		model.addAttribute("timeplans", timeplans);
		model.addAttribute("year", aSession.getCalendar().getYear());
		model.addAttribute("month", aSession.getCalendar().getMonth());
		
		return "adminRequest";
	}
	
	@PostMapping("confirmAdminRequest")
	public String showConfirmAdminRequest(
			@RequestParam("requestShift") String[] shifts,
			Model model
			) {
		
		List<Timeplan> timeplans = aService.findTimeplanByStore(aSession.getStore());
		model.addAttribute("timeplans", timeplans);
		model.addAttribute("year", aSession.getCalendar().getYear());
		model.addAttribute("month", aSession.getCalendar().getMonth());
		model.addAttribute("shifts", shifts);
		
		return "confirmAdminRequest";
	}
	
	@PostMapping("/completeAdminRequest")
	public String completeAdminRequest(
			@RequestParam("requestShift")String[] shifts
			) {
		
		for(int i = 0; i < shifts.length; i++) {
			
			String shift = shifts[i];
			String[] str = shift.split(":");
			Long plan = Long.parseLong(str[0]);
			String date = str[1];
			int num = Integer.parseInt(str[2]);
			
			AdminRequest request = new AdminRequest();
			request.setTimeplan(aService.findByTimeplanId(plan));
			request.setNum(num);
			request.setStore(aSession.getStore());
			Calendar calendar = aSession.getCalendar();
			calendar.setDate(date);
			request.setCalendar(calendar);
			
			aService.saveAdminRequest(request);
			
		}
		return "redirect:/resultAdminRequest";
	}
	
	@GetMapping("/resultAdminRequest")
	public String showResultAdminRequest() {
		return "resultAdminRequest";
	}
	/*------------------------------------------------------------
	  シフト編集
	 ------------------------------------------------------------*/
	@PostMapping("/adminEditHome")
	public String showAdminEditHome() {
		return "adminEditHome";
	}
	/*------------------------------------------------------------
	  アカウント作成
	 ------------------------------------------------------------*/
	@GetMapping("/createAccount")
	public String showCreateAccount(
			@ModelAttribute
			CreateAccountForm form
			) {
		return "createAccount";
	}
	@PostMapping("/confirmCreateAccount")
	public String showConfirmCreateAccount(
			@Valid @ModelAttribute
			CreateAccountForm form,
			BindingResult br
			) {
		
		if(!aService.matchPass(form.getPass1(), form.getPass2())) {
			br.rejectValue("pass1", null, "パスワードが一致しません");
		}
		if(aService.existMail(form.getMail())) {
			br.rejectValue("mail", null, "メールアドレスが既に存在しています");
		}
		if(br.hasErrors()) {
			return "createAccount";
		}
		return "confirmCreateAccount";
	}
	@PostMapping("/completeCreateAccount")
	public String completeCreateAccount(
			@Valid @ModelAttribute
			CreateAccountForm form,
			BindingResult br
			) {
		
		if(!aService.matchPass(form.getPass1(), form.getPass2())) {
			br.rejectValue("pass1", null, "パスワードが一致しません");
		}
		if(aService.existMail(form.getMail())) {
			br.rejectValue("mail", null, "メールアドレスが既に存在しています");
		}
		if(br.hasErrors()) {
			return "createAccount";
		}
		
		form.setPass1(aService.Encoder(form.getPass1()));//passハッシュ化していれる
		form.setPass2(aService.Encoder(form.getPass2()));
		
		aService.saveAdmin(form);
		
		return "redirect:/resultCreateAccount";
	}
	@GetMapping("/resultCreateAccount")
	public String showResultCreateAccount() {
		return "resultCreateAccount";
	}
	
	/*------------------------------------------------------------
	  店舗新規登録
	 ------------------------------------------------------------*/
	@GetMapping("/addStore")
	public String showAddStore(
			@ModelAttribute
			AddStoreForm form
			) {
		return "addStore";
	}
	@PostMapping("/confirmAddStore")
	public String showConfirmAddStore(
			@Valid @ModelAttribute
			AddStoreForm form,
			BindingResult br
			) {
		if(!aService.matchPass(form.getPass1(), form.getPass2())) {
			br.rejectValue("pass1", null, "パスワードが一致しません");
		}
		if(br.hasErrors()) {
			return "addStore";
		}
		return "confirmAddStore";
	}
	@PostMapping("/completeAddStore")
	public String completeAddStore(
			@Valid @ModelAttribute
			AddStoreForm form,
			BindingResult br
			) {
		if(!aService.matchPass(form.getPass1(), form.getPass2())) {
			br.rejectValue("pass1", null, "パスワードが一致しません");
		}
		if(br.hasErrors()) {
			return "addStore";
		}
		
		aService.saveStore(form, aSession.getAdmin());
		List<Store> stores = aService.findStoreByAdmin(aSession.getAdmin());
		long max = 0;
		for(Store store : stores) {
			long id = store.getId();
			if(id > max) {
				max = id;
			}
		}
		Store store = aService.findByStoreId(max);
		
		for(int i = 0; i < 15; i++) {
			Timeplan plan = new Timeplan();
			plan.setStore(store);
			aService.saveTimeplan(plan);
		}
		for(int i = 0; i < 30; i++) {
			Member member = new Member();
			member.setStore(store);
			aService.saveMember(member);
		}
		
		return "redirect:/resultAddStore";
	}
	@GetMapping("/resultAddStore")
	public String showResultAddStore() {
		return "resultAddStore";
	}
	
	/*------------------------------------------------------------
	シフト締め切り
	------------------------------------------------------------*/
	@PostMapping("/closingHome")
	public String showClosingHome(
			Model model
			) {
		
		model.addAttribute("months", aService.findClosingMonth(aSession.getStore()));
		
		return "closingHome";
	}
	
	@PostMapping("/closingShift")
	public String showClosingShift(
			Model model,
			@RequestParam("month") String month
			) {
		
		List<Timeplan> plans = aService.findTimeplanByStore(aSession.getStore());
		List<Member> members = aService.findMemberByStore(aSession.getStore());
		List<AdminRequest> adminRequests = aService.findAdminRequestByStore(aSession.getStore());
		List<String> requests = new ArrayList<String>();
		for(AdminRequest ar : adminRequests) {
			String temp = ar.getCalendar().getDate() + ":" + ar.getTimeplan().getId() + ":" + ar.getNum();
			requests.add(temp);
		}
		
		model.addAttribute("plans", plans);
		model.addAttribute("members", members);
		model.addAttribute("requests", requests);
		model.addAttribute("month", month);
		
		return "closingShift";
	}
	
	
}