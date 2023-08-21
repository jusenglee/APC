package kr.oprs.admin.apc.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.argo.journal.beans.ApcVO;
import com.argo.journal.beans.Journal;
import com.argo.journal.beans.User;
import kr.oprs.admin.apc.ApcDTO;


public interface ApcPageService {
  
  boolean refreshLanguageCache();
  
  /**
   * APC 정보 조회 (DB)
   * 
   * @param Journal 
   * @return List<apcVO>
   * @throws Exception
   */
  List<ApcVO> getAllApcInfos(Journal journal) throws Exception;
  
  
  /**
   * APC 정보 저장 (Service)
   * 
   * @param apcDTO ,Journal , User
   * @return 
   * @throws Exception
   */
  void setApcVO(ApcDTO dto,Journal journal, User user) throws Exception;
  
  /**
   * OA 정보 저장 (Service)
   * 
   * @param apcDTO ,Journal , User
   * @return 
   * @throws Exception
   */
  void setApcOA(ApcDTO dto,Journal journal, User user) throws Exception;
  
  /**
   * APC 삭제 (DB)
   * 
   * @param String jrnlSeq
   * @return 
   * @throws Exception
   */
  void deleteApcInfos(String jrnlSeq) throws Exception;
  
  /**
   * APC 정보 저장 (DB)
   * 
   * @param apcVO
   * @return 
   * @throws Exception
   */
  void modifyApcInfo(ApcVO apcVO) throws Exception;
 
  /**
   * APC 설정 정보 저장 (DB)
   * 
   * @param HttpServletRequest request
   * @return 
   * @throws Exception
   */
  void setLanguages(HttpServletRequest request) throws Exception;

}
