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
	 * 설정에 따른 정책정보 관리 페이지 호출
	 * 
	 * @param request
	 * @return
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("policyInfoManage")
	public ModelAndView policyInfoManage(HttpServletRequest request) throws AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();

		Journal journal = sessionService.getSite(request);

		if ("Y".equals(journal.getUseJR()) && "N".equals(journal.getUseAP())) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/oprs/jrnlManage/policyInfoManageLicense.do");
		} else {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/oprs/jrnlManage/policyInfoManageOaPolicy.do");
		}
	}

	/**
	 * OA정책 및 동료심사방식 페이지 호출
	 * 
	 * @param request
	 * @return
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("policyInfoManageOaPolicy.do")
	public ModelAndView policyInfoManageOaPolicy(HttpServletRequest request) throws AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();

		Journal journal = sessionService.getSite(request);
		ModelAndView mav = new ModelAndView("admin/oprs/jrnlManage/oaPolicy");

		String journalSetting = propertiesService.getProperty("system", "journal-setting", journal.getJournalNo());
		if (journalSetting.equals("no")) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/");
		} else {
			mav.addObject("journalSetting", journalSetting);
		}

		// 학술지 설정 정보 조회
		Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journal.getJournalNo());
		mav.addObject("jrnlConfig", jrnlConfig);
		Map<String, Object> configMap = submissionService.selectMetaConfig(journal.getJrnlSeq());
		mav.addAllObjects(configMap);

		// 온라인 심사양식 설정 정보 조회 (확정된 최신설정정보 또는 버전1)
		Map<String, String> onlinReviewMap = jrnlManageService.selectOnlineReviewUseAt(journal.getJrnlSeq());
		mav.addObject("onlinReviewMap", onlinReviewMap);

		mav.addObject("tabTy", "1");
		return mav;
	}

	/**
	 * OA정책 및 동료심사방식 저장 Ajax
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("oaPolicyRegistAjax.do")
	public Object oaPolicyRegistAjax(@RequestBody Map<String, String> oaPolicyMap, HttpServletRequest request) throws IOException, AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		Journal journal = sessionService.getSite(request);		
		User user = (User)request.getSession().getAttribute(journal.getJournalUrl() + "_" + "admin_info");

		Map<String, Object> jrMetaVo = new HashMap<String, Object>();
		List<Map<String, Object>> localeList = propertiesService.getProperties("locale", journal.getJournalNo());
		if (localeList.size() > 1) {
			jrMetaVo.put("locale", "ko");
		} else {
			jrMetaVo.put("locale", localeList.get(0).get("name").toString());
		}

		jrMetaVo.put("packages", "info");
		jrMetaVo.put("journalno", journal.getJournalNo());
		jrMetaVo.put("jrnlSeq", journal.getJrnlSeq());
		jrMetaVo.put("updtId", user.getId());

		Iterator<String> jrConfKeySet = oaPolicyMap.keySet().iterator();

		while (jrConfKeySet.hasNext()) {
			String key = jrConfKeySet.next();
			String value = oaPolicyMap.get(key);

			// OA여부
			if ("journal.oaYn".equals(key)) {
				jrMetaVo.put("messagekey", key);
				jrMetaVo.put("messagecontent", value);
				languageService.updateLanguageInfo(jrMetaVo);
			} else {
				jrMetaVo.put("key", key);
				jrMetaVo.put("value", value);
				submissionService.updateJournalConfig(jrMetaVo);

				if ("peerReviewType".equals(key)) {
					// 동료 심사 방식 변경에 따른 논문 파일 업로드 설정값 변경
//					submissionService.updatePeerReviewFileConfig(jrMetaVo); (추후 리뉴얼전 커밋 시 주석제거)
				}
			}
		}

		return Collections.singletonMap("message", "변경되었습니다");
	}

	/**
	 * 정책정보관리 > 자유이용 라이센스
	 * @param request
	 * @return
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("/policyInfoManageLicense.do")
	public ModelAndView policyInfoManageLicense(HttpServletRequest request) throws AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();

		Journal journal = sessionService.getSite(request);
		ModelAndView mav = new ModelAndView("admin/oprs/jrnlManage/license");

		String journalSetting = propertiesService.getProperty("system", "journal-setting", journal.getJournalNo());
		if (journalSetting.equals("no")) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/");
		} else {
			mav.addObject("journalSetting", journalSetting);
		}
		
		int journalno = journal.getJournalNo();
		Map<String, Object> configMap = submissionService.selectMetaConfig(journal.getJrnlSeq());
		mav.addAllObjects(configMap);

		Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journalno);
		mav.addObject("jrnlConfig", jrnlConfig);

		mav.addObject("tabTy", "2");
		
		return mav;
	}	
	
	/**
	 * 정책정보관리 > 저작권 주체
	 * 
	 * @param request
	 * @return
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("/policyInfoManageCopyright.do")
	public ModelAndView policyInfoManageCopyright(HttpServletRequest request) throws AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();

		Journal journal = sessionService.getSite(request);
		ModelAndView mav = new ModelAndView("admin/oprs/jrnlManage/policyInfoManageCopyright");

		String journalSetting = propertiesService.getProperty("system", "journal-setting", journal.getJournalNo());
		if (journalSetting.equals("no")) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/");
		} else {
			mav.addObject("journalSetting", journalSetting);
		}
		
		Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journal.getJournalNo());
		mav.addObject("jrnlConfig", jrnlConfig);
		Map<String, Object> configMap = submissionService.selectMetaConfig(journal.getJrnlSeq());
		mav.addAllObjects(configMap);
		
		mav.addObject("tabTy", "3");
		
		return mav;
	}
	
	/**
	 * 정책정보관리 > 셀프 아카이빙 정책
	 * @param request
	 * @return
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@RequestMapping("/policyInfoManageSelfArchiving.do")
	public ModelAndView policyInfoManageSelfArchiving(HttpServletRequest request) throws AuthenticationException, Exception {
		if (!sessionService.checkAuthority(request, new String[] {"ADMIN_ATTRIBUTE"}))
			throw new AuthenticationException();

		Journal journal = sessionService.getSite(request);
		ModelAndView mav = new ModelAndView("admin/oprs/jrnlManage/selfArchiving");

		String journalSetting = propertiesService.getProperty("system", "journal-setting", journal.getJournalNo());
		if (journalSetting.equals("no")) {
			return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/");
		} else {
			mav.addObject("journalSetting", journalSetting);
		}
		
		int journalno = journal.getJournalNo();
		MetaConfigVO selfArchivingPolicyUseYn = submissionService.selectMetaConfig(journal.getJrnlSeq(),"selfArchivingPolicyUseYn");
		mav.addObject("selfArchivingPolicyUseYn",selfArchivingPolicyUseYn);
		Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journalno);
		mav.addObject("jrnlConfig", jrnlConfig);
		
		// 범위
		List<CodeVO> archiveSettings = journalService.selectArchivingScope(journalno);
		mav.addObject("archiveSettings", archiveSettings);
		
		mav.addObject("tabTy", "4");
		
		return mav;
	}	
	
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
