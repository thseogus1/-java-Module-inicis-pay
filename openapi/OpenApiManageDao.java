package com.shcorp.openapi;

import java.util.List;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.shcorp.common.dao.AbstractDAO;

/**
 * API를 이용한 Front Page와 이니시스 결제 Flow를 담당하는 정보  DAO
 *
 * @author  CSH
 * @date    2023.07.07
 * @since   1.0
 */
@Repository("openApiManageDao")
public class OpenApiManageDao extends AbstractDAO{

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectTableInfoList(Map<String, Object> map) throws Exception{
		return (List<Map<String, Object>>) selectList("openApiManage.selectTableInfoList", map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectPensionInfoList(Map<String, Object> map) throws Exception{
		return (List<Map<String, Object>>) selectList("openApiManage.selectPensionInfoList", map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectReserveTblCntList(Map<String, Object> map) throws Exception{
		return (List<Map<String, Object>>) selectList("openApiManage.selectReserveTblCntList", map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectReservePensionCntList (Map<String, Object> map) throws Exception {
		return (List<Map<String, Object>>) selectList("openApiManage.selectReservePensionCntList", map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectTableInfo (Map<String, Object> map) throws Exception {
		return (Map<String, Object>) selectOne("openApiManage.selectTableInfo", map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectPensionInfo (Map<String, Object> map) throws Exception {
		return (Map<String, Object>) selectOne("openApiManage.selectPensionInfo", map);
	}

	@SuppressWarnings("unchecked")
	public int insertReserveInfo (Map<String, Object> map) throws Exception {
		return (int) insert("openApiManage.insertReserveInfo", map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectFileList(Map<String, Object> map) throws Exception{
		return (List<Map<String, Object>>) selectList("openApiManage.selectFileList", map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectTableCommentList(Map<String, Object> map) throws Exception{
		return (List<Map<String, Object>>) selectList("openApiManage.selectTableComment", map);
	}

	@SuppressWarnings("unchecked")
	public int insertOrderMenu (Map<String, Object> map) throws Exception {
		return (int) insert("openApiManage.insertOrderMenu", map);
	}

	@SuppressWarnings("unchecked")
	public int insertRsvTable(Map<String, Object> map) throws Exception {
		return (int) insert("openApiManage.insertRsvTable", map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectRsvTableMax(Map<String, Object> map) throws Exception {
		return (Map<String, Object>) selectOne("openApiManage.selectRsvTableMax", map);
	}

	@SuppressWarnings("unchecked")
	public int insertPaymentTable(Map<String, String> map) throws Exception {
		return (int) insert("openApiManage.insertPaymentTable", map);
	}

	@SuppressWarnings("unchecked")
	public int updateReserveTable(Map<String, String> map) throws Exception {
		return (int) update("openApiManage.updateReserveTable", map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectReserveTableTelQuery(Map<String, String> map) throws Exception{
		return (Map<String, Object>) selectList("openApiManage.selectReserveTableTelQuery", map);
	}

	@SuppressWarnings("unchecked")
	public int deleteReserveTable(Map<String, String> map) throws Exception {
		return (int) delete("openApiManage.deleteReserveTable", map);
	}

	@SuppressWarnings("unchecked")
	public int deleteOrderMenuTable(Map<String, String> map) throws Exception {
		return (int) delete("openApiManage.deleteOrderMenuTable", map);
	}

	@SuppressWarnings("unchecked")
	public int deleteRsvTable(Map<String, String> map) throws Exception {
		return (int) delete("openApiManage.deleteRsvTable", map);
	}
	
	@SuppressWarnings("unchecked")
	public int deletePaymentTable(Map<String, String> map) throws Exception {
		return (int) delete("openApiManage.deletePaymentTable", map);
	}

	@SuppressWarnings("unchecked")
	public int updatePaymentTable(Map<String, Object> map) throws Exception {
		return (int) update("openApiManage.updatePaymentTable", map);
	}

	@SuppressWarnings("unchecked")
	public int updateReservePensionInfo(Map<String, Object> map) throws Exception {
		return (int) update("openApiManage.updateReservePensionInfo", map);
	}




	@SuppressWarnings("unchecked")
	public int updateAllCardRefundPayment(Map<String, String> map) throws Exception {
		return (int) update("openApiManage.updateAllCardRefundPayment", map);
	}

	@SuppressWarnings("unchecked")
	public int updatePatialCardRefundPayment(Map<String, String> map) throws Exception {
		return (int) update("openApiManage.updatePatialCardRefundPayment", map);
	}

	@SuppressWarnings("unchecked")
	public int updateAllGitaRefundPayment(Map<String, String> map) throws Exception {
		return (int) update("openApiManage.updateAllGitaRefundPayment", map);
	}

	@SuppressWarnings("unchecked")
	public int updatePatialGitaRefundPayment(Map<String, String> map) throws Exception {
		return (int) update("openApiManage.updatePatialGitaRefundPayment", map);
	}

}
