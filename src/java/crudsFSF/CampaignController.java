/** 
 * $LastChangedBy: Stuart $
 * $LastChangedDate: 2016-10-31 15:05:56 +0200 (Mon, 31 Oct 2016) $
 * $LastChangedRevision: 1618 $
 */

package crudsFSF;

import crudsFSF.util.JsfUtil;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.sql.DataSource;

/**
 *
 * @author Jean-Pierre Erasmus
 */
@Named("campaignController")
@SessionScoped
public class CampaignController implements Serializable {
   /**
     * 
     */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;
        
    private int showRows = 20;
    private String returnMessage = "";    
    private List<MFCampaignData> campaignItems = null;
    private List<MFCampaignData> filteredCampaignItems = null;    
    private MFCampaignData selectedCAMP = null;
    
    public CampaignController() {
         //Blank Item Array 
        campaignItems = new ArrayList<>();
        selectedCAMP = null;
    }
    
    /**
     * 
     * @return
     * @throws Exception 
     */
    public Connection getMySqlConnection() throws Exception {
         return dataSourceGridController.getConnection();
    }
    
    /**
     * 
     * @return 
     */
    public MFCampaignData getSelectedCAMP() {
        if ( selectedCAMP != null) { 
            selectedCAMP.setDataSourceGridController(dataSourceGridController);
        }
        return selectedCAMP;
    }
   
    /**
     * 
     * @return 
     */
    public List<MFCampaignData> getFilteredCampaignItems() {
        return filteredCampaignItems;
    }

    /**
     * 
     * @param filteredItems 
     */
    public void setFilteredCampaignItems(List<MFCampaignData> filteredItems) {
        this.filteredCampaignItems = filteredItems;
    }
    
    /**
     * 
     * @return 
     */
    public List<MFCampaignData> getCampaignItems() {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;

        try {
            returnMessage = "";
            
            MFCampaignData singleItem;
            
            campaignItems = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMFCampaignData( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);

            cs.execute();
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            returnMessage = cs.getString(1);
            
            rs = cs.getResultSet();
            
            // Fetch each row from the result set
            while (rs.next()) {
                singleItem = new MFCampaignData();
                singleItem.setDataSourceGridController(dataSourceGridController);
                
                singleItem.setIMF_Campaign_Id( rs.getLong("IMF_Campaign_Id") );
                singleItem.setImf_Description( rs.getString("Description") );
                singleItem.setImf_active( rs.getBoolean( "Active") );                
                singleItem.setImf_creationDateTime( rs.getTimestamp("CreationDateTime") );               
                
                campaignItems.add(singleItem);            
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
            
            Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      
        return campaignItems;
    }

    /**
     * 
     * @return 
     */
    public MFCampaignData prepareCreate() {
        selectedCAMP = new MFCampaignData();
        selectedCAMP.setDataSourceGridController(dataSourceGridController);
         
        selectedCAMP.setImf_Description("");
        selectedCAMP.setIMF_Campaign_Id(0);         
        selectedCAMP.setImf_active(false); 
        
        return selectedCAMP;
    }

    /**
     * 
     */
    public void create() {
        if (selectedCAMP != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  addIMFCampaign('Biggest Sale Ever',  true, @newRecordID);
                String sqlProc = "{  CALL addIMFCampaign( ?, ?, ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setString(1, selectedCAMP.getImf_Description().trim() );
                cs.setBoolean(2, selectedCAMP.isImf_active() );
                
                cs.registerOutParameter(3, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(3);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Campaign Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Campaign Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            selectedCAMP = null;
            campaignItems = null;    // Invalidate list of items to trigger re-query.
        }
    }

    /**
     * 
     */
    public void update() {
         if (selectedCAMP != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                //call editIMF_campaignData( 3, 'Shazam',  NULL, @result ) ;
                String sqlProc = "{  CALL editIMF_campaignData( ?, ?, ?, ?  )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setLong(1, selectedCAMP.getIMF_Campaign_Id());
                cs.setString(2, selectedCAMP.getImf_Description().trim() );
                cs.setBoolean(3, selectedCAMP.isImf_active() );
                
                cs.registerOutParameter(4, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(4);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message                
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam );        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Edit Campaign Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Edit Campaign Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            selectedCAMP = null;
            campaignItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
    
    /**
     * 
     */
    public void destroy() {
        if (!JsfUtil.isValidationFailed()) {
            selectedCAMP = null; // Remove selection
            campaignItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
  
    /**
     * @param campmfItems
     */
    public void setCampaignItems(List<MFCampaignData> campmfItems) {
        this.campaignItems = campmfItems;        
    }

    /**
     * @param selectedCampaignMF
     */
    public void setSelectedCAMP(MFCampaignData selectedCampaignMF) {
        this.selectedCAMP = selectedCampaignMF;
        
        if (  this.selectedCAMP != null ) { 
             this.selectedCAMP.setDataSourceGridController(dataSourceGridController);
        }
        
    }

    /**
     * @return the showRows
     */
    public int getShowRows() {
        return showRows;
    }

    /**
     * @param showRows the showRows to set
     */
    public void setShowRows(int showRows) {
        this.showRows = showRows;
    }
 
    /**
     * 
     */
    public void createCampaignMIDS() {
        if (selectedCAMP != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFCampaignMIDLink`(	 IN mid_Ids VARCHAR(10000), IN campaign_id BIGINT(20), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFCampaignMIDLink( ?, ?, ? )  }";                            
                String merchantMIDList;
                
                {
                    merchantMIDList = "";

                    //Build merchant List per campaign
                    for ( int x=0; x < selectedCAMP.getMerchantObjs().getTarget().size(); x++ ) { 
                        String midID;
                        try { 
                            midID = Long.toString( ((MIDParent) selectedCAMP.getMerchantObjs().getTarget().get(x)).getValue() );
                            
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly
                            midID = selectedCAMP.getMerchantObjs().getTarget().get(x) + "";
                        }
                        
                        if ( midID != null && !midID.trim().isEmpty()) { 

                            if (x > 0) {                                 
                                merchantMIDList += ",";
                            }
                            
                            merchantMIDList += midID;                            
                        }                        
                    } 
                    
                    //Update Database for campaign                   
                    cs = conn.prepareCall(sqlProc);
                    
                    cs.setString(1, merchantMIDList );
                    cs.setLong(2, selectedCAMP.getIMF_Campaign_Id() );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                  //  Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Campaign Merchant Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Campaign Merchant Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        campaignItems = null;    // Invalidate list of items to trigger re-query.
        selectedCAMP = null;
        filteredCampaignItems = null;
    }
 
  
    /**
     * 
     */
    public void createCampaignCats() {
        if (selectedCAMP != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFCampaignCategoryLink`(	 IN category_Ids VARCHAR(10000), IN campaign_id BIGINT(20), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFCampaignCategoryLink( ?, ?, ? )  }";                            
                String campaignCATList;
                
                {
                    campaignCATList = "";

                    //Build merchant List  
                    for ( int x=0; x < selectedCAMP.getCategoryObjs().getTarget().size(); x++ ) { 
                        String catID;
                        try { 
                            catID = Long.toString( ((MerchantFilterCategory) selectedCAMP.getCategoryObjs().getTarget().get(x)).getImf_Mid_Category_Id() );
                            
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly as String
                            catID = selectedCAMP.getCategoryObjs().getTarget().get(x) + "";
                        }
                        
                        if ( catID != null && !catID.trim().isEmpty()) { 

                            if (x > 0) {                                 
                                campaignCATList += ",";
                            }
                            
                            campaignCATList += catID;
                            
                        }
                        
                    } 
                    
                    //Update Database                 
                    cs = conn.prepareCall(sqlProc);

                    cs.setString(1, campaignCATList );
                    cs.setLong(2, selectedCAMP.getIMF_Campaign_Id() );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                   // Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Campaign Category Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Campaign Category Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        campaignItems = null;    // Invalidate list of items to trigger re-query.
        selectedCAMP = null;
        filteredCampaignItems = null;
    }
   
     /**
     * 
     */
    public void createCampaignCardsLink() {
     
        if (selectedCAMP != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFCampaignCardStockLink`(	 IN card_Ids VARCHAR(10000), IN campaign_id BIGINT(20), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFCampaignCardStockLink( ?, ?, ? )  }";                            
                String campaignCARDList;
                
                {
                    campaignCARDList = "";
                                        
                    //Build card List  
                    for ( int x=0; x < selectedCAMP.getCardObjs().getTarget().size(); x++ ) { 
                        String cardID;
                        try { 
                            
                            cardID = Long.toString( ((MFCardStock) selectedCAMP.getCardObjs().getTarget().get(x)).getImf_CardStock_Id() );
                             
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly as String
                            cardID = selectedCAMP.getCardObjs().getTarget().get(x) + "";
                        
                        }
                        
                        if ( cardID != null && !cardID.trim().isEmpty()) { 

                            if (x > 0) {                                 
                                campaignCARDList += ",";
                            }
                            
                            campaignCARDList += cardID;
                            
                        }
                        
                    } 
                    //Update Database                 
                    cs = conn.prepareCall(sqlProc);

                    cs.setString(1, campaignCARDList );
                    cs.setLong(2, selectedCAMP.getIMF_Campaign_Id() );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                    //Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Campaign Card Stock Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Campaign Card Stock Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        campaignItems = null;    // Invalidate list of items to trigger re-query.
        selectedCAMP = null;
        filteredCampaignItems = null;
    }
   
    
    
}
