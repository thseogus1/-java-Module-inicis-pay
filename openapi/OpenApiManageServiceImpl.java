package com.shcorp.openapi;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * API를 이용한 Front Page와 이니시스 결제 Flow를 담당하는 서비스 구현체
 *
 * @author  kyle
 * @date    2023.07.07
 * @since   1.0
 */

@Service("openApiManageService")
public class OpenApiManageServiceImpl implements OpenApiManageService {

	@Autowired
	OpenApiManageDao openApiManageDao;


	@Override
	public List<Map<String, Object>> selectTableInfoList(Map<String, Object> map) throws Exception {
		return openApiManageDao.selectTableInfoList(map);
	}

	@Override
	public List<Map<String, Object>> selectPensionInfoList(Map<String, Object> map) throws Exception {
		return openApiManageDao.selectPensionInfoList(map);
	}

	@Override
	public List<Map<String, Object>> selectReserveTblCntList(Map<String, Object>map) throws Exception {
		return openApiManageDao.selectReserveTblCntList(map);
	}

	@Override
	public List<Map<String, Object>> selectReservePensionCntList(Map<String, Object> map) throws Exception {
		return  openApiManageDao.selectReservePensionCntList(map);
	}

	@Override
	public Map<String, Object> selectTableInfo(Map<String, Object> map) throws Exception {
		return (Map<String, Object>) openApiManageDao.selectTableInfo(map);
	}

	@Override
	public Map<String, Object> selectPensionInfo(Map<String, Object> map) throws Exception {
		return (Map<String, Object>) openApiManageDao.selectPensionInfo(map);
	}

	@Override
	public List<Map<String, Object>> selectFileList(Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		return openApiManageDao.selectFileList(map);
	}
	
	@Override
	public List<Map<String, Object>> selectTableCommentList(Map<String, Object> map) throws Exception {
		return openApiManageDao.selectTableCommentList(map);
	}

	@Override
	public int insertReserveInfo(Map<String, Object> map) throws Exception {
		return (int) openApiManageDao.insertReserveInfo(map);
	}

	@Override
	public int insertOrderMenu(Map<String, Object> map) throws Exception {
		return (int) openApiManageDao.insertOrderMenu(map);
	}

	@Override
	public int insertRsvTable(Map<String, Object> map) throws Exception {
		return (int) openApiManageDao.insertRsvTable(map);
	}

	@Override
	public Map<String, Object> selectRsvTableMax(Map<String, Object> map) throws Exception {
		return (Map<String, Object>) openApiManageDao.selectRsvTableMax(map);
	}

	@Override
	public int insertPaymentTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.insertPaymentTable(map);
	}

	@Override
	public int updateReserveTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.updateReserveTable(map);
	}

	@Override
	public Map<String, Object> selectReserveTableTelQuery(Map<String, String> map) throws Exception {
		return openApiManageDao.selectReserveTableTelQuery(map);
	}

	@Override
	public int deleteReserveTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.deleteReserveTable(map);
	}

	@Override
	public int deleteOrderMenuTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.deleteOrderMenuTable(map);
	}

	@Override
	public int deleteRsvTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.deleteRsvTable(map);
	}

	@Override
	public int deletePaymentTable(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.deletePaymentTable(map);
	}

	@Override
	public int updatePaymentTable(Map<String, Object> map) throws Exception {
		return (int) openApiManageDao.updatePaymentTable(map);
	}

	@Override
	public int updateReservePensionInfo(Map<String, Object> map) throws Exception {
		return (int) openApiManageDao.updateReservePensionInfo(map);
	}



	@Override
	public int updateAllCardRefundPayment(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.updateAllCardRefundPayment(map);
	}

	@Override
	public int updatePatialCardRefundPayment(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.updatePatialCardRefundPayment(map);
	}

	@Override
	public int updateAllGitaRefundPayment(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.updateAllGitaRefundPayment(map);
	}

	@Override
	public int updatePatialGitaRefundPayment(Map<String, String> map) throws Exception {
		return (int) openApiManageDao.updatePatialGitaRefundPayment(map);
	}

}
