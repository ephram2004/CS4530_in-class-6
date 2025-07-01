package sales;

import java.sql.Date;

// Simple class to provide test data in SalesDAO

public class HomeSale {
    public String councilName, address, propertyType, propertyName, areaType, zoning,
            natureOfProperty, primaryPurpose, legalDescription;
    public int propertyId, purchasePrice, postCode, strataLotNumber;
    public Date downloadDate, contractDate, settlementDate;
    public double area;

    public HomeSale(int propertyId, Date downloadDate, String councilName,
            int purchasePrice, String address, int postCode,
            String propertyType, int strataLotNumber, String propertyName, double area,
            String areaType, Date contractDate, Date settlementDate, String zoning,
            String natureOfProperty, String primaryPurpose, String legalDescription) {
        this.propertyId = propertyId;
        this.downloadDate = downloadDate;
        this.councilName = councilName;
        this.purchasePrice = purchasePrice;
        this.address = address;
        this.postCode = postCode;
        this.propertyType = propertyType;
        this.strataLotNumber = strataLotNumber;
        this.propertyName = propertyName;
        this.area = area;
        this.areaType = areaType;
        this.contractDate = contractDate;
        this.settlementDate = settlementDate;
        this.zoning = zoning;
        this.natureOfProperty = natureOfProperty;
        this.primaryPurpose = primaryPurpose;
        this.legalDescription = legalDescription;
    }

    // needed for JSON conversion
    public HomeSale() {
    }
}
