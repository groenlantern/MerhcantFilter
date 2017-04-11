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
import org.primefaces.model.DualListModel;

/**
 *
 * @author Jean-Pierre Erasmus
 */
@Named("catController")
@SessionScoped
public class CATController implements Serializable {
   /**
     * 
     */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;
        
    private int showRows = 20;
    private String returnMessage = "";    
    private List<MerchantFilterCategory> catItems = null;
    private List<MerchantFilterCategory> catItemsMIDS = null;
    private List<MerchantFilterCategory> filteredCatItems = null;    
    private MerchantFilterCategory selectedCAT = null;
    
    public CATController() {
         //Blank Item Array 
        catItems = new ArrayList<>();
        
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
    public MerchantFilterCategory getSelectedCAT() {
        return selectedCAT;
    }
   
    /**
     * 
     * @return 
     */
    public List<MerchantFilterCategory> getFilteredCatItems() {
        return filteredCatItems;
    }

    /**
     * 
     * @param filteredItems 
     */
    public void setFilteredCatItems(List<MerchantFilterCategory> filteredItems) {
        this.filteredCatItems = filteredItems;
    }
    
    /**
     * 
     * @return 
     */
    public List<MerchantFilterCategory> getCatItems() {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;

        try {
            returnMessage = "";
            
            MerchantFilterCategory singleItem;
            
            catItems = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMFCategories( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(1);
            
            rs = cs.getResultSet();
            
            // Fetch each row from the result set
            while (rs.next()) {
                singleItem = new MerchantFilterCategory();
                
                singleItem.setImf_Mid_Category_Id( rs.getLong("IMF_Mid_Category_Id") );
                singleItem.setImf_description( rs.getString("Description") );
                singleItem.setImf_active( rs.getBoolean( "Active") );                
                singleItem.setImf_create_date( rs.getTimestamp("CreationDateTime") );               
                
                catItems.add(singleItem);            
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
            
            Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return catItems;
    }

    
    /**
     * 
     * @return 
     */
    public MerchantFilterCategory prepareCreate() {
        selectedCAT = new MerchantFilterCategory();
        
        selectedCAT.setImf_description("");
        selectedCAT.setImf_Mid_Category_Id(0);         
        selectedCAT.setImf_active(false); 
        
        return selectedCAT;
    }

    /**
     * 
     */
    public void create() {
        if (selectedCAT != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  addIMFCategories('Gardening Tools', NULL, @newRecordID);
                String sqlProc = "{  CALL addIMFCategories( ?, ?, ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setString(1, selectedCAT.getImf_description().trim() );
                cs.setBoolean(2, selectedCAT.isImf_active() );
                
                cs.registerOutParameter(3, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(3);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Category Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Category Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            catItems = null;    // Invalidate list of items to trigger re-query.
        }
    }

    /**
     * 
     */
    public void update() {
         if (selectedCAT != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                //call editIMFCategory( 1, 'Electrnoic Goods', true, @result ) ;
                String sqlProc = "{  CALL editIMFCategory( ?, ?, ?, ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setLong(1, selectedCAT.getImf_Mid_Category_Id() );
                cs.setString(2, selectedCAT.getImf_description().trim() );
                cs.setBoolean(3, selectedCAT.isImf_active() );
                
                cs.registerOutParameter(4, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(4);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message                
                Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam );        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Edit Category Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Edit Category Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            catItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
    
    /**
     * 
     */
    public void destroy() {
        if (!JsfUtil.isValidationFailed()) {
            selectedCAT = null; // Remove selection
            catItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
  
    /**
     * @param catItems the catItems to set
     */
    public void setCatItems(List<MerchantFilterCategory> catItems) {
        this.catItems = catItems;
    }

    /**
     * @param selectedCat
     */
    public void setSelectedCAT(MerchantFilterCategory selectedCat) {
        this.selectedCAT = selectedCat;
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
     * @param catIDID
     * @param methodtoCall
     * @return the catMidsList
     */
    public List<MIDParent> getMidListSourceTarget( Long catIDID, String methodtoCall ) {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;
        
        List<MIDParent> catMidsList = new ArrayList<>();
                
        try {
            returnMessage = "";
            
            returnMessage = "";
            
            MIDParent singleItem;
            
            catMidsList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL " + methodtoCall + "( ?, ?  )  }";            
            
            cs = conn.prepareCall(sqlProc);

            cs.setLong(1, catIDID );
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
                
                catMidsList.add(singleItem);            
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
            
            Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return catMidsList;
    }
       
    /**
     * @return the catItemsMIDS
     */
    public List<MerchantFilterCategory> getCatItemsMIDS() {
        catItemsMIDS = getCatItems();
        
        for ( MerchantFilterCategory catItem : catItemsMIDS ) { 
            List<MIDParent> midsSource;
            List<MIDParent> midsTarget;
                       
            midsSource = getMidListSourceTarget( catItem.getImf_Mid_Category_Id() , "getMidListSource" ) ;
            midsTarget = getMidListSourceTarget( catItem.getImf_Mid_Category_Id() , "getMidListTarget" ) ;
                         
            catItem.setMerchantObjs( new DualListModel<>(midsSource, midsTarget) );
        }
        
        return catItemsMIDS;
    }

    /**
     * @param catItemsMIDS the catItemsMIDS to set
     */
    public void setCatItemsMIDS(List<MerchantFilterCategory> catItemsMIDS) {
        this.catItemsMIDS = catItemsMIDS;
    }

    /**
     * 
     */
    public void createCatMIDS() {
        if (catItemsMIDS != null) {
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  `addIMFMerchantCategoryLink`(	IN categoryLinkID bigint(20), IN merchantList VARCHAR(10000), OUT ret VARCHAR(2000)
                String sqlProc = "{  CALL addIMFMerchantCategoryLink( ?, ?, ? )  }";                            
                String merchantMIDList;
                
                //Process each Category
                for ( MerchantFilterCategory catItem : catItemsMIDS ) { 
                    merchantMIDList = "";

                    //Build merchant List per category
                    for ( int x=0; x < catItem.getMerchantObjs().getTarget().size(); x++ ) { 
                        String midID;
                        try { 
                            midID = Long.toString( ((MIDParent) catItem.getMerchantObjs().getTarget().get(x)).getValue() );
                        } catch (Exception e4) { 
                            //Cast exception - asume the value was placed directly
                            midID = catItem.getMerchantObjs().getTarget().get(x) + "";
                        }
                        
                        if ( midID != null && !midID.trim().isEmpty()) { 
                            if (x > 0) {                                 
                                merchantMIDList += ",";
                            }

                            merchantMIDList += midID;
                        }
                    } 
                    //Update Database for category                    
                    cs = conn.prepareCall(sqlProc);

                    cs.setLong(1, catItem.getImf_Mid_Category_Id() );
                    cs.setString(2, merchantMIDList );

                    cs.registerOutParameter(3, java.sql.Types.VARCHAR);

                    cs.execute();

                    resultParam = cs.getString(3);

                    //If error message - Long.parseLong will through exception
                   // Long.parseLong( resultParam );                                   
                }
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Category Merchant Link Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Category Merchant Link  Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(CATController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            catItemsMIDS = null;    // Invalidate list of items to trigger re-query.
        }
    }
 
  
   
}
