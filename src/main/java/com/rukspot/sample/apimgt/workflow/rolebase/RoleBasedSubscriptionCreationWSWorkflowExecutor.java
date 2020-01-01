package com.rukspot.sample.apimgt.workflow.rolebase;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

import javax.xml.stream.XMLStreamException;

public class RoleBasedSubscriptionCreationWSWorkflowExecutor extends SubscriptionCreationWSWorkflowExecutor {
    private static final Log log = LogFactory.getLog(RoleBasedSubscriptionCreationWSWorkflowExecutor.class);
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        try {
            String action = WorkflowConstants.CREATE_SUBSCRIPTION_WS_ACTION;
            ServiceClient client = getClient(action);
            String payload = "<wor:SubscriptionApprovalWorkFlowProcessRequest " +
                    "         xmlns:wor=\"http://workflow.subscription.apimgt.carbon.wso2.org\">\n" +
                    "         <wor:apiName>$1</wor:apiName>\n" +
                    "         <wor:apiVersion>$2</wor:apiVersion>\n" +
                    "         <wor:apiContext>$3</wor:apiContext>\n" +
                    "         <wor:apiProvider>$4</wor:apiProvider>\n" +
                    "         <wor:subscriber>$5</wor:subscriber>\n" +
                    "         <wor:applicationName>$6</wor:applicationName>\n" +
                    "         <wor:tierName>$7</wor:tierName>\n" +
                    "         <wor:workflowExternalRef>$8</wor:workflowExternalRef>\n" +
                    "         <wor:callBackURL>$9</wor:callBackURL>\n" +
                    "         <wor:deptAdminRole>$deptAdminRole</wor:deptAdminRole>\n" +
                    "      </wor:SubscriptionApprovalWorkFlowProcessRequest>";

            SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
            String callBackURL = subsWorkflowDTO.getCallbackUrl();

            payload = payload.replace("$1", subsWorkflowDTO.getApiName());
            payload = payload.replace("$2", subsWorkflowDTO.getApiVersion());
            payload = payload.replace("$3", subsWorkflowDTO.getApiContext());
            payload = payload.replace("$4", subsWorkflowDTO.getApiProvider());
            payload = payload.replace("$5", subsWorkflowDTO.getSubscriber());
            payload = payload.replace("$6", subsWorkflowDTO.getApplicationName());
            payload = payload.replace("$7", subsWorkflowDTO.getTierName());
            payload = payload.replace("$8", subsWorkflowDTO.getExternalWorkflowReference());
            payload = payload.replace("$9", callBackURL != null ? callBackURL : "?");
            payload = payload.replace("$deptAdminRole", "hr_admin_role");

            client.fireAndForget(AXIOMUtil.stringToOM(payload));

            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.addWorkflowEntry(workflowDTO);
            publishEvents(workflowDTO);
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        } catch (XMLStreamException e) {
            log.error("Error converting String to OMElement", e);
            throw new WorkflowException("Error converting String to OMElement", e);
        } catch (APIManagementException e) {
            throw new WorkflowException("Error while persisting workflow", e);
        }
        return new GeneralWorkflowResponse();
    }
}
