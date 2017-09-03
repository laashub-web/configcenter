/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2017-08-20 15:22 创建
 */
package org.antframework.configcenter.biz.service;

import org.antframework.boot.bekit.AntBekitException;
import org.antframework.common.util.facade.Status;
import org.antframework.configcenter.dal.dao.AppDao;
import org.antframework.configcenter.dal.dao.PropertyKeyDao;
import org.antframework.configcenter.dal.entity.App;
import org.antframework.configcenter.facade.enums.ResultCode;
import org.antframework.configcenter.facade.order.manage.DeleteAppOrder;
import org.antframework.configcenter.facade.result.manage.DeleteAppResult;
import org.bekit.service.annotation.service.Service;
import org.bekit.service.annotation.service.ServiceExecute;
import org.bekit.service.engine.ServiceContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 删除应用服务
 */
@Service(enableTx = true)
public class DeleteAppService {
    @Autowired
    private AppDao appDao;
    @Autowired
    private PropertyKeyDao propertyKeyDao;

    @ServiceExecute
    public void execute(ServiceContext<DeleteAppOrder, DeleteAppResult> context) {
        DeleteAppOrder order = context.getOrder();

        App app = appDao.findLockByAppCode(order.getAppCode());
        if (app == null) {
            return;
        }
        if (propertyKeyDao.existsByAppCode(order.getAppCode())) {
            throw new AntBekitException(Status.FAIL, ResultCode.ILLEGAL_STATE.getCode(), String.format("应用[%s]还存在key，不能删除", order.getAppCode()));
        }

        appDao.delete(app);
    }
}