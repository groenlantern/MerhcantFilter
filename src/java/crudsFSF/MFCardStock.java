/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 
DROP TABLE IF EXISTS `halo161`.`imf_card_stock`;

CREATE TABLE IF NOT EXISTS `halo161`.`imf_card_stock` (
  `IMF_CardStock_Id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `Pan_number` VARCHAR(100) NOT NULL,
  `Balance` VARCHAR(12) NULL,
  `Active` TINYINT(1) NOT NULL,
  `CreationDateTime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`IMF_CardStock_Id`),
  INDEX `index_pan` (`Pan_number` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8;

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
public class MFCardStock implements Serializable {
    private long      imf_CardStock_Id = 0;
    private String    imf_pan_number = "";
    private String    imf_balance = "";
    private double    imf_balance_Num = 0;
    private boolean   imf_active = false;
    private Timestamp imf_creationDateTime;    
    private DualListModel<MIDParent> merchantObjs;
    private DualListModel<MerchantFilterCategory> categoryObjs;
    private String returnMessage = ""; 
    private boolean isPANGroup = false;
    
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
     * @return the imf_CardStock_Id
     */
    public long getImf_CardStock_Id() {
        return imf_CardStock_Id;
    }

    /**
     * @param imf_CardStock_Id the imf_CardStock_Id to set
     */
    public void setImf_CardStock_Id(long imf_CardStock_Id) {
        this.imf_CardStock_Id = imf_CardStock_Id;
    }

    /**
     * @return the imf_pan_number
     */
    public String getImf_pan_number() {
        return imf_pan_number;
    }

    /**
     * @param imf_pan_number the imf_pan_number to set
     */
    public void setImf_pan_number(String imf_pan_number) {
        this.imf_pan_number = imf_pan_number;
    }

    /**
     * @return the imf_balance
     */
    public String getImf_balance() {
        return imf_balance;
    }

    /**
     * @param imf_balance the imf_balance to set
     */
    public void setImf_balance(String imf_balance) {
        this.imf_balance = imf_balance;
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

            midsSource = getMidListSourceTarget( getImf_CardStock_Id(), "getCardMidListSource" ) ;
            midsTarget = getMidListSourceTarget( getImf_CardStock_Id() , "getCardMidListTarget" ) ;

            return new DualListModel<>(midsSource, midsTarget);
        } else { 
            return merchantObjs;
        }
    }

    /**
     * @param merchantObjs the merchantObjs to set
     */
    public void setMerchantObjs(DualListModel<MIDParent> merchantObjs) {
        this.merchantObjs = merchantObjs;
    }

    /**
     * @return the imf_balance_Num
     */
    public double getImf_balance_Num() {
        try { 
            return Double.parseDouble(imf_balance);
        } catch (Exception e1) { 
            return 0;
        }
    }

    /**
     * @param imf_balance_Num the imf_balance_Num to set
     */
    public void setImf_balance_Num(double imf_balance_Num) {
        this.imf_balance_Num = imf_balance_Num;
        
        try { 
            this.imf_balance = Double.toString( this.imf_balance_Num );
        } catch (Exception e1) { 
            this.imf_balance = Double.toString( 0 );
        }        
    }            

    /**
     * @param cardIDID
     * @param methodtoCall
     * @return the cardMidsList
     */
    public List<MIDParent> getMidListSourceTarget( Long cardIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MIDParent> cardMidsList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            MIDParent singleItem;
            
            cardMidsList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ?, ?  )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, cardIDID );
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
                
                cardMidsList.add(singleItem);            
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
            
            Logger.getLogger(MFCardStock.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MFCardStock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return cardMidsList;
    }
        
     /**
     * @param cardIDID
     * @param methodtoCall
     * @return the cardCatsList
     */
    public List<MerchantFilterCategory> getCatListSourceTarget( Long cardIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MerchantFilterCategory> cardCatsList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            MerchantFilterCategory singleItem;
            
            cardCatsList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ? , ?  )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, cardIDID );
            cs.registerOutParameter(2, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(2);
            
            rs = cs.getResultSet();
            
            while (rs.next()) {
                singleItem = new MerchantFilterCategory();
                
                singleItem.setImf_description( rs.getString("label") );
                singleItem.setImf_Mid_Category_Id(  rs.getLong("value") );
 
                cardCatsList.add(singleItem);            
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
            
            Logger.getLogger(MFCardStock.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MFCardStock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return cardCatsList;
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

            catSource = getCatListSourceTarget( getImf_CardStock_Id(), "getCardCategoryListSource" ) ;
            catTarget = getCatListSourceTarget( getImf_CardStock_Id() , "getCardCategoryListTarget" ) ;

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
     * @return the isPANGroup
     */
    public boolean isIsPANGroup() {
        return isPANGroup;
    }

    /**
     * @param isPANGroup the isPANGroup to set
     */
    public void setIsPANGroup(boolean isPANGroup) {
        this.isPANGroup = isPANGroup;
    }

}
