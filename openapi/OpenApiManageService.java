package com.shcorp.openapi;

import java.util.List;
import java.util.Map;

/**
 * API를 이용한 Front Page와 이니시스 결제 Flow를 담당하는 인터페이스
 *
 * @author  CSH
 * @date    2023.07.07
 * @since   1.0
 */

public interface OpenApiManageService {


	public List<Map<String, Object>> selectTableInfoList(Map<String, Object>map) throws Exception;
	
	public List<Map<String, Object>> selectPensionInfoList(Map<String, Object>map) throws Exception;
	
	public List<Map<String, Object>> selectReserveTblCntList(Map<String, Object>map) throws Exception;

	public List<Map<String, Object>> selectReservePensionCntList(Map<String, Object> map) throws Exception;		

	public Map<String, Object> selectTableInfo(Map<String, Object> map) throws Exception;

	public Map<String, Object> selectPensionInfo(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectFileList (Map<String, Object>map) throws Exception;

	public List<Map<String, Object>> selectTableCommentList(Map<String, Object>map) throws Exception;

	public int insertReserveInfo(Map<String, Object> map) throws Exception;

	public int insertOrderMenu(Map<String, Object> map) throws Exception;

	public int insertRsvTable(Map<String, Object> map) throws Exception;

	public Map<String, Object> selectRsvTableMax(Map<String, Object> map) throws Exception;

	public int insertPaymentTable(Map<String, String> map) throws Exception;

	public int updateReserveTable(Map<String, String> map) throws Exception;

	public Map<String, Object> selectReserveTableTelQuery(Map<String, String> map) throws Exception;

	public int deleteReserveTable(Map<String, String> map) throws Exception;

	public int deleteOrderMenuTable(Map<String, String> map) throws Exception;

	public int deleteRsvTable(Map<String, String> map) throws Exception;
	
	public int deletePaymentTable(Map<String, String> map) throws Exception;

	public int updatePaymentTable(Map<String, Object> map) throws Exception;
	
	public int updateReservePensionInfo(Map<String, Object> map) throws Exception;


	
	public int updateAllCardRefundPayment(Map<String, String> map) throws Exception;

	public int updatePatialCardRefundPayment(Map<String, String> map) throws Exception;

	public int updateAllGitaRefundPayment(Map<String, String> map) throws Exception;

	public int updatePatialGitaRefundPayment(Map<String, String> map) throws Exception;

}
