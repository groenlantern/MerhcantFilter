/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
`imf_mid` (
  `IMF_Mid_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Description` varchar(100) DEFAULT NULL,
  `Name` varchar(100) NOT NULL,
  `Parent_Id` bigint(20) DEFAULT NULL,
  `MID_Category_Type` varchar(100) NOT NULL DEFAULT '0',
  `CreationDateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
 */
package crudsFSF;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Jean-Pierre Erasmus
 */
public class MerchantFilterMIDHierarchy implements Serializable {
    private long      imf_mid_id = 0;
    private String    imf_mid_midCode = "";
    private String    imf_mid_description = "";
    private String    imf_mid_name = "";
    private String    imf_mid_nameOriginal = "";
    private String    imf_hierarchy = "";
    private String    imf_parentName = "";
    private long      imf_mid_parent = -1;        
    private int       imf_mid_level = 0;
    private Timestamp imf_create_date;    
    
    /**
     * @return the imf_mid_id
     */
    public long getImf_mid_id() {
        return imf_mid_id;
    }

    /**
     * @param imf_mid_id the imf_mid_id to set
     */
    public void setImf_mid_id(long imf_mid_id) {
        this.imf_mid_id = imf_mid_id;
    }

    /**
     * @return the imf_mid_description
     */
    public String getImf_mid_description() {
        if ( imf_mid_description == null || 
             imf_mid_description.trim().isEmpty()) { 
            return (getImf_mid_name() != null) ? getImf_mid_name() : "";
        }
           
        return imf_mid_description;
    }

    /**
     * @param imf_mid_description the imf_mid_description to set
     */
    public void setImf_mid_description(String imf_mid_description) {
        this.imf_mid_description = imf_mid_description;
    }

    /**
     * @return the imf_mid_name
     */
    public String getImf_mid_name() {
        if ( imf_mid_name != null && 
             imf_mid_name.trim().isEmpty()) { 
            return null;
        }
        
        return imf_mid_name;
    }

    /**
     * @param imf_mid_name the imf_mid_name to set
     */
    public void setImf_mid_name(String imf_mid_name) {
        this.imf_mid_name = imf_mid_name;
    }

    /**
     * @return the imf_mid_parent
     */
    public long getImf_mid_parent() {
        if ( imf_mid_parent < 1) { 
            return 0;
        }
        
        return imf_mid_parent;
    }

    /**
     * @param imf_mid_parent the imf_mid_parent to set
     */
    public void setImf_mid_parent(long imf_mid_parent) {
        this.imf_mid_parent = imf_mid_parent;
    }

    /**
     * @return the imf_mid_level
     */
    public int getImf_mid_level() {
        return imf_mid_level;
    }

    /**
     * @param imf_mid_level the imf_mid_level to set
     */
    public void setImf_mid_level(int imf_mid_level) {
        this.imf_mid_level = imf_mid_level;
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
     * @return the imf_hierarchy
     */
    public String getImf_hierarchy() {
        return imf_hierarchy;
    }

    /**
     * @param imf_hierarchy the imf_hierarchy to set
     */
    public void setImf_hierarchy(String imf_hierarchy) {
        this.imf_hierarchy = imf_hierarchy;
    }

    /**
     * @return the imf_mid_nameOriginal
     */
    public String getImf_mid_nameOriginal() {
        return imf_mid_nameOriginal;
    }

    /**
     * @param imf_mid_nameOriginal the imf_mid_nameOriginal to set
     */
    public void setImf_mid_nameOriginal(String imf_mid_nameOriginal) {
        this.imf_mid_nameOriginal = imf_mid_nameOriginal;
    }

    /**
     * @return the imf_parentName
     */
    public String getImf_parentName() {
        
        return imf_parentName + " [" + imf_mid_parent + "]" ;
    }

    /**
     * @param imf_parentName the imf_parentName to set
     */
    public void setImf_parentName(String imf_parentName) {
        this.imf_parentName = imf_parentName;
    }

    /**
     * @return the imf_mid_midCode
     */
    public String getImf_mid_midCode() {
        return imf_mid_midCode;
    }

    /**
     * @param imf_mid_midCode the imf_mid_midCode to set
     */
    public void setImf_mid_midCode(String imf_mid_midCode) {
        this.imf_mid_midCode = imf_mid_midCode;
    }


}
