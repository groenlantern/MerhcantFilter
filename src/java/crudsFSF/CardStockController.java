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
@Named("cardStockController")
@SessionScoped
public class CardStockController implements Serializable {
   /**
     * 
     */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;
        
    private int showRows = 20;
    private String returnMessage = "";    
    private List<MFCardStock> cardItems = null;
    private List<MFCardStock> filteredCardItems = null;    
    private MFCardStock selectedCARD = null;
    
    public CardStockController() {
         //Blank Item Array 
        cardItems = new ArrayList<>();
        selectedCARD = null;
        
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
    public MFCardStock getSelectedCARD() {
        if ( selectedCARD != null) { 
            selectedCARD.setDataSourceGridController(dataSourceGridController);
        }
             
        return selectedCARD;
    }
   
    /**
     * 
     * @return 
     */
    public List<MFCardStock> getFilteredCardItems() {
        return filteredCardItems;
    }

    /**
     * 
     * @param filteredItems 
     */
    public void setFilteredCardItems(List<MFCardStock> filteredItems) {
        this.filteredCardItems = filteredItems;
    }
    
    /**
     * 
     * @return 
     */
    public List<MFCardStock> getCardItems() {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;

        try {
            returnMessage = "";
            
            MFCardStock singleItem;
            
            cardItems = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMFCardStock( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(1);
            
            rs = cs.getResultSet();
            
            // Fetch each row from the result set
            while (rs.next()) {
                singleItem = new MFCardStock();
                singleItem.setDataSourceGridController(dataSourceGridController);
                
                singleItem.setImf_CardStock_Id( rs.getLong("IMF_CardStock_Id") );
                singleItem.setImf_pan_number( rs.getString("Pan_number") );
                singleItem.setImf_balance( rs.getString("Balance") );
                singleItem.setImf_active( rs.getBoolean( "Active") );                
                singleItem.setImf_creationDateTime( rs.getTimestamp("CreationDateTime") );               
                singleItem.setIsPANGroup( rs.getBoolean( "isPANGroup") );                
                
                cardItems.add(singleItem);            
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
            
            Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      
        return cardItems;
    }

    /**
     * 
     * @return 
     */
    public MFCardStock prepareCreate() {
        selectedCARD = new MFCardStock();
        selectedCARD.setDataSourceGridController(dataSourceGridController);
         
        selectedCARD.setImf_pan_number("");
        selectedCARD.setImf_balance("0");
        selectedCARD.setImf_CardStock_Id(0);         
        selectedCARD.setImf_active(false); 
        selectedCARD.setIsPANGroup(false);
        
        return selectedCARD;
    }

    /**
     * 
     */
    public void create() {
        if (selectedCARD != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  addIMFCardStock('0891237637223322', '1000', true, @newRecordID);
                String sqlProc = "{  CALL addIMFCardStock( ?, ?, ?, ? , ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setString(1, selectedCARD.getImf_pan_number().trim() );
                cs.setString(2, selectedCARD.getImf_balance().trim() );
                cs.setBoolean(3, selectedCARD.isImf_active() );
                cs.setBoolean(4, selectedCARD.isIsPANGroup() );
                
                cs.registerOutParameter(5, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(5);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Card Stock Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Card Stock Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            selectedCARD = null;
            cardItems = null;    // Invalidate list of items to trigger re-query.
        }
    }

    /**
     * 
     */
    public void update() {
         if (selectedCARD != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                //call editIMF_card_stock( 1, '089123763722332X', '', true, @result ) ;
                String sqlProc = "{  CALL editIMF_card_stock( ?, ?, ?, ?, ? , ?  )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setLong(1, selectedCARD.getImf_CardStock_Id() );
                cs.setString(2, selectedCARD.getImf_pan_number().trim() );
                cs.setString(3, selectedCARD.getImf_balance().trim() );
                cs.setBoolean(4, selectedCARD.isImf_active() );
                cs.setBoolean(5, selectedCARD.isIsPANGroup() );
                
                cs.registerOutParameter(6, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(6);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message                
                Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam );        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Edit Card Stock Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Edit Card Stock Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            selectedCARD = null;
            cardItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
    
    /**
     * 
     */
    public void destroy() {
        if (!JsfUtil.isValidationFailed()) {
            selectedCARD = null; // Remove selection
            cardItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
  
    /**
     * @param cardmfItems
     */
    public void setCardItems(List<MFCardStock> cardmfItems) {
        this.cardItems = cardmfItems;        
    }

    /**
     * @param selectedCardMF
     */
    public void setSelectedCARD(MFCardStock selectedCardMF) {
        this.selectedCARD = selectedCardMF;
        
        if (  this.selectedCARD != null ) { 
           this.selectedCARD.setDataSourceGridController(dataSourceGridController);
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
    public void createCardMIDS() {
        if (selectedCARD != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFCardMIDLink`(	 IN mid_Ids VARCHAR(10000), IN cardStock_id BIGINT(20), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFCardMIDLink( ?, ?, ? )  }";                            
                String merchantMIDList;
                
                //Process each card
                
                {
                    merchantMIDList = "";

                    //Build merchant List per card
                    for ( int x=0; x < selectedCARD.getMerchantObjs().getTarget().size(); x++ ) { 
                        String midID;
                        try { 
                            midID = Long.toString( ((MIDParent) selectedCARD.getMerchantObjs().getTarget().get(x)).getValue() );
                            
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly
                            midID = selectedCARD.getMerchantObjs().getTarget().get(x) + "";
                            
                    
                        }
                        
                        if ( midID != null && !midID.trim().isEmpty()) { 

                            if (x > 0) {                                 
                                merchantMIDList += ",";
                            }
                            
                            merchantMIDList += midID;
                            
                        }
                        
                    } 
                    
                    //Update Database for card                    
                    cs = conn.prepareCall(sqlProc);

                    cs.setString(1, merchantMIDList );
                    cs.setLong(2, selectedCARD.getImf_CardStock_Id() );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                   // Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Card Merchant Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Card Merchant Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        cardItems = null;    // Invalidate list of items to trigger re-query.
        selectedCARD = null;
        filteredCardItems = null;
    }
 
  
    /**
     * 
     */
    public void createCardCats() {
        if (selectedCARD != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFCardMCategoryLink`(	 IN category_Ids VARCHAR(10000), IN cardStock_id BIGINT(20), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFCardMCategoryLink( ?, ?, ? )  }";                            
                String cardCATList;
                
                //Process each card
                
                {
                    cardCATList = "";

                    //Build merchant List per card
                    for ( int x=0; x < selectedCARD.getCategoryObjs().getTarget().size(); x++ ) { 
                        String catID;
                        try { 
                            catID = Long.toString( ((MerchantFilterCategory) selectedCARD.getCategoryObjs().getTarget().get(x)).getImf_Mid_Category_Id() );
                            
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly
                            catID = selectedCARD.getCategoryObjs().getTarget().get(x) + "";
                            
                    
                        }
                        
                        if ( catID != null && !catID.trim().isEmpty()) { 

                            if (x > 0) {                                 
                                cardCATList += ",";
                            }
                            
                            cardCATList += catID;
                            
                        }
                        
                    } 
                    
                    //Update Database for card                    
                    cs = conn.prepareCall(sqlProc);

                    cs.setString(1, cardCATList );
                    cs.setLong(2, selectedCARD.getImf_CardStock_Id() );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                  //  Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Card Categories Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Card Categories Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CardStockController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        cardItems = null;    // Invalidate list of items to trigger re-query.
        selectedCARD = null;
        filteredCardItems = null;
    }
   
}
