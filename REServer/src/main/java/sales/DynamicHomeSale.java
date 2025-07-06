package sales;

import java.sql.Date;

import com.fasterxml.jackson.databind.JsonNode;

import io.micrometer.common.lang.Nullable;

public class DynamicHomeSale extends ASale {

    public int propertyId;
    @Nullable
    public Date downloadDate;
    @Nullable
    public String councilName;
    public int purchasePrice;
    @Nullable
    public String address;
    public int postCode;
    @Nullable
    public String propertyType;
    @Nullable
    public Integer strataLotNumber;
    @Nullable
    public String propertyName;
    public double area;
    @Nullable
    public String areaType;
    @Nullable
    public Date contractDate;
    @Nullable
    public Date settlementDate;
    @Nullable
    public String zoning;
    @Nullable
    public String natureOfProperty;
    @Nullable
    public String primaryPurpose;
    @Nullable
    public String legalDescription;

    public DynamicHomeSale(JsonNode json) {
        super(json);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) attributes.get(key);
    }

    public String getString(String key) {
        return (String) attributes.get(key);
    }
}
