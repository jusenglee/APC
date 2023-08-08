package kr.oprs.admin.apc.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.argo.journal.KoarService;
import com.argo.journal.LanguageService;
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
   * APC 정보 뷰 반환 (Service)
   * 
   * @param Journal
   * @return apcDTO
   * @throws Exception
   */
  @Override
  public ApcDTO getApcDTO(Journal journal) throws Exception {

    List<ApcVO> apcInfoList = getAllApcInfos(journal);
    ApcDTO dto = new ApcDTO();

    for (ApcVO vo : apcInfoList) {
      if (vo.getTyCode() != null) {
        ApcVO apcData = new ApcVO();
        apcData.setTyCode(vo.getTyCode());
        apcData.setDollarAmount(vo.getDollarAmount());
        apcData.setIemNmKor(vo.getIemNmKor());
        apcData.setWonAmount(vo.getWonAmount());
        apcData.setRate(vo.getRate());
        apcData.setDdctCode(vo.getDdctCode());
        dto.getApcDataList().add(apcData);
      }
    }
    return dto;
  }


  /**
   * APC 정보 저장 (Service)
   * 
   * @param apcDTO ,Journal , User
   * @return
   * @throws Exception
   */
  @Override
  public void setApcVO(ApcDTO dto, Journal journal, User user) throws Exception {

    if (dto.getTyCodeList() != null) { // 데이터가 왔을경우 시작
      for (int i = 0; i < dto.getTyCodeList().size(); i++) {

        if (!dto.getTyCodeList().get(i).equals("")) { // 유형코드가 비었을경우 건너뜀
          if (dto.getTyCodeList().get(i).equals("OP2406")) { // 1.무소속 연구자
            ApcVO VO = new ApcVO();
            VO.setJrnlSeq(journal.getJrnlSeq());
            VO.setUpdtId(user.getId()); // 기본값 세팅
            
            VO.setTyCode(dto.getTyCodeList().get(i));
            Map<String, String> resultMap = koarService.getSysCodeNm(dto.getTyCodeList().get(i));
            VO.setIemNmKor(resultMap.get("CODE_NM"));// 국문 이름
            VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));// 영문 이름
            VO.setDdctCode(dto.getDdctCode().get(0));

            if (dto.getDdctCode().get(0).equals("OP2501")) { // 할인코드가 면제면 0 세팅 후 바로 저장
              VO.setAmount("0");
              modifyApcInfo(VO);// insert 실행

            } else {
              if (!dto.getDdctWonAmount().get(0).equals("")
                  && !dto.getDdctDollarAmount().get(0).equals("")) {
                  // 둘 다 있는 경우, 더 높은 값을 amount에 설정
                  Double wonAmount = Double.parseDouble(dto.getDdctWonAmount().get(0));
                  Double dollarAmount = Double.parseDouble(dto.getDdctDollarAmount().get(0));
                  VO.setWonAmount(dto.getDdctWonAmount().get(0));
                  VO.setDollarAmount(dto.getDdctDollarAmount().get(0));

                if (wonAmount > dollarAmount) {
                  VO.setAmount(dto.getDdctWonAmount().get(0));

                } else {
                  VO.setAmount(dto.getDdctDollarAmount().get(0));

                }
              } else if (!dto.getDdctWonAmount().get(0).equals("")
                  && dto.getDdctDollarAmount().get(0).equals("")) {
                  // WonAmount 만 있는 경우
                  VO.setAmount(dto.getDdctWonAmount().get(0));
                  VO.setWonAmount(dto.getDdctWonAmount().get(0));

              } else if (!dto.getDdctDollarAmount().get(0).equals("")
                  && dto.getDdctWonAmount().get(0).equals("")) {
                  // DollarAmount 만 있는 경우
                  VO.setAmount(dto.getDdctDollarAmount().get(0));
                  VO.setDollarAmount(dto.getDdctDollarAmount().get(0));
                
              } else {
                VO.setAmount(dto.getRate().get(0));
                VO.setRate(dto.getRate().get(0));
                
              }
              modifyApcInfo(VO);// insert 실행
            }

          }else if (dto.getTyCodeList().get(i).equals("OP2407")) { // 2.학생 연구자
            ApcVO VO = new ApcVO();
            VO.setJrnlSeq(journal.getJrnlSeq());
            VO.setUpdtId(user.getId()); // 기본값 세팅
           
            VO.setTyCode(dto.getTyCodeList().get(i));
            Map<String, String> resultMap = koarService.getSysCodeNm(dto.getTyCodeList().get(i));
            VO.setIemNmKor(resultMap.get("CODE_NM")); // 국문 이름
            VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));// 영문 이름
            VO.setDdctCode(dto.getDdctCode().get(1));

            if (dto.getDdctCode().get(1).equals("OP2501")) { // 할인코드가 면제면 0 세팅 후 바로 저장
                VO.setAmount("0");
                modifyApcInfo(VO);// insert 실행

            } else { if (!dto.getDdctWonAmount().get(1).equals("")// 할인코드가 할인(OP2502)면
                  && !dto.getDdctDollarAmount().get(1).equals("")) { // 둘 다 있는 경우, 더 높은 값을 amount에 설정
              
                Double wonAmount = Double.parseDouble(dto.getDdctWonAmount().get(1));
                Double dollarAmount = Double.parseDouble(dto.getDdctDollarAmount().get(1));
                VO.setWonAmount(dto.getDdctWonAmount().get(1));
                VO.setDollarAmount(dto.getDdctDollarAmount().get(1));
                
                if (wonAmount > dollarAmount) {
                  VO.setAmount(dto.getDdctWonAmount().get(1));
                } else {
                  VO.setAmount(dto.getDdctDollarAmount().get(1));
                }

              } else if (!dto.getDdctWonAmount().get(1).equals("")
                  && dto.getDdctDollarAmount().get(1).equals("")) {
                  // WonAmount 만 있는 경우
                  VO.setAmount(dto.getDdctWonAmount().get(1));
                  VO.setWonAmount(dto.getDdctWonAmount().get(1));
                
              } else if (!dto.getDdctDollarAmount().get(1).equals("")
                  && dto.getDdctWonAmount().get(1).equals("")) {
                  // DollarAmount 만 있는 경우
                  VO.setAmount(dto.getDdctDollarAmount().get(1));
                  VO.setDollarAmount(dto.getDdctDollarAmount().get(1));
                  
              } else { // 둘 다 없다면 비율
                  VO.setAmount(dto.getRate().get(1));
                  VO.setRate(dto.getRate().get(1));
                
              }
              modifyApcInfo(VO);// insert 실행
              
            }
          } else { // 3. APC 관련 세팅
            try {
              if ((!dto.getWonAmount().get(i).equals("")|| !dto.getDollarAmount().get(i).equals("")) || !dto.getRate().get(i).equals("")) {
                ApcVO VO = new ApcVO();
                VO.setJrnlSeq(journal.getJrnlSeq());
                VO.setUpdtId(user.getId()); // 기본값 세팅

                VO.setTyCode(dto.getTyCodeList().get(i));
                Map<String, String> resultMap = koarService.getSysCodeNm(dto.getTyCodeList().get(i));
                VO.setIemNmKor(resultMap.get("CODE_NM"));
                VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));

                if (!dto.getWonAmount().get(i).equals("")
                    && !dto.getDollarAmount().get(i).equals("")) {
                  
                    // 둘 다 있는 경우, 더 높은 값을 amount에 설정
                    Double wonAmount = Double.parseDouble(dto.getWonAmount().get(i));
                    Double dollarAmount = Double.parseDouble(dto.getDollarAmount().get(i));
                    VO.setWonAmount(dto.getWonAmount().get(i));
                    VO.setDollarAmount(dto.getDollarAmount().get(i));

                  if (wonAmount > dollarAmount) {
                    VO.setAmount(dto.getWonAmount().get(i));

                  } else {
                    VO.setAmount(dto.getDollarAmount().get(i));

                  }
                } else if (!dto.getWonAmount().get(i).equals("")
                    && dto.getDollarAmount().get(i).equals("")) {
                  
                    // WonAmount 만 있는 경우
                    VO.setAmount(dto.getWonAmount().get(i));
                    VO.setWonAmount(dto.getWonAmount().get(i));
                    
                } else if (!dto.getDollarAmount().get(i).equals("")
                    && dto.getWonAmount().get(i).equals("")) {
                  
                    // DollarAmount 만 있는 경우
                    VO.setAmount(dto.getDollarAmount().get(i));
                    VO.setDollarAmount(dto.getDollarAmount().get(i));
                    
                }
                modifyApcInfo(VO);// insert 실행
                
              }
            } catch (Exception e) {
              Logger.error("An exception occurred while processing APC information", e);
              e.printStackTrace();
            }
          }

        }
      }
    }



    if (dto.getEtcCodeList() != null) { // 4. 할인 - 기타 항목 데이터는 별도 처리
      for (int i = 0; i < dto.getEtcCodeList().size(); i++) {
        if (dto.getEtcCodeList().get(i).equals("OP2408")) { // //기타
          
          ApcVO VO = new ApcVO();
          VO.setJrnlSeq(journal.getJrnlSeq());
          VO.setUpdtId(user.getId()); // 기본값 세팅

          VO.setTyCode(dto.getEtcCodeList().get(i));
          VO.setIemNmKor(dto.getEtcNm().get(i));
          VO.setIemNmEng("ETC");
          VO.setDdctCode(dto.getEtcDdctCodeList().get(i));

          if (dto.getEtcDdctCodeList().get(i).equals("OP2501")) {
            VO.setAmount("0");
            modifyApcInfo(VO);
            
          } else { 
            if (dto.getEtcWonAmount() != null && i < dto.getEtcWonAmount().size()
                && dto.getEtcDollarAmount() != null && i < dto.getEtcDollarAmount().size()) {
              
              String wonAmount = dto.getEtcWonAmount().get(i);
              String dollarAmount = dto.getEtcDollarAmount().get(i);

              if (wonAmount != null && !wonAmount.equals("") && dollarAmount != null
                  && !dollarAmount.equals("")) {
              
                // 둘 다 있는 경우, 더 높은 값을 amount에 설정
                Double parsedWonAmount = Double.parseDouble(dto.getEtcWonAmount().get(i));
                Double parsedDollarAmount = Double.parseDouble(dto.getEtcDollarAmount().get(i));
                VO.setWonAmount(dto.getEtcWonAmount().get(i));
                VO.setDollarAmount(dto.getEtcDollarAmount().get(i));

                if (parsedWonAmount > parsedDollarAmount) {
                  VO.setAmount(dto.getEtcWonAmount().get(i));
                } else {
                  VO.setAmount(dto.getEtcDollarAmount().get(i));
                }
              } else if (!dto.getEtcWonAmount().get(i).equals("")
                  && dto.getEtcDollarAmount().get(i).equals("")) {
                // WonAmount 만 있는 경우
                VO.setAmount(dto.getEtcWonAmount().get(i));
                VO.setWonAmount(dto.getEtcWonAmount().get(i));
                
              } else if (!dto.getEtcDollarAmount().get(i).equals("")
                  && dto.getEtcWonAmount().get(i).equals("")) {
                // DollarAmount 만 있는 경우
                VO.setAmount(dto.getEtcDollarAmount().get(i));
                VO.setDollarAmount(dto.getEtcDollarAmount().get(i));
                
              }
            } else if (dto.getRateEtc().get(i) != null && !dto.getRateEtc().get(i).equals("")) {
              VO.setAmount(dto.getRateEtc().get(i));
              VO.setRate(dto.getRateEtc().get(i));
              
            }
            modifyApcInfo(VO);
          }
        }
      }
    }
  }

  /**
   * OA 저장 (DB)
   * 
   * @param String jrnlSeq
   * @return
   * @throws Exception
   */
  @Override
  public void setApcOA(ApcDTO dto, Journal journal, User user) throws Exception {
    if (dto.getOaTyCodeList() != null) { // 데이터가 왔을경우 시작
      for (int i = 0; i < dto.getOaTyCodeList().size(); i++) {
        
        if (dto.getOaTyCodeList().get(i).equals("OP2451")
            || dto.getOaTyCodeList().get(i).equals("OP2452")) {
          ApcVO VO = new ApcVO();
          VO.setJrnlSeq(journal.getJrnlSeq());
          VO.setUpdtId(user.getId()); // 기본값 세팅
          
          VO.setTyCode(dto.getOaTyCodeList().get(i));
          Map<String, String> resultMap = koarService.getSysCodeNm(dto.getOaTyCodeList().get(i));
          VO.setIemNmKor(resultMap.get("CODE_NM"));
          VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));
          VO.setAmount("0");
          modifyApcInfo(VO);// insert 실행
          
        }
        
        if (dto.getOaTyCodeList() != null && !dto.getOaNm().get(i).equals("")) {
          ApcVO VO = new ApcVO();
          VO.setJrnlSeq(journal.getJrnlSeq());
          VO.setUpdtId(user.getId()); // 기본값 세팅
          
          VO.setTyCode(dto.getOaTyCodeList().get(i));
          Map<String, String> resultMap = koarService.getSysCodeNm(dto.getOaTyCodeList().get(i));
          
          if (dto.getOaNm().get(i).equals("")) {
            VO.setIemNmKor(resultMap.get("CODE_NM"));
            
          } else {
            VO.setIemNmKor(dto.getOaNm().get(i));
            
          }
          
          VO.setIemNmEng(resultMap.get("CODE_NM_ENG"));// 영문 이름
          VO.setAmount("0");
          modifyApcInfo(VO);// insert 실행
          
        }
      }
    }
  };


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


  @Override
  public void setLanguages(HttpServletRequest request) throws Exception {
    Map<String, String[]> jrConfigMap = request.getParameterMap();
    Iterator<String> jrConfKeySet = jrConfigMap.keySet().iterator();
    Map<String, Object> jrMetaVo = new HashMap<String, Object>();

    Journal journal = sessionService.getSite(request);
    User user =
        (User) request.getSession().getAttribute(journal.getJournalUrl() + "_" + "admin_info");
    String locale = "ko";

    while (jrConfKeySet.hasNext()) {
      jrMetaVo.put("locale", locale);
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
    }
  }
}
