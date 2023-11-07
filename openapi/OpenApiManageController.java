package com.shcorp.openapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.ParseUtil;
import com.inicis.std.util.SignatureUtil;
import com.shcorp.common.common.CommandMap;
import com.shcorp.common.service.CommonService;
import com.shcorp.common.util.Sha256Util;
import com.shcorp.contents.ContentsManageService;
import com.shcorp.reservestatus.ReserveStatusManageService;
import com.shcorp.common.util.AES128INICIS;
//import com.shcorp.common.util.FileUtils;
import com.shcorp.common.util.Paging;
import com.shcorp.common.util.SHA512INICIS;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;

/**
 * API를 이용한 Front Page와 이니시스 결제 Flow를 담당하는 컨트롤러
 * 
 * @author  CSH
 * @date    2023.07.07
 * @since   1.0
 */
@Controller
public class OpenApiManageController {

	Logger LOG = Logger.getLogger(this.getClass());

	@Autowired
	private CommonService commonService;

	@Autowired
	private OpenApiManageService openApiManageService;

//	@Autowired
//	private ReserveManageService reserveManageService;

//	@Value(value = "#{props['NamyangReserveManager.downloadFirmPath']}")
//	private String formwareDownloadPath;

	@Value(value = "#{props['NamyangReserveManager.inicis.mid']}")
	private String inicisMid;

	@Value(value = "#{props['NamyangReserveManager.inicis.signKey']}")
	private String inicisSignKey;

	@Value(value = "#{props['NamyangReserveManager.inicis.js.url']}")
	private String inicisUrl;

	@Value(value = "#{props['NamyangReserveManager.inicis.return.url']}")
	private String inicisReturnUrl;

	@Value(value = "#{props['NamyangReserveManager.inicis.close.url']}")
	private String inicisCloseUrl;
	
	@Value(value = "#{props['NamyangReserveManager.inicis.fc']}")
	private String inicisFc;
	
	@Value(value = "#{props['NamyangReserveManager.inicis.ks']}")
	private String inicisKs;
	
	@Value(value = "#{props['NamyangReserveManager.inicis.stg']}")
	private String inicisStg;
	
	@Value(value = "#{props['NamyangReserveManager.inicis.notilog']}")
	private String inicisNotiLog;

	@Value(value = "#{props['NamyangReserveManager.inicis.refund.url']}")
	private String inicisRefundUrl;

	
	@Value(value = "#{props['NamyangReserveManager.sms.url']}")
	private String smsUrl;

	@Value(value = "#{props['NamyangReserveManager.sms.userid']}")
	private String smsUserId;

	@Value(value = "#{props['NamyangReserveManager.sms.key']}")
	private String smsKey; 
	
	@Value(value = "#{props['NamyangReserveManager.sms.admin']}")
	private String smsAdmin; 
	
	@Value(value = "#{props['NamyangReserveManager.sms.sender']}")
	private String smsSender; 
	
	@Value(value = "#{props['NamyangReserveManager.sms.title']}")
	private String smsTitle;


	
	/**
	 * Front 남양펜션/계곡 예약등록 (Test) 화면 호출
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveTest.do")
	public ModelAndView reserveTest(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteManage START :: ");
		}

		String result = "success";
		// ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage");
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/paytest/INIstdpay_pc_req");
		
		mv.addObject("result", result);
		
		return mv;
	}

	/**
	 * Front 남양펜션/계곡 예약등록 화면 호출
	 * @see parameter로 tableCd를 받는데 null 또는 empty이면 전체 table정보를 보여주고, value가 있으면 해당 테이블의 정보만 얻어온다
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveManage.do")
	public ModelAndView reserveRegisteManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteManage START :: ");
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage");
		
		// 주메뉴 리스트
//		List<Map<String, Object>> largeMenuList = commonService.selectCode01("02"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
//		mv.addObject("largeMenuList", largeMenuList);

		// 부메뉴 리스트
//		List<Map<String, Object>> subMenuList = commonService.selectCode01("03"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
//		mv.addObject("subMenuList", subMenuList);
		
		// 대여용품 리스트
//		List<Map<String, Object>> rentalList = commonService.selectCode01("04"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
//		mv.addObject("rentalMenuList", rentalList);

		// 전체 테이블 리스트
//		List<Map<String, Object>> tableInfoList = openApiManageService.selectTableInfoList(map);; /* 전체 테이블 리스트 */		
//		mv.addObject("tableList", tableInfoList);

		// 전체 펜션 리스트
//		List<Map<String, Object>> pensionInfoList = openApiManageService.selectPensionInfoList(map);; /* 전체 펜션 리스트 */		
//		mv.addObject("pensionList", pensionInfoList);

//		if (map.get("tableCd") != null &&  !map.get("tableCd").toString().isEmpty()) {
//			// 테이블정보 (테이블 Spec, 테이블 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
//			Map<String, Object> tableInfo = openApiManageService.selectTableInfo(map);		
//			mv.addObject("tableInfo", tableInfo);
//		}
//		else if (map.get("pensionCd") != null && !map.get("pensionCd").toString().isEmpty()) {
//			// 펜션정보 (펜션 Spec, 펜션 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
//			Map<String, Object> pensionInfo = openApiManageService.selectPensionInfo(map);		
//			mv.addObject("pensionInfo", pensionInfo);
//		}
		
		/* paging control set */
//		if(map.containsKey("currentPageNo") == false || StringUtils.isEmpty(map.get("currentPageNo")) == true) map.put("currentPageNo","1");
//		int currentPageNo = Integer.valueOf((String)map.get("currentPageNo"));
//		int page_row = 5;
//		int start = (page_row*currentPageNo)-page_row;
//		map.put("START", start);
//		map.put("PAGE_ROW", page_row);
		/* paging control set End */

		// 후기 (댓글) 목록 (페이지당 5개)
//		List<Map<String, Object>> commentList = openApiManageService.selectTableCommentList(map);		
//		int total   = 0;
//		if(commentList.size() != 0 ) {
//			total = ((BigInteger) commentList.get(0).get("total_count")).intValue();
//		}
		
		/* paging html make */
//		Paging paging = new Paging();
//		paging.setCurrentPage(currentPageNo);
//		paging.setPageBlock(5);
//		paging.setPageSize(page_row);
//		paging.setTotalA(total);
//		paging.makePagingHTML("/openapi/reserveManage.do");
		/* paging html make end */
		
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("### OpenApiManageController reserveRegisteManage commentList size :: " + commentList.size() + ", commentList=" + commentList); 
//		}

//		mv.addObject("paging", paging.getPagingHTML());
//    	mv.addObject("total", total);
//		mv.addObject("tableCommentList", commentList);


		mv.addObject("result", result);
		
		return mv;
	}

	/**
	 * 현재 calendar의 예약건수/총건수(테이블, 펜션) 를 얻는다. 
	 * @see 화면에서 calendar이 랜더링 될때 호출된다. 
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/selectReserveTotCnt.do")
	public ModelAndView selectReserveTotCnt(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController selectReserveTotCnt START");
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		Calendar calendar = Calendar.getInstance();
		Locale currentLocale = new Locale("KOREAN", "KOREA");
		String pattern = "yyyyMMdd"; //hhmmss로 시간,분,초만 뽑기도 가능
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, currentLocale);

		if (map.get("ymd") == null || map.get("ymd").equals("")) {
			map.put("ymd", sdf.format(calendar.getTime()) );
		}
		
		// 현재월 이후 테이블(일반, 평상, 애견동반) 예약건수/총건수
		List<Map<String, Object>> reserveTblCntList = openApiManageService.selectReserveTblCntList(map);		
		mv.addObject("reserveTblCntList", reserveTblCntList);
		
		// 현재원 이후 펜션 예약건수/총건수
		List<Map<String, Object>> reservePensionCntList = openApiManageService.selectReservePensionCntList(map);		
		mv.addObject("reservePensionCntList", reservePensionCntList);

				
		mv.addObject("result", result);
		
		return mv;
	}

	/**
	 * Front 남양펜션/계곡 날짜 선택 후 테이블 목록 화면 호출
	 * @see parameter로 tableCd를 받는데 null 또는 empty이면 전체 table정보를 보여주고, value가 있으면 해당 테이블의 정보만 얻어온다
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveManage2.do")
	public ModelAndView reserveRegisteManage2(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteManage2 START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage2");

		mv.addObject("ymd", map.get("ymd"));

		// 주메뉴 리스트
		List<Map<String, Object>> largeMenuList = commonService.selectCode01("02"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
		mv.addObject("largeMenuList", largeMenuList);

		// 부메뉴 리스트
		List<Map<String, Object>> subMenuList = commonService.selectCode01("03"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
		mv.addObject("subMenuList", subMenuList);
		
		// 대여용품 리스트
		List<Map<String, Object>> rentalList = commonService.selectCode01("04"); /* 공통코드(01:업종코드, 02:주메뉴코드, 03:부메뉴코드, 04:대여용품코드) */		
		mv.addObject("rentalMenuList", rentalList);

		// 전체 테이블 리스트
		List<Map<String, Object>> tableInfoList = openApiManageService.selectTableInfoList(map);; /* 전체 테이블 리스트 */		
		mv.addObject("tableList", tableInfoList);

		// 전체 펜션 리스트
		List<Map<String, Object>> pensionInfoList = openApiManageService.selectPensionInfoList(map);; /* 전체 펜션 리스트 */		
		mv.addObject("pensionList", pensionInfoList);

//		if (map.get("tableCd") != null &&  !map.get("tableCd").toString().isEmpty()) {
//			// 테이블정보 (테이블 Spec, 테이블 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
//			Map<String, Object> tableInfo = openApiManageService.selectTableInfo(map);		
//			mv.addObject("tableInfo", tableInfo);
//		}
//		else if (map.get("pensionCd") != null && !map.get("pensionCd").toString().isEmpty()) {
//			// 펜션정보 (펜션 Spec, 펜션 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
//			Map<String, Object> pensionInfo = openApiManageService.selectPensionInfo(map);		
//			mv.addObject("pensionInfo", pensionInfo);
//		}
		
		/* paging control set */
		if(map.containsKey("currentPageNo") == false || StringUtils.isEmpty(map.get("currentPageNo")) == true) map.put("currentPageNo","1");
		int currentPageNo = Integer.valueOf((String)map.get("currentPageNo"));
		int page_row = 5;
		int start = (page_row*currentPageNo)-page_row;
		map.put("START", start);
		map.put("PAGE_ROW", page_row);
		/* paging control set End */

		// 후기 (댓글) 목록 (페이지당 5개)
		List<Map<String, Object>> commentList = openApiManageService.selectTableCommentList(map);		
		int total   = 0;
		if(commentList.size() != 0 ) {
			total = ((BigInteger) commentList.get(0).get("total_count")).intValue();
		}
		
		/* paging html make */
		Paging paging = new Paging();
		paging.setCurrentPage(currentPageNo);
		paging.setPageBlock(5);
		paging.setPageSize(page_row);
		paging.setTotalA(total);
		paging.makePagingHTML("/openapi/reserveManage2.do");
		/* paging html make end */
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteManage2 commentList size :: " + commentList.size() + ", commentList=" + commentList); 
		}

		mv.addObject("paging", paging.getPagingHTML());
    	mv.addObject("total", total);
		mv.addObject("tableCommentList", commentList);


		mv.addObject("result", result);
		
		return mv;
	}

	/**
	 * 선택한 테이블(펜션) 정보를 얻는다. 
	 * @see 화면에서 테이블 선택 시 해당 테이블의 상세정보를 얻는다. 
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/selectDetailInfo.do")
	public ModelAndView selectDetailInfo(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController selectDetailInfo START :: tableCd=" + map.get("tableCd") );
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		if (map.get("tableCd") != null &&  !map.get("tableCd").toString().isEmpty()) {
			// 테이블정보 (테이블 Spec, 테이블 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
			Map<String, Object> tableInfo = openApiManageService.selectTableInfo(map);		
			mv.addObject("tableInfo", tableInfo);
			
			map.put("atchFileId", tableInfo.get("tableImageAll"));
			List<Map<String, Object>> fileMap = openApiManageService.selectFileList(map);
			mv.addObject("atchfilelist", fileMap);
		}
		else if (map.get("pensionCd") != null && !map.get("pensionCd").toString().isEmpty()) {
			// 펜션정보 (펜션 Spec, 펜션 상세설명, 이미지, 썸네일 이미지 1 ~ 5)
			Map<String, Object> pensionInfo = openApiManageService.selectPensionInfo(map);		
			mv.addObject("pensionInfo", pensionInfo);

			map.put("atchFileId", pensionInfo.get("pensionImageAll"));
			List<Map<String, Object>> fileMap = openApiManageService.selectFileList(map);
			mv.addObject("atchfilelist", fileMap);
		}
				
		mv.addObject("result", result);
		
		return mv;
	}

	/**
	 * Front 남양펜션/계곡 예약등록 
	 * @see 바로예약 버튼 클릭 시 
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveRegiste.do")
	public ModelAndView reserveRegisteFront(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception{
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("### OpenApiManageController reserveRegisteFront START ::\n" +
//					" 결제를 위한 파라미터 : inicisMid=" + inicisMid + "\n" + ", inicisSignKey=" + inicisSignKey + "\n" + 
//					", inicisUrl=" + inicisUrl + "\n, inicisReturnUrl=" + inicisReturnUrl + "\n, inicisCloseUrl=" + inicisCloseUrl + "\n" +
//					" 예약을 위한 파라미터 : orderName=" + map.get("orderName") + ", reserveName=" + map.get("reserveName") + ", reserveTel=" + map.get("reserveTel") + ", reserveEmail=" + map.get("reserveEmail") + 
//					", reserveAdult=" + map.get("reserveAdult") + ", reserveChildren=" + map.get("reserveChildren") + 
//					", reserveStartDate=" + map.get("reserveStartDate") + ", reserveEndDate=" + map.get("reserveEndDate") + "\n" +
//					" 테이블 단독 : reserveTableCnt=" + map.get("reserveTableCnt") + "\n" +  
//					" Pension 단독 : reserveDongCd=" + map.get("reserveDongCd") + ", reserveRoomCd=" + map.get("reserveRoomCd") +  
//					", reserveAppendCnt=" + map.get("reserveAppendCnt") + "\n" +
//					" 메뉴 : 주메뉴=" + map.get("lmenuchk") + ", 수량=" + map.get("largeMenuItem") + "\n" + 
//					"     : 부메뉴=" + map.get("smenuchk") + ", 수량=" + map.get("subMenuItem") + "\n" +
//					"     : 대여메뉴=" + map.get("rmenuchk") + ", 수량=" + map.get("rentalMenuItem") + "\n" +
//					" 공통 : bigo=" + map.get("bigo") + "\n" + 
//					", payVat=" + map.get("payVat") + ", payAmt=" + map.get("payAmt")   
//			);
//		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");
		
		Calendar calendar = Calendar.getInstance();
		Locale currentLocale = new Locale("KOREAN", "KOREA");
		String pattern = "yyyyMMdd"; //hhmmss로 시간,분,초만 뽑기도 가능
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, currentLocale);
		String nowDate = sdf.format(calendar.getTime());

		String mKey = SignatureUtil.hash(inicisSignKey, "SHA-256");
		String timestamp = SignatureUtil.getTimestamp();			// util에 의해서 자동생성
		String orderNumber	= inicisMid +"_" + SignatureUtil.getTimestamp();	// 가맹점 주문번호(가맹점에서 직접 설정)
		String use_chkfake	= "Y";									// verification 검증 여부 ('Y' , 'N')

		Map<String, String> signParam = new HashMap<String, String>();

		signParam.put("oid", orderNumber);
		signParam.put("price", map.get("payAmt").toString());
		signParam.put("timestamp", timestamp);
		
		String signature = SignatureUtil.makeSignature(signParam);			// signature 대상: oid, price, timestamp (알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
		signParam.put("signKey", inicisSignKey);
		String verification = SignatureUtil.makeSignature(signParam);		// verification 대상 : oid, price, signkey, timestamp (알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)

		mv.addObject("mKey", mKey);
		mv.addObject("mid", inicisMid);
		mv.addObject("url", inicisUrl);
		mv.addObject("timeStamp", timestamp);
		mv.addObject("orderNumber", orderNumber);
		mv.addObject("useChkFake", use_chkfake);
		mv.addObject("signature", signature);
		mv.addObject("verification", verification);
		
		mv.addObject("goodName", map.get("orderName"));
		mv.addObject("buyerName", map.get("reserveName"));
		mv.addObject("buyerTel", map.get("reserveTel"));
		mv.addObject("buyerEmail", map.get("reserveEmail"));
		mv.addObject("price", map.get("payAmt"));

//		mv.addObject("rsvAdultCnt", map.get("reserveAdult"));
//		mv.addObject("rsvChildCnt", map.get("reserveChildren"));
//		mv.addObject("rsvTableCnt", map.get("reserveTableCnt"));
		mv.addObject("rsvStartDate", map.get("reserveStartDate"));
		mv.addObject("rsvEndDate", map.get("reserveEndDate"));
//		
//		mv.addObject("rsvDongCd", map.get("reserveDongCd"));
//		mv.addObject("rsvRoomCd", map.get("reserveRoomCd"));
//		mv.addObject("rsvAppendCnt", map.get("reserveAppendCnt"));
//
//		mv.addObject("rsvSpecialBigo", map.get("bigo"));

		String acptMethod = "HPP(1):below1000:centerCd(Y):vbank(" + nowDate + ")";
		mv.addObject("acceptMethod", acptMethod);
//		mv.addObject("acceptMethod", "HPP(1):below1000:centerCd(Y)");
		
		mv.addObject("returnUrl", inicisReturnUrl);
		mv.addObject("closeUrl", inicisCloseUrl);
		mv.addObject("tax", map.get("payVat")); /* 부가세 */
		mv.addObject("charset", "UTF-8"); /* 결과수신 인코딩 타입 */

//		mv.addObject("mechantData", "abcd!@#$ABCD");
		mv.addObject("mechantData", map);
		
		// *****************
		// DB에 예약정보 저장 (orderNumber을 이용하여 저장해야 한다. 이니시스에서 reserveReturn.do를 호출할때 파라미터로 orderNumber만 보내줄수 있다.)
		// *****************
		// 예약정보 저장
		map.put("orderNumber", orderNumber);
		int retResult = (int) openApiManageService.insertReserveInfo(map);
		
		// 주메뉴 주문정보 저장
		// 주메뉴 예약정보(메뉴 종류/갤수)를  order_menu table에 저장한다.
		String[] strLmenuChk = request.getParameterValues("lmenuchk");
		String[] strLmenuItem = request.getParameterValues("largeMenuItem");
		String[] strLmenuPrice = request.getParameterValues("lmenuunitprice");

		if (strLmenuChk != null && strLmenuChk.length > 0) {
			int[] retOrderLargeResult = new int[strLmenuChk.length];
			for(int i=0; i<strLmenuChk.length; i++ ) {
				Map<String, Object> menuMap = new HashMap<String, Object>();
				menuMap.put("orderNumber", orderNumber);		
				menuMap.put("menuGubunCd", strLmenuChk[i].substring(0, 2) );
				menuMap.put("menuCd", strLmenuChk[i] );
				menuMap.put("menuCnt", strLmenuItem[i] );
				menuMap.put("menuUnitPrice", strLmenuPrice[i] );
				menuMap.put("reserveStartDate", map.get("reserveStartDate") );
				menuMap.put("reserveEndDate", map.get("reserveEndDate") );
			
				retOrderLargeResult[i] = (int) openApiManageService.insertOrderMenu(menuMap);
				
				// if ( LOG.isDebugEnabled()) {
				// 		LOG.debug("large menuMap=" + menuMap.toString()); 
				// }
			}
		}

		// 부메뉴 주문정보 저장
		// 부메뉴 예약정보(메뉴 종류/갤수)를  order_menu table에 저장한다.
		String[] strSmenuChk = request.getParameterValues("smenuchk");
		String[] strSmenuItem = request.getParameterValues("subMenuItem");
		String[] strSmenuPrice = request.getParameterValues("smenuunitprice");

		if (strSmenuChk != null && strSmenuChk.length > 0) {
			int[] retOrderSubResult = new int[strSmenuChk.length];
			for(int i=0; i<strSmenuChk.length; i++ ) {
				Map<String, Object> menuMap = new HashMap<String, Object>();
				menuMap.put("orderNumber", orderNumber);		
				menuMap.put("menuGubunCd", strSmenuChk[i].substring(0, 2) );
				menuMap.put("menuCd", strSmenuChk[i] );
				menuMap.put("menuCnt", strSmenuItem[i] );
				menuMap.put("menuUnitPrice", strSmenuPrice[i] );
				menuMap.put("reserveStartDate", map.get("reserveStartDate") );
				menuMap.put("reserveEndDate", map.get("reserveEndDate") );
			
				retOrderSubResult[i] = (int) openApiManageService.insertOrderMenu(menuMap);

				// if ( LOG.isDebugEnabled()) {
				// 		LOG.debug("sub menuMap=" + menuMap.toString());
				// }
			}
		}

		// 대여메뉴 주문정보 저장
		// 대여메뉴 예약정보(메뉴 종류/갤수)를 order_menu table에 저장한다.
		String[] strRmenuChk = request.getParameterValues("rmenuchk");
		String[] strRmenuItem = request.getParameterValues("rentalMenuItem");
		String[] strRmenuPrice = request.getParameterValues("rmenuunitprice");

		if (strRmenuChk != null && strRmenuChk.length > 0) {
			int[] retOrderRentalResult = new int[strRmenuChk.length];
			for(int i=0; i<strRmenuChk.length; i++ ) {
				Map<String, Object> menuMap = new HashMap<String, Object>();
				menuMap.put("orderNumber", orderNumber);		
				menuMap.put("menuGubunCd", strRmenuChk[i].substring(0, 2) );
				menuMap.put("menuCd", strRmenuChk[i] );
				menuMap.put("menuCnt", strRmenuItem[i] );
				menuMap.put("menuUnitPrice", strRmenuPrice[i] );
				menuMap.put("reserveStartDate", map.get("reserveStartDate") );
				menuMap.put("reserveEndDate", map.get("reserveEndDate") );
			
				retOrderRentalResult[i] = (int) openApiManageService.insertOrderMenu(menuMap);

				// if ( LOG.isDebugEnabled()) {
				// 		LOG.debug("rental menuMap=" + menuMap.toString());
				// }
			}
		}

		
		// 예약테이블 정보 저장
		if (map.get("reserveTableNPensionGubun").equals("0")) { // 테이블/펜션 구분 (0:테이블, 1:펜션)
			String tableGubunCd = map.get("reserveTableGubunCd").toString();
			int rsvTableCnt = Integer.parseInt(map.get("reserveTableCnt").toString());
			// String startTableName = map.get("ptTitle").toString();
			String startTableCd = map.get("reserveTableCd").toString();
			// String tempTableCd = ""; //startTableCd;
			
			if (rsvTableCnt > 0) {
				int[] retOrderRsvTableResult = new int[rsvTableCnt];
				
				for(int i=0; i<rsvTableCnt; i++ ) {
					Map<String, Object> tableRsvMap = new HashMap<String, Object>();
					tableRsvMap.put("orderNumber", orderNumber);		
					tableRsvMap.put("tableGubunCd", tableGubunCd );					
					tableRsvMap.put("tableCd", startTableCd );
					
					tableRsvMap.put("rsvStartDate", map.get("reserveStartDate") );
					tableRsvMap.put("rsvEndDate", map.get("reserveEndDate") );

					// 처음 예약 테이블 정보 저장
					retOrderRsvTableResult[i] = (int) openApiManageService.insertRsvTable(tableRsvMap);
					
					if ( LOG.isDebugEnabled()) {
						LOG.debug("tableRsvMap=" + tableRsvMap.toString());
					}
					
					if (i+1 <= rsvTableCnt) { // 예약하는 테이블이 다수일 경우
						Map<String, Object> tableRsvMaxMap = new HashMap<String, Object>();
						tableRsvMaxMap.put("orderNumber", orderNumber);		
						tableRsvMaxMap.put("tableGubunCd", tableGubunCd );					
						tableRsvMaxMap.put("tableCd", startTableCd );
						
						tableRsvMaxMap.put("rsvStartDate", map.get("reserveStartDate") );
						tableRsvMaxMap.put("rsvEndDate", map.get("reserveEndDate") );
	
						// 예약된 테이블 중에 가까운 테이블 CD 얻기
						tableRsvMaxMap = openApiManageService.selectRsvTableMax(tableRsvMaxMap);
						if (tableRsvMaxMap != null && tableRsvMaxMap.size() > 0) {
							startTableCd = tableRsvMaxMap.get("tableCd").toString();
						}
						
						if ( LOG.isDebugEnabled()) {
							LOG.debug("tableRsvMaxMap=" + tableRsvMaxMap.toString());
						}
						
					}
				}
			}
		}
//		else { // 예약 펜션 정보 저장 (펜션으로 예약될 경우 reserve 테이블에 직접 저장을 한다)
//			
//			// map.put("orderNumber", orderNumber);
//			int retUpdatePensionInfoResult = (int) openApiManageService.updateReservePensionInfo(map);
//		}

		
		// *****************

		mv.addObject("result", result);
		
		return mv;
	}
	
	@RequestMapping(value="/openapi/reserveReturnTest.do")
	public ModelAndView reserveRegisteReturnManageTest(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteReturnManageTest START :: ");
			LOG.debug("### OpenApiManageController return Map : " + map.toString() ); // map.get("orderNumber")
		}

		String result = "success";
		String resultCode = "0000";
		String resultMsg = "결제승인이 정상처리 되었습니다.";
		String orderNumber = (String) map.get("orderNumber");
		
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage3");
		
		resultMsg += "<br/><br/>";
		resultMsg += "주문 번호 : " 		+ "12345678"	+ "<br/>"; 
		resultMsg += "상 품 명 : " 		+ "남양계곡상품등" 	+ "<br/>"; 
		resultMsg += "예약자 명 : " 		+ "홍길동" 	+ "<br/>";
		resultMsg += "예약자 연락처  : " 	+ "010-1234-5678" 	+ "<br/>";
		resultMsg += "예약자 Email : " 	+ "abc@naver.com" 	+ "<br/>";
		resultMsg += "결제 금액 : " 		+ "10000원" 	+ "<br/>";

		mv.addObject("result", result);
		mv.addObject("resultCode", resultCode);
		mv.addObject("resultMsg", resultMsg);
		
		String smsResult = sendSMS("01040731867","test123 - second test");
		if (LOG.isDebugEnabled()) {
			LOG.debug("### =========== : " + smsResult);
		}
		
		
		return mv;		
	}

	
	/**
	 * Front 남양펜션/계곡 결제 팝업이 모두 끝난뒤 후 이니시스에서 호출하는 서비스
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveReturn.do")
	public ModelAndView reserveRegisteReturnManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteReturnManage START :: ");
			LOG.debug("### OpenApiManageController return Map : " + map.toString() ); // map.get("orderNumber")
		}

		String result = "success";
		String resultCode = "0000";
		String resultMsg = "결제승인이 정상처리 되었습니다.";
		String orderNumber = (String) map.get("orderNumber");
		
		
//		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage");
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontManage3");

		Map<String, String> resultMap = new HashMap<String, String>();

		try {

			//#############################
			// 인증결과 파라미터 일괄 수신
			//#############################
			request.setCharacterEncoding("UTF-8");

			Map<String,String> paramMap = new Hashtable<String,String>();
			
			Enumeration elems = request.getParameterNames();
			
			String temp = "";

			while(elems.hasMoreElements()) {
				temp = (String) elems.nextElement();
				paramMap.put(temp, request.getParameter(temp));
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("### OpenApiManageController paramMap : " + paramMap.toString() ); // paramMap.get("orderNumber")
			}

			//##############################
			// 인증성공 resultCode=0000 확인
			// IDC센터 확인 [idc_name=fc,ks,stg]	
			// idc_name 으로 수신 받은 값 기준 properties 에 설정된 승인URL과 authURL 이 같은지 비교
			// 승인URL은  https://manual.inicis.com 참조
			//##############################
			String idcName = "";
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("### paramMap.get('authUrl') : " + paramMap.get("authUrl") + ", paramMap.get('idc_name') : " + paramMap.get("idc_name") );
			}

			// if("0000".equals(paramMap.get("resultCode")) && paramMap.get("authUrl").equals(ResourceBundle.getBundle("properties/idc_name").getString(paramMap.get("idc_name")))){
			if (paramMap.get("idc_name") != null) {
				if (paramMap.get("idc_name").equals("fc") ) {
					idcName = inicisFc;
				}
				else if (paramMap.get("idc_name").equals("ks")) {
					idcName = inicisKs;
				}
				else if (paramMap.get("idc_name").equals("stg")) {
					idcName = inicisStg;
				}
				else {
					idcName = "";
				}
			}
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("### idcName : " + idcName + ", resultCode : " + paramMap.get("resultCode") + ", resultMsg : " + paramMap.get("resultMsg"));
			}

			if("0000".equals(paramMap.get("resultCode")) && paramMap.get("authUrl").equals( idcName ) ) {
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("#### 인증성공/승인요청 ####");
				}

				//############################################
				// 1.전문 필드 값 설정(***가맹점 개발수정***)
				//############################################
				
				String mid 		= paramMap.get("mid");
				String timestamp= SignatureUtil.getTimestamp();
				String charset 	= "UTF-8";
				String format 	= "JSON";
				String authToken= paramMap.get("authToken");
				String authUrl	= paramMap.get("authUrl");
				String netCancel= paramMap.get("netCancelUrl");	
				String merchantData = paramMap.get("merchantData");
				
				//#####################
				// 2.signature 생성
				//#####################
				Map<String, String> signParam = new HashMap<String, String>();

				signParam.put("authToken",	authToken);		// 필수
				signParam.put("timestamp",	timestamp);		// 필수

				// signature 데이터 생성 (모듈에서 자동으로 signParam을 알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
				String signature = SignatureUtil.makeSignature(signParam);
				
				signParam.put("signKey",	inicisSignKey); // "SU5JTElURV9UUklQTEVERVNfS0VZU1RS");		// 필수
				
				// signature 데이터 생성 (모듈에서 자동으로 signParam을 알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
				String verification = SignatureUtil.makeSignature(signParam);				
				

				if (LOG.isDebugEnabled()) {
					LOG.debug("#### 인증성공/승인요청 전문생성 ####");
				}

				//#####################
				// 3.API 요청 전문 생성
				//#####################
				Map<String, String> authMap = new Hashtable<String, String>();

				authMap.put("mid"			, mid);			// 필수
				authMap.put("authToken"		, authToken);	// 필수
				authMap.put("signature"		, signature);	// 필수
				authMap.put("verification"	, verification);	// 필수
				authMap.put("timestamp"		, timestamp);	// 필수
				authMap.put("charset"		, charset);		// default=UTF-8
				authMap.put("format"		, format);

				HttpUtil httpUtil = new HttpUtil();

				try {
					//#####################
					// 4.API 통신 시작
					//#####################

					String authResultString = "";
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("#### 인증성공/승인요청 전문 전송중 ... ####");
					}

					authResultString = httpUtil.processHTTP(authMap, authUrl);
					
					//############################################################
					//6.API 통신결과 처리(***가맹점 개발수정***)
					//############################################################
					
					String test = authResultString.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "");
								
					if (LOG.isDebugEnabled()) {
						LOG.debug("#### 인증성공/승인요청 전문 처리결과 ... ####");
						LOG.debug("#### authResultString : " + test);
					}

					resultMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱

					resultCode = resultMap.get("resultCode");
					resultMsg = "결제가 " + resultMap.get("resultMsg");
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("#### resultCode : " + resultCode + ", resultMsg : " + resultMsg + ", resultMap : " + resultMap.toString());
					}

				  	// 수신결과를 파싱후 resultCode가 "0000"이면 승인성공 이외 실패
				   	//throw new Exception("강제 망취소 요청 Exception ");
					
					if (resultCode.equals("0000")) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("#### 인증성공/승인요청 전문 처리결과 끝 ####");
						}
						
						// *****************
						// DB에 결제상태 저장
						// *****************
						// 이니시스에서 수신된 내용을 payment 테이블에 저장 moid(주문번호 orderNumber) 
						int retPaymentTableResult = 0;
						int retReserveTableUpdate = 0;
						resultMap.put("orderNumber", orderNumber);

						// payment 테이블에 결제내역 저장
						retPaymentTableResult = (int) openApiManageService.insertPaymentTable(resultMap);
						
						resultMsg += "<br/><br/>";
						resultMsg += "주문 번호 : " 		+ resultMap.get("orderNumber")	+ "<br/>"; 
						resultMsg += "상 품 명 : " 		+ resultMap.get("goodName") 	+ "<br/>"; 
						resultMsg += "예약자 명 : " 		+ resultMap.get("buyerName") 	+ "<br/>";
						resultMsg += "예약자 연락처  : " 	+ resultMap.get("buyerTel") 	+ "<br/>";
						resultMsg += "예약자 Email : " 	+ resultMap.get("buyerEmail") 	+ "<br/>";
						resultMsg += "결제 금액 : " 		+ resultMap.get("TotPrice") 	+ "<br/>";
						// resultMsg += "지불 수단 : " 		+ resultMap.get("payMethod") 	+ "<br/>";
						
				        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				        
				        // LOG.debug("resultMap.get('applDate') + ' ' + resultMap.get('applTime') = " + resultMap.get("applDate") + " " + resultMap.get("applTime"));
				        
				        // 문자열 -> Date
				        String strDate = resultMap.get("applDate") + resultMap.get("applTime");
				        LocalDateTime localTime = LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
						resultMsg += "승인 일자 : " 		+ localTime + "<br/>"; //resultMap.get("applDate") 	+ " " + resultMap.get("applTime") + "\n";
						
						if (resultMap.get("payMethod").equals("VBank" )) { // 무통장입금이면
							resultMsg += "입금은행명  : " + resultMap.get("vactBankName") + "<br/>";
							resultMsg += "입금계좌번호 : " + resultMap.get("VACT_Num") + "<br/>";
							resultMsg += "예금주명    : " + resultMap.get("VACT_Name") + "<br/>";

							strDate = resultMap.get("VACT_Date") + resultMap.get("VACT_Time");
					        localTime = LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
							resultMsg += "입금기한일자 : " + localTime + "<br/>"; // resultMap.get("VACT_Date") + " " + resultMap.get("VACT_Time") + "\n";
							resultMsg += "송금자명    : " + resultMap.get("VACT_InputName") + "<br/>";
						}
						else { // 신용카드 이면 
							// 예약 테이블에 결제완료 update
							resultMap.put("compPayFlag", "0"); // 결제완료 여부(null: 결제중사용자 취소, 0:결제완료, 1:결제대기중(무통장입금의 경우), 8:관리자가 예약등록, 9:관리자가 예약취소, 이니시스 승인상태에 따라 추가)
							retReserveTableUpdate = (int) openApiManageService.updateReserveTable(resultMap);
							
							resultMsg += "카드 번호 : " + resultMap.get("CARD_Num") + "<br/>";
							if (resultMap.get("CARD_CheckFlag").equals("0")) { // 신용카드
								resultMsg += "카드 종류 : " + "신용카드" + "<br/>";	
							}
							else if (resultMap.get("CARD_CheckFlag").equals("1")) { // 체크카드
								resultMsg += "카드 종류 : " + "체크카드" + "<br/>";							
							}
							else {
								resultMsg += "카드 종류 : " + "기타카드" + "<br/>";							
							}
							
							resultMsg += "승인 번호 : " + resultMap.get("applNum") + "<br/>";
							resultMsg += "카드할부기간 : " + resultMap.get("CARD_Quota") + "<br/>";

							// 관리자 계정으로 문자를 보낸다. (카드로 결제했을 경우만 메시지를 보내고, 무통장 입금일 경우 Noti에서 입금 수신을 했을 경우 메시지를 보낸다.
							// String smsResult = sendSMS(smsAdmin, "Card", resultMap.get("buyerName"), resultMap.get("TotPrice"));
							String smsResult = sendSMS(smsAdmin, "관리자님\n" + "\n" + 
										"입금자 : " + resultMap.get("buyerName") + "\n" +  
										"금 액  : " + resultMap.get("TotPrice") + "원"+ "\n" + 
										"을 카드로 결제하셨습니다.");
							
							if (LOG.isDebugEnabled()) {
								LOG.debug("SMS Result = " + smsResult);
							}
							
						}

					}
//					else { // 망취소를 결제 취소 기능으로 사용하지 말것
//						if ( !resultCode.equals("R201")) { // 승인요청 완료건으로 재 승인요청 불가
//							throw new Exception("강제 망취소 요청 Exception ");
//						}
//					}
				  	
				} catch (Exception ex) { // 인증성공/승인요청 중 Exception

					//####################################
					// 실패시 처리(***가맹점 개발수정***)
					//####################################
					// *****************
					// DB에 예약정보 삭제 (orderNumber을 가지고 삭제)
					// 예약정보 삭제					
					// 메뉴주문정보 삭제
					// 예약테이블 정보 삭제
					// *****************
					resultMap.put("orderNumber", orderNumber);
					int retReserveTableResult = 0;
					retReserveTableResult = (int) openApiManageService.deleteReserveTable(resultMap);

					int retOrderMenuTableResult = 0;
					retOrderMenuTableResult = (int) openApiManageService.deleteOrderMenuTable(resultMap);

					int retRsvTableResult = 0;
					retRsvTableResult = (int) openApiManageService.deleteRsvTable(resultMap);
					
					int retPaymentTableResult = 0;
					retPaymentTableResult = (int) openApiManageService.deletePaymentTable(resultMap);

					//---- db 저장 실패시 등 예외처리----//
					// System.out.println(ex);
					if (LOG.isDebugEnabled()) {
						LOG.debug("#### 인증성공/승인요청 실패 -> 망취소 호출 #### : " + ex);
					}

					//#####################
					// 망취소 API
					//#####################
					String netcancelResultString = httpUtil.processHTTP(authMap, netCancel);	// 망취소 요청 API url(고정, 임의 세팅 금지)

					// out.println("## 망취소 API 결과 ##");
					// 망취소 결과 확인
					// out.println("<p>"+netcancelResultString.replaceAll("<", "&lt;").replaceAll(">", "&gt;")+"</p>");
					
					resultMap = ParseUtil.parseStringToMap( netcancelResultString.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "") ); //문자열을 MAP형식으로 파싱

					if (LOG.isDebugEnabled()) {						
						LOG.debug("#### 망취소 API 송신 결과 #### " + resultMap.toString());
						LOG.debug("#### 망취소 API 송신 결과 : resultCode=" + resultMap.get("resultCode") + ", resultMsg=" + resultMap.get("resultMsg") );
					}
					
					resultCode = "9999"; //resultMap.get("resultCode");
					resultMsg = ex.getMessage(); //.get("resultMsg") + "\n" + ex.getMessage(); //"승인요청 중 오류로 인해 취소 되었습니다.";
				}

			} else { // 인증실패
				resultMap.put("resultCode", paramMap.get("resultCode"));
				resultMap.put("resultMsg", paramMap.get("resultMsg"));
				
				resultCode = paramMap.get("resultCode");
				resultMsg = paramMap.get("resultMsg"); // 인증이 실패 하였습니다.
			}

		} catch(Exception e) { // 인증결과 받다가 Exception
			// System.out.println(e);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception : " + e);
			}
		}

		mv.addObject("result", result);
		mv.addObject("resultCode", resultCode);
		mv.addObject("resultMsg", resultMsg);
		
		sendSMS("010-4073-1867", "test123");
		
		return mv;		
	}

	/**
	 * Front 남양펜션/계곡 예약결제 진행중 팝업에서 취소 또는 창이 닫힐때 이니시스에서 호출하는 Close 페이지
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/reserveClose.do")
	public ModelAndView reserveRegisteCloseManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController reserveRegisteCloseManage START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("/NamyangReserveManager/front/reserveFrontPayClose");
		
		//#############################
		// 파라미터 일괄 수신
		//#############################
		request.setCharacterEncoding("UTF-8");

		Map<String,String> paramMap = new Hashtable<String,String>();
		
		Enumeration elems = request.getParameterNames();
		
		String temp = "";

		while(elems.hasMoreElements()) {
			temp = (String) elems.nextElement();
			paramMap.put(temp, request.getParameter(temp));
		}
		//#############################

		// ***************** 삭제가 맞는지 다시한번 확인 필요
		// DB에 예약정보 삭제 (orderNumber을 가지고 삭제)
		// 예약정보 삭제					
		// 메뉴주문정보 삭제
		// 예약테이블 정보 삭제
		// *****************

		if (LOG.isDebugEnabled()) {
			LOG.debug("### 사용자 취소 또는 결제 팝업 진행중 close 선택/승인요청 없음 ####");
			// LOG.debug("### paramMap : " + paramMap.toString() );
		}

		mv.addObject("result", result);
		mv.addObject("resultCode", "9999");
		mv.addObject("resultMsg", "결제 진행중 사용자 취소");
		
		return mv;		
	}

	/**
	 * Noti 수신 URL (이니시스에서 가상계좌에 고객이 입금을 했을 경우에 해당 Noti URL을 후출해준다)
	 * 이니시스에서 Noti URL을 호출할때 한글을 EUC-KR로 Encoding해서 보내므로 
	 * /common/util/MyEncodingFilter을 등록하고 web.xml에 Encoding 관련 filter을 2개 등록한 상태이므로
	 * 한글 Encoding 관련 이슈가 생기면 참고해서 볼것 !
	 *  
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/namyangNotiUrl.do")
	public String vactNotiManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {
	
		if (LOG.isDebugEnabled()) {
//			LOG.debug("### OpenApiManageController vactNotiManage START :: map=" + map.toString());
			LOG.debug("### OpenApiManageController vactNotiManage START :: map=" + URLDecoder.decode(map.toString(), "euc-kr"));
		}

		String result = "OK";
		
		/*******************************************************************************
		 * FILE NAME : vacctinput.jsp
		 * DATE : 2009.07
		 * 이니시스 가상계좌 입금내역 처리demon으로 넘어오는 파라메터를 control 하는 부분 입니다.
		 * [수신정보] 자세한 내용은 메뉴얼 참조
		 * 변수명           한글명                           
		 * no_tid           거래번호                         
		 * no_oid           주문번호                         
		 * cd_bank          거래발생 기관코드                
		 * cd_deal          취급기관코드                     
		 * dt_trans         거래일자                         
		 * tm_trans         거래시각                         
		 * no_vacct         계좌번호                         
		 * amt_input        입금금액                         
		 * amt_check        미결제타점권금액                 
		 * flg_close        마감구분                         
		 * type_msg         거래구분                         
		 * nm_inputbank     입금은행명                       
		 * nm_input         입금자명                         
		 * dt_inputstd      입금기준일자                     
		 * dt_calculstd     정산기준일자                     
		 * dt_transbase     거래기준일자                     
		 * cl_trans         거래구분코드 "1100"              
		 * cl_close         마감전후 구분,  0:마감점, 1마감후
		 * cl_kor           한글구분코드, 2:KSC5601          
		 *
		 * (가상계좌채번시 현금영수증 자동발급신청시에만 전달)
		 * dt_cshr          현금영수증 발급일자              
		 * tm_cshr          현금영수증 발급시간              
		 * no_cshr_appl     현금영수증 발급번호              
		 * no_cshr_tid      현금영수증 발급TID               
		 *******************************************************************************/

		/***********************************************************************************
		 * 이니시스가 전달하는 가상계좌이체의 결과를 수신하여 DB 처리 하는 부분 입니다.	
		 * 필요한 파라메터에 대한 DB 작업을 수행하십시오.
		 ***********************************************************************************/	

		//PG에서 보냈는지 IP로 체크 
		String REMOTE_IP = request.getRemoteAddr();
		String PG_IP = REMOTE_IP.substring(0, 10);
			
		if(PG_IP.equals("203.238.37") || PG_IP.equals("39.115.212") || PG_IP.equals("183.109.71")) {

			String file_path = inicisNotiLog; // "/home/was/INIpayJAVA/vacct";

			id_merchant  = request.getParameter("id_merchant");
			no_tid 		 = request.getParameter("no_tid");			// 거래번호
			no_oid 		 = request.getParameter("no_oid");			// 주문번호
			no_vacct 	 = request.getParameter("no_vacct"); 		// 계좌번호
			amt_input 	 = request.getParameter("amt_input");		// 입금금액

			nm_inputbank = URLDecoder.decode(map.get("nm_inputbank").toString(), "euc-kr"); // request.getParameter("nm_inputbank");
			nm_input 	 = URLDecoder.decode(map.get("nm_input").toString(), "euc-kr"); // request.getParameter("nm_input");
			
			dt_trans	 = request.getParameter("dt_trans"); // 거래일자
			tm_trans     = request.getParameter("tm_trans"); // 거래시각
			dt_inputstd  = request.getParameter("dt_inputstd"); // 입금기준일자
			dt_calculstd = request.getParameter("dt_calculstd"); // 정산기준일자
			dt_transbase = request.getParameter("dt_transbase"); // 거래기준일자


			// 매뉴얼을 보시고 추가하실 파라메터가 있으시면 아래와 같은 방법으로 추가하여 사용하시기 바랍니다.
			// String value = reqeust.getParameter("전문의 필드명");

			try {
				writeLog(file_path);

				//***********************************************************************************
				//
				//	위에서 상점 데이터베이스에 등록 성공유무에 따라서 성공시에는 "OK"를 이니시스로
				//	리턴하셔야합니다. 아래 조건에 데이터베이스 성공시 받는 FLAG 변수를 넣으세요
				//	(주의) OK를 리턴하지 않으시면 이니시스 지불 서버는 "OK"를 수신할때까지 계속 재전송을 시도합니다
				//	기타 다른 형태의 out.println(response.write)는 하지 않으시기 바랍니다
				
				// 수신된 정보 저장
				Map<String, Object> insertMap = new HashMap<String, Object>();
				insertMap.put("noTid", no_tid);					// 거래번호
				insertMap.put("noOid", no_oid);					// 주문번호
				insertMap.put("noVacct", no_vacct);				// 계좌번호
				insertMap.put("amtInput", amt_input);			// 입금금액
				insertMap.put("nmInputbank", nm_inputbank);		// 입금은행명
				insertMap.put("nmInput", nm_input);				// 입금자명
				insertMap.put("dtTrans"	, dt_trans); 			// 거래일자
				insertMap.put("tmTrans", tm_trans); 			// 거래시각
				insertMap.put("dtInputstd", dt_inputstd); 		// 입금기준일자
				insertMap.put("dtCalculstd", dt_calculstd); 	// 정산기준일자
				insertMap.put("dtTransbase", dt_transbase); 	// 거래기준일자
				
				int retReserveResult = (int) openApiManageService.updatePaymentTable(insertMap); // 결제테이블(Payment)갱신
				
				Map<String, String> upMap = new HashMap<String, String>();
				upMap.put("orderNumber", no_oid);
				upMap.put("compPayFlag", "0");
				int retReserve = openApiManageService.updateReserveTable(upMap); // 예약테이블에 결제완료 Flag Setting
				
				if (retReserveResult > 0) {
					//	out.print("OK"); // 절대로 지우지 마세요
					
//					String smsResult = sendSMS(smsAdmin, "무통장 으", nm_input, amt_input);
					String smsResult = sendSMS(smsAdmin, "관리자님\n" + 
										"입금자 : " + nm_input + "\n" +  
										"금 액 :  " + amt_input + "원" + "\n" +  
										"을 무통장입금 결제하셨습니다.");
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("SMS Result = " + smsResult);
					}
					
					result = "OK";
				}
			}
			catch(Exception e) {
				// out.print(e.getMessage());
				result = e.getMessage();
			}
		}

		return result;
	}

	/**
	 * 관리자에게 문자를 발송한다.
	 * 알리고 서비스를 사용 중이며 서비스 가입을 해야 한다. (smartssms.aligo.in)
	 * 
	 * @return
	 */
//	private String sendSMS(String _receiver, String _inputBankName, String _inputName, String _inputAmt) {
	private String sendSMS(String _receiver, String _strMsg) {
		
		String result = "";
		try{
			final String encodingType = "utf-8";
			final String boundary = "____boundary____";


			/**************** 문자전송하기 예제 ******************/
			/* "result_code":결과코드,"message":결과문구, */
			/* "msg_id":메세지ID,"error_cnt":에러갯수,"success_cnt":성공갯수 */
			/* 동일내용 > 전송용 입니다.  
			/******************** 인증정보 ********************/
			String sms_url = smsUrl; // "https://apis.aligo.in/send/"; // 전송요청 URL
			
			Map<String, String> sms = new HashMap<String, String>();
			
			sms.put("user_id", smsUserId); // "casaiic"); // SMS 아이디
			sms.put("key", smsKey); // "vn5h46t2rk4wis9lvp430hs1gsempqj1"); //인증키
			
			/******************** 인증정보 ********************/

			/******************** 전송정보 ********************/
//			sms.put("msg", "%고객명%님. 안녕하세요. API TEST SEND"); // 메세지 내용
			sms.put("receiver", _receiver); 					// 수신번호
			sms.put("msg", _strMsg); // "관리자님\n" + _inputName + "님께서 " + _inputBankName + "로 " + _inputAmt + "원을 입금하였습니다."); //"관리자님. 안녕하세요. 입금확인 API TEST SEND"); // 메세지 내용
			sms.put("destination", smsAdmin + "|%고객명%"); //01037355981|최성호"); 	// 수신인 %고객명% 치환
			sms.put("sender", smsSender); // ""); // 발신번호
			sms.put("rdate", ""); // 예약일자 - 20161004 : 2016-10-04일기준
			sms.put("rtime", ""); // 예약시간 - 1930 : 오후 7시30분
			sms.put("testmode_yn", "N"); // Y 인경우 실제문자 전송X , 자동취소(환불) 처리
			sms.put("title", smsTitle); // "SMS테스트"); //  LMS, MMS 제목 (미입력시 본문중 44Byte 또는 엔터 구분자 첫라인)
			
			String image = "";
			//image = "/tmp/pic_57f358af08cf7_sms_.jpg"; // MMS 이미지 파일 위치
			
			/******************** 전송정보 ********************/
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			
			builder.setBoundary(boundary);
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setCharset(Charset.forName(encodingType));
			
			for(Iterator<String> i = sms.keySet().iterator(); i.hasNext();){
				String key = i.next();
				builder.addTextBody(key, sms.get(key)
						, ContentType.create("Multipart/related", encodingType));
			}
			
			File imageFile = new File(image);
			if(image!=null && image.length()>0 && imageFile.exists()){
		
				builder.addPart("image", new FileBody(imageFile, ContentType.create("application/octet-stream"), URLEncoder.encode(imageFile.getName(), encodingType)));
			}
			
			HttpEntity entity = builder.build();
			
			HttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost(sms_url);
			post.setEntity(entity);
			
			HttpResponse res = client.execute(post);
			
			if(res != null){
				BufferedReader in = new BufferedReader(new InputStreamReader(res.getEntity().getContent(), encodingType));
				String buffer = null;
				while((buffer = in.readLine())!=null){
					result += buffer;
				}
				in.close();
			}
			
//			out.print(result);			
		} catch(Exception e){
//			out.print(e.getMessage());
			result = e.getMessage();
		}
		
		return result;
	}

    private String id_merchant;
    private String no_tid;
    private String no_oid;
    private String no_vacct;
    private String amt_input;
    private String nm_inputbank;
    private String nm_input;
    private String dt_trans;
    private String tm_trans;
    private String dt_inputstd;
    private String dt_calculstd;
    private String dt_transbase;

    private StringBuffer times;

    private String getDate()
    {
    	Calendar calendar = Calendar.getInstance();
    	
    	times = new StringBuffer();
        times.append(Integer.toString(calendar.get(Calendar.YEAR)));
		if((calendar.get(Calendar.MONTH)+1)<10) { 
            times.append("0"); 
        }
		
		times.append(Integer.toString(calendar.get(Calendar.MONTH)+1));
		if((calendar.get(Calendar.DATE))<10) { 
            times.append("0");	
        } 
		
		times.append(Integer.toString(calendar.get(Calendar.DATE)));
    	
    	return times.toString();
    }
    
    private String getTime()
    {
    	Calendar calendar = Calendar.getInstance();
    	
    	times = new StringBuffer();

    	times.append("[");
    	if((calendar.get(Calendar.HOUR_OF_DAY))<10) { 
            times.append("0"); 
        } 
    	
 		times.append(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
 		times.append(":");
 		if((calendar.get(Calendar.MINUTE))<10) { 
            times.append("0"); 
        }
 		
 		times.append(Integer.toString(calendar.get(Calendar.MINUTE)));
 		times.append(":");
 		if((calendar.get(Calendar.SECOND))<10) { 
            times.append("0"); 
        }
 		
 		times.append(Integer.toString(calendar.get(Calendar.SECOND)));
 		times.append("]");
 		
 		return times.toString();
    }

    private void writeLog(String file_path) throws Exception
    {

        File file = new File(file_path);
        file.createNewFile();

        FileWriter file2 = new FileWriter(file_path+"/vacctinput_"+getDate()+".log", true);

        file2.write("\n************************************************\n");
        file2.write("PageCall time : " + getTime());
        file2.write("\nID_MERCHANT : " + id_merchant);
        file2.write("\nNO_TID : " + no_tid);
        file2.write("\nNO_OID : " + no_oid);
        file2.write("\nNO_VACCT : " + no_vacct);
        file2.write("\nAMT_INPUT : " + amt_input);
        file2.write("\nNM_INPUTBANK : " + nm_inputbank);
        file2.write("\nNM_INPUT : " + nm_input);        
        file2.write("\n************************************************\n");

        file2.close();

    }

	/**
	 * 전체 결제 취소 (Card)
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/refundUrl.do")
	public ModelAndView refundManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController refundManage START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		Date date_now = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");
		SHA512INICIS sha512 = new SHA512INICIS();

		//step1. 요청을 위한 파라미터 설정
		String key = "ItEQKi3rY7uvDS8l"; 
		String type = "Refund";
		String paymethod = "Card";
		String timestamp = fourteen_format.format(date_now);
		String clientIp = InetAddress.getLocalHost().getHostAddress(); // "111.222.333.889";
		String mid = inicisMid; // "INIpayTest";
		String tid = (String) map.get("tid"); // ""; 
		String msg = "취소요청";
		
		// Hash Encryption
		String data_hash = key + type + paymethod + timestamp + clientIp + mid + tid;
		String hashData = sha512.hash(data_hash);
		
		// Request URL
		String apiUrl = inicisRefundUrl; // "https://iniapi.inicis.com/api/v1/refund";

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("type", type);
		resultMap.put("paymethod", paymethod);
		resultMap.put("timestamp", timestamp);
		resultMap.put("clientIp", clientIp);
		resultMap.put("mid", mid);
		resultMap.put("tid", tid);
		resultMap.put("msg", msg);
		resultMap.put("hashData", hashData);
		
		StringBuilder postData = new StringBuilder();
		for(Map.Entry<String, Object> params: resultMap.entrySet()) {
			
			if(postData.length() != 0) postData.append("&");
			try {
				postData.append(URLEncoder.encode(params.getKey(), "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(String.valueOf(params.getValue()), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//step2. key=value 로 post 요청
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			if (conn != null) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				conn.setRequestMethod("POST");
				conn.setDefaultUseCaches(false);
				conn.setDoOutput(true);
				
				if (conn.getDoOutput()) {
					conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}

				conn.connect();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
				
				//step3. 요청 결과
//				System.out.println(br.readLine());
//				br.close();
				String resultBr = br.readLine();
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("### OpenApiManageController refundManage[Card 전체 취소] : " + resultBr);
				}
				br.close();

//				String resultBr = br.toString();
				String test = resultBr.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "");
							
				Map<String, String> returnMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱
				String resultCode = returnMap.get("resultCode"); // 결과코드
				
				// 걸제 테이블에 전체취소(Card) update
				returnMap.put("tid", tid);
				int retPaymentRefundUpdate = (int) openApiManageService.updateAllCardRefundPayment(returnMap);					

				if(resultCode.equals("00")) {	// 전체취소 요청 성공
					// 예약 테이블에 전체환불(무통장) update
					returnMap.put("orderNumber", map.get("refundRsvId").toString());
					returnMap.put("compPayFlag",  "3"); // 결제완료 여부(null: 결제중사용자 취소, 0:결제완료, 1:결제대기중(무통장입금의 경우), 3:고객전체취소(Card), 4:고객부분취소(Card), 5:고객전체환불(가상계좌, 무통장), 6:고객부분환불(가상계좌, 무통장), 8:관리자가 예약등록, 9:관리자가 예약취소, 이니시스 승인상태에 따라 추가)
					int retReserveUpdate = (int) openApiManageService.updateReserveTable(returnMap);

					// update한 내역중 orderNumber의 custName, custMobile, custEmail 정보를 얻는다.
					Map<String, Object> telResult = openApiManageService.selectReserveTableTelQuery(returnMap);
					
					// 고객에게 취소 되었다는 문자를 보낸다.
					if (telResult != null) {
						int price = Integer.valueOf(telResult.get("payAmt").toString()) + Integer.valueOf(telResult.get("payVat").toString());
						String custMobile = telResult.get("custMobile").toString().replace("-", "");
						
	//					String smsResult = sendSMS(custMobile, "무통장 으", telResult.get("custName").toString(), Integer.toString(price));
						String smsResult = sendSMS(custMobile, telResult.get("custName") + "님께서 예약하신\n" + 
								"예약번호 : " + telResult.get("rsvId") + "\n" +  
								"결제방식 :  " + paymethod + "\n" +  
								"의 결제를 취소 하셨습니다.");
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("SMS Result = " + smsResult);
						}					
					}
				}

			}

		}catch(Exception e ) {
			e.printStackTrace();
		} 

		return mv;
	}

	/**
	 * 부분 결제 취소 (Card)
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/patialRefund.do")
	public ModelAndView parialRefundManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController parialRefundManage START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		Date date_now = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");
		SHA512INICIS sha512 = new SHA512INICIS();

		//step1. 요청을 위한 파라미터 설정
		String key = "ItEQKi3rY7uvDS8l"; 
		String type = "PartialRefund";
		String paymethod = "Card";
		String timestamp = fourteen_format.format(date_now);
		String clientIp = InetAddress.getLocalHost().getHostAddress(); // "111.222.333.889";
		String mid = inicisMid; // "INIpayTest";
		String tid = (String) map.get("tid"); // "";
		String msg = "취소요청";
		String price = (String) map.get("price"); // "";
		String confirmPrice = (String) map.get("confirmPrice"); //""; // 부분취소 후 남은금액
		
		String currency = "WON";
		String tax = (String) map.get("vat"); //""; // 부가세
		// String taxFree = ""; // 비과세
		
		// Hash Encryption
		String data_hash = key + type + paymethod + timestamp + clientIp + mid + tid + price + confirmPrice;
		String hashData = sha512.hash(data_hash);
		
		// Request URL
		String apiUrl = inicisRefundUrl; // "https://iniapi.inicis.com/api/v1/refund";

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("type", type);
		resultMap.put("paymethod", paymethod);
		resultMap.put("timestamp", timestamp);
		resultMap.put("clientIp", clientIp);
		resultMap.put("mid", mid);
		resultMap.put("tid", tid);
		resultMap.put("msg", msg);
		resultMap.put("price", price);
		resultMap.put("confirmPrice", confirmPrice);
		resultMap.put("hashData", hashData);

		resultMap.put("tax", tax);

		StringBuilder postData = new StringBuilder();
		for(Map.Entry<String, Object> params: resultMap.entrySet()) {
			
			if(postData.length() != 0) postData.append("&");
			try {
				postData.append(URLEncoder.encode(params.getKey(), "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(String.valueOf(params.getValue()), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//step2. key=value 로 post 요청
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			if (conn != null) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				conn.setRequestMethod("POST");
				conn.setDefaultUseCaches(false);
				conn.setDoOutput(true);
				
				if (conn.getDoOutput()) {
					conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}

				conn.connect();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
				
				//step3. 요청 결과
//				System.out.println(br.readLine());
//				br.close();
				String resultBr = br.readLine();
				if (LOG.isDebugEnabled()) {
					LOG.debug("### OpenApiManageController vRefundManage[부분 결제 취소(Card)] : " + resultBr);
				}
				br.close();

//				String resultBr = br.toString();
				String test = resultBr.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "");
							
				Map<String, String> returnMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱
				String resultCode = returnMap.get("resultCode"); // 결과코드
				
				// 걸제 테이블에 부분취소(Card) update
				int retPaymentRefundUpdate = (int) openApiManageService.updatePatialCardRefundPayment(returnMap);					

				if(resultCode.equals("00")) {	// 부분취소 및 환불 요청 성공
					// 예약 테이블에 전체환불(무통장) update
					returnMap.put("orderNumber", map.get("refundRsvId").toString());
					returnMap.put("compPayFlag",  "4"); // 결제완료 여부(null: 결제중사용자 취소, 0:결제완료, 1:결제대기중(무통장입금의 경우), 3:고객전체취소(Card), 4:고객부분취소(Card), 5:고객전체환불(가상계좌, 무통장), 6:고객부분환불(가상계좌, 무통장), 8:관리자가 예약등록, 9:관리자가 예약취소, 이니시스 승인상태에 따라 추가)
					int retReserveUpdate = (int) openApiManageService.updateReserveTable(returnMap);					

					// update한 내역중 orderNumber의 custName, custMobile, custEmail 정보를 얻는다.
					Map<String, Object> telResult = openApiManageService.selectReserveTableTelQuery(returnMap);
					
					// 고객에게 취소 되었다는 문자를 보낸다.
					if (telResult != null) {
						String custMobile = telResult.get("custMobile").toString().replace("-", "");						
//						String smsResult = sendSMS(custMobile, "무통장 으", telResult.get("custName").toString(), price);
						String smsResult = sendSMS(custMobile, telResult.get("custName") + "님께서 예약하신\n" + 
								"예약번호 : " + telResult.get("rsvId") + "\n" +  
								"결제방식 : " + paymethod + "\n" +  
								"결제금액 : " + price + confirmPrice + "원 중" + "\n" +
								"환불규정에 의해 " + confirmPrice + "원이 차감되어" + "\n" +
								"환불금액 : " + price + "원이 결제 취소 되었습니다.");
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("SMS Result = " + smsResult);
						}
					}
				}

			}

		}catch(Exception e ) {
			e.printStackTrace();
		} 

		return mv;
	}

	/**
	 * 가상계좌(무통장) 전체 취소 및 환불요청 : 가상계좌(무통장)의 경우 결제 취소를 하면 환볼 요청을 해야 지정한 환불계좌로 결제했던 금액을 되돌려 받는다.
	 *                        			환불요청일 경우 환불은행, 환불계좌, 환분계좌 예금주명을 입력 받아와야 한다.
	 *                        			이니시스와 동일한 은행코드 사용할 것.
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/vRefundUrl.do")
	public ModelAndView vRefundManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController vRefundManage START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		SHA512INICIS sha512 = new SHA512INICIS();
		AES128INICIS aes128 = new AES128INICIS();
		Date date_now = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");

		//step1. 요청을 위한 파라미터 설정
		String key = "ItEQKi3rY7uvDS8l"; 							
		String iv = "HYb3yQ4f65QL89==";								
		String type = "Refund";										
		String paymethod = "Vacct";									
		String timestamp = fourteen_format.format(date_now);
		String clientIp = InetAddress.getLocalHost().getHostAddress(); // "111.222.333.889";							
		String mid = inicisMid; // "INIpayTest";										
		String tid = (String) map.get("tid"); // ""; 			
		String msg = "가상계좌 환불요청";								
		String refundAcctNum = (String) map.get("refundAcctNum"); // ""; 	// 환불계좌번호 (AES 암호와 필요) 40 Byte
		String refundBankCode = (String) map.get("refundBankCode"); // "";	// 환불계좌은행코드 80 Byte
		String refundAcctName = (String) map.get("refundAcctName"); //"";	// 환불계좌예금주명 20 Byte
		
		
		// AES Encryption
		String enc_refundAcctNum = aes128.encAES(refundAcctNum, key, iv);
		
		// Hash Encryption
		String data_hash = key + type + paymethod + timestamp + clientIp + mid + tid + enc_refundAcctNum ;
		String hashData = sha512.hash(data_hash);
		
		// reqeust URL
		String apiUrl = inicisRefundUrl; // "https://iniapi.inicis.com/api/v1/refund";

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("type", type);
		resultMap.put("paymethod", paymethod);
		resultMap.put("timestamp", timestamp);
		resultMap.put("clientIp", clientIp);
		resultMap.put("mid", mid);
		resultMap.put("tid", tid);
		resultMap.put("msg", msg);
		resultMap.put("refundAcctNum", enc_refundAcctNum);
		resultMap.put("refundBankCode", refundBankCode);
		resultMap.put("refundAcctName", refundAcctName);
		resultMap.put("hashData", hashData);
		
		StringBuilder postData = new StringBuilder();
		for(Map.Entry<String, Object> params: resultMap.entrySet()) {
			
			if(postData.length() != 0) postData.append("&");
			try {
				postData.append(URLEncoder.encode(params.getKey(), "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(String.valueOf(params.getValue()), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//step2. key=value 로 post 요청
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			if (conn != null) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				conn.setRequestMethod("POST");
				conn.setDefaultUseCaches(false);
				conn.setDoOutput(true);
				
				if (conn.getDoOutput()) {
					conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}

				conn.connect();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
				
				//step3. 요청 결과
//				System.out.println(br.readLine());
//				br.close();
				String resultBr = br.readLine();
				if (LOG.isDebugEnabled()) {
					LOG.debug("### OpenApiManageController vRefundManage[가상계좌(무통장) 전체 결제 환불요청] : " + resultBr);
				}
				br.close();

//				String resultBr = br.toString();
				String test = resultBr.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "");
							
				Map<String, String> returnMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱
				String resultCode = returnMap.get("resultCode"); // 결과코드
				
				// 걸제 테이블에 전체환불(가상계좌, 무통장입금) update
				returnMap.put("tid", tid);
				int retPaymentRefundUpdate = (int) openApiManageService.updateAllGitaRefundPayment(returnMap);					

				if(resultCode.equals("00")) {	// 부분취소 및 환불 요청 성공
					// 예약 테이블에 전체환불(무통장) update
					returnMap.put("orderNumber", map.get("refundRsvId").toString());
					returnMap.put("compPayFlag",  "5"); // 결제완료 여부(null: 결제중사용자 취소, 0:결제완료, 1:결제대기중(무통장입금의 경우), 3:고객전체취소(Card), 4:고객부분취소(Card), 5:고객전체환불(가상계좌, 무통장), 6:고객부분환불(가상계좌, 무통장), 8:관리자가 예약등록, 9:관리자가 예약취소, 이니시스 승인상태에 따라 추가)
					int retReserveUpdate = (int) openApiManageService.updateReserveTable(returnMap);					

					// update한 내역중 orderNumber의 custName, custMobile, custEmail 정보를 얻는다.
					Map<String, Object> telResult = openApiManageService.selectReserveTableTelQuery(returnMap);
					
					// 고객에게 취소 되었다는 문자를 보낸다.
					if (telResult != null) {
						int price = Integer.valueOf(telResult.get("payAmt").toString()) + Integer.valueOf(telResult.get("payVat").toString());
						String custMobile = telResult.get("custMobile").toString().replace("-", "");
						
	//					String smsResult = sendSMS(custMobile, "무통장 으", telResult.get("custName").toString(), Integer.toString(price));
						String smsResult = sendSMS(custMobile, telResult.get("custName") + "님께서 예약하신\n" + 
								"예약번호 : " + telResult.get("rsvId") + "\n" +  
								"결제방식 : " + "무통장입금" + "\n" +
								"결제금액 : " + price + "원\n" +
								"의 결제를 취소 하셨습니다.");
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("SMS Result = " + smsResult);
						}					
					}
				}

			}

		}catch(Exception e ) {
			e.printStackTrace();
		} 

		return mv;
	}

	/**
	 * 가상계좌(무통장) 부분 결제 취소 및 환불요청
	 * @param commandMap
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/openapi/vPatialRefund.do")
	public ModelAndView vParialRefundManage(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("### OpenApiManageController vParialRefundManage START :: map=" + map.toString());
		}

		String result = "success";
		ModelAndView mv = new ModelAndView("jsonView");

		SHA512INICIS sha512 = new SHA512INICIS();
		AES128INICIS aes128 = new AES128INICIS();
		Date date_now = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");

		//step1. 요청을 위한 파라미터 설정
		String key = "ItEQKi3rY7uvDS8l"; 
		String iv = "HYb3yQ4f65QL89==";
		String type = "PartialRefund";
		String paymethod = "Vacct";
		String timestamp = fourteen_format.format(date_now);
		String clientIp = InetAddress.getLocalHost().getHostAddress(); // "111.222.333.889";							
		String mid = inicisMid; // "INIpayTest";										
		String tid = (String) map.get("tid"); // ""; 			
		String msg = "가상계좌 부분환불요청";
		String price = (String) map.get("price"); // "";		// 취소 요청 금액
		String confirmPrice = (String) map.get("confirmPrice"); // "";  // 부분취소 후 남은금액										
		String refundAcctNum = (String) map.get("refundAcctNum"); // ""; 	// 환불계좌번호 (AES 암호와 필요) 40 Byte
		String refundBankCode = (String) map.get("refundBankCode"); // "";	// 환불계좌은행코드 80 Byte
		String refundAcctName = (String) map.get("refundAcctName"); //"";	// 환불계좌예금주명 20 Byte
		
		// AES Encryption
		String enc_refundAcctNum = aes128.encAES(refundAcctNum, key, iv);
		
		// Hash Encryption
		String data_hash = key + type + paymethod + timestamp + clientIp + mid + tid + price + confirmPrice + enc_refundAcctNum ;
		String hashData = sha512.hash(data_hash);
		
		// Request URL
		String apiUrl = inicisRefundUrl; // "https://iniapi.inicis.com/api/v1/refund";

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("type", type);
		resultMap.put("paymethod", paymethod);
		resultMap.put("timestamp", timestamp);
		resultMap.put("clientIp", clientIp);
		resultMap.put("mid", mid);
		resultMap.put("tid", tid);
		resultMap.put("msg", msg);
		resultMap.put("price", price);
		resultMap.put("confirmPrice", confirmPrice);
		resultMap.put("refundAcctNum", enc_refundAcctNum);
		resultMap.put("refundBankCode", refundBankCode);
		resultMap.put("refundAcctName", refundAcctName);
		resultMap.put("hashData", hashData);
		
		StringBuilder postData = new StringBuilder();
		for(Map.Entry<String, Object> params: resultMap.entrySet()) {
			
			if(postData.length() != 0) postData.append("&");
			try {
				postData.append(URLEncoder.encode(params.getKey(), "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(String.valueOf(params.getValue()), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//step2. key=value 로 post 요청
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			if (conn != null) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				conn.setRequestMethod("POST");
				conn.setDefaultUseCaches(false);
				conn.setDoOutput(true);
				
				if (conn.getDoOutput()) {
					conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}

				conn.connect();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
				
				//step3. 요청 결과
				// System.out.println(br.readLine());
				String resultBr = br.readLine();
				if (LOG.isDebugEnabled()) {
					LOG.debug("### OpenApiManageController vParialRefundManage[가상계좌(무통장) 부분 결제 취소 및 환불요청] : " + resultBr);
				}
				br.close();

//				String resultBr = br.toString();
				String test = resultBr.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ","").replace("\n", "").replace("}", "").replace("{", "");
							
//				if (LOG.isDebugEnabled()) {
//					LOG.debug("### OpenApiManageController vParialRefundManage[가상계좌(무통장) 부분 결제 취소 및 환불요청] : " + test);
//				}

				Map<String, String> returnMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱
				String resultCode = returnMap.get("resultCode"); // 결과코드
				
				// 걸제 테이블에 부분환불(가상계좌, 무통장입금) update
				int retPaymentRefundUpdate = (int) openApiManageService.updatePatialGitaRefundPayment(returnMap);					

				if(resultCode.equals("00")) {	// 부분취소 및 환불 요청 성공
					// 예약 테이블에 부분환불(무통장) update
					returnMap.put("orderNumber", map.get("refundRsvId").toString());
					returnMap.put("compPayFlag", "6"); // 결제완료 여부(null: 결제중사용자 취소, 0:결제완료, 1:결제대기중(무통장입금의 경우), 3:고객전체취소(Card), 4:고객부분취소(Card), 5:고객전체환불(가상계좌, 무통장), 6:고객부분환불(가상계좌, 무통장), 8:관리자가 예약등록, 9:관리자가 예약취소, 이니시스 승인상태에 따라 추가)
					int retReserveUpdate = (int) openApiManageService.updateReserveTable(returnMap);					

					// update한 내역중 orderNumber의 custName, custMobile, custEmail 정보를 얻는다.
					Map<String, Object> telResult = openApiManageService.selectReserveTableTelQuery(returnMap);
					
					// 고객에게 취소 되었다는 문자를 보낸다.
					if (telResult != null) {
						String custMobile = telResult.get("custMobile").toString().replace("-", "");						
//						String smsResult = sendSMS(custMobile, "무통장 으", telResult.get("custName").toString(), price);
						String smsResult = sendSMS(custMobile, telResult.get("custName") + "님께서 예약하신\n" + 
								"예약번호 : " + telResult.get("rsvId") + "\n" +  
								"결제방식 : " + "무통장입금" + "\n" +  
								"결제금액 : " + price + confirmPrice + "원 중" + "\n" +
								"환불규정에 의해 " + confirmPrice + "원이 차감되어" + "\n" +
								"환불금액 : " + price + "원이 결제 취소 되었습니다.");
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("SMS Result = " + smsResult);
						}
					}
				}
			}

		}catch(Exception e ) {
			e.printStackTrace();
		} 

		return mv;
	}


}