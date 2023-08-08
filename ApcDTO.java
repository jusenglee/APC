package kr.oprs.admin.apc;

import java.util.ArrayList;
import java.util.List;
import com.argo.journal.beans.ApcVO;

public class ApcDTO {
  // 뷰 - DTO - 서버 - AO - DB
  //필드 외. 
  //데이터를 받아오기 위한 DTO.
  private List<ApcVO> apcDataList = new ArrayList<>();
  
  //Apc세팅 페이지
  private List<String> tyCodeList = new ArrayList<>();
  private List<String> dollarAmount = new ArrayList<>();
  private List<String> wonAmount = new ArrayList<>();
  private List<String> amount = new ArrayList<>();
  
  
  private List<String> ddctDollarAmount = new ArrayList<>();
  private List<String> ddctWonAmount = new ArrayList<>();
  private List<String> ddctCode = new ArrayList<>();
  private List<String> rate = new ArrayList<>();
  
  private List<String> etcNm = new ArrayList<>();
  private List<String> etcCodeList = new ArrayList<>();
  private List<String> etcDdctCodeList = new ArrayList<>();
  private List<String> etcWonAmount = new ArrayList<>();
  private List<String> etcDollarAmount = new ArrayList<>();
  private List<String> rateEtc = new ArrayList<>();
  
  //OA 유형공개 페이지
  private List<String> OaNm = new ArrayList<>();
  private List<String> OaTyCodeList = new ArrayList<>();
  

  public List<String> getEtcNm() {
    return etcNm;
  }

  public void setEtcNm(List<String> etcNm) {
    this.etcNm = etcNm;
  }

  public List<String> getDdctCode() {
    return ddctCode;
  }

  public void setDdctCode(List<String> ddctCode) {
    this.ddctCode = ddctCode;
  }

  public List<String> getAmount() {
    return amount;
  }

  public void setAmount(List<String> amount) {
    this.amount = amount;
  }

  public List<String> getDollarAmount() {
    return dollarAmount;
  }

  public void setDollarAmount(List<String> dollarAmount) {
    this.dollarAmount = dollarAmount;
  }

  public List<String> getTyCodeList() {
    return tyCodeList;
  }

  public void setTyCodeList(List<String> tyCodeList) {
    this.tyCodeList = tyCodeList;
  }

  public List<String> getWonAmount() {
    return wonAmount;
  }

  public void setWonAmount(List<String> wonAmount) {
    this.wonAmount = wonAmount;
  }

  public List<String> getDdctDollarAmount() {
    return ddctDollarAmount;
  }

  public void setDdctDollarAmount(List<String> ddctDollarAmount) {
    this.ddctDollarAmount = ddctDollarAmount;
  }

  public List<String> getRate() {
    return rate;
  }

  public void setRate(List<String> rate) {
    this.rate = rate;
  }

  public List<String> getEtcDdctCodeList() {
    return etcDdctCodeList;
  }

  public void setEtcDdctCodeList(List<String> etcDdctCodeList) {
    this.etcDdctCodeList = etcDdctCodeList;
  }

  public List<String> getEtcCodeList() {
    return etcCodeList;
  }

  public void setEtcCodeList(List<String> etcCodeList) {
    this.etcCodeList = etcCodeList;
  }


  public List<String> getOaNm() {
    return OaNm;
  }

  public void setOaNm(List<String> oaNm) {
    OaNm = oaNm;
  }

  public List<String> getDdctWonAmount() {
    return ddctWonAmount;
  }

  public void setDdctWonAmount(List<String> ddctWonAmount) {
    this.ddctWonAmount = ddctWonAmount;
  }

  public List<String> getRateEtc() {
    return rateEtc;
  }

  public void setRateEtc(List<String> rateEtc) {
    this.rateEtc = rateEtc;
  }

  public List<String> getEtcDollarAmount() {
    return etcDollarAmount;
  }

  public void setEtcDollarAmount(List<String> etcDollarAmount) {
    this.etcDollarAmount = etcDollarAmount;
  }

  public List<String> getEtcWonAmount() {
    return etcWonAmount;
  }

  public void setEtcWonAmount(List<String> etcWonAmount) {
    this.etcWonAmount = etcWonAmount;
  }

  public List<String> getOaTyCodeList() {
    return OaTyCodeList;
  }

  public void setOaTyCodeList(List<String> oaTyCodeList) {
    OaTyCodeList = oaTyCodeList;
  }

  public List<ApcVO> getApcDataList() {
    return apcDataList;
  }

  public void setApcDataList(List<ApcVO> apcDataList) {
    this.apcDataList = apcDataList;
  };//기타항목 이름
  
}
