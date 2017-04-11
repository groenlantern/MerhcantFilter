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
@Named("midController")
@SessionScoped
public class MIDController implements Serializable {
   /**
     * 
     */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;
        
    private int showRows = 20;
    private String returnMessage = "";    
    private List<MerchantFilterMIDHierarchy> midItems = null;
    private List<MerchantFilterMIDHierarchy> filteredMidItems = null;    
    private MerchantFilterMIDHierarchy selectedMID = null;
    
    private List<MIDParent> midParentList = null;
        
    public MIDController() {
         //Blank Item Array 
        midItems = new ArrayList<>();
        
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
    public MerchantFilterMIDHierarchy getSelectedMID() {
        return selectedMID;
    }

    /**
     * 
     * @return 
     */
    public List<MerchantFilterMIDHierarchy> getFilteredMidItems() {
        return filteredMidItems;
    }

    /**
     * 
     * @param filteredItems 
     */
    public void setFilteredMidItems(List<MerchantFilterMIDHierarchy> filteredItems) {
        this.filteredMidItems = filteredItems;
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

        try {
            returnMessage = "";
            
            MerchantFilterMIDHierarchy singleItem;
            
            midItems = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMIDHierarchy(  ? )  }";            
          
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
                
                singleItem.setImf_hierarchy( rs.getString("concatParent") );

                try { 
                    singleItem.setImf_mid_level( Integer.parseInt( rs.getString("MID_Category_Type") ) );
                    
                    String tabbedName = "";
                    
                    String treeChar = "└";

                    for (int x=2; x < singleItem.getImf_mid_level(); x++) {                         
                        for (int y=0;y<5;y++) { 
                            tabbedName += " " ;                        
                        }
                    }
                    
                    //for (int x=1; x < singleItem.getImf_mid_level(); x++) {     
                    if ( singleItem.getImf_mid_level() > 1) { 
                        for (int y=0;y<5;y++) { 
                            tabbedName += treeChar;
                            treeChar = "─";
                        }
                        tabbedName += "   ";
                    }
                    
                    tabbedName += singleItem.getImf_mid_name();
                    
                    singleItem.setImf_mid_name( tabbedName );
                } catch (SQLException | NumberFormatException e) { 
                    singleItem.setImf_mid_level( 0 );
                }
                
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
            
            Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return midItems;
    }

    
    /**
     * 
     * @return 
     */
    public MerchantFilterMIDHierarchy prepareCreate() {
        selectedMID = new MerchantFilterMIDHierarchy();
        
        selectedMID.setImf_mid_description("");
        selectedMID.setImf_mid_level(0);
        selectedMID.setImf_mid_name("");
        selectedMID.setImf_mid_parent(-1);
        selectedMID.setImf_hierarchy("");
        selectedMID.setImf_mid_midCode("");
        selectedMID.setImf_mid_id(0);
        
        return selectedMID;
    }

    /**
     * 
     */
    public void create() {
        if (selectedMID != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                //call  addIMFMerchantAutoLevel('Item','Now 24', 16, @newRecordID);
                String sqlProc = "{  CALL addIMFMerchantAutoLevel(?, ?, ?, ?, ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setString(1, selectedMID.getImf_mid_midCode().trim() );
                cs.setString(2, selectedMID.getImf_mid_description().trim() );
                cs.setString(3, selectedMID.getImf_mid_name().trim() );
                cs.setLong(4, selectedMID.getImf_mid_parent() );
                
                cs.registerOutParameter(5, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(5);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message
                Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam);        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Create Merchant Hierarchy Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Create Merchant Hierarchy Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            midItems = null;    // Invalidate list of items to trigger re-query.
        }
    }

    /**
     * 
     */
    public void update() {
         if (selectedMID != null) {
             
            //Connection details    
            Connection conn = null;
            CallableStatement cs;
            String resultParam = "";
                    
            try {
                returnMessage = "";
                
                conn = getMySqlConnection();
                
                String sqlProc = "{  CALL editIMFMerchant(?, ?, ?, ?, ?, ?, ? )  }";            

                cs = conn.prepareCall(sqlProc);
                
                cs.setLong(1, selectedMID.getImf_mid_id() );
                cs.setString(2, selectedMID.getImf_mid_midCode().trim() );
                cs.setString(3, selectedMID.getImf_mid_description().trim() );
                cs.setString(4, selectedMID.getImf_mid_nameOriginal().trim() );
                cs.setString(5, Integer.toString( selectedMID.getImf_mid_level() ) );
                cs.setLong(6, selectedMID.getImf_mid_parent() );
                
                cs.registerOutParameter(7, java.sql.Types.VARCHAR);
                
                cs.execute();
                
                resultParam = cs.getString(7);
                
                //If error message - Long.parseLong will through exception
                Long.parseLong( resultParam );                
                
            } catch (Exception ex) {
                //Output Exception Message                
                Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex + " : " + resultParam );        
                
                String msg = "";
                
                Throwable cause = ex.getCause();
                
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage("Edit Merchant Hierarchy Failed : " + msg + " : " + resultParam );
                } else {
                    JsfUtil.addErrorMessage(ex, ("Edit Merchant Hierarchy Failed : " + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured") + " : " + resultParam) );
                }
                
            } finally { 
                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        
        if (!JsfUtil.isValidationFailed()) {
            midItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
    
    /**
     * 
     */
    public void destroy() {
        if (!JsfUtil.isValidationFailed()) {
            selectedMID = null; // Remove selection
            midItems = null;    // Invalidate list of items to trigger re-query.
        }
    }
  
    /**
     * @param midItems the midItems to set
     */
    public void setMidItems(List<MerchantFilterMIDHierarchy> midItems) {
        this.midItems = midItems;
    }

    /**
     * @param selectedMID the selectedMID to set
     */
    public void setSelectedMID(MerchantFilterMIDHierarchy selectedMID) {
        this.selectedMID = selectedMID;
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
     * @param dataSourceGridControllr
     * @return 
     */
    public List<MIDParent> getMidParentList(DataSource dataSourceGridControllr) {
        dataSourceGridController = (dataSourceGridControllr);
        
        return getMidParentList();
    }
    
    /**
     * @return the midParentList
     */
    public List<MIDParent> getMidParentList() {
       //Connection details    
        Connection conn = null;
        CallableStatement cs;
        ResultSet rs;

        try {
            returnMessage = "";
            
            MIDParent singleItem;
            
            midParentList = new ArrayList<>();
   
            conn = getMySqlConnection();
                                 
            String sqlProc = "{  CALL getMIDParentHierarchy( ? )  }";            
          
            cs = conn.prepareCall(sqlProc);
            cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            returnMessage = cs.getString(1);
            
            rs = cs.getResultSet();
            
            while (rs.next()) {
                singleItem = new MIDParent();
                
                singleItem.setDescription( rs.getString("description") );
                singleItem.setLabel(  rs.getString("label") );
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
                
                midParentList.add(singleItem);            
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
            
            Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);        
        } finally { 
            try {
                if ( conn != null ) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(MIDController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        return midParentList;
    }

    /**
     * @param midParentList the midParentList to set
     */
    public void setMidParentList(List<MIDParent> midParentList) {
        this.midParentList = midParentList;
    }

   
}
