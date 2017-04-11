/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crudsFSF;

import crudsFSF.util.JsfUtil;
import crudsFSF.util.MerchantFilter_Service;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author Jean-Pierre Erasmus
 */
@Named("approvecardmerchanttest")
@SessionScoped
public class ApproveCardMerchantTest implements Serializable {
     
    /**
     * 
     */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;
 
    private String returnMessage = ""; 
    
    /**
     * 
     * @return
     * @throws Exception 
     */
    public Connection getMySqlConnection() throws Exception {
         return getDataSourceGridController().getConnection();
    }
    
    private MerchantFilterMIDHierarchy selectedMerchant;
    
    /**
     * 
     */
    public ApproveCardMerchantTest() {    
        cardPANString = "";
        midCodeString = "";
        selectedMerchant = new MerchantFilterMIDHierarchy();
    }
    
        /**
     * 
     */
    public void destroy() {
        if (!JsfUtil.isValidationFailed()) {
            cardPANString = "";
            midCodeString = "";          
            selectedMerchant = new MerchantFilterMIDHierarchy();
        }
    }
  
    /**
     * 
     */
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8080/StellrMerchantFilterService/MerchantFilter.wsdl")
    private MerchantFilter_Service service;

    /**
     *  
     */
    private String cardPANString = "";
    private String midCodeString = "";
    
    /**
     * Processes requests for approval
     * methods.
     *
     */
    public void processRequest() {
        try { 
            boolean err = false;

            if ( cardPANString == null) { 
                JsfUtil.addErrorMessage("Null Card PAN!"); 
                err = true;
            }
            if ( cardPANString != null && cardPANString.trim().isEmpty()  ) { 
                JsfUtil.addErrorMessage("Blank Card PAN!"); 
                err = true;
            }

            //Check if merchant selected from select box
            if ( selectedMerchant != null && 
                 selectedMerchant.getImf_mid_midCode() != null && 
                !selectedMerchant.getImf_mid_midCode().trim().isEmpty()) { 
                midCodeString = selectedMerchant.getImf_mid_midCode();
            }

            if ( midCodeString == null ) { 
                 JsfUtil.addErrorMessage("Null Merchant Code!"); 
                 err = true;
            }
            if ( midCodeString != null && midCodeString.trim().isEmpty() ) { 
                 JsfUtil.addErrorMessage("Blank Merchant Code!"); 
                 err = true;
            }
            
            if ( err == false ) { 
                if ( approveCardMerchant( cardPANString, midCodeString) ) { 
                    JsfUtil.addSuccessMessage("Card is Authorized for Redemption at merchant!");
                } else { 
                    JsfUtil.addErrorMessage("Card not Authorized for Redemption at Merchant!");
                }
            }
        } catch (Exception e12) { 
            JsfUtil.addErrorMessage("PAN Approval Exception : " + e12.getMessage()); 
        }
        
        cardPANString = "";
        midCodeString = "";
        selectedMerchant = new MerchantFilterMIDHierarchy();
    }

    private Boolean approveCardMerchant(java.lang.String cardPan, java.lang.String merchantCode) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        crudsFSF.util.MerchantFilter port = service.getMerchantFilterPort();
        return port.approveCardMerchant(cardPan, merchantCode);
    }

    /**
     * @return the cardPANString
     */
    public String getCardPANString() {
        return cardPANString;
    }

    /**
     * @param cardPANString the cardPANString to set
     */
    public void setCardPANString(String cardPANString) {
        this.cardPANString = cardPANString;
    }

    /**
     * @return the midCodeString
     */
    public String getMidCodeString() {
        return midCodeString;
    }
        
    /** 
     * @param midCodeString the midCodeString to set
     */
    public void setMidCodeString(String midCodeString) {
        this.midCodeString = midCodeString;
    }

    
    /**
     *          
     * @return      
     */
    public List<MerchantFilterMIDHierarchy> getMidItems() {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        ArrayList<MerchantFilterMIDHierarchy> midItems = new ArrayList<>();
        selectedMerchant = new MerchantFilterMIDHierarchy();
          
        try {
            returnMessage = "";
            MerchantFilterMIDHierarchy singleItem;
            
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMIDHierarchy( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);
            
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
             
            cs.execute();
            
            returnMessage = cs.getString(1); 
            
            rs = cs.getResultSet();
            
            // Fetch each row from the result set
            while (rs.next()) {
                singleItem = new MerchantFilterMIDHierarchy();
                
                singleItem.setImf_mid_id( rs.getLong("IMF_Mid_Id") );
                singleItem.setImf_mid_description( rs.getString("Description") );
                singleItem.setImf_mid_name( rs.getString("Name") );
                singleItem.setImf_mid_midCode(rs.getString("imf_mid_MerchantCode") );
                singleItem.setImf_mid_nameOriginal( rs.getString("Name") );
                singleItem.setImf_parentName(rs.getString("parentName") );
                singleItem.setImf_mid_level( Integer.parseInt( rs.getString("MID_Category_Type") ) );
                singleItem.setImf_hierarchy( rs.getString("concatParent") );
                singleItem.setImf_create_date( rs.getTimestamp("CreationDateTime") );
                singleItem.setImf_mid_parent( rs.getLong("Parent_Id") );
                
                midItems.add(singleItem);            
            }            
            
            if ( returnMessage != null && !returnMessage.trim().isEmpty() ) { 
                throw new Exception(returnMessage);
            }
            
        } catch (Exception ex) {
            Throwable cause = ex.getCause();                
            
            String msg = "";
                
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }

            JsfUtil.addErrorMessage("Exception : " + msg + " : " + ex.getMessage()  );    
            
            Logger.getLogger(ApproveCardMerchantTest.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ApproveCardMerchantTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return midItems;
    }

    /**
     * 
     * @return 
     */
    public List<String> getPANItems() {
        
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        ArrayList<String> panItems = new ArrayList<>();
        
        try {
            returnMessage = "";
            
            String panItem;
            
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMFCardStock( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);
            
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(1);
            
            rs = cs.getResultSet();
            
            // Fetch each row from the result set
            while (rs.next()) {
                panItem = rs.getString("Pan_number");
                
                panItems.add(panItem);            
            }            
            
            if ( returnMessage != null && !returnMessage.trim().isEmpty() ) { 
                throw new Exception(returnMessage);
            }
            
        } catch (Exception ex) {
            Throwable cause = ex.getCause();                
            String msg = "";
            
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }

            JsfUtil.addErrorMessage("Exception : " + msg + " : " + ex.getMessage()  );    
            
            Logger.getLogger(ApproveCardMerchantTest.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ApproveCardMerchantTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return panItems;
    }
    
    /**
     * @return the selectedMerchant
     */
    public MerchantFilterMIDHierarchy getSelectedMerchant() {
        return selectedMerchant;
    }

    /**
     * @param selectedMerchant the selectedMerchant to set
     */
    public void setSelectedMerchant(MerchantFilterMIDHierarchy selectedMerchant) {
        this.selectedMerchant = selectedMerchant;
    }

    /**
     * @return the dataSourceGridController
     */
    public DataSource getDataSourceGridController() {
        return dataSourceGridController;
    }

    /**
     * @param dataSourceGridController the dataSourceGridController to set
     */
    public void setDataSourceGridController(DataSource dataSourceGridController) {
        this.dataSourceGridController = dataSourceGridController;
    }

}
