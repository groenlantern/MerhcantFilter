/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crudsFSF;

import java.util.List;
import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.sql.DataSource;

/**
 *
 * @author Jean-Pierre Erasmus
 */
@FacesConverter(value = "MerchantConvertor")
public class MerchantConvertor implements Converter {
  /**
   * 
   */
    @Resource(name="java:app/haloLocal")
    private DataSource dataSourceGridController;

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

    @Override
    public MerchantFilterMIDHierarchy getAsObject(FacesContext ctx, UIComponent component, String submittedValue) {
        MerchantFilterMIDHierarchy retMID = null;
        
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }
        
        try {                     
            ApproveCardMerchantTest approvMerch = new ApproveCardMerchantTest();
            approvMerch.setDataSourceGridController(dataSourceGridController);

            List<MerchantFilterMIDHierarchy> midList = approvMerch.getMidItems();

            for (MerchantFilterMIDHierarchy mid : midList) {
                
                //Match on Code
                if ( submittedValue.equals(mid.getImf_mid_midCode() )) {
                    retMID = mid;
                    break;
                }
                
                //Match on Description
                if ( submittedValue.equals(mid.getImf_mid_description() )) {
                    retMID = mid;
                    break;
                }
            }
        } catch (Exception e) { 
            return null;
        }
        
        return retMID;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object modelValue) {

        MerchantFilterMIDHierarchy datMID = (MerchantFilterMIDHierarchy) modelValue;
        
        if (datMID == null) {
            return "";
        } else if (datMID instanceof MerchantFilterMIDHierarchy) {
            return datMID.getImf_mid_description();
        } else {
            return null;
        }
    }

}