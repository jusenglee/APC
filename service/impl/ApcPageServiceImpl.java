package kr.oprs.admin.apc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import com.argo.journal.KoarService;
import com.argo.journal.LanguageService;
import com.argo.journal.PropertiesService;
import com.argo.journal.SessionService;
import com.argo.journal.beans.ApcVO;
import com.argo.journal.beans.Journal;
import com.argo.journal.beans.User;
import kr.oprs.admin.apc.ApcDTO;

import kr.oprs.admin.apc.service.ApcPageService;

@Service
public class ApcPageServiceImpl extends SqlSessionDaoSupport implements ApcPageService {
  private static final String LG = "com.argo.journal.languages.";

  @Autowired
  private KoarService koarService;

  @Autowired
  private LanguageService languageService;

  @Autowired
  private SessionService sessionService;
  

  @Autowired
  @Override
  public void setSqlSessionFactory(SqlSessionFactory factory) {
    super.setSqlSessionFactory(factory);
  }

  @Override
  public boolean refreshLanguageCache() {
    // languages 캐시 초기화
    return true;
  }

  private Logger Logger = LoggerFactory.getLogger(getClass());

  /**
   * APC 정보 저장 (Service)
   * 
   * @param apcDTO ,Journal , User
   * @return
   * @throws Exception
   */
  @Override
  public void setApcVO(ApcDTO dto, Journal journal, User user) throws Exception {
    List<String> errorList = new ArrayList<>();
    // 타입 코드 리스트 처리
    if (dto.getTyCodeList() != null) {
      for (int i = 0; i < dto.getTyCodeList().size(); i++) {
        String tyCode = dto.getTyCodeList().get(i);
        if (!StringUtils.isBlank(tyCode)) {
          try {
            processTyCode(tyCode, dto, journal, user, i);
          } catch (Exception e) {
            // 현재 tyCode에 대한 처리 오류 기록
            errorList.add("Error processing tyCode: " + tyCode + " - " + e.getMessage());
          }
        }
      } 
    }

    // 기타 코드 리스트 처리
    if (dto.getEtcCodeList() != null) {
      for (int i = 0; i < dto.getEtcCodeList().size(); i++) {
        String etcCode = dto.getEtcCodeList().get(i);
        if ("OP2408".equals(etcCode)) {
          try {
            processEtcCode(dto, journal, user, i);
          } catch (Exception e) {
            // 현재 tyCode에 대한 처리 오류 기록
            errorList.add("Error processing etcTyCode: " + etcCode + " - " + e.getMessage());
          }
        }
      } 
    }
    
    // 오류 목록이 비어있지 않으면, 오류 처리
    if (!errorList.isEmpty()) {
        // 오류 목록을 하나의 문자열로 결합
        String errorMessage = String.join("\n", errorList);
        // 로그에 오류 기록
        Logger.error("APC 설정 중 다음 오류가 발생하였습니다:\n" + errorMessage);
        // 필요한 경우 오류 메시지 반환
        // response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        // response.getWriter().write("APC 설정 중 오류가 발생하였습니다. 상세 정보는 로그를 참조해주세요.");
    }
   }
  
  // 타입 코드에 따른 처리
  @Transactional
  private void processTyCode(String tyCode, ApcDTO dto, Journal journal, User user, int index) throws Exception {
    ApcVO vo = createApcVO(dto, journal, user, index);
    vo.setTyCode(tyCode);

    switch (tyCode) {
      case "OP2406":
        vo.setDdctCode(dto.getDdctCode().get(0));
        processDiscountCode(vo, dto, 0,  dto.getDdctWonAmount().get(0), dto.getDdctDollarAmount().get(0), dto.getRate().get(0));
        break;
        
      case "OP2407":
        vo.setDdctCode(dto.getDdctCode().get(1));
        processDiscountCode(vo, dto, 1, dto.getDdctWonAmount().get(0), dto.getDdctDollarAmount().get(0), dto.getRate().get(1));
        break;
        
      default:
        processDefault(vo, dto, index);
        break;
    }
    
    modifyApcInfo(vo);
    
  }
  
  // 기본 처리
  private void processDefault(ApcVO vo, ApcDTO dto, int index) {
    if (!StringUtils.isBlank(dto.getWonAmount().get(index)) || !StringUtils.isBlank(dto.getDollarAmount().get(index)) || !StringUtils.isBlank(dto.getRate().get(index))) {
      setAmounts(vo, dto.getWonAmount().get(index), dto.getDollarAmount().get(index));
    }
    
  }

  // 기타 코드 처리
  @Transactional
  private void processEtcCode(ApcDTO dto, Journal journal, User user, int index) throws Exception {
    ApcVO vo = new ApcVO();
    vo.setJrnlSeq(journal.getJrnlSeq());
    vo.setUpdtId(user.getId());
    vo.setTyCode(dto.getEtcCodeList().get(index));
    vo.setIemNmKor(dto.getEtcNm().get(index));
    vo.setIemNmEng(dto.getEtcNm().get(index));
    vo.setDdctCode(dto.getEtcDdctCodeList().get(index));
    
    String wonAmount = (index < dto.getEtcWonAmount().size() && dto.getEtcWonAmount().get(index) != null) ? dto.getEtcWonAmount().get(index) : null;
    String dollarAmount = (index < dto.getEtcDollarAmount().size() && dto.getEtcDollarAmount().get(index) != null) ? dto.getEtcDollarAmount().get(index) : null;
    String rate =(index < dto.getRateEtc().size() && dto.getRateEtc().get(index) != null) ? dto.getRateEtc().get(index) : null;
    
    processDiscountCode(vo, dto, index, wonAmount, dollarAmount, rate);
    
    modifyApcInfo(vo);
    
  }

  // 할인 코드 처리
  private void processDiscountCode(ApcVO vo, ApcDTO dto, int index,String wonAmount, String dollarAmount, String Rate) {
    if ("OP2502".equals(vo.getDdctCode())) {
        setAmounts(vo, wonAmount, dollarAmount);
    }
    
    if ("OP2503".equals(vo.getDdctCode())) {
        vo.setRate(Rate);
    }
    
  }

  // 금액 설정
  private void setAmounts(ApcVO vo, String wonAmount, String dollarAmount) {
    if (!StringUtils.isBlank(wonAmount)) {
      vo.setWonAmount(wonAmount.replace(",", ""));
    }
    
    if (!StringUtils.isBlank(dollarAmount)) {
      vo.setDollarAmount(dollarAmount.replace(",", ""));
    }
    
  }
  

  // ApcVO 객체 생성
  private ApcVO createApcVO(ApcDTO dto, Journal journal, User user, int index) {
    ApcVO vo = new ApcVO();
    vo.setJrnlSeq(journal.getJrnlSeq());
    vo.setUpdtId(user.getId());
   
    Map<String, String> resultMap = koarService.getSysCodeNm(dto.getTyCodeList().get(index));
    vo.setIemNmKor(resultMap.get("CODE_NM"));
    vo.setIemNmEng(resultMap.get("CODE_NM_ENG"));
    
    return vo;
  }



  /**
   * OA 저장 (DB)
   * 
   * @param String jrnlSeq
   * @return
   * @throws Exception
   */
  @Override
  @Transactional
  public void setApcOA(ApcDTO dto, Journal journal, User user) throws Exception {
    List<String> errorList = new ArrayList<>();
    List<String> oaTyCodeList = dto.getOaTyCodeList();
    List<String> oaNmList = dto.getOaNm();
    if (oaTyCodeList != null) {
      for (int i = 0; i < oaTyCodeList.size(); i++) {
        
        try {
          String code = oaTyCodeList.get(i);
          ApcVO VO = createApcVO(journal, user, code);
          if ("OP2451".equals(code) || "OP2452".equals(code)) {
              Map<String, String> resultMap = koarService.getSysCodeNm(code);
              VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));
              VO.setIemNmKor(resultMap.get("CODE_NM"));
          } else {
              String name = oaNmList.get(i);
              Map<String, String> resultMap = koarService.getSysCodeNm(code);
              VO.setIemNmKor(name);
              VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));
          }
          modifyApcInfo(VO); // insert 실행
        } catch (Exception e) {
          // 현재 tyCode에 대한 처리 오류 기록
          errorList.add("Error processing tyCode: " + oaTyCodeList + " - " + e.getMessage());
        }
      }
      // 오류 목록이 비어있지 않으면, 오류 처리
      if (!errorList.isEmpty()) {
        // 사용자에게 오류 목록을 알리거나 기타 처리 수행
        String errorMessage = String.join("\n", errorList);
        // 로그에 오류 기록
        Logger.error("OA 설정 중 다음 오류가 발생하였습니다:\n" + errorMessage);
      }
    }
  }
  
  private ApcVO createApcVO(Journal journal, User user, String code) {
    ApcVO VO = new ApcVO();
    VO.setJrnlSeq(journal.getJrnlSeq());
    VO.setUpdtId(user.getId());
    VO.setTyCode(code);
    return VO;
  }

  /**
   * APC 관련 Languages 저장 (DB)
   * 
   * @param HttpServletRequest request
   * @return
   * @throws Exception
   */
  @Override
  public void setLanguages(HttpServletRequest request) throws Exception {
    Map<String, String[]> jrConfigMap = request.getParameterMap();
    Iterator<String> jrConfKeySet = jrConfigMap.keySet().iterator();
    Map<String, Object> jrMetaVo = new HashMap<String, Object>();
    HttpSession session = request.getSession();
    Journal journal = sessionService.getSite(request);
    User user = (User) request.getSession().getAttribute(journal.getJournalUrl() + "_" + "admin_info");
    Locale locale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
    String languageTag = locale.toLanguageTag();
    
    while (jrConfKeySet.hasNext()) {
      jrMetaVo.put("locale", languageTag);
      jrMetaVo.put("packages", "info");
      jrMetaVo.put("journalno", journal.getJournalNo());
      jrMetaVo.put("jrnlSeq", journal.getJrnlSeq());
      jrMetaVo.put("updtId", user.getId());
      String key = jrConfKeySet.next();
      String value[] = jrConfigMap.get(key);
      if (key.equals("journal.apc.paidYn")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
      if (key.equals("journal.apc.unitWonYn") || key.equals("journal.apc.unitDollarYn")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
      if (key.equals("journal.page.min") || key.equals("journal.page.max")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
      if (key.equals("journal.apcOriginOpenYn")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
      if (key.equals("journal.apcStandardOpenYn")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
      if (key.equals("journal.apcRatioOpenYn")) {
        jrMetaVo.put("messagekey", key);
        jrMetaVo.put("messagecontent", value[0]);
        languageService.updateLanguageInfo(jrMetaVo);
      }
    }
  }
  
  /**
   * APC 삭제 (DB)
   * 
   * @param String jrnlSeq
   * @return
   * @throws Exception
   */
  @Override
  public void deleteApcInfos(String jrnlSeq) throws Exception {
    getSqlSession().delete(LG + "deleteApcInfos", jrnlSeq);

  }

  /**
   * APC 정보 저장 (DB)
   * 
   * @param apcVO
   * @return
   * @throws Exception
   */
  @Override
  public void modifyApcInfo(ApcVO apcVO) throws Exception {
    getSqlSession().update(LG + "updateApcInfo2", apcVO);
  }

  /**
   * APC 정보 조회 (DB)
   * 
   * @param Journal
   * @return List<apcVO>
   * @throws Exception
   */
  @Override
  public List<ApcVO> getAllApcInfos(Journal journal) throws Exception {
    // apcVO 객체를 생성하고 필요한 정보를 채웁니다.
    ApcVO vo = new ApcVO();
    vo.setJrnlSeq(journal.getJrnlSeq());


    // 쿼리를 실행하고 결과를 반환합니다.
    return getSqlSession().selectList(LG + "selectApcInfo", vo);
  }
}
