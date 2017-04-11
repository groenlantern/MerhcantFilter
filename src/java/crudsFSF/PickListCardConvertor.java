package crudsFSF;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Jean-Pierre Erasmus
 */
@FacesConverter(value = "PickListCardConvertor")
public class PickListCardConvertor implements Converter {

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        Object ret = null;
        
        if (arg1 instanceof PickList) {
            DualListModel dl = (DualListModel) ((PickList) arg1).getValue();
            
            for (Object o : dl.getSource()) {
                String id = "" + ((MFCardStock) o).getImf_CardStock_Id();

                if (arg2.equals(id)) {
                    ret = o;
                    break;
                }
            }
            
            if (ret == null) {
                
                for (Object o : dl.getTarget()) {
                    String id = "" + ((MFCardStock) o).getImf_CardStock_Id();
                    
                    if (arg2.equals(id)) {
                        ret = o;
                        break;
                    }
                }
                
            }
            
        }
        
        return ret;
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        
        String str = "";
        
        if (arg2 instanceof MFCardStock) {
            str = "" + ((MFCardStock) arg2).getImf_CardStock_Id();
        }
        
        return str;
    }

}
