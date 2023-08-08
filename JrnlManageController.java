package kr.oprs.admin.jrnlManage.web;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.argo.journal.LanguageService;
import com.argo.journal.JournalService;
import com.argo.journal.PropertiesService;
import com.argo.journal.SessionService;
import com.argo.journal.beans.CodeVO;
import com.argo.journal.beans.Journal;
import com.argo.journal.beans.MetaConfigVO;
import com.argo.journal.beans.User;

import kr.oprs.admin.apc.ApcDTO;
import kr.oprs.admin.apc.service.ApcPageService;
import kr.oprs.admin.jrnlManage.service.JrnlManageService;
import kr.oprs.admin.submission.service.SubmissionService;

@Controller
@RequestMapping("{siteName}/admin/oprs/jrnlManage/")
public class JrnlManageController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private PropertiesService propertiesService;
	
	@Autowired
	private JrnlManageService jrnlManageService;
	
	@Autowired
	private ApcPageService apcPageService;
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private LanguageService languageService;
	
	@Autowired
	private JournalService journalService;

	
	private Logger Logger = LoggerFactory.getLogger(getClass());
	/**
	 * 정책정보관리 > 논문 출판비용(APC) 정보
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("/policyInfoManageApcInfo.do")
	public ModelAndView policyInfoManageApcInfo(HttpServletRequest request) throws IOException, AuthenticationException, Exception {
		if(!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();
		
		Journal journal = sessionService.getSite(request);
		int journalno = journal.getJournalNo();
		ModelAndView mav = new ModelAndView("admin/oprs/jrnlManage/apcPage");
		String journalSetting = propertiesService.getProperty("system", "journal-setting", journal.getJournalNo());
		Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journalno);
		
		ApcDTO dto = new ApcDTO();
		dto = apcPageService.getApcDTO(journal);//APC 정보
		
		if (journalSetting.equals("no")) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/oprs/jrnlManage/apcPage");
		} else {
			mav.addObject("journalSetting", journalSetting);
		}
		
        mav.addObject("jrnlConfig", jrnlConfig);
		mav.addObject("apcDTO", dto);
		mav.addObject("tabTy", "5");//탭번호
		return mav;
	}
	
	@ResponseBody
	@PostMapping("/policyInfoManageApcInfoAjax.do")
	public void ApcAjax(@ModelAttribute ApcDTO apcDTO, HttpServletRequest request)throws IOException, AuthenticationException, Exception{
		Journal journal = sessionService.getSite(request);
		User user = (User)request.getSession().getAttribute(journal.getJournalUrl() + "_" + "admin_info");
		String jrnlSeq = journal.getJrnlSeq();
		apcPageService.setLanguages(request);
		apcPageService.deleteApcInfos(jrnlSeq); //0. 이전 APC 세팅 삭제
		apcPageService.setApcVO(apcDTO,journal,user);//1. 현 APC 세팅 저장
		apcPageService.setApcOA(apcDTO,journal,user);//2. 현 OA 세팅 저장
	  
	}
}
