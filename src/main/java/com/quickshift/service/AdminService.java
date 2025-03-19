package com.quickshift.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quickshift.entity.Admin;
import com.quickshift.entity.AdminRequest;
import com.quickshift.entity.Member;
import com.quickshift.entity.Store;
import com.quickshift.entity.Timeplan;
import com.quickshift.form.AddStoreForm;
import com.quickshift.form.CreateAccountForm;
import com.quickshift.repository.AdminRepository;
import com.quickshift.repository.AdminRequestRepository;
import com.quickshift.repository.MemberRepository;
import com.quickshift.repository.StoreRepository;
import com.quickshift.repository.TimeplanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	
	private final AdminRepository adminRep;
	private final StoreRepository storeRep;
	private final MemberRepository memberRep;
	private final TimeplanRepository timeplanRep;
	private final AdminRequestRepository requestRep;
	private final PasswordEncoder passwordEncoder;
	
	public boolean existMail(String mail) {
		
		return adminRep.findByMail(mail) != null;
	}
	
	public boolean matchPass(String pass1, String pass2) {
		
	    if (pass1 == null || pass2 == null) {
	        return false;
	    }
	    return pass1.equals(pass2);
	}
	
	public boolean isCanLogin(String mail, String pass) {
		
		Admin admin = adminRep.findByMail(mail);
	    //return admin != null && pass.equals(admin.getAdminPass());
		return admin != null && passwordEncoder.matches(pass, admin.getPass());
	}
	/*--------------------------------------------------------
	  Adminテーブル操作
	 --------------------------------------------------------*/
	public Admin findByAdminMail(String mail) {
		return adminRep.findByMail(mail);
	}
	@Transactional
	public void saveAdmin(CreateAccountForm form) {
		
		Admin admin = new Admin();
		admin.setMail(form.getMail());
		admin.setName(form.getName());
		admin.setPass(form.getPass1());
		
		adminRep.save(admin);
	}
	/*--------------------------------------------------------
	  Storeテーブル操作
	 --------------------------------------------------------*/
	public Store findByStoreId(Long id) {
		
		return storeRep.findById(id).get();
	}
	
	public List<Store> findStoreByAdmin(Admin admin){
		
		return storeRep.findByAdmin(admin);
	}
	
	@Transactional
	public void saveStore(AddStoreForm form, Admin admin) {
		
		Store store = new Store();
		store.setName(form.getName());
		store.setPass(form.getPass1());
		store.setAdmin(admin);
		
		storeRep.save(store);
	}
	/*--------------------------------------------------------
	  Memberテーブル操作
	 --------------------------------------------------------*/
	public List<Member> findMemberByStore(Store store){
		
		return memberRep.findByStore(store);
	}
	
	@Transactional
	public void updateMemberName(Long id, String name) {
		
		memberRep.updateName(id, name);
	}
	
	@Transactional
	public void deleteAllMember(Store store) {
		
		memberRep.deleteAllByStore(store);
	}
	@Transactional
	public void saveMember(Member member) {
		
		memberRep.save(member);
	}
	/*--------------------------------------------------------
	  Timeplanテーブル操作
	 --------------------------------------------------------*/
	public Timeplan findByTimeplanId(Long id) {
		return timeplanRep.findById(id).get();
	}
	
	public List<Timeplan> findTimeplanByStore(Store store){
		
		return timeplanRep.findByStore(store);
	}
	
	@Transactional
	public void deleteAllTimeplan(Store store) {
		
		timeplanRep.deleteAllByStore(store);
	}
	
	@Transactional
	public void saveTimeplan(Timeplan timeplan) {
		
		timeplanRep.save(timeplan);
	}
	
	@Transactional
	public void updateTimeplan(Long id, String name, String from, String to) {
		
		timeplanRep.updateName(id, name, from, to);
	}
	/*--------------------------------------------------------
	  AdminRequestテーブル操作
	 --------------------------------------------------------*/
	@Transactional
	public void saveAdminRequest(AdminRequest request) {
		
		requestRep.save(request);
	}
	
	public List<AdminRequest> findAdminRequestByStore(Store store){
		
		return requestRep.findByStore(store);
	}
	/*--------------------------------------------------------
	  Shiftテーブル操作
	 --------------------------------------------------------*/
	public List<String> findClosingMonth(Store store) {
		
		List<AdminRequest> requests = requestRep.findByStore(store);
		List<String> months = new ArrayList<String>();
		
		for(AdminRequest req : requests) {
			
			String str = req.getCalendar().getYear() + ":" + req.getCalendar().getMonth();
			
			if(!months.contains(str)) {
				months.add(str);
			}
		}
		
		return months;
	}
	
	/*=*=*=*=*=* 自動シフト作成ロジック *=*=*=*=*=*/
//	public List<Shift> autoShift(List<Member> members, List<AdminRequest> adminRequests, List<MemberRequest> MemberRequests){
//		
//		int[] counts = new int[members.size()];
//		
//		for(AdminRequest request : adminRequests) {
//			
//			int num = request.getNum();
//			
//			
//		}
//	}
	
	/*--------------------------------------------------------
	  パスワード操作
	 --------------------------------------------------------*/
	
	public String Encoder(String pass) {
      return passwordEncoder.encode(pass);
    }
	
	
}