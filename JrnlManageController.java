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
        String journalSetting = propertiesService.getProperty("system", "journal-setting", journalno);
        Map<String, String> jrnlConfig = languageService.selectAllJrnlConfig(journalno);

        // APC 정보
        List<ApcVO> apcList = apcPageService.getAllApcInfos(journal);
        Map<String, List<ApcVO>> apcMap = apcList.stream().collect(Collectors.groupingBy(ApcVO::getTyCode));
        mav.addObject("apcMap", apcMap);

        // APC 유형 코드 목록
        List<CodeVO> apcTyCodeList = koarService.selectCodeList("OP24");
        mav.addObject("apcTyCodeList", apcTyCodeList);

        // APC 계산기 정보 추가
        CalcVO calc = apcService.getApcCalc(journalno, 0);
        mav.addObject("calc", calc);

        if (journalSetting.equals("no")) {
            return new ModelAndView("redirect:/" + journal.getJournalUrl() + "/admin/oprs/jrnlManage/apcPage");
        } else {
            mav.addObject("journalSetting", journalSetting);
        }

        if (languageService.findByKeyAndLocale(journal.getJournalUrl(), "journal.manage.apc.msg", "ko", "info") != null) {
            mav.addObject("contentKor", languageService.findByKeyAndLocale(journal.getJournalUrl(), "journal.manage.apc.msg", "ko", "info").getMessagecontent());
        }
        if (languageService.findByKeyAndLocale(journal.getJournalUrl(), "journal.manage.apc.msg", "en", "info") != null) {
            mav.addObject("contentEng", languageService.findByKeyAndLocale(journal.getJournalUrl(), "journal.manage.apc.msg", "en", "info").getMessagecontent());
        }

        mav.addObject("jrnlConfig", jrnlConfig);
        mav.addObject("tabTy", "5");//탭번호

        return mav;
    }
	
	/**
	 * 정책정보관리 > 논문 출판비용(APC) 정보 저장 Ajax
	 * 
	 * @param apcDTO
	 * @param request
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws Exception
	 */
	@ResponseBody
	@PostMapping("/policyInfoManageApcInfoAjax.do")
	public void ApcAjax(@ModelAttribute ApcDTO apcDTO, HttpServletRequest request, @RequestParam(value = "locale", required = false, defaultValue = "") String locale)throws IOException, AuthenticationException, Exception{
		Journal journal = sessionService.getSite(request);
		User user = (User)request.getSession().getAttribute(journal.getJournalUrl() + "_" + "admin_info");
		String jrnlSeq = journal.getJrnlSeq();
		apcPageService.deleteApcInfos(jrnlSeq); //0. 이전 APC 세팅 삭제
		
		
		try {
		  apcPageService.setLanguages(request);
        } catch (Exception e) {
          // 로그 기록, 오류 메시지 등 처리
          Logger.error("APC Languages 세팅 오류 발생", e);
        }

		try {
		    apcPageService.setApcVO(apcDTO, journal, user); //1. 현 APC 세팅 저장
		} catch (Exception e) {
		    // 로그 기록, 오류 메시지 등 처리
		    Logger.error("APC 설정 저장 중 오류 발생", e);
		}
		
		try {
		    apcPageService.setApcOA(apcDTO, journal, user); //2. 현 OA 세팅 저장
		} catch (Exception e) {
		    // 로그 기록, 오류 메시지 등 처리
		    Logger.error("OA 설정 저장 중 오류 발생", e);
		}
	}
}
