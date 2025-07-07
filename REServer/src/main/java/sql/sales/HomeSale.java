package sql.sales;

import java.sql.Date;

// "Simple" class to provide test data in SalesDAO
public class HomeSale {

    public String councilName;
    public String address;
    public String propertyType;
    public String propertyName;
    public String areaType;
    public String zoning;
    public String natureOfProperty;
    public String primaryPurpose;
    public String legalDescription;
    public int propertyId;
    public int purchasePrice;
    public int postCode;
    public int strataLotNumber;
    public Date downloadDate;
    public Date contractDate;
    public Date settlementDate;
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

    public int getPropertyId() {
        return propertyId;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public String getCouncilName() {
        return councilName;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public String getAddress() {
        return address;
    }

    public int getPostCode() {
        return postCode;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public int getStrataLotNumber() {
        return strataLotNumber;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public double getArea() {
        return area;
    }

    public String getAreaType() {
        return areaType;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public String getZoning() {
        return zoning;
    }

    public String getNatureOfProperty() {
        return natureOfProperty;
    }

    public String getPrimaryPurpose() {
        return primaryPurpose;
    }

    public String getLegalDescription() {
        return legalDescription;
    }
}
