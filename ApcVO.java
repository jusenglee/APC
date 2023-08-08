package com.argo.journal.beans;

import org.apache.ibatis.type.Alias;

@Alias("apcVO")
public class ApcVO {
    //필드
	private String jrnlSeq;//저널 일련
	private String apcSeq;//APC 일련
	private String tyCode;//유형 코드
	private String iemNmKor;//항목 명 영문
	private String iemNmEng;//항목 명 국문
	private String amount;//금액
	private String wonAmount;//원 금액
	private String dollarAmount;//달러 금액
	private String rate;//비율
	private String ddctCode;//차감 코드
	private String creatDate;//생성 날짜
	private String creatId;//생성 ID
	private String updtDate;//수정 날짜
	private String updtId;//수정 ID
	
	
  public String getJrnlSeq() {
    return jrnlSeq;
  }
  public void setJrnlSeq(String jrnlSeq) {
    this.jrnlSeq = jrnlSeq;
  }
  public String getApcSeq() {
    return apcSeq;
  }
  public void setApcSeq(String apcSeq) {
    this.apcSeq = apcSeq;
  }
  public String getTyCode() {
    return tyCode;
  }
  public void setTyCode(String tyCode) {
    this.tyCode = tyCode;
  }

  public String getAmount() {
    return amount;
  }
  public void setAmount(String amount) {
    this.amount = amount;
  }
  public String getWonAmount() {
    return wonAmount;
  }
  public void setWonAmount(String wonAmount) {
    this.wonAmount = wonAmount;
  }
  public String getDollarAmount() {
    return dollarAmount;
  }
  public void setDollarAmount(String dollarAmount) {
    this.dollarAmount = dollarAmount;
  }
  public String getDdctCode() {
    return ddctCode;
  }
  public void setDdctCode(String ddctCode) {
    this.ddctCode = ddctCode;
  }
  public String getCreatDate() {
    return creatDate;
  }
  public void setCreatDate(String creatDate) {
    this.creatDate = creatDate;
  }
  public String getCreatId() {
    return creatId;
  }
  public void setCreatId(String creatId) {
    this.creatId = creatId;
  }
  public String getUpdtDate() {
    return updtDate;
  }
  public void setUpdtDate(String updtDate) {
    this.updtDate = updtDate;
  }
  public String getUpdtId() {
    return updtId;
  }
  public void setUpdtId(String updtId) {
    this.updtId = updtId;
  }
  public String getIemNmKor() {
    return iemNmKor;
  }
  public void setIemNmKor(String iemNmKor) {
    this.iemNmKor = iemNmKor;
  }
  public String getIemNmEng() {
    return iemNmEng;
  }
  public void setIemNmEng(String iemNmEng) {
    this.iemNmEng = iemNmEng;
  }
  public String getRate() {
    return rate;
  }
  public void setRate(String rate) {
    this.rate = rate;
  }
  }
