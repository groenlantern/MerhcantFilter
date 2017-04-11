DELIMITER $$
DROP PROCEDURE IF EXISTS `getMIDHierarchy`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMIDHierarchy`( OUT ret VARCHAR(255) )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMIDHierarchy ( ) code: ', code, ' message: ', msg) INTO ret;
            END;

  select a.*,		
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Name, '' ), if ( r.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Name, '' ), if ( q.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Name, '' ), if ( p.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Name, '' ), if ( o.Parent_Id IS NOT NULL, '/', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Name, '' ), if ( i.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Name, '' ), if ( a.Parent_Id IS NOT NULL, '/', '' ),                        
				  a.Name ) as concatParent,
                  coalesce(i.Name,'') as parentName
                        
 from imf_mid as a 
 left outer join imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
order by concatParent;                       
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `getMFCardStock`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMFCardStock`( OUT ret VARCHAR(255) )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMFCardStock ( ) code: ', code, ' message: ', msg) INTO ret;
            END;

 select a.*            
 from imf_card_stock as a 
 order by Pan_number;                       
    
END$$
DELIMITER ;




DELIMITER $$
DROP PROCEDURE IF EXISTS `getMFCategories`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMFCategories`( OUT ret VARCHAR(255) )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMFCategories ( ) code: ', code, ' message: ', msg) INTO ret;
            END;

 select a.*            
 from  imf_category as a 
 order by Description;                       
    
END$$
DELIMITER ;




DELIMITER $$
DROP PROCEDURE IF EXISTS `editIMFCategory`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `editIMFCategory`(	IN updateID bigint(20),
															    IN description VARCHAR(100),
																IN active BOOLEAN,
																OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
    
	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Editing Category (',updateID , ' - ' , description,') code: ', code, ' message: ', msg) INTO ret;
            END;
            	
    IF (active IS NULL) THEN
		set active = false;
    END IF;
            
    IF (description IS NULL) THEN
		SELECT CONCAT('Error Editing Category - ',updateID , ' - Null Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(description)) = '' ) THEN
		SELECT CONCAT('Error Editing Category - ', updateID , ' Blank Name Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
       
		UPDATE imf_category SET `Description` = description, 
								`Active` = active 
		WHERE IMF_Mid_Category_Id = updateID;

		SELECT ROW_COUNT() into mercID;
			
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Editing Category (', updateID , ' - ' , description, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;            
         
	END IF;
	
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFMerchantCategoryLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFMerchantCategoryLink`(	IN categoryLinkID bigint(20),
																			IN merchantList VARCHAR(10000),
																			OUT ret VARCHAR(2000) )
BEGIN	
	DECLARE mercLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE merchantID bigInt(20) DEFAULT 0;
    
	DEClARE merchantLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Id as merID from imf_mid where FIND_IN_SET(IMF_Mid_Id, merchantList) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error Inserting Merchant Category Link (',categoryLinkID,') code: ', code, ' message: ', msg) INTO ret;
            END;
	
	OPEN merchantLinkCursor;      
   
	-- Delete records for category
	DELETE FROM imf_mid_Category_Link where `Category_Id` = categoryLinkID;

	get_Merch: LOOP
		FETCH merchantLinkCursor INTO merchantID;
        
		IF finished = 1 THEN 
			LEAVE get_Merch;
		END IF;
                 
        -- if SQL crashed Exit handler will handle the crash 
		-- Insert Record 
		INSERT INTO imf_mid_Category_Link ( `Category_Id`, `MID_Id` )
		VALUES ( categoryLinkID, merchantID );
    
	END LOOP get_Merch;
   
	SET mercLinkID = LAST_INSERT_ID(); 
			
    IF (merchantList IS NOT NULL) AND ( rtrim(ltrim(merchantList)) != '' ) THEN
    
		IF (mercLinkID < 1) THEN
			SELECT CONCAT('Error Inserting Merchant Category Link 1  (', categoryLinkID, ') ' ) INTO ret;
		ELSE
			SELECT mercLinkID INTO ret;
		END IF;
	
	END IF;
	
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getMidListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMidListSource`( IN categoryID bigint(20),
																OUT ret VARCHAR(255) )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMidListSource (' + categoryID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;

  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from  imf_mid as a 
 left outer join  imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join  imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join  imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join  imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join  imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join  imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id NOT IN ( select  `MID_Id` 
							 from    imf_mid_Category_Link 
                             where   `Category_Id` = categoryID ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getMidListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMidListTarget`( IN categoryID bigint(20),
																OUT ret VARCHAR(255)  )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMidListTarget (' + categoryID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;


  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from  imf_mid as a 
 left outer join  imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join  imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join  imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join  imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join  imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join  imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id IN ( select  `MID_Id` 
						 from    imf_mid_Category_Link 
                         where   `Category_Id` = categoryID ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `getMFCampaignData`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMFCampaignData`( OUT ret VARCHAR(255) )
BEGIN

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMFCampaignData (  ) code: ', code, ' message: ', msg) INTO ret;
            END;


 select a.*            
 from imf_campaign as a 
 order by Description;                       
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCampaign`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCampaign`(	IN Description VARCHAR(100),
															    IN active BOOLEAN,
																OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Inserting Campaign (',Description,') code: ', code, ' message: ', msg) INTO ret;
            END;

    IF (active IS NULL) THEN
		set active = false;
    END IF;
    
    IF (Description IS NULL) THEN
		SELECT CONCAT('Error Inserting Campaign - Null Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(Description)) = '' ) THEN
		SELECT CONCAT('Error Inserting Campaign  - Blank Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  	
		INSERT INTO imf_campaign (`Description`, `Active` )
		VALUES (Description, active);

		SET mercID = LAST_INSERT_ID(); 
	    
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Inserting Campaign (', Description, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;
	END IF;
	
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `editIMF_campaignData`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `editIMF_campaignData`(	IN updateID bigint(20),
																	IN Description VARCHAR(100),
																	IN active BOOLEAN,
																	OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
    
	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Editing Campaign (',updateID , ' - ' , Description,') code: ', code, ' message: ', msg) INTO ret;
            END;
            	
    IF (active IS NULL) THEN
		set active = false;
    END IF;

   
    IF (Description IS NULL) THEN
		SELECT CONCAT('Error Inserting Campaign - Null Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(Description)) = '' ) THEN
		SELECT CONCAT('Error Inserting Campaign  - Blank Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
      
		UPDATE imf_campaign SET `Description` = Description, 
  							    `Active`      = active 
		WHERE IMF_Campaign_Id = updateID;

		SELECT ROW_COUNT() into mercID;
			
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Editing Campaign (', updateID , ' - ' , Description, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;            
         
	END IF;
	
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCampaignMIDLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCampaignMIDLink`(IN mid_Ids  VARCHAR(10000),
																	IN campaign_id BIGINT(20),
																	OUT ret VARCHAR(2000) )
BEGIN	
	DECLARE mercLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE merchantID bigInt(20) DEFAULT 0;
    
	DEClARE merchantLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Id as merID from imf_mid where FIND_IN_SET(IMF_Mid_Id, mid_Ids) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error Inserting Campaign Mid Link (',campaign_id,') code: ', code, ' message: ', msg) INTO ret;
            END;
           
    IF (campaign_id IS NULL) THEN
		SELECT CONCAT('Error Inserting Campaign Mid Link - Null Campaign is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(campaign_id)) = '' ) THEN
		SELECT CONCAT('Error Inserting Campaign Mid Link - Blank Campaign is Not Allowed ' ) INTO ret;
    END IF;
   
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
		OPEN merchantLinkCursor;      
	   
		-- Delete records for category
		DELETE FROM imf_campaign_mid_link where `imf_campaign_Id` = campaign_id;

		get_Merch: LOOP
			FETCH merchantLinkCursor INTO merchantID;
			
			IF finished = 1 THEN 
				LEAVE get_Merch;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Record 
			INSERT INTO imf_campaign_mid_link ( `imf_campaign_Id`, `imf_MID_Id` )
			VALUES ( campaign_id, merchantID );
		
		END LOOP get_Merch;
	   
		SET mercLinkID = LAST_INSERT_ID(); 

		IF (mid_Ids IS NOT NULL) AND ( rtrim(ltrim(mid_Ids)) != '' ) THEN						
			IF (mercLinkID < 1) THEN
				SELECT CONCAT('Error Inserting Campaign Mid Link  (', campaign_id, ') ', mercLinkID ) INTO ret;
			ELSE
				SELECT mercLinkID INTO ret;
			END IF;
		END IF;
	END IF;
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCampaignCategoryLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCampaignCategoryLink`(IN category_Ids  VARCHAR(10000),
																		 IN campaign_id   BIGINT(20),
																	     OUT ret          VARCHAR(2000) )
BEGIN	
	DECLARE catLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE categoryID bigInt(20) DEFAULT 0;
    
	DEClARE categoryLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Category_Id as catID from imf_category where FIND_IN_SET(IMF_Mid_Category_Id, category_Ids) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

				set ret = CONCAT('Error Inserting Campaign Category Link (', campaign_id ,') code: ', code, ' message: ', msg);
            END;
        
    IF (campaign_id IS NULL) THEN
		set ret = 'Error Inserting Campaign Category Link - Null Campaign is Not Allowed ' ;
    END IF;
    
    IF ( rtrim(ltrim(campaign_id)) = '' ) THEN
		set ret = 'Error Inserting Campaign Category Link - Blank Campaign  Not Allowed ';
    END IF;
   
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
		OPEN categoryLinkCursor;      
	   
		-- Delete records for category
		DELETE FROM imf_campaign_category_link where `imf_campaign_Id` = campaign_id;

		get_Cat: LOOP
			FETCH categoryLinkCursor INTO categoryID;
			
			IF finished = 1 THEN 
				LEAVE get_Cat;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Record 
			INSERT INTO imf_campaign_category_link ( `imf_campaign_Id`, `imf_category_Id` )
			VALUES ( campaign_id, categoryID );
		
		END LOOP get_Cat;
	   
		SET catLinkID = LAST_INSERT_ID(); 

		IF (category_Ids IS NOT NULL) AND ( rtrim(ltrim(category_Ids)) != '' ) THEN			
			IF (catLinkID < 1) THEN
				set ret = CONCAT('Error Inserting Campaign Category Link  (', campaign_id, ') ' ) ;
			ELSE
				SELECT catLinkID INTO ret;
			END IF;
		END IF;
	END IF;
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCampaignCardStockLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCampaignCardStockLink`(IN card_Ids  VARCHAR(10000),
																	     IN campaign_id   BIGINT(20),
																	     OUT ret          VARCHAR(2000) )
BEGIN	
	DECLARE cardLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE cardID bigInt(20) DEFAULT 0;
    
	DEClARE cardLinkCursor CURSOR FOR 
		SELECT IMF_CardStock_Id as cardID from imf_card_stock where FIND_IN_SET(IMF_CardStock_Id, card_Ids) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

				set ret = CONCAT('Error Inserting Campaign Card Stock Link (', campaign_id ,') code: ', code, ' message: ', msg);
            END;
        
    IF (campaign_id IS NULL) THEN
		set ret = 'Error Inserting Campaign Card Stock Link - Null Campaign is Not Allowed ' ;
    END IF;
    
    IF ( rtrim(ltrim(campaign_id)) = '' ) THEN
		set ret = 'Error Inserting Campaign Card Stock Link - Blank Campaign  Not Allowed ';
    END IF;
   
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
		OPEN cardLinkCursor;      
	   
		-- Delete records for category
		DELETE FROM imf_campaign_card_link where `imf_campaign_Id` = campaign_id;

		get_Card: LOOP
			FETCH cardLinkCursor INTO cardID;
			
			IF finished = 1 THEN 
				LEAVE get_Card;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Record 
			INSERT INTO imf_campaign_card_link ( `imf_campaign_Id`, `imf_CARD_Id` )
			VALUES ( campaign_id, cardID );
		
		END LOOP get_Card;
	   
		SET cardLinkID = LAST_INSERT_ID(); 

		IF (card_Ids IS NOT NULL) AND ( rtrim(ltrim(card_Ids)) != '' ) THEN			
			IF (cardLinkID < 1) THEN
				set ret = CONCAT('Error Inserting Campaign Card Stock Link  (', campaign_id, ') ' ) ;
			ELSE
				SELECT cardLinkID INTO ret;
			END IF;
		END IF;
	END IF;
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCardStock`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCardStock`(	IN Pan_number VARCHAR(100),
																IN balance VARCHAR(12),
															    IN active BOOLEAN,
                                                                IN isPanGroupX BOOLEAN,
																OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Inserting Card Stock (',Pan_number,') code: ', code, ' message: ', msg) INTO ret;
            END;

    IF (active IS NULL) THEN
		set active = false;
    END IF;
    
    IF (isPanGroupX IS NULL) THEN
		set isPanGroupX = false;
    END IF;

    IF (balance IS NULL) THEN
		set balance ='0';
    END IF;
    
    IF ( rtrim(ltrim(balance)) = '' ) THEN
		set balance ='0';
    END IF;
            
    IF (Pan_number IS NULL) THEN
		SELECT CONCAT('Error Inserting Card Stock - Null Card Stock is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(Pan_number)) = '' ) THEN
		SELECT CONCAT('Error Inserting Card Stock  - Blank Card Stock  Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  	
		INSERT INTO imf_card_stock (`Pan_number`, `Balance`, `Active`, `isPANGroup`  )
		VALUES (Pan_number, balance, active, isPanGroupX );

		SET mercID = LAST_INSERT_ID(); 
	    
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Inserting Card Stock (', Pan_number, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;
	END IF;
	
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCategories`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCategories`(	IN description VARCHAR(100),
															    IN active BOOLEAN,
																OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Inserting Category (',description,') code: ', code, ' message: ', msg) INTO ret;
            END;

    IF (active IS NULL) THEN
		set active = false;
    END IF;
            
    IF (description IS NULL) THEN
		SELECT CONCAT('Error Inserting Category - Null Description is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(description)) = '' ) THEN
		SELECT CONCAT('Error Inserting Category - Blank Name Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  	
		INSERT INTO imf_category (`Description`, `Active` )
		VALUES (description, active);

		SET mercID = LAST_INSERT_ID(); 
	    
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Inserting Category (', description, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;
	END IF;
	
END$$
DELIMITER ;
 

DELIMITER $$
DROP PROCEDURE IF EXISTS `editIMF_card_stock`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `editIMF_card_stock`(	IN updateID bigint(20),
																	IN Pan_number VARCHAR(100),
																	IN balance VARCHAR(12),
																	IN active BOOLEAN,
                                                                    IN isPanGroupX BOOLEAN,
																	OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
    
	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Editing Card Stock (',updateID , ' - ' , Pan_number,') code: ', code, ' message: ', msg) INTO ret;
            END;
            	
     IF (active IS NULL) THEN
		set active = false;
    END IF;

     IF (isPanGroupX IS NULL) THEN
		set isPanGroupX = false;
    END IF;

    IF (balance IS NULL) THEN
		set balance ='0';
    END IF;
    
    IF ( rtrim(ltrim(balance)) = '' ) THEN
		set balance ='0';
    END IF;
            
    IF (Pan_number IS NULL) THEN
		SELECT CONCAT('Error Editing Card Stock - Null Card Stock is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(Pan_number)) = '' ) THEN
		SELECT CONCAT('Error Editing Card Stock  - Blank Card Stock  Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
      
		UPDATE imf_card_stock SET `Pan_number` = Pan_number, 
								  `Balance`    = balance ,
								  `Active`     = active ,
                                  `isPANGroup`  = isPanGroupX                                  
		WHERE IMF_CardStock_Id = updateID;

		SELECT ROW_COUNT() into mercID;
			
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Editing Card Stock (', updateID , ' - ' , Pan_number, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;            
         
	END IF;
	
END$$
DELIMITER ;




DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCardMIDLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCardMIDLink`(IN mid_Ids  VARCHAR(10000),
																IN cardStock_id BIGINT(20),
																OUT ret VARCHAR(2000) )
BEGIN	
	DECLARE mercLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE merchantID bigInt(20) DEFAULT 0;
    
	DEClARE merchantLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Id as merID from imf_mid where FIND_IN_SET(IMF_Mid_Id, mid_Ids) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error Inserting Card Stock Mid Link (',cardStock_id,') code: ', code, ' message: ', msg) INTO ret;
            END;
           
    IF (cardStock_id IS NULL) THEN
		SELECT CONCAT('Error Inserting Card Stock Mid Link - Null Card Stock is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(cardStock_id)) = '' ) THEN
		SELECT CONCAT('Error Inserting Card Stock Mid Link - Blank Card Stock is Not Allowed ' ) INTO ret;
    END IF;
   
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
		OPEN merchantLinkCursor;      
	   
		-- Delete records for category
		DELETE FROM imf_card_stock_mid_link where `imf_card_stock_Id` = cardStock_id;

		get_Merch: LOOP
			FETCH merchantLinkCursor INTO merchantID;
			
			IF finished = 1 THEN 
				LEAVE get_Merch;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Record 
			INSERT INTO imf_card_stock_mid_link ( `imf_card_stock_Id`, `imf_mid_Id` )
			VALUES ( cardStock_id, merchantID );
		
		END LOOP get_Merch;
	   
		SET mercLinkID = LAST_INSERT_ID(); 

		IF (mid_Ids IS NOT NULL) AND ( rtrim(ltrim(mid_Ids)) != '' ) THEN						
			IF (mercLinkID < 1) THEN
				SELECT CONCAT('Error Inserting Card Stock Mid Link  (', cardStock_id, ') ', mercLinkID ) INTO ret;
			ELSE
				SELECT mercLinkID INTO ret;
			END IF;
		END IF;
	END IF;
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFCardMCategoryLink`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFCardMCategoryLink`(IN category_Ids  VARCHAR(10000),
																	  IN cardStock_id BIGINT(20),
																	  OUT ret VARCHAR(2000) )
BEGIN	
	DECLARE catLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE categoryID bigInt(20) DEFAULT 0;
    
	DEClARE categoryLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Category_Id as catID from imf_category where FIND_IN_SET(IMF_Mid_Category_Id, category_Ids) > 0;  
          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

				set ret = CONCAT('Error Inserting Card Stock Category Link (',cardStock_id,') code: ', code, ' message: ', msg);
            END;
        
    IF (cardStock_id IS NULL) THEN
		set ret = 'Error Inserting Card Stock Category Link - Null Card Stock is Not Allowed ' ;
    END IF;
    
    IF ( rtrim(ltrim(cardStock_id)) = '' ) THEN
		set ret = 'Error Inserting Card Stock Category Link - Blank Card Stock  Not Allowed ';
    END IF;
   
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
		OPEN categoryLinkCursor;      
	   
		-- Delete records for category
		DELETE FROM imf_card_category_link where `imf_card_Id` = cardStock_id;

		get_Cat: LOOP
			FETCH categoryLinkCursor INTO categoryID;
			
			IF finished = 1 THEN 
				LEAVE get_Cat;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Record 
			INSERT INTO imf_card_category_link ( `imf_card_Id`, `imf_mid_category_Id` )
			VALUES ( cardStock_id, categoryID );
		
		END LOOP get_Cat;
	   
		SET catLinkID = LAST_INSERT_ID(); 

		IF (category_Ids IS NOT NULL) AND ( rtrim(ltrim(category_Ids)) != '' ) THEN			
			IF (catLinkID < 1) THEN
				set ret = CONCAT('Error Inserting Card Stock Category Link  (', cardStock_id, ') ' ) ;
			ELSE
				SELECT catLinkID INTO ret;
			END IF;
		END IF;
	END IF;
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampMidListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampMidListSource`( IN campID bigint(20),
																	OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampMidListSource (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;


  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from imf_mid as a 
 left outer join  imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join  imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join  imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join  imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join  imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join  imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id NOT IN ( select  `imf_MID_Id` 
							 from    imf_campaign_mid_link 
                             where   `imf_campaign_Id` = campID ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampMidListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampMidListTarget`( IN campID bigint(20),
																	OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampMidListTarget (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;


  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from  imf_mid as a 
 left outer join  imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join  imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join  imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join  imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join  imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join  imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id IN (select  `imf_MID_Id` 
							 from    imf_campaign_mid_link 
                             where   `imf_campaign_Id` =  campID  ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampaignCategoryListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampaignCategoryListSource`( IN campID bigint(20),
																			 OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampaignCategoryListSource (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;



  select a.imf_mid_category_Id as value,		
		 a.Description as label,
         a.Description as description
                        
 from  imf_category as a 
 
 where a.imf_mid_category_Id NOT IN ( select  `imf_category_Id` 
									  from    imf_campaign_category_link 
								      where   `imf_campaign_Id` = campID ) 
and    a.Active = true                                      
 
order by description;                       
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampaignCategoryListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampaignCategoryListTarget`( IN campID bigint(20),
																			 OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampaignCategoryListTarget (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;



  select  a.imf_mid_category_Id as value,		
	  	  a.Description as label,
          a.Description as description
                        
 from  imf_category as a 
 
 
 where a.imf_mid_category_Id IN (select  `imf_category_Id` 
									  from    imf_campaign_category_link 
								      where   `imf_campaign_Id` = campID ) 
 
and    a.Active = true 
 
order by description;                       
    
END$$
DELIMITER ;


DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampaignCardListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampaignCardListSource`( IN campID bigint(20),
																		 OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampaignCardListSource (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;
            
  select a.IMF_CardStock_Id as value,		
		 a.Pan_number as label,
         a.Pan_number as description
                        
 from  imf_card_stock as a 
 
 where a.IMF_CardStock_Id NOT IN ( select     `imf_CARD_Id` 
									  from    imf_campaign_card_link 
								      where   `imf_campaign_Id` = campID ) 
and    a.Active = true                                      
 
order by description;                       
    
END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS `getCampaignCardListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCampaignCardListTarget`( IN campID bigint(20),
																	     OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCampaignCardListTarget (' + campID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;
            
 select a.IMF_CardStock_Id as value,		
		 a.Pan_number as label,
         a.Pan_number as description
                        
 from  imf_card_stock as a 
 
 
 where a.IMF_CardStock_Id IN (select     `imf_CARD_Id` 
							  from    imf_campaign_card_link 
							  where   `imf_campaign_Id` = campID  ) 
 
and    a.Active = true 
 
order by description;                       
    
END$$
DELIMITER ;






DELIMITER $$
DROP PROCEDURE IF EXISTS `getCardMidListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCardMidListSource`( IN cardID bigint(20),
																	OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCardMidListSource (' + cardID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;
            

  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from imf_mid as a 
 left outer join imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id NOT IN ( select  `imf_mid_Id` 
							 from    imf_card_stock_mid_link 
                             where   `imf_card_stock_Id` = cardID ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS `getCardMidListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCardMidListTarget`( IN cardID bigint(20),
																	OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCardMidListTarget (' + cardID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;
            

  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Description, '' ), if ( r.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Description, '' ), if ( q.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Description, '' ), if ( p.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Description, '' ), if ( o.Parent_Id IS NOT NULL, ' / ', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Description, '' ), if ( i.Parent_Id IS NOT NULL, ' / ', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Description, '' ), if ( a.Parent_Id IS NOT NULL, ' / ', '' ),                        
				  a.Description ) as concatParent
                        
 from imf_mid as a 
 left outer join imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 
 where a.IMF_Mid_Id IN (select  `imf_mid_Id` 
						from    imf_card_stock_mid_link 
                        where   `imf_card_stock_Id` = cardID  ) 
 
order by concatParent;                       
    
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `getCardCategoryListSource`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCardCategoryListSource`( IN cardID bigint(20),
																		 OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCardCategoryListSource (' + cardID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;

  select a.imf_mid_category_Id as value,		
		 a.Description as label,
         a.Description as description
                        
 from imf_category as a 
 
 where a.imf_mid_category_Id NOT IN ( select  `imf_mid_category_Id` 
									  from    imf_card_category_link 
								      where   `imf_card_Id` = cardID ) 
and    a.Active = true                                      
 
order by description;                       
    
END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS `getCardCategoryListTarget`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getCardCategoryListTarget`( IN cardID bigint(20),
																		 OUT ret VARCHAR(255) )
BEGIN	
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getCardCategoryListTarget (' + cardID + ' ) code: ', code, ' message: ', msg) INTO ret;
            END;

  select  a.imf_mid_category_Id as value,		
	  	  a.Description as label,
          a.Description as description
                        
 from imf_category as a 
 
 
 where a.imf_mid_category_Id IN (select  `imf_mid_category_Id` 
							     from    imf_card_category_link 
								 where   `imf_card_Id` = cardID   ) 
 
and    a.Active = true 
 
order by description;                       
    
END$$
DELIMITER ;






DELIMITER $$
DROP PROCEDURE IF EXISTS `addIMFMerchantAutoLevel`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addIMFMerchantAutoLevel`(	IN merchantCode VARCHAR(100),
																		IN description VARCHAR(100),
																		IN merchantName VARCHAR(100),
																		IN merchantParent bigint(20),
																		OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
    DECLARE merchantLevel VARCHAR(100);

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Inserting Merchant (',merchantName,') code: ', code, ' message: ', msg) INTO ret;
            END;
            
    set merchantLevel = NULL;        
	select (CAST(MID_Category_Type AS UNSIGNED)  + 1) INTO merchantLevel from imf_mid where IMF_Mid_Id = merchantParent;
    select coalesce( merchantLevel, '1') into merchantLevel;
    
    IF (merchantParent < 1) THEN
		set merchantParent = NULL;
    END IF;

    IF (merchantCode IS NULL) THEN
		SELECT CONCAT('Error Inserting Merchant - Null Code Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(merchantCode)) = '' ) THEN
		SELECT CONCAT('Error Inserting Merchant - Blank Code Not Allowed ' ) INTO ret;
    END IF;
    
    IF (merchantName IS NULL) THEN
		SELECT CONCAT('Error Inserting Merchant - Null Name Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(merchantName)) = '' ) THEN
		SELECT CONCAT('Error Inserting Merchant - Blank Name Not Allowed ' ) INTO ret;
    END IF;

    IF (description IS NULL) THEN
		set description = merchantName;
    END IF;
    
    IF ( rtrim(ltrim(description)) = '' ) THEN
		set description = merchantName;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  	
		INSERT INTO imf_mid (`imf_mid_MerchantCode`, `Description`, `Name`, `Parent_Id`, `MID_Category_Type`  )
		VALUES (merchantCode, description, merchantName, merchantParent, merchantLevel );

		SET mercID = LAST_INSERT_ID(); 
	    
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Inserting Merchant (', merchantName, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;
	END IF;
	
END$$
DELIMITER ;



DELIMITER $$
DROP PROCEDURE IF EXISTS `editIMFMerchant`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `editIMFMerchant`(	IN updateID bigint(20),
																IN merchantCode VARCHAR(100),
															    IN description VARCHAR(100),
																IN merchantName VARCHAR(100),
																IN merchantLevel VARCHAR(100),
																IN merchantParent bigint(20),
																OUT ret VARCHAR(255) )
BEGIN	
	DECLARE mercID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
    
	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error Editing Merchant (',merchantName,') code: ', code, ' message: ', msg) INTO ret;
            END;
            
	
    set merchantLevel = NULL;
	select (CAST(MID_Category_Type AS UNSIGNED)  + 1) INTO merchantLevel from imf_mid where IMF_Mid_Id = merchantParent;
    select coalesce( merchantLevel, '1') into merchantLevel;
    
    IF (merchantParent < 1) THEN
		set merchantParent = NULL;
    END IF;

    IF (merchantCode IS NULL) THEN
		SELECT CONCAT('Error Editing Merchant - Null Code No Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(merchantCode)) = '' ) THEN
		SELECT CONCAT('Error Editing Merchant - Blank Code No Allowed ' ) INTO ret;
    END IF;
    
    IF (merchantName IS NULL) THEN
		SELECT CONCAT('Error Editing Merchant - Null Name No Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(merchantName)) = '' ) THEN
		SELECT CONCAT('Error Editing Merchant - Blank Name No Allowed ' ) INTO ret;
    END IF;

    IF (description IS NULL) THEN
		set description = merchantName;
    END IF;
    
    IF ( rtrim(ltrim(description)) = '' ) THEN
		set description = merchantName;
    END IF;
    
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
       
		UPDATE imf_mid SET	`imf_mid_MerchantCode` = merchantCode,
							`Description` = description, 
						    `Name` = merchantName,
						    `Parent_Id` = merchantParent, 
						    `MID_Category_Type` = merchantLevel 
		WHERE IMF_Mid_Id = updateID;

		SELECT ROW_COUNT() into mercID;
			
		IF (mercID < 1) THEN
			SELECT CONCAT('Error Editing Merchant (', merchantName, ') ' ) INTO ret;
		ELSE
			SELECT mercID INTO ret;
		END IF;            
         
	END IF;
	
END$$
DELIMITER ;





DELIMITER $$
DROP PROCEDURE IF EXISTS `getMIDParentHierarchy`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMIDParentHierarchy`( OUT ret VARCHAR(255) )
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
				SELECT CONCAT('Error getMIDParentHierarchy (  ) code: ', code, ' message: ', msg) INTO ret;
            END;
            
  select max(CAST( MID_Category_Type AS UNSIGNED ) ) into @MAX_CHILD_LEVEL
  from imf_mid;
  
  select a.IMF_Mid_Id as value,		
		 a.Description as label,
         a.Name as description,
         CAST(a.MID_Category_Type as UNSIGNED) as level,
         a.Parent_Id as parent,
	   concat   ( 
				  if ( r.Parent_Id IS NOT NULL, j.Name, '' ), if ( r.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( q.Parent_Id IS NOT NULL, r.Name, '' ), if ( q.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( p.Parent_Id IS NOT NULL, q.Name, '' ), if ( p.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( o.Parent_Id IS NOT NULL, p.Name, '' ), if ( o.Parent_Id IS NOT NULL, '/', '' ),                                                
                  if ( i.Parent_Id IS NOT NULL, o.Name, '' ), if ( i.Parent_Id IS NOT NULL, '/', '' ),                                                
				  if ( a.Parent_Id IS NOT NULL, i.Name, '' ), if ( a.Parent_Id IS NOT NULL, '/', '' ),                        
				  a.Name ) as concatParent
                        
 from imf_mid as a 
 left outer join imf_mid as i on i.IMF_Mid_Id = a.Parent_Id
 left outer join imf_mid as o on o.IMF_Mid_Id = i.Parent_Id
 left outer join imf_mid as p on p.IMF_Mid_Id = o.Parent_Id
 left outer join imf_mid as q on q.IMF_Mid_Id = p.Parent_Id
 left outer join imf_mid as r on r.IMF_Mid_Id = q.Parent_Id
 left outer join imf_mid as j on j.IMF_Mid_Id = r.Parent_Id
 where CAST(a.MID_Category_Type as UNSIGNED) < @MAX_CHILD_LEVEL
order by concatParent;                       
    
END$$
DELIMITER ;





DELIMITER $$
DROP PROCEDURE IF EXISTS `checkMerchantsbyCardPANMIDCode`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `checkMerchantsbyCardPANMIDCode`(IN card_Pan VARCHAR(100),
																			 IN merchant_Code VARCHAR(100),
																			 OUT isMerchantActive boolean,
																			 OUT ret VARCHAR(2000) )

BEGIN	

	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE mid_ID bigInt(20) DEFAULT 0;
              
-- Exit and crash handlers          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error checkMerchantsbyCardPANMIDCode,  (',card_Pan, ' : ', merchant_Code, ') code: ', code, ' message: ', msg) INTO ret;
            END;

-- Check for null or blank MID Code           
    IF (merchant_Code IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCardPANMIDCode : Error Merchant Code is Null is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(merchant_Code)) = '' ) THEN
		SELECT CONCAT('checkMerchantsbyCardPANMIDCode : Error Merchant Code is Blank is Not Allowed ' ) INTO ret;
    END IF;
           
-- Check for null or blank card           
    IF (card_Pan IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCardPANMIDCode : Error Card is Null is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(card_Pan)) = '' ) THEN
		SELECT CONCAT('checkMerchantsbyCardPANMIDCode : Error  Card is Blank is Not Allowed ' ) INTO ret;
    END IF;
    
	-- Set output as default false
    set isMerchantActive = false;
       
    -- Only process if valid card
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		
        set mid_ID = 0;
        
		SELECT IMF_Mid_Id into mid_ID from imf_mid where imf_mid_MerchantCode = merchant_Code limit 1;
        
        if  ( mid_ID IS NOT NULL ) THEN 
			call checkMerchantsbyCardPAN(card_Pan, 
										 mid_ID, 
                                         isMerchantActive, 
                                         ret );
        END IF;
        
	END IF;
    
END$$
DELIMITER ;




DELIMITER $$
DROP PROCEDURE IF EXISTS `checkMerchantsbyCardPAN`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `checkMerchantsbyCardPAN`(IN card_Pan VARCHAR(100),
																	  IN actualMerchant_id BIGINT(20),
																      OUT isMerchantActive boolean,	
																      OUT ret VARCHAR(2000) )

BEGIN	

	DECLARE cardLinkedIdList VARCHAR(500) default '';
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE cardID bigInt(20) DEFAULT 0;
    
    -- Cursor to get all card ID's matchning PAN, should just be one but...
	DEClARE cardListCursor CURSOR FOR 
    
        SELECT IMF_CardStock_Id 
		FROM imf_card_stock                   
    	where  Active = true 
        and ( ( SUBSTRING( card_Pan ,1, length(Pan_number)  ) = Pan_number				
			    and isPANGroup = true ) 
                OR ( card_Pan = Pan_number   ) );
 
          
-- Exit and crash handlers          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error checkMerchantsbyCardPAN,  (',card_Pan,') code: ', code, ' message: ', msg) INTO ret;
            END;
           
-- Check for null or blank card           
    IF (card_Pan IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCardPAN : Error Card is Null is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(card_Pan)) = '' ) THEN
		SELECT CONCAT('checkMerchantsbyCardPAN : Error  Card is Blank is Not Allowed ' ) INTO ret;
    END IF;

-- Check for null or blank merchant           
    IF (actualMerchant_id IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCardPAN : Error Merchant ID is Null is Not Allowed ' ) INTO ret;
    END IF;
    
	-- Set output as default false
    set isMerchantActive = false;
       
    -- Only process if valid card
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		OPEN cardListCursor;      
        
	   	set cardLinkedIdList = '';	 
        
		get_Cards: LOOP
			FETCH cardListCursor INTO cardID;
			
			IF finished = 1 THEN 
				LEAVE get_Cards;
			END IF;					
			
            -- Add comma if card list not blank
            if ( cardLinkedIdList is NOT NULL AND ltrim(rtrim(cardLinkedIdList)) != '') THEN 
				set cardLinkedIdList = concat(cardLinkedIdList, ',');
            END IF;
            
            -- Add Card ID to list 
            set cardLinkedIdList = concat(cardLinkedIdList, cardID);
                      
		END LOOP get_Cards;
	       
		if ( cardLinkedIdList is NOT NULL AND ltrim(rtrim(cardLinkedIdList)) != '') THEN 
				call checkMerchantsbyCard(cardLinkedIdList, 
										  actualMerchant_id, 
                                          isMerchantActive, 
                                          ret );
		END IF;
	END IF;
    
END$$
DELIMITER ;
   
   
   
   



DELIMITER $$
DROP PROCEDURE IF EXISTS `checkMerchantsbyCard`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `checkMerchantsbyCard`(IN card_idList VARCHAR(500),
																   IN actualMerchant_id BIGINT(20),
																   OUT isMerchantActive boolean,
																   OUT ret VARCHAR(2000) )

BEGIN	

	DECLARE randomX int;
	DECLARE upperX int;
	DECLARE lowerX int;
	DECLARE rndtableX varchar(100);
	
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE merchantID bigInt(20) DEFAULT 0;
    
    -- Cursor to get all merchants linked to card
	DEClARE merchantLinkCursor CURSOR FOR 
			SELECT distinct * from ( 
                   
                    -- Direct Link : Card --> Merchant
					SELECT j.imf_mid_Id  as imf_mid_Id
					FROM   imf_card_stock_mid_link as j, imf_card_stock as k
					where  FIND_IN_SET(j.imf_card_stock_Id, card_idList) > 0
                    and    k.IMF_CardStock_Id = j.imf_card_stock_Id
                    and    k.Active = true

				UNION

					-- Link : Card --> Category --> Merchant
					select MID_Id  as imf_mid_Id
					from  imf_mid_category_link
					where Category_Id IN ( SELECT g.imf_mid_category_Id 
										   FROM  imf_card_category_link as g, imf_category as h
										   where FIND_IN_SET(g.imf_card_Id, card_idList) > 0 
                                           and   h.IMF_Mid_Category_Id = g.imf_mid_category_Id 									
                                           and   h.Active = true )

				UNION
					-- Link : Card --> Campaign --> Merchant	
					select imf_MID_Id  as imf_mid_Id
					from imf_campaign_mid_link
					where imf_campaign_Id IN ( SELECT e.imf_campaign_Id 
											   FROM  imf_campaign_card_link as e, imf_campaign as f
											   where FIND_IN_SET(e.imf_CARD_Id, card_idList) > 0 
                                               and   f.IMF_Campaign_Id = e.imf_campaign_Id 
                                               and   f.Active = true  )                                           

				UNION
					-- Link : Card --> Campaign --> Category --> Merchant   
					select MID_Id  as imf_mid_Id
					from  imf_mid_category_link
					where Category_Id IN (select c.imf_category_Id  
										  from   imf_campaign_category_link as c, imf_category as d
										  where  c.imf_campaign_Id IN ( SELECT  a.imf_campaign_Id
													  				    FROM    imf_campaign_card_link as a, imf_campaign as b
																	    where   FIND_IN_SET(a.imf_CARD_Id, card_idList) > 0 
                                                                        and     b.IMF_Campaign_Id = a.imf_campaign_Id
																	    and     b.Active = true )
										  and 	d.IMF_Mid_Category_Id = c.imf_category_Id  										
                                          and   d.Active = true )

															
			) as midList;
                                     
-- Exit and crash handlers          
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('Error checkMerchantsbyCard,  (',cardPan_id,') code: ', code, ' message: ', msg) INTO ret;
            END;
           
-- Check for null or blank card           
    IF (card_idList IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCard : Error Card is Null is Not Allowed ' ) INTO ret;
    END IF;
    
    IF ( rtrim(ltrim(card_idList)) = '' ) THEN
		SELECT CONCAT('checkMerchantsbyCard : Error  Card is Blank is Not Allowed ' ) INTO ret;
    END IF;

-- Check for null or blank merchant           
    IF (actualMerchant_id IS NULL) THEN
		SELECT CONCAT('checkMerchantsbyCard : Error Merchant ID is Null is Not Allowed ' ) INTO ret;
    END IF;
   
   -- Set output as default false
    set isMerchantActive = false;
    
    -- Setup Temp Table
	set lowerX = 1;
	set upperX = 999;
	set randomX = ROUND(((upperX - lowerX) * rand() + lowerX),0);
	set rndtableX = concat('tempMerchantChildrenTable', cast(randomX as CHAR(20) ), '');

    -- Only process if valid card
    IF ( ret IS NULL OR rtrim(ltrim(ret)) = '' )
	THEN  			 
		-- Create Temp table with dynamic name 
		set @createTableSQL = concat( 'CREATE TEMPORARY TABLE  ', rndtableX, ' ( merchantID BIGINT(20) ) ' );
		PREPARE dynamicStatement FROM @createTableSQL;
		EXECUTE dynamicStatement;    
		DEALLOCATE PREPARE dynamicStatement;
        
		OPEN merchantLinkCursor;      
	   		 
		get_Merch: LOOP
			FETCH merchantLinkCursor INTO merchantID;
			
			IF finished = 1 THEN 
				LEAVE get_Merch;
			END IF;					
			
           	-- Insert Child Record into temptable
			set @insertParentMerchSQL = concat( 'insert into ', rndtableX, ' ( merchantID ) values ( ', merchantID, ' ) '  ) ;           
			PREPARE dynamicInsertParentMerchStatement FROM @insertParentMerchSQL;
			EXECUTE dynamicInsertParentMerchStatement;    
			DEALLOCATE PREPARE dynamicInsertParentMerchStatement; 
            
			-- Call to find this parents children and insert into temp table
			 if ( merchantID  IS NOT NULL AND  ltrim(rtrim(merchantID))  != '' AND 
				  rndtableX IS NOT NULL AND ltrim(rtrim(rndtableX)) != '')       THEN 
				call getMidChildrenLoop( merchantID , rndtableX, ret);
			END IF;      		            
		
		END LOOP get_Merch;
	       
	-- Check merchant if linked to card via main merchant or merchant hierarchy children
    -- Send true if linked in output value
		set isMerchantActive = false;
        set @isMerchantActive = false;
        
		set @compareMerchSQL = concat( ' SELECT true into @isMerchantActive FROM ', rndtableX, ' where merchantID =  ', actualMerchant_id, ' limit 1 ' ) ;           
		PREPARE dynamicCompareMerchStatement FROM @compareMerchSQL;
		EXECUTE dynamicCompareMerchStatement;    
		DEALLOCATE PREPARE dynamicCompareMerchStatement; 
        set isMerchantActive = @isMerchantActive;
        
	-- Drop temp table 
		set @dropSQL = concat( ' DROP TABLE ', rndtableX ) ;           
		PREPARE dynamicDropStatement FROM @dropSQL;
		EXECUTE dynamicDropStatement;    
		DEALLOCATE PREPARE dynamicDropStatement;
        
	END IF;
    
END$$
DELIMITER ;
   


DELIMITER $$
DROP PROCEDURE IF EXISTS `getMidChildrenLoop`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getMidChildrenLoop` ( IN midParent_id BIGINT(20),
																   IN tempTableNameX VARCHAR(100),
																   OUT ret VARCHAR(2000) )
BEGIN
	DECLARE mercChildLinkID BIGINT(20) DEFAULT 0;
	DECLARE code CHAR(5) DEFAULT '00000';
	DECLARE msg TEXT;
	DECLARE finished INTEGER DEFAULT 0;
    
	DEClARE merchantChildLinkCursor CURSOR FOR 
		SELECT IMF_Mid_Id as merchID from imf_mid where Parent_Id = midParent_id;  
                   
    DECLARE CONTINUE HANDLER 
		FOR NOT FOUND SET finished = 1;

	DECLARE EXIT HANDLER FOR
		SQLEXCEPTION
			BEGIN
            
				GET DIAGNOSTICS CONDITION 1
				code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;

			SELECT CONCAT('getMidChildrenLoop Error / Failure (',midParent_id,') code: ', code, ' message: ', msg) INTO ret;
            END;
            
    set max_sp_recursion_depth=512;

	OPEN merchantChildLinkCursor;          
    
    get_MerchChildren: LOOP
			FETCH merchantChildLinkCursor INTO mercChildLinkID;
			
			IF finished = 1 THEN 
				LEAVE get_MerchChildren;
			END IF;
					 
			-- if SQL crashed Exit handler will handle the crash 
			-- Insert Child Record into temptable
			  -- Get the merchant children and add to temp table 
			set @insertChildMerchSQL = concat( 'insert into ', tempTableNameX, ' ( merchantID ) values ( ', mercChildLinkID, ' ) '  ) ;           
			PREPARE dynamicInsertChildMerchStatement FROM @insertChildMerchSQL;
			EXECUTE dynamicInsertChildMerchStatement;    
			DEALLOCATE PREPARE dynamicInsertChildMerchStatement; 
            
			-- Recursive call to find this childs children and insert into temp table, when no children are found there will 
            -- be no loop itterations as the cursor will not return any rows, thus the recursive loop call will not happen
			 if ( mercChildLinkID  IS NOT NULL AND  ltrim(rtrim(mercChildLinkID))  != '' AND 
				  tempTableNameX IS NOT NULL AND ltrim(rtrim(tempTableNameX)) != '')       THEN 
				call getMidChildrenLoop( mercChildLinkID , tempTableNameX, ret);
			END IF;      		
        
	END LOOP get_MerchChildren;
	    
END$$
DELIMITER ;	


