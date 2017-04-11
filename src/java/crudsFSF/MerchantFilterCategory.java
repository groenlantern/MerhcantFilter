/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
CREATE TABLE IF NOT EXISTS `halo161`.`imf_category` (
  `IMF_Mid_Category_Id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `Description` VARCHAR(100) NOT NULL,
  `Active` TINYINT(1) NULL DEFAULT NULL,
  `CreationDateTime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`IMF_Mid_Category_Id`),
  UNIQUE INDEX `IMF_MID_Category_UNIQUE_ID` (`IMF_Mid_Category_Id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
 */
package crudsFSF;

import java.io.Serializable;
import java.sql.Timestamp;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Jean-Pierre Erasmus
 */
public class MerchantFilterCategory implements Serializable {
    private long      imf_Mid_Category_Id = 0;
    private String    imf_description = "";
    private boolean   imf_active = false;
    private Timestamp imf_create_date;    
    private DualListModel<MIDParent> merchantObjs;
            
    /**
     * @return the imf_Mid_Category_Id
     */
    public long getImf_Mid_Category_Id() {
        return imf_Mid_Category_Id;
    }

    /**
     * @param imf_Mid_Category_Id the imf_Mid_Category_Id to set
     */
    public void setImf_Mid_Category_Id(long imf_Mid_Category_Id) {
        this.imf_Mid_Category_Id = imf_Mid_Category_Id;
    }

    /**
     * @return the imf_description
     */
    public String getImf_description() {
        return imf_description;
    }

    /**
     * @param imf_description the imf_description to set
     */
    public void setImf_description(String imf_description) {
        this.imf_description = imf_description;
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
     * @return the imf_create_date
     */
    public Timestamp getImf_create_date() {
        return imf_create_date;
    }

    /**
     * @param imf_create_date the imf_create_date to set
     */
    public void setImf_create_date(Timestamp imf_create_date) {
        this.imf_create_date = imf_create_date;
    }

    /**
     * @return the merchantObjs
     */
    public DualListModel<MIDParent> getMerchantObjs() {
        return merchantObjs;
    }

    /**
     * @param merchantObjs the merchantObjs to set
     */
    public void setMerchantObjs(DualListModel<MIDParent> merchantObjs) {
        this.merchantObjs = merchantObjs;
    }



}
