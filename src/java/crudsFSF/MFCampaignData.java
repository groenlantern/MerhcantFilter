/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 
CREATE TABLE `imf_campaign` (
  `IMF_Campaign_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Description` varchar(100) NOT NULL,
  `Active` tinyint(1) DEFAULT NULL,
  `CreationDateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`IMF_Campaign_Id`),
  UNIQUE KEY `IMF_MID_Client_UNIQUE_ID` (`IMF_Campaign_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 */
package crudsFSF;

import crudsFSF.util.JsfUtil;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Jean-Pierre Erasmus
 */
public class MFCampaignData implements Serializable {
    private long      IMF_Campaign_Id = 0;
    private String    imf_Description = "";
    private boolean   imf_active = false;
    private Timestamp imf_creationDateTime;    
    private String returnMessage = ""; 
    private DualListModel<MIDParent> merchantObjs;
    private DualListModel<MerchantFilterCategory> categoryObjs;
    private DualListModel<MFCardStock> cardObjs;

   /**
     * 
     */
    private DataSource dataSourceGridController;

     
    /**
     * 
     * @return
     * @throws Exception 
     */
    public Connection getMySqlConnection() throws Exception {
         return getDataSourceGridController().getConnection();
    }
    
    
    /**
     * @return the imf_active
     */
    public boolean isImf_active() {
        return imf_active;
    }

    /**
     * @param imf_active the imf_active to set
     */
    public void setImf_active(boolean imf_active) {
        this.imf_active = imf_active;
    }

    /**
     * @return the imf_creationDateTime
     */
    public Timestamp getImf_creationDateTime() {
        return imf_creationDateTime;
    }

    /**
     * @param imf_creationDateTime the imf_creationDateTime to set
     */
    public void setImf_creationDateTime(Timestamp imf_creationDateTime) {
        this.imf_creationDateTime = imf_creationDateTime;
    }

    /**
     * @return the merchantObjs
     */
    public DualListModel<MIDParent> getMerchantObjs() {
            
            if ( this.merchantObjs == null || 
                 this.merchantObjs.getSource() == null ||
                 this.merchantObjs.getTarget() == null ||
                  (this.merchantObjs.getSource().size() < 1 && 
                   this.merchantObjs.getTarget().size() < 1) ) { 
                //Load Linked Merchants
                List<MIDParent> midsSource;
                List<MIDParent> midsTarget;

                midsSource = getMidListSourceTarget(getIMF_Campaign_Id(), "getCampMidListSource" ) ;
                midsTarget = getMidListSourceTarget(getIMF_Campaign_Id(), "getCampMidListTarget" ) ;

                return new DualListModel<>(midsSource, midsTarget);
            } else { 
                return this.merchantObjs;
            }
    }

    /**
     * @param merchantObjs the merchantObjs to set
     */
    public void setMerchantObjs(DualListModel<MIDParent> merchantObjs) {
        this.merchantObjs = merchantObjs;
    }
 
    /**
     * @param campIDID
     * @param methodtoCall
     * @return the campMidsList
     */
    public List<MIDParent> getMidListSourceTarget( Long campIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MIDParent> campMidsList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            MIDParent singleItem;
            
            campMidsList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ?, ?  )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, campIDID );
            cs.registerOutParameter(2, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(2);
            
            rs = cs.getResultSet();
            
            while (rs.next()) {
                singleItem = new MIDParent();
                
                singleItem.setDescription( rs.getString("label") + " (" + rs.getString("concatParent") + ")" );
                singleItem.setLabel(  rs.getString("concatParent") );
                singleItem.setValue( rs.getLong("value") );

                try { 
                    int levelCde = rs.getInt("level");
                    
                    String tabbedName = "";
                    
                    String treeChar = "└";
                                        
                    if ( levelCde > 1) { 
                        for (int x=1; x < levelCde; x++) {                         
                            for (int y=0;y<2;y++) { 
                                tabbedName += " " ;                        
                            }
                        }
                        
                        for (int y=0;y<3;y++) { 
                            tabbedName += treeChar;
                            treeChar = "─";
                        }
                        tabbedName += "  ";
                    }
                    
                    tabbedName += singleItem.getDescription();
                    
                    singleItem.setDescription(tabbedName );                    
                } catch (SQLException | NumberFormatException e) {                     
                }
                
                campMidsList.add(singleItem);            
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
            
            Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return campMidsList;
    }
        
     /**
     * @param campIDID
     * @param methodtoCall
     * @return the campCatsList
     */
    public List<MerchantFilterCategory> getCatListSourceTarget( Long campIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MerchantFilterCategory> campCatsList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            MerchantFilterCategory singleItem;
            
            campCatsList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ? , ? )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, campIDID );
            cs.registerOutParameter(2, java.sql.Types.VARCHAR); 
            
            cs.execute();
            
            returnMessage = cs.getString(2);
            
            rs = cs.getResultSet();
            
            while (rs.next()) {
                singleItem = new MerchantFilterCategory();
                
                singleItem.setImf_description( rs.getString("label") );
                singleItem.setImf_Mid_Category_Id(  rs.getLong("value") );
 
                campCatsList.add(singleItem);            
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
            
            Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return campCatsList;
    }
    
    /**
     * @param campaignIDID
     * @param methodtoCall
     * @return the cardMidsList
     */
    public List<MFCardStock> getCardListSourceTarget( Long campaignIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MFCardStock> campsCardList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            MFCardStock singleItem;
            
            campsCardList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ?, ?  )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, campaignIDID );
            cs.registerOutParameter(2, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(2);
            
            rs = cs.getResultSet();
            
            while (rs.next()) {
                singleItem = new MFCardStock();
                
                singleItem.setImf_pan_number( rs.getString("label") );
                singleItem.setImf_CardStock_Id( rs.getLong("value") );
 
                campsCardList.add(singleItem);            
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
            
            Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MFCampaignData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return campsCardList;
    }
    
    /**
     * @return the categoryObjs
     */
    public DualListModel<MerchantFilterCategory> getCategoryObjs() {
        if ( this.categoryObjs == null || 
             this.categoryObjs.getSource() == null ||
             this.categoryObjs.getTarget() == null ||
             (this.categoryObjs.getSource().size() < 1 && 
              this.categoryObjs.getTarget().size() < 1) ) { 
              
            List<MerchantFilterCategory> catSource;
            List<MerchantFilterCategory> catTarget;

            catSource = getCatListSourceTarget(getIMF_Campaign_Id(), "getCampaignCategoryListSource" ) ;
            catTarget = getCatListSourceTarget(getIMF_Campaign_Id(), "getCampaignCategoryListTarget" ) ;

            return new DualListModel<>(catSource, catTarget);
        } else { 
            return categoryObjs;
        }

    }

    /**
     * @param categoryObjs the categoryObjs to set
     */
    public void setCategoryObjs(DualListModel<MerchantFilterCategory> categoryObjs) {
        this.categoryObjs = categoryObjs;
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

    /**
     * @return the IMF_Campaign_Id
     */
    public long getIMF_Campaign_Id() {
        return IMF_Campaign_Id;
    }

    /**
     * @param IMF_Campaign_Id the IMF_Campaign_Id to set
     */
    public void setIMF_Campaign_Id(long IMF_Campaign_Id) {
        this.IMF_Campaign_Id = IMF_Campaign_Id;
    }

    /**
     * @return the imf_Description
     */
    public String getImf_Description() {
        return imf_Description;
    }

    /**
     * @param imf_Description the imf_Description to set
     */
    public void setImf_Description(String imf_Description) {
        this.imf_Description = imf_Description;
    }

    /**
     * @return the cardObjs
     */
    public DualListModel<MFCardStock> getCardObjs() {
        if ( this.cardObjs == null || 
             this.cardObjs.getSource() == null ||
             this.cardObjs.getTarget() == null ||
             (this.cardObjs.getSource().size() < 1 && 
              this.cardObjs.getTarget().size() < 1) ) { 
        
            List<MFCardStock> catSource;
            List<MFCardStock> catTarget;

            catSource = getCardListSourceTarget(getIMF_Campaign_Id(), "getCampaignCardListSource" ) ;
            catTarget = getCardListSourceTarget(getIMF_Campaign_Id(), "getCampaignCardListTarget" ) ;

            return new DualListModel<>(catSource, catTarget);        
        } else { 
            return cardObjs;
        }
    }

    /**
     * @param cardObjs the cardObjs to set
     */
    public void setCardObjs(DualListModel<MFCardStock> cardObjs) {
        this.cardObjs = cardObjs;
    }

}
